package communication.HeaderProtocol;

import battlecode.common.Clock;

import java.util.Arrays;
import java.util.Comparator;

public class HeaderProtocol<E extends Enum<E> & IGetNumBits> {
    private final int[] headers;
    private final int[] dataMasks;
    private TreeNode<E> root = null;

    static abstract class TreeNode<E> {
        abstract E get(int idx);
    }

    static class TreeNodeLeaf<E> extends TreeNode<E> {
        E value;

        TreeNodeLeaf(E value) {
            this.value = value;
        }

        @Override
        E get(int idx) {
            return value;
        }
    }

    static class TreeNodeInternal<E> extends TreeNode<E> {
        TreeNode<E>[] children;
        int shamt;
        int mask;

        TreeNodeInternal(int numChildren, int shamt, int mask) {
            //noinspection unchecked
            children = (TreeNode<E>[]) new TreeNode<?>[numChildren];
            this.shamt = shamt;
            this.mask = mask;
        }

        @Override
        E get(int idx) {
            return children[(idx >>> shamt) & mask].get(idx);
        }
    }

    private static <E extends Enum<E> & IGetNumBits> TreeNode<E> buildTree(int[] indexes, int[] dataSizes, E[] values, int s, int e, int lastShamt) {
        int size = e - s;
        if (size == 0)
            return null; // unused space or something, idk, probably won't be used.
        if (size == 1)
            return new TreeNodeLeaf<>(values[s]);
        int shamt = 0;
        for (int i = s; i < e; i++) {
            if (dataSizes[i] > shamt)
                shamt = dataSizes[i];
        }
        int decodeBits = lastShamt - shamt;
        int mask = (1 << decodeBits) - 1;
        int maxHead = (indexes[e-1] >>> shamt) & mask;
        TreeNodeInternal<E> node = new TreeNodeInternal<>(maxHead+1, shamt, mask);
        int i = s;
        int currHead = 0;
        while (i < e) {
            int ns = i;
            while (i < e && ((indexes[i] >>> shamt) & mask) == currHead) {
                i++;
            }
            int ne = i;
            node.children[currHead] = buildTree(indexes, dataSizes, values, ns, ne, shamt);
            currHead++;
        }
        return node;
    }

    private static <E extends Enum<E> & IGetNumBits> TreeNode<E> buildTree(int[] indexes, int[] dataSizes, E[] values, int numBits) {
        return buildTree(indexes, dataSizes, values, 0, indexes.length, numBits);
    }

