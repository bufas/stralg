package core;

import java.util.*;

public class TandemRepeat {

    String input;        // The string the suffix tree contains
    int[] dfsNumbering;  // A conversion array from leaf indices to their DFS numbers
    Set<Repeat> repeats; // Contains all tandem repeats found

    public TandemRepeat(String input, Node root) {
        this.input = input;
        this.repeats = new HashSet<Repeat>();
        dfsNumbering = new int[input.length()];
        findBranchingRepeats(root, 1, 0);
//        findNonBranchingRepeats();
    }

    public String toString() {
        return repeats.size() + "";
//        int branchingCount = 0;
//        StringBuilder sb = new StringBuilder();
//        for (Repeat r : repeats) {
//            sb.append(r.toString()).append('\n');
//            if (r.branching) branchingCount++;
//        }
//        sb.append(branchingCount).append(" ").append(repeats.size() - branchingCount).append('\n');
//        return sb.toString();
    }

    /**
     * Find all non-branching tandem repeats from the branching ones found by
     * left rotating them.
     */
    private void findNonBranchingRepeats() {
        List<Repeat> nonBranching = new ArrayList<Repeat>();
        for (Repeat r : repeats) {
            int curIdx = r.idx - 1;
            while (curIdx >= 0 && input.charAt(curIdx) == input.charAt(curIdx + r.len)) {
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
    private VeryList<Integer> findBranchingRepeats(Node n, int curIdx, int depth) {
        // Handle leaves
        if (n.getAllEdges().isEmpty()) {
            int leafNumber = n.leafIdx - 1;
            dfsNumbering[leafNumber] = curIdx;
            return new VeryList<Integer>(leafNumber);
        }

        // Handle internal nodes
        int dfsSpanStart = curIdx;
        List<VeryList<Integer>> subtreeLeafLists = new ArrayList<VeryList<Integer>>();
        int largestSubtree = -1;
        for (Edge e : n.getAllEdges()) {
            VeryList<Integer> subtreeLeafList = findBranchingRepeats(e.getTo(), curIdx, depth + e.getLength());
            subtreeLeafLists.add(subtreeLeafList);
            curIdx += subtreeLeafList.size();

            // Keep track of the largest subtree
            if (largestSubtree == -1 || subtreeLeafList.size() > subtreeLeafLists.get(largestSubtree).size()) {
                largestSubtree = subtreeLeafLists.size() - 1;
            }
        }
        int dfsSpanEnd = curIdx;

        // Build LL and LL' (the leaf list without the largest child subtree)
        VeryList<Integer> leafList = new VeryList<Integer>();
        VeryList<Integer> leafListPrime = new VeryList<Integer>();
        for (int i = 0; i < subtreeLeafLists.size(); i++) {
            leafList.append(subtreeLeafLists.get(i));
            if (i != largestSubtree) leafListPrime.append(subtreeLeafLists.get(i));
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
    private void processNode(VeryList<Integer> leafListPrime, int depth, int dfsSpanStart, int dfsSpanEnd) {
        for (Integer i : leafListPrime) {
            int x = i + depth;
            int y = i - depth;
            int z = i + (2 * depth);
            int n = input.length();

            // Case 1
            if (z < n) {
                int xDFS = dfsNumbering[x];
                if(xDFS >= dfsSpanStart && xDFS <= dfsSpanEnd && input.charAt(i) != input.charAt(z)) {
                    repeats.add(new Repeat(i, depth, true));
                }
            }

            // Case 2
            if (y >= 0 && x < n) {
                int yDFS = dfsNumbering[y];
                if (yDFS >= dfsSpanStart && yDFS <= dfsSpanEnd && input.charAt(y) != input.charAt(x)) {
                    repeats.add(new Repeat(y, depth, true));
                }
            }
        }
    }

    /**
     * A simple class to contain the information of a tandem repeat.
     */
    private class Repeat implements Comparable<Repeat> {
        boolean branching;
        int idx, len;
        public Repeat(int i, int l, boolean b) { idx = i; len = l; branching = b; }
        public String toString() { return String.format("(%d,%d,2) %s", idx, len, (branching) ? "branching" : "non-branching"); }
        public int hashCode() { return idx * 19 + len * 7; }

        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (obj == null) return false;
            if (!(obj instanceof Repeat)) return false;

            Repeat r = (Repeat) obj;
            return (idx == r.idx && len == r.len);
        }

        public int compareTo(Repeat r) {
            if (idx > r.idx) return 1;
            if (idx < r.idx) return -1;
            return len - r.len;
        }
    }

    private static class VeryList<E> implements Iterable<E> {
        private Entry<E> first;
        private Entry<E> last;

        private int size;

        public VeryList() {
            first = null;
            last  = null;
            size  = 0;
        }

        public VeryList(E element) {
            this();
            add(element);
        }

        public void add(E element) {
            Entry<E> entry = new Entry<E>(element);
            if (first == null) {
                first = last = entry;
            } else {
                last.next = entry;
                last = entry;
            }
            size++;
        }

        public void append(VeryList<E> other) {
            if (first == null) {
                first = other.first;
                last  = other.last;
            } else {
                last.next = other.first;
                last = other.last;
            }
            size += other.size;
        }

        public int size() {
            return size;
        }

        @Override
        public Iterator<E> iterator() {
            return new Iterator<E>() {
                private Entry<E> current = null;

                @Override
                public boolean hasNext() {
                    return first != null && (current == null || current.next != null);
                }

                @Override
                public E next() {
                    if (current == null) {
                        current = first;
                    } else {
                        current = current.next;
                    }
                    return current.element;
                }

                @Override
                public void remove() {
                    throw new IllegalArgumentException("Ulovligheder!!");
                }
            };
        }

        private class Entry<E> {
            private E element;
            private Entry<E> next;

            public Entry(E element) {
                this.element = element;
                this.next    = null;
            }
        }
    }
}
