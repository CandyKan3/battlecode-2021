package communication.HeaderProtocol;

public class MessageBlock {
    public int start;
    public int end;
    public MessageBlock next = null;

    public MessageBlock(int start, int end) {
        this.start = start;
        this.end = end;
    }
}
