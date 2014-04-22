package core;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class Node {

    private Map<Character, Edge> edges;
    private Edge parentEdge;
    private Node suffixLink;
    private int idx;
    private int length;

    public Node(int idx, int length) {
//        System.out.println("Creating node with idx="+idx+" and length="+length);

        try {
            if (idx < 0 || length < 0) throw new Exception();
        } catch (Exception e) {
            System.err.println("Attempt to create node with negative length (idx="+idx+" & length="+length+")");
            e.printStackTrace();
            System.exit(-1);
        }

        this.parentEdge = null;
        this.idx = idx;
        this.length = length;
        this.edges = new HashMap<Character, Edge>();
    }

    public void addEdge(char first, Edge e) { edges.put(first, e); }
    public Edge getEdge(char first) { return edges.get(first); }
    public Collection<Edge> getAllEdges() { return edges.values(); }
    public Edge getParentEdge() { return parentEdge; }
    public void setParentEdge(Edge e) { parentEdge = e; }
    public Node getSuffixLink() { return suffixLink; }
    public void setSuffixLink(Node n) { suffixLink = n; }
    public int getLength() { return length; }
    public int getIdx() { return idx; }
    public Node getParent() { return parentEdge.getFrom(); }

    public String getLabel(String str) {
        return str.substring(idx, idx + length);
    }

    public String toString(String str) {
        if (parentEdge == null) {
            return "Node[root]";
        }
        String p = parentEdge.getFrom().getLabel(str);
        String pel = parentEdge.getLabel(str);
        String suf = (suffixLink != null) ? ", suffixlink='"+suffixLink.getLabel(str)+"'" : "";
        return "Node[label='"+getLabel(str)+"', parent='"+p+"', parentEdgeLabel='"+pel+"'"+suf+"]";
    }

}
