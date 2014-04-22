package core;

public class Edge {

    private Node from;
    private Node to;
    private int idx;
    private int length;

    public Edge(Node from, Node to, int idx, int length) {
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
        String label = str.substring(idx, idx + length);
        if (to.getAllEdges().isEmpty()) label += "$";
        return label;
    }
}
