package core;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class TandemRepeat {

    String input;         // The string the suffix tree contains
    int[] dfsNumbering;   // A conversion array from leaf indices to their DFS numbers
    List<Repeat> repeats; // Contains all tandem repeats found

    public TandemRepeat(String input, Node root) {
        this.input = input;
        this.repeats = new ArrayList<Repeat>();
        dfsNumbering = new int[input.length()];
        traverse(root, 1, 0);
        findNonBranching();
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (Repeat r : repeats) sb.append(r.toString()).append('\n');
        return sb.toString();
    }

    /**
     * Find all non-branching tandem repeats from the branching ones found by
     * left rotating them.
     */
    private void findNonBranching() {
        List<Repeat> nonBranching = new ArrayList<Repeat>();
        for (Repeat r : repeats) {
            int curIdx = r.idx - 1;
            while (input.charAt(curIdx) == input.charAt(curIdx + r.len)) {
                nonBranching.add(new Repeat(curIdx, r.len, false));
                curIdx--;
            }
        }
        repeats.addAll(nonBranching);
    }

    /**
     * DFS post-order traverse the tree, and process each internal node by
     * finding branching tandem repeats.
     * @param n the node to process
     * @param curIdx the DFS-number to give the next leaf
     * @param depth the depth of the node aka. the length of the label of the node
     * @return a list of leaf indices that is in the subtree of this node
     */
    private List<Integer> traverse(Node n, int curIdx, int depth) {
        // Handle leaves
        if (n.getAllEdges().isEmpty()) {
            int leafNumber = input.length() - n.getLabel().length();
            dfsNumbering[leafNumber] = curIdx;
            return new ArrayList<Integer>(Arrays.asList(leafNumber));
        }

        // Handle internal nodes
        int dfsSpanStart = curIdx;
        List<Integer> leafList = new ArrayList<Integer>();
        for (Edge e : n.getAllEdges()) {
            List<Integer> subtreeLeafList = traverse(e.getTo(), curIdx, depth + e.getLength());
            leafList.addAll(subtreeLeafList);
            curIdx += subtreeLeafList.size();
        }
        int dfsSpanEnd = curIdx;

        processNode(leafList, depth, dfsSpanStart, dfsSpanEnd);

        return leafList;
    }

    /**
     * Find all branching tandem repeats in the given node. We iterate through each leaf "i" in
     * the subtree, and if leaf "i+depth" is also in the subtree, but in another child's subtree
     * than "i", we have found a tandem repeat.
     * @param leafList a list of all leaf indices in the subtree
     * @param depth the depth of the node aka. the length of the label of the node
     * @param dfsSpanStart the start of the DFS numbering of the children
     * @param dfsSpanEnd the end of the DFS numbering of the children
     */
    private void processNode(List<Integer> leafList, int depth, int dfsSpanStart, int dfsSpanEnd) {
        for (Integer i : leafList) {
            // Check if wer are out of bounds
            if (i + depth >= dfsNumbering.length || (i + (2 * depth)) >= input.length()) continue;

            // Do checks from 2b page 6
            int dfsNum = dfsNumbering[i + depth];
            if ((dfsNum >= dfsSpanStart && dfsNum <= dfsSpanEnd) && input.charAt(i) != input.charAt(i + (2 * depth))) {
                // We found a branching repeat
                repeats.add(new Repeat(i, depth, true));
            }
        }
    }

    /**
     * A simple class to contain the information of a tandem repeat.
     */
    private class Repeat {
        boolean branching;
        int idx, len;
        public Repeat(int i, int l, boolean b) { idx = i; len = l; branching = b; }
        public String toString() { return String.format("(%d,%d,2) %s", idx, len, (branching) ? "branching" : "non-branching"); }
    }
}