    private static <E extends Enum<E> & IGetNumBits> void printTree(TreeNode<E> root, int level) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < level; i++)
            sb.append('\t');
        if (root instanceof TreeNodeLeaf) {
            System.out.println(sb + "Leaf: " + root.get(0));
            return;
        }
        TreeNodeInternal<E> cRoot = (TreeNodeInternal<E>) root;
        System.out.println(sb + "Internal: " + cRoot.shamt + " - " + Integer.toHexString(cRoot.mask) + " - " + cRoot.children.length + " options:");
        for (int i = 0; i < cRoot.children.length; i++) {
            printTree(cRoot.children[i], level+1);
        }
    }

    private void initTree(E[] enumValues, int[] headers, int[] dataSizes, int bitSize) {
        Integer[] indexes = new Integer[headers.length];
        for (int i = 0; i < indexes.length; i++) {
            indexes[i] = i;
        }
        Arrays.sort(indexes, Comparator.comparingInt(a -> headers[a]));
        int[] sortedHeaders = new int[indexes.length];
        int[] sortedDataSizes = new int[indexes.length];
        E[] sortedEnumValues = enumValues.clone();
        for (int i = 0; i < indexes.length; i++) {
            int j = indexes[i];
            sortedHeaders[i] = headers[j];
            sortedDataSizes[i] = dataSizes[j];
            sortedEnumValues[i] = enumValues[j];
        }
        root = buildTree(sortedHeaders, sortedDataSizes, sortedEnumValues, bitSize);
        //printTree(root, 0);
    }

    private void init(E[] enumValues, int bitSize) {
        MessageSpaceAllocator msa = new MessageSpaceAllocator(bitSize);
        int[] dataSizes = new int[enumValues.length];
        for (E enumValue : enumValues) {
            int numBits = enumValue.getNumBits();
            MessageBlock block = msa.allocate(numBits);
            if (block == null) {
                System.out.println("Unable to allocate!");
                block = new MessageBlock(0,0);
            }
            headers[enumValue.ordinal()] = block.start;
            dataMasks[enumValue.ordinal()] = (1 << numBits) - 1;
            dataSizes[enumValue.ordinal()] = numBits;
        }
        initTree(enumValues, headers, dataSizes, bitSize);
    }

    static class SerializedHeaderProtocol {
        private final byte[] data;
        private int cursor;

        SerializedHeaderProtocol(byte[] data) {
            this.data = data;
            this.cursor = 0;
        }

        int nextInt() {
            cursor += 4;
            return (data[cursor-4] & 0xFF) | (data[cursor-3] & 0xFF) << 8 | (data[cursor-2] & 0xFF) << 16 | (data[cursor-1] & 0xFF) << 24;
        }

        byte nextByte() {
            return data[cursor++];
        }

        void putInt(int i) {
            data[cursor] = (byte)i;
            data[cursor+1] = (byte)(i >>> 8);
            data[cursor+2] = (byte)(i >>> 16);
            data[cursor+3] = (byte)(i >>> 24);
            cursor += 4;
        }

        void putByte(byte i) {
            data[cursor++] = i;
        }
    }

    private static <E extends Enum<E> & IGetNumBits> TreeNode<E> deSerializeTree(SerializedHeaderProtocol shp, E[] enumValues) {
        int type = shp.nextByte();
        if (type == 0) {
            int ord = shp.nextInt();
            return new TreeNodeLeaf<>(enumValues[ord]);
        }
        int numChildren = shp.nextInt();
        int shamt = shp.nextByte();
        int mask = shp.nextInt();
        TreeNodeInternal<E> node = new TreeNodeInternal<>(numChildren, shamt, mask);
        for (int i = 0; i < numChildren; i++) {
            TreeNode<E> child = deSerializeTree(shp, enumValues);
            node.children[i] = child;
        }
        return node;
    }

    public static <E extends Enum<E> & IGetNumBits> HeaderProtocol<E> deSerialize(byte[] data, E[] enumValues) {
        SerializedHeaderProtocol shp = new SerializedHeaderProtocol(data);
        int numHeaders = shp.nextInt();
        int[] headers = new int[numHeaders];
        int[] dataMasks = new int[numHeaders];
        for (int i = 0; i < numHeaders; i++) {
            headers[i] = shp.nextInt();
            dataMasks[i] = shp.nextInt();
        }
        TreeNode<E> root = deSerializeTree(shp, enumValues);
        //printTree(root, 0);
        return new HeaderProtocol<>(headers, dataMasks, root);
    }

    private int calculateNodeBytes(TreeNode<E> node) {
        if (node instanceof TreeNodeLeaf) {
            return 5;
        }
        TreeNodeInternal<E> cNode = (TreeNodeInternal<E>) node;
        int numBytes = 10;
        for (int i = 0; i < cNode.children.length; i++) {
            numBytes += calculateNodeBytes(cNode.children[i]);
        }
        return numBytes;
    }

    private int calculateBytes() {
        return 4 * (1 + 2*headers.length) + calculateNodeBytes(root);
    }

    private static <E extends Enum<E> & IGetNumBits> void serializeTree(SerializedHeaderProtocol shp, TreeNode<E> node) {
        if (node instanceof TreeNodeLeaf) {
            shp.putByte((byte) 0x00);
            shp.putInt(node.get(0).ordinal());
            return;
        }
        TreeNodeInternal<E> cNode = (TreeNodeInternal<E>) node;
        shp.putByte((byte) 0x01);
        shp.putInt(cNode.children.length);
        shp.putByte((byte) cNode.shamt);
        shp.putInt(cNode.mask);
        for (TreeNode<E> child : cNode.children) {
            serializeTree(shp, child);
        }
    }

    public byte[] serialize() {
        byte[] data = new byte[calculateBytes()];
        SerializedHeaderProtocol shp = new SerializedHeaderProtocol(data);
        shp.putInt(headers.length);
        for (int i = 0; i < headers.length; i++) {
            shp.putInt(headers[i]);
            shp.putInt(dataMasks[i]);
        }
        serializeTree(shp, root);
        return shp.data;
    }

    /*
    Can't use getEnumConstants()...
    public HeaderProtocol(Class<E> enumType, int bitSize) {
        E[] enumValues = enumType.getEnumConstants();
        this.headers = new int[enumValues.length];
        this.dataMasks = new int[enumValues.length];
        init(enumValues, bitSize);
    }
    */

    public HeaderProtocol(E[] enumValues, int bitSize) {
        this.headers = new int[enumValues.length];
        this.dataMasks = new int[enumValues.length];
        init(enumValues, bitSize);
    }

    private HeaderProtocol(int[] headers, int[] dataMasks, TreeNode<E> root) {
        this.headers = headers;
        this.dataMasks = dataMasks;
        this.root = root;
    }

    public int getHeader(E enumValue) {
        return this.headers[enumValue.ordinal()];
    }

    public int getDataMask(E enumValue) {
        return this.dataMasks[enumValue.ordinal()];
    }

    public E getType(int message) {
        return root.get(message);
    }
}
