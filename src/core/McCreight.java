package core;

import java.util.ArrayList;
import java.util.List;

public class McCreight {

    public static char TERM_SYMBOL = '$';

    private String input;
    private Node root;

    public McCreight(String input) {
        this.input = input + TERM_SYMBOL;
        constructSuffixTree();
    }

    private boolean checkPrefixMatch(String s1, String s2) {
        for (int i= 0; i < Math.min(s1.length(), s2.length()); i++) {
            if (s1.charAt(i) != s2.charAt(i)) return false;
        }
        return true;
    }

    /**
     * This is the same as slowscan, but it does not create a new node
     * it just returns the next if it ends on an edge
     * @return A node which matches the search, or null if the string is not in the tree
     */
    private Node slowscanNoCreate(Node node, String find) {
        int findCharCount = 0;

        while (findCharCount < find.length()) {
            Edge e = node.getEdge(find.charAt(findCharCount));
            if (e == null || !checkPrefixMatch(e.getLabel(input), find.substring(findCharCount))) return null;
            findCharCount += e.getLength();
            node = e.getTo();
        }

        return node;
    }

    private List<Integer> listAllIndicesOfSubtree(Node n) {
        List<Integer> res = new ArrayList<Integer>();

        // If node is a leaf, return its index
        if (n.getAllEdges().isEmpty()) {
            res.add(n.getIdx() + 1);
            return res;
        }

        // Else, recurse on all children
        for (Edge e : n.getAllEdges()) {
            res.addAll(listAllIndicesOfSubtree(e.getTo()));
        }

        return res;
    }

    public List<Integer> search(String query) {
        // Serach as far down the tree as possible
        Node top = slowscanNoCreate(root, query);

        // The string was not found
        if (top == null) return new ArrayList<Integer>();

        // List the start index of all terminal nodes in the subtree rooted at top
        return listAllIndicesOfSubtree(top);
    }

    private void constructSuffixTree() {
        // Construct T_1
        root = new Node(0, 0);
        Node n1 = new Node(0, input.length());
        makeEdge(root, n1, 0, input.length());
        root.setSuffixLink(root);

        // Construct T_{i+2}
        Node head   = root;
        String tail = input;
        for (int i = 0; i < input.length() - 1; i++) {

            // Initialize u and v
            Node u   = root;
            String v = "";

            // Change u and v if head is not root
            if (head != root) {
                u = head.getParent();
                v = head.getParentEdge().getLabel(input);
            }

            Node w;
            boolean wIsNew; // Tells whether w is a new node (created in this iteration) or not
            if (u != root) {
                NodeAndNewFlag nanf = fastscan(u.getSuffixLink(), v);
                w = nanf.n;
                wIsNew = nanf.isNew;
            } else {
                if (v.length() < 2) {
                    w = u;
                    wIsNew = false;
                } else {
                    NodeAndNewFlag nanf = fastscan(root, v.substring(1));
                    w = nanf.n;
                    wIsNew = nanf.isNew;
                }
            }

            Node newHead;
            if (wIsNew) newHead = w;
            else newHead = createNodeIfNecessary(slowscan(w, (v.isEmpty()) ? input.substring(i+1) : tail));

            // Add suffix link to old head
            head.setSuffixLink(w);

            // Add tail(i+1)
            String newTail = input.substring((i+1) + newHead.getLength(), input.length());
            Node terminalNode = new Node(i+1, input.length() - (i+1));
            makeEdge(newHead, terminalNode, (i+1) + newHead.getLength(), newTail.length());

            // Update head
            head = newHead;
            tail = newTail;
        }

        // Set the last suffix link
        head.setSuffixLink(root);
    }

    private Node createNodeIfNecessary(NodeAndOffset nao) {
        if (nao.offset == 0) return nao.node;
        else return splitEdge(nao.node.getParentEdge(), nao.offset);
    }

    /**
     * We know that the string find is in the tree.
     */
    private NodeAndNewFlag fastscan(Node start, String find) {
        // Edge case (search for the empty string)
        if (find.equals("")) return new NodeAndNewFlag(start, false);

        // Regular case
        int searchDist = 0;
        Node curNode = start;
        Edge e;
        do {
            e = curNode.getEdge(find.charAt(searchDist));
            searchDist += e.getLength();
            curNode = e.getTo();
        } while (find.length() > searchDist);

        if (searchDist == find.length()) {
            return new NodeAndNewFlag(curNode, false);
        } else {
            // Search ended on an edge. Split edge by inserting a new node.
            Node newNode = splitEdge(e, e.getLength() - (searchDist - find.length()));
            return new NodeAndNewFlag(newNode, true);
        }
    }

    /**
     * We do not know if the string find is in the tree or not.
     */
    private NodeAndOffset slowscan(Node start, String find) {
        Node curNode = start;
        int findCharCount = 0;

        while (true) {
            Edge e = curNode.getEdge(find.charAt(findCharCount));
            if (e == null) return new NodeAndOffset(curNode,0);
            for (int i = 0; i < e.getLength(); i++) {
                if (findCharCount == find.length() || input.charAt(e.getIdx() + i) != find.charAt(findCharCount)) {
                    // Break this edge
                    return new NodeAndOffset(e.getTo(), i);
                }
                findCharCount++;
            }
            curNode = e.getTo();
        }
    }

    private Edge makeEdge(Node n1, Node n2, int idx, int length) {
        Edge e = new Edge(n1, n2, idx, length);
        n1.addEdge(input.charAt(idx), e);
        n2.setParentEdge(e);
        return e;
    }

    private Node splitEdge(Edge e, int offset) {
        if (offset <= 0 || offset >= e.getLength()) throw new IllegalArgumentException("Can't split edge "+e.getLabel(input)+" at offset "+offset);

        // Prepare nodes
        Node n1 = e.getFrom();
        Node n3 = e.getTo();
        Node n2 = new Node(e.getIdx() - n1.getLength(), n1.getLength() + offset);

        // Insert new edge between n2 and n3
        makeEdge(n2, n3, e.getIdx() + offset, e.getLength() - offset);

        // Shorten edge (from and idx does not change)
        n2.setParentEdge(e);
        e.setTo(n2);
        e.setLength(offset);

        // Return newly created node
        return n2;
    }

    /**
     * Private class to use as return value from slowscan.
     */
    private class NodeAndOffset {
        public Node node;
        public int offset;
        public NodeAndOffset(Node n, int o) {
            node = n;
            offset = o;
        }
    }

    /**
     * Private class to use as return value from fastscan.
     */
    // TODO make fastscan use NodeAndOffset
    private class NodeAndNewFlag {
        public Node n;
        public boolean isNew;

        private NodeAndNewFlag(Node n, boolean isNew) {
            this.n = n;
            this.isNew = isNew;
        }
    }

}
