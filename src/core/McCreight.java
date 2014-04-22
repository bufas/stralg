package core;

import java.io.IOException;
import java.io.PrintWriter;

public class McCreight {

    public static char TERM_SYMBOL = '$';
    private String input;
    private Node root;

    public McCreight(String input) {
        this.input = input + TERM_SYMBOL;
        constructSuffixTree();
    }

    public Edge makeEdge(Node n1, Node n2, int idx, int length) {
        Edge e = new Edge(n1, n2, idx, length);
        n1.addEdge(input.charAt(idx), e);
        n2.setParentEdge(e);
        return e;
    }

    private void constructSuffixTree() {
        // Construct T_0
        root = new Node(0, 0);
        Node n1 = new Node(0, input.length());
        makeEdge(root, n1, 0, input.length());
        root.setSuffixLink(root);

        System.out.println("T_1 created!");

        // Construct T_{i+1}
        Node head = root;
        String tail = input;
        for (int i = 0; i < input.length() - 1; i++) {
            makeDot();

            System.out.println("-- BUILDING TREE( "+(i+2)+" ) --");
            System.out.println("\tstring  = " + input.substring(i+1));
            System.out.println("\thead("+(i+1)+") = " + head.toString(input));
            System.out.println("\ttail("+(i+1)+") = " + tail + "$");

            Node u;
            String v;

            if (head.getParentEdge() == null) {
//                System.out.println(1);
                u = root;
                v = "";
            } else {
//                System.out.println(2);
                Edge pe = head.getParentEdge();
                u = pe.getFrom();
                v = input.substring(pe.getIdx(), pe.getIdx() + pe.getLength());
            }

            System.out.println("\tu       = " + u.toString(input));
            System.out.println("\tv       = " + v);

            Node w;
            boolean wIsNew;
            if (u != root) {
//                System.out.println(3);
                NodeAndNewFlag nanf = fastscan(u.getSuffixLink(), v);
                w = nanf.n;
                wIsNew = nanf.isNew;
            } else {
                if (v.length() < 2) {
//                    System.out.println(5);
                    w = u;
                    wIsNew = false;
                } else {
//                    System.out.println(6);
                    // TODO wrong
                    w = new Node(i + 1, u.getLength() + v.length());
                    makeEdge(u.getSuffixLink(), w, w.getIdx() - (w.getLength() - u.getSuffixLink().getLength()), v.length() - (i + 1) - 1);
                    wIsNew = true;
                }
            }

            Node newHead;
            if (wIsNew) {
//                System.out.println(7);
                newHead = w;
            } else {
                if (v.isEmpty()) {
//                    System.out.println(8);
                    // Special case not covered in the book pseudocode
                    newHead = slowscan(w, input.substring(i + 1)); // TODO last parameter might be too long
                } else {
//                    System.out.println(9);
                    newHead = slowscan(w, tail);
                }
            }

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

        makeDot();
    }

    /**
     * We know that the string find is in the tree.
     */
    private NodeAndNewFlag fastscan(Node start, String find) {
        System.out.println("Fastscanning all the way!");
        // core.Edge case (search for the empty string)
        if (find.equals("")) return new NodeAndNewFlag(start, false);

        // Regular case
        int searchDist = 0;
        Node curNode = start;
        Edge e;
        do {
            e = curNode.getEdge(find.charAt(0));
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
    private Node slowscan(Node start, String find) {
        System.out.println("slowscanning from " + start.getLabel(input) + " for string " + find);

        Node curNode = start;
        int findCharCount = 0;

        while (true) {
            Edge e = curNode.getEdge(find.charAt(findCharCount));
            if (e == null) return curNode;
            System.out.println("\tSearching edge " + e.getLabel(input) + " for " + find.substring(findCharCount));
            for (int i = 0; i < e.getLength(); i++) {
                if (findCharCount == find.length() || input.charAt(e.getIdx() + i) != find.charAt(findCharCount)) {
                    // Break this edge
                    return splitEdge(e, i);
                }
                findCharCount++;
            }
            curNode = e.getTo();
        }
    }

    private Node splitEdge(Edge e, int offset) {
        // Prepare nodes
        Node n1 = e.getFrom();
        Node n3 = e.getTo();
        Node n2 = new Node(e.getIdx() - n1.getLength(), n1.getLength() + offset);
        System.out.print("Splitting edge " + e.getLabel(input) + " at offset "+offset+" into ");

        // Insert new edge between n2 and n3
        Edge newEdge = makeEdge(n2, n3, e.getIdx() + offset, e.getLength() - offset);

        // Shorten edge (from and idx does not change)
        n2.setParentEdge(e);
        e.setTo(n2);
        e.setLength(offset);

        System.out.println(e.getLabel(input) + " and " + newEdge.getLabel(input));

        // Return newly created node
        return n2;
    }

    /**
     * Private class to use as return value from fastscan.
     */
    private class NodeAndNewFlag {
        public Node n;
        public boolean isNew;

        private NodeAndNewFlag(Node n, boolean isNew) {
            this.n = n;
            this.isNew = isNew;
        }
    }

    private void makeDot() {
        try {
            PrintWriter writer = new PrintWriter("suffixtree.dot");
            writer.println("digraph suffixtree {");

            // Run through the tree
            printSubtreeNodes(root, writer);
            writer.println("");
            printSubtreeEdges(root, writer);

            writer.println("}");
            writer.close();

            System.in.read(); // Wait for user input in order to inspect the tree
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void printSubtreeNodes(Node n, PrintWriter w) {
        // Print a label (index i) on terminal nodes
        String label = n.getLabel(input);
        String shape = "ellipse";

        if (n.getAllEdges().isEmpty()) {
            label = input.length() - n.getLabel(input).length() + 1 + "";
            shape = "ellipse";
        }

        w.println("\t\"_" + n.getLabel(input) + "\" [label=\""+label+"\", shape=\""+shape+"\"]");

        for (Edge e : n.getAllEdges()) {
            printSubtreeNodes(e.getTo(), w);
        }
    }

    private void printSubtreeEdges(Node n, PrintWriter w) {
        for (Edge e : n.getAllEdges()) {
            w.println("\t\"_" + e.getFrom().getLabel(input) + "\" -> \"_" + e.getTo().getLabel(input) + "\" [label=\" "+ e.getLabel(input) +"\"]");
            printSubtreeEdges(e.getTo(), w);
        }
    }

    public static void main(String[] args) {
        new McCreight("abaab");
    }
}
