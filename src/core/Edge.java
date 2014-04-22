package core;

public class Edge {

    private Node from;
    private Node to;
    private int idx;
    private int length;

    public Edge(Node from, Node to, int idx, int length) {

        try {
            if (idx < 0 || length < 0) throw new Exception();
        } catch (Exception e) {
            System.err.println("Attempt to create edge with negative length (idx="+idx+" & length="+length+")");
            e.printStackTrace();
            System.exit(-1);
        }

        this.from   = from;
        this.to     = to;
        this.idx    = idx;
        this.length = length;
    }

    public Node getFrom() { return from; }
    public Node getTo() { return to; }
    public int getIdx() { return idx; }
    public int getLength() { return length; }

    public void setFrom(Node n) { from = n; }
    public void setTo(Node n) { to = n; }
    public void setIdx(int i) { idx = i; }
    public void setLength(int l) { length = l; }

    public String getLabel(String str) {
        return str.substring(idx, idx + length);
    }
}
