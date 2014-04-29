package core;

import java.io.IOException;
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

    /**
     * Checks for two strings if one is a prefix of the other, or in
     * case they are of equal length, if they are equal. The parameters
     * are interchangeable.
     * @return true if one string is a prefix of the other, false otherwise
     */
    private boolean checkPrefixMatch(String s1, String s2) {
        for (int i= 0; i < Math.min(s1.length(), s2.length()); i++) {
            if (s1.charAt(i) != s2.charAt(i)) return false;
        }
        return true;
    }

    /**
     * Search character by character for a string starting at a given node.
     * @param node the node in which we start the search
     * @param find the string to search for
     * @return if the search ends in a node, this is returned. If the search
     * ends on an edge, the node it ends in is returned.
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

    /**
     * Builds a list of all leaves in the subtree of a given node.
     * @param n the root of the subtree which we want to list
     * @return a list of the leaves in the subtree rooted at the given node
     */
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

    /**
     * Lists all occurrences of the query string in the original string.
     * @param query the string to search for
     * @return a list of all occurrences of the query string in the original string
     */
    public List<Integer> search(String query) {
        // Serach as far down the tree as possible
        Node top = slowscanNoCreate(root, query);

        // The string was not found
        if (top == null) return new ArrayList<Integer>();

        // List the start index of all terminal nodes in the subtree rooted at top
        return listAllIndicesOfSubtree(top);
    }

    /**
     * Constructs the suffix tree.
     */
    private void constructSuffixTree() {
//        DotMaker dot = null;
//        try { dot = new DotMaker(input); } catch (IOException e) { System.exit(-1); }

        /*
         * First thing we need to do is to construct T_1. This is done by simply
         * creating the root node, and an edge to a new leaf node representing the
         * entire string.
         */
        root = new Node(0, 0);
        Node n1 = new Node(0, input.length());
        makeEdge(root, n1, 0, input.length());
        root.setSuffixLink(root);


        /*
         * We will iteratively add smaller and smaller suffixes of the input string
         * to the suffix tree. We start from input[1:n] and end with the empty string.
         *
         * Head represents the last node on the path to the most recently inserted
         * string, and is therefore initialized to root.
         *
         * Tail is a string containing the label of the edge from head to newest leaf.
         *
         * This construction means that the concatenation of head and tail will spell
         * out the most recently inserted suffix.
         */
        Node head   = root;
        String tail = input;
        for (int i = 0; i < input.length() - 1; i++) {
//            dot.addTree(root);

            /*
             * We initialize the variables u and v. They are constructed in such a
             * way that the suffix link of u concatenated with v is known to be in
             * the tree. That is 'suffixLink(u).concat(v)' is in the tree.
             *
             * u is set to the parent of head, and in case head is the root, u is
             * simply set to the root.
             *
             * v is the label of the edge going from u to head. Again, if head is
             * the root, v is just the empty string.
             */
            Node u   = (head == root) ? root : head.getParent();
            String v = (head == root) ? "" : head.getParentEdge().getLabel(input);

            /*
             * Because we know that the concatenation of s(u) and v is in the tree,
             * we can use fastscan to search for v from node s(u). (where s(u) is the
             * suffix link of u).
             *
             * If u is not the root, we can just to a regular fastscan for v from the
             * node pointed to by the suffix link of u.
             *
             * If u is the root, we will fastscan from the root (which is also equal to
             * s(u)) for v[1:]. Of course v can be the empty string (it is in the first
             * iteration), we have to check for that too as we can't take the substring.
             *
             * We set w to to be the node where the search ends. If the search completes
             * on an edge, a new node is created. We also need to store this information,
             * which is why w is stored in a 'NodeAndNewFlag' variable.
             */
            NodeAndNewFlag w;
            if (u != root) w = fastscan(u.getSuffixLink(), v);
            else           w = (v.isEmpty()) ? new NodeAndNewFlag(u, false) : fastscan(root, v.substring(1));

            /*
             * If w was just created, i.e. the search for s(u)v ended on an edge, we
             * just have to add an edge with the rest of the suffix (i.e. tail) and
             * we are done. Therefore we will just set newHead to w and proceed to adding
             * tail.
             *
             * If, however, w was already a node in the tree, it can be the case that
             * more of the suffix is already present in the tree. Because we do not
             * know this for a fact, we will have to use slowscan starting at node w.
             * The string we are going to search for is tail, as s(u)v concatenated with
             * tail is equal to the entire suffix we are trying to insert (unless head
             * is the root (and v therefore is empty), we have to scan for the entire
             * suffix that we want to insert. This is a special case as u and v act
             * weird when head is the root).
             * This search can, again, either end in a node or on an edge. If it ends in
             * a node, this will be the new head, and nothing else happens. If it ends
             * on an edge, the edge is split by a new node, which will also be the new
             * head.
             */
            Node newHead;
            if (w.isNew) newHead = w.n;
            else newHead = createNodeIfNecessary(slowscan(w.n, (v.isEmpty()) ? input.substring(i+1) : tail));

            /*
             * Now that we have found/created the node corresponding to s(u)v, we can
             * update the suffix link of the old head to point to this node.
             */
            head.setSuffixLink(w.n);

            /*
             * We now have to insert the leaf node corresponding to the suffix we are
             * inserting. To connect it to the tree, we create an edge from newHead,
             * and its label will be newTail (which is the rest of the suffix from
             * newHead).
             */
            String newTail = input.substring((i+1) + newHead.getLength(), input.length());
            Node terminalNode = new Node(i+1, input.length() - (i+1));
            makeEdge(newHead, terminalNode, (i+1) + newHead.getLength(), newTail.length());

            /*
             * Finally we will update head and tail.
             */
            head = newHead;
            tail = newTail;
        }

//        dot.addTree(root);
//        try { dot.close(); } catch (IOException e) {System.exit(-1);}
    }

    /**
     * Creates a new node at on a given edge at a given offset if the offset
     * is not zero, as the node already exists in this case.
     * @param nao a node and offset pair. The offset is on the parent edge.
     * @return the newly created node, or the given node if none was created
     */
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

    /**
     * Creates an edge between two nodes
     * @param n1 the parent node
     * @param n2 the child node
     * @param idx the starting index of the edge substring
     * @param length the length of the edge substring
     * @return the newly created edge
     */
    private Edge makeEdge(Node n1, Node n2, int idx, int length) {
        Edge e = new Edge(n1, n2, idx, length);
        n1.addEdge(input.charAt(idx), e);
        n2.setParentEdge(e);
        return e;
    }

    /**
     * Splits an edge by creating a new node and inserting it on the edge.
     * @param e the edge to split
     * @param offset the offset from the parent node to split the edge
     * @return the newly created node
     */
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
