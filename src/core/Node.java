package core;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class Node {

    private String str;
    private Map<Character, Edge> edges;
    private Edge parentEdge;
    private Node suffixLink;
    private int idx;
    private int length;

    public Node(String str, int idx, int length) {
        if (idx < 0 || length < 0) throw new IllegalArgumentException("Attempt to create node with negative length (idx="+idx+" & length="+length+")");

        this.str = str;
        this.parentEdge = null;
        this.idx = idx;
        this.length = length;
        this.edges = new HashMap<Character, Edge>();
    }

    public void addEdge(char first, Edge e) { edges.put(first, e); }
    public Edge getEdge(char first)         { return edges.get(first); }
    public Collection<Edge> getAllEdges()   { return edges.values(); }
    public Edge getParentEdge()             { return parentEdge; }
    public void setParentEdge(Edge e)       { parentEdge = e; }
    public Node getSuffixLink()             { return suffixLink; }
    public void setSuffixLink(Node n)       { suffixLink = n; }
    public int getLength()                  { return length; }
    public int getIdx()                     { return idx; }
    public Node getParent()                 { return parentEdge.getFrom(); }
    public String getLabel()                { return str.substring(idx, idx + length); }

}
