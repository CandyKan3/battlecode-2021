package communication.HeaderProtocol;


public class MessageSpaceAllocator {

    private final MessageBlock root = new MessageBlock(0,0);
    private final int totalBits;

    public MessageSpaceAllocator(int numBits) {
        assert(0 <= numBits && numBits <= 32);
        this.totalBits = numBits;
        root.next = new MessageBlock(0, (1 << numBits));
    }

    public MessageBlock allocate(int numBits) {
        assert(0 <= numBits && numBits <= totalBits);
        MessageBlock lastBlock = root;
        MessageBlock currBlock = root.next;
        int loMask = (1 << numBits) - 1;
        while (currBlock != null) {
            int first = currBlock.start;
            if ((first & loMask) != 0) {
                first = (first | loMask) + 1;
            }
            int last = (first | loMask) + 1;
            if (currBlock.start <= first && last <= currBlock.end) {
                // Found block!
                // Fix stored free space
                if (currBlock.start == first) {
                    if (currBlock.end == last) {
                        // Allocated entire free block! Remove and return it
                        lastBlock.next = currBlock.next;
                        return currBlock;
                    }
                    // Allocated start of a free block. Update the start of the current block.
                    currBlock.start = last;
                } else {
                    // Allocated from the middle of a free block. Change the end of this block
                    int oldEnd = currBlock.end;
                    currBlock.end = first;
                    if (last != currBlock.end) {
                        // Allocated a block in the middle of another one. Create a new block for
                        // the messages at the end of the free block allocated from, and place it
                        // correctly in the linked list.
                        MessageBlock newBlock = new MessageBlock(last, oldEnd);
                        newBlock.next = currBlock.next;
                        currBlock.next = newBlock;
                    }
                }

                // return block found (unless currBlock was entirely allocated)
                return new MessageBlock(first, last);
            }
            // No space in this free block, look in next one
            lastBlock = currBlock;
            currBlock = currBlock.next;
        }
        // No free space to allocate a block of messages of that size
        return null;
    }
}
