package core;

import java.io.*;

public class DotMaker implements Closeable {

    private String input;
    private int prefixIdx;
    private PrintWriter out;

    public DotMaker(String i) throws IOException {
        input     = i;
        prefixIdx = 1;

        out = new PrintWriter(new BufferedWriter(new FileWriter("suffixtree.dot")));
        out.println("digraph suffixtree {");
    }

    @Override
    public void close() throws IOException {
        out.println("}");
        if (out != null) out.close();
    }


    public void addTree(Node root) {
        // Run through the tree
        printSubtreeNodes(root);
        out.println("");
        printSubtreeEdges(root);
        prefixIdx++;
    }

    private void printSubtreeNodes(Node n) {
        // Print a label (index i) on terminal nodes
        String label = n.getLabel(input);
        String shape = "ellipse";

        if (n.getAllEdges().isEmpty()) {
            label = input.length() - n.getLabel(input).length() + 1 + "";
            shape = "ellipse";
        }

        out.println("\t\"_["+prefixIdx+"]_" + n.getLabel(input) + "\" [label=\""+label+"\", shape=\""+shape+"\"]");

        for (Edge e : n.getAllEdges()) {
            printSubtreeNodes(e.getTo());
        }
    }

    private void printSubtreeEdges(Node n) {
//        if (n.getSuffixLink() != null) out.println("\t\"_["+prefixIdx+"]_" + n.getLabel(input) + "\" -> \"_["+prefixIdx+"]_" + n.getSuffixLink().getLabel(input) + "\" [weight=0, color=\"blue\"]");
        for (Edge e : n.getAllEdges()) {
            out.println("\t\"_["+prefixIdx+"]_" + n.getLabel(input) + "\" -> \"_["+prefixIdx+"]_" + e.getTo().getLabel(input) + "\" [label=\" "+ e.getLabel(input) +"\"]");
            printSubtreeEdges(e.getTo());
        }
    }


}
