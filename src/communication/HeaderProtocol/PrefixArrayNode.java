package communication.HeaderProtocol;

import java.util.*;

// A sparse array of type T. If an index with no value
// is accessed, returns the value at the occupied index
// with the longest common prefix as the index queried.
class PrefixArrayNode<E> {
    PrefixArrayNode<E>[] children = null;
    int shamt = 0;
    Integer key = null;
    E value = null;

    public PrefixArrayNode(int shamt) {
        this.shamt = shamt;
        this.children = null;
    }

    public PrefixArrayNode(int shamt, int key, E value) {
        this.shamt = shamt;
        this.key = key;
        this.value = value;
    }

    private PrefixArrayNode<E>[] newChildren(int size) {
        //noinspection unchecked
        return (PrefixArrayNode<E>[]) new PrefixArrayNode<?>[size];
    }

    public boolean isLeaf() {
        return key != null;
    }

    public E getValue() {
        return value;
    }

    public PrefixArrayNode<E> getChild(int key) {
        return children[key >>> shamt];
    }

    public void setChild(int key, PrefixArrayNode<E> value) {
        children[key >>> shamt] = value;
    }

    public PrefixArrayNode<E> add(int index, int shamt, E value) {
        if (value == null)
            return null;
        int shiftedIndex = index >>> this.shamt;
        if (this.key != null) {
            int newShamt = Math.max(shamt, this.shamt);
            PrefixArrayNode<E> newMe = new PrefixArrayNode<>(newShamt);
            newMe.children = newChildren(2);
            newMe.children[index >>> newShamt] = new PrefixArrayNode<>(shamt, index, value);
            newMe.children[this.key >>> newShamt] = this;
            return newMe;
        }
        if (shamt <= this.shamt) {
            PrefixArrayNode<E>[] newChilds = newChildren(shiftedIndex + 1);
            System.arraycopy(this.children, 0, newChilds, 0, this.children.length);
            for (int i = this.children.length; i < newChilds.length; i++)
                newChilds[i] = null;
            this.children = newChilds;
            PrefixArrayNode<E> child = children[shiftedIndex];
            if (child == null) {
                children[shiftedIndex] = new PrefixArrayNode<>(shamt, index, value);
                return null;
            }
            PrefixArrayNode<E> newChild = child.add(index, shamt, value);
            if (newChild != null)
                children[shiftedIndex] = newChild;
            return null;
        } else {
            int newShiftedIndex = index >>> shamt;
            PrefixArrayNode<E> newMe = new PrefixArrayNode<>(shamt);
            int numOverlap = (1 << (shamt - this.shamt));
            int numPrefixCollisions = this.children.length / numOverlap + (this.children.length % numOverlap != 0 ? 1 : 0);
            int minIndexes = newShiftedIndex / numOverlap + (newShiftedIndex % numOverlap != 0 ? 1 : 0);
            int numChildren = Math.max(numPrefixCollisions, minIndexes);
            newMe.children = newChildren(numChildren);
            int[] prefixIdxCounts = new int[numPrefixCollisions];
            for (int i = 0; i < numChildren; i++)
                prefixIdxCounts[i] = 0;
            for (int shidx = 0; shidx < children.length; shidx++)
                prefixIdxCounts[shidx >>> (shamt - this.shamt)]++;
            for (int shidx = children.length-1; shidx < children.length; shidx++) {
                PrefixArrayNode<E> node = children[shidx];
                int newShidx = shidx >>> (shamt - this.shamt);
                if (prefixIdxCounts[newShidx] == 1) {
                    newMe.children[newShidx] = node;
                    continue;
                }
                PrefixArrayNode<E> middleNode = newMe.children[newShidx];
                if (middleNode == null) {
                    middleNode = new PrefixArrayNode<>(this.shamt);
                    middleNode.children = newChildren(numOverlap);
                    newMe.children[newShidx] = middleNode;
                }
                middleNode.children[shidx] = node;
            }
            newMe.children[newShiftedIndex] = new PrefixArrayNode<>(shamt, index, value);
            return newMe;
        }
    }
}

