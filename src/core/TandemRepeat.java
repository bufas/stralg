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
        int branchingCount = 0;
        StringBuilder sb = new StringBuilder();
        for (Repeat r : repeats) {
            sb.append(r.toString()).append('\n');
            if (r.branching) branchingCount++;
        }
        sb.append(branchingCount).append(" ").append(repeats.size() - branchingCount).append('\n');
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
        List<List<Integer>> subtreeLeafLists = new ArrayList<List<Integer>>();
        int largestSubtree = -1;
        for (Edge e : n.getAllEdges()) {
            List<Integer> subtreeLeafList = traverse(e.getTo(), curIdx, depth + e.getLength());
            subtreeLeafLists.add(subtreeLeafList);
            curIdx += subtreeLeafList.size();

            // Keep track of the largest subtree
            if (largestSubtree == -1 || subtreeLeafList.size() > subtreeLeafLists.get(largestSubtree).size()) {
                largestSubtree = subtreeLeafLists.size() - 1;
            }
        }
        int dfsSpanEnd = curIdx;

        // Build LL and LL'
        List<Integer> leafList = new ArrayList<Integer>();
        List<Integer> leafListPrime = new ArrayList<Integer>();
        for (int i = 0; i < subtreeLeafLists.size(); i++) {
            leafList.addAll(subtreeLeafLists.get(i));
            if (i != largestSubtree) leafListPrime.addAll(subtreeLeafLists.get(i));
        }

        processNode(leafListPrime, depth, dfsSpanStart, dfsSpanEnd);

        return leafList;
    }

    /**
     * Find all branching tandem repeats in the given node. We iterate through each leaf "i" in
     * the subtree, and if leaf "i+depth" is also in the subtree, but in another child's subtree
     * than "i", we have found a tandem repeat.
     *
     * Case 1:
     * +----------------+----------+----------+-------+
     * |                |a ...     |a ...     |b      |
     * +----------------+----------+----------+-------+
     *  0     y          i          x          z      n
     *
     * Case 2:
     * +-----+----------+----------+------------------+
     * |     |a ...     |a ...     |b                 |
     * +-----+----------+----------+------------------+
     *  0     y          i          x          z      n
     *
     *  x = i + depth
     *  y = i - depth
     *  z = i + (2*depth)
     *  n = input.length()
     *
     * @param leafListPrime a list of all leaf indices in the subtree except for the largest child subtree
     * @param depth the depth of the node aka. the length of the label of the node
     * @param dfsSpanStart the start of the DFS numbering of the children
     * @param dfsSpanEnd the end of the DFS numbering of the children
     */
    private void processNode(List<Integer> leafListPrime, int depth, int dfsSpanStart, int dfsSpanEnd) {
        for (Integer i : leafListPrime) {
            int x = i + depth;
            int y = i - depth;
            int z = i + (2 * depth);
            int n = input.length();

            int xDFS = dfsNumbering[x];
            int yDFS = dfsNumbering[y];

            // Case 1
            if (z < n && xDFS >= dfsSpanStart && xDFS <= dfsSpanEnd && input.charAt(i) != input.charAt(z)) {
                repeats.add(new Repeat(i, depth, true));
            }

            // Case 2
            if (y >= 0 && x < n && yDFS >= dfsSpanStart && yDFS <= dfsSpanEnd && input.charAt(y) != input.charAt(x)) {
                repeats.add(new Repeat(y, depth, true));
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
