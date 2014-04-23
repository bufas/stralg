package core;

import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Scanner;

public class McCreight {

    public static char TERM_SYMBOL = '$';
    private static boolean OUTPUT_DOT = false;

    private String input;
    private Node root;
    private PrintWriter dotOutput;

    public McCreight(String input) {
        this.input = input + TERM_SYMBOL;

        try {
            dotOutput = new PrintWriter(new BufferedWriter(new FileWriter("suffixtree.dot")));
            dotOutput.println("digraph suffixtree {");

            constructSuffixTree();

            dotOutput.println("}");
            dotOutput.close();
        } catch (IOException e) { e.printStackTrace(); }

    }

    /**
     * This is the same as slowscan, but it does not create a new node
     * it just returns the next if it ends on an edge
     * @return A node which matches the search, or null if the string is not in the tree
     */
    private Node slowscanNoCreate(Node start, String find) {
        // Handle empty strings
        if (find.equals("")) return start;

        Node curNode = start;
        int findCharCount = 0;
        while (true) {
            Edge e = curNode.getEdge(find.charAt(findCharCount));
            if (e == null) return null;
            for (int i = 0; i < e.getLength(); i++) {
                if (findCharCount == find.length() - 1) {
                    return e.getTo();
                } else if (input.charAt(e.getIdx() + i) != find.charAt(findCharCount)) {
                    return null;
                }
                findCharCount++;
            }

            curNode = e.getTo();
        }
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

    private Edge makeEdge(Node n1, Node n2, int idx, int length) {
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

        // Construct T_{i+1}
        Node head = root;
        String tail = input;
        for (int i = 0; i < input.length() - 1; i++) {
            makeDot(i);

            Node u;
            String v;

            if (head.getParentEdge() == null) {
                u = root;
                v = "";
            } else {
                u = head.getParent();
                v = head.getParentEdge().getLabel(input);
            }

//            System.out.println("-- INSERTING NODE "+(i+2)+" --");
//            System.out.println("\tstring  = " + input.substring(i+1));
//            System.out.println("\thead("+(i+1)+") = " + head.toString(input));
//            System.out.println("\ttail("+(i+1)+") = " + tail);
//            System.out.println("\tu       = " + u.toString(input));
//            System.out.println("\tv       = " + v);

            Node w;
            boolean wIsNew;
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
            if (wIsNew) {
                newHead = w;
            } else {
                if (v.isEmpty()) {
                    // Special case not covered in the book pseudocode
                    newHead = slowscan(w, input.substring(i + 1));
                } else {
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

        makeDot(input.length());
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
    private Node slowscan(Node start, String find) {
        Node curNode = start;
        int findCharCount = 0;

        while (true) {
            Edge e = curNode.getEdge(find.charAt(findCharCount));
            if (e == null) return curNode;
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

    private void makeDot(int iteration) {
        if (!OUTPUT_DOT) return;
        // Run through the tree
        printSubtreeNodes(root, dotOutput, iteration);
        dotOutput.println("");
        printSubtreeEdges(root, dotOutput, iteration);
    }

    private void printSubtreeNodes(Node n, PrintWriter w, int iteration) {
        // Print a label (index i) on terminal nodes
        String label = n.getLabel(input);
        String shape = "ellipse";

        if (n.getAllEdges().isEmpty()) {
            label = input.length() - n.getLabel(input).length() + 1 + "";
            shape = "ellipse";
        }

        w.println("\t\"_["+iteration+"]_" + n.getLabel(input) + "\" [label=\""+label+"\", shape=\""+shape+"\"]");

        for (Edge e : n.getAllEdges()) {
            printSubtreeNodes(e.getTo(), w, iteration);
        }
    }

    private void printSubtreeEdges(Node n, PrintWriter w, int iteration) {
//        if (n.getSuffixLink() != null) w.println("\t\"_["+iteration+"]_" + n.getLabel(input) + "\" -> \"_["+iteration+"]_" + n.getSuffixLink().getLabel(input) + "\" [weight=0, color=\"blue\"]");
        for (Edge e : n.getAllEdges()) {
            w.println("\t\"_["+iteration+"]_" + n.getLabel(input) + "\" -> \"_["+iteration+"]_" + e.getTo().getLabel(input) + "\" [label=\" "+ e.getLabel(input) +"\"]");
            printSubtreeEdges(e.getTo(), w, iteration);
        }
    }

    public static void main(String[] args) throws IOException {
        if (args.length != 2) {
            System.out.println("Please call this program with a file and a search string.");
            System.out.println("Ex. java core.McCreight.java file.txt xx");
            return;
        }

        StringBuilder input = new StringBuilder();
        BufferedReader br = new BufferedReader(new FileReader(args[0]));
        int c;
        while ((c = br.read()) != -1) input.append((char) c);

        McCreight mc = new McCreight(input.toString());
        List<Integer> search = mc.search(args[1]);
        Collections.sort(search);

        System.out.println();
        System.out.println();
        System.out.println("The input is of length " + input.toString().length());
        System.out.print("The search returned:");
        for (int i : search) {
            System.out.print(" " + i);
        }
        System.out.println();
    }
}
