package core;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class Node {

    private Map<Character, Edge> edges;
    private Edge parentEdge;
    private Node suffixLink;
    public int leafIdx;

    public Node() {
        this(-1);
    }

    public Node(int leafIdx) {
        this.parentEdge = null;
        this.edges = new HashMap<Character, Edge>();
        this.leafIdx = leafIdx;
    }

    public void addEdge(char first, Edge e) { edges.put(first, e); }
    public Edge getEdge(char first)         { return edges.get(first); }
    public Collection<Edge> getAllEdges()   { return edges.values(); }
    public Edge getParentEdge()             { return parentEdge; }
    public void setParentEdge(Edge e)       { parentEdge = e; }
    public Node getSuffixLink()             { return suffixLink; }
    public void setSuffixLink(Node n)       { suffixLink = n; }
    public Node getParent()                 { return parentEdge.getFrom(); }

}
