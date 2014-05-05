package core;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class TandemRepeat {

    String input;
    int[] dfsNumbering;

    public void printTandemRepeats(String input, Node root) {
        this.input = input;
        dfsNumbering = new int[input.length()];
        traverse(root, 1, 0);
    }

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
        int dfsSpanEnd = curIdx += leafList.size() - 1;

        processNode(leafList, depth, dfsSpanStart, dfsSpanEnd);

        return leafList;
    }

    private void processNode(List<Integer> leafList, int depth, int dfsSpanStart, int dfsSpanEnd) {
        for (Integer i : leafList) {
            // Check if wer are out of bounds
            if (i + depth >= dfsNumbering.length || (i + (2 * depth)) >= input.length()) continue;

            // Do checks from 2b page 6
            int dfsNum = dfsNumbering[i + depth];
            if ((dfsNum >= dfsSpanStart && dfsNum <= dfsSpanEnd) && input.charAt(i) != input.charAt(i + (2 * depth))) {
                // We found a branching repeat
                System.out.printf("(%d,%d,2) Branching\n", i, depth);
            }
        }
    }
}
