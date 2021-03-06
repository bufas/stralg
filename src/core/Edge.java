package core;

public class Edge {

    private String str;
    private Node from;
    private Node to;
    private int idx;
    private int length;

    public Edge(String str, Node from, Node to, int idx, int length) {
        if (idx < 0 || length < 0) throw new IllegalArgumentException("Attempt to create edge with negative length (idx="+idx+" & length="+length+")");

        this.str    = str;
        this.from   = from;
        this.to     = to;
        this.idx    = idx;
        this.length = length;
    }

    public Node getFrom()  { return from; }
    public Node getTo()    { return to; }
    public int getIdx()    { return idx; }
    public int getLength() { return length; }

    public void setFrom(Node n)  { from = n; }
    public void setTo(Node n)    { to = n; }
    public void setIdx(int i)    { idx = i; }
    public void setLength(int l) { length = l; }

    public String getLabel() { return str.substring(idx, idx + length); }
}
