package core;

import java.util.ArrayList;
import java.util.List;

public class ExactMatch {

    private String input;
    private String pattern;

    public ExactMatch(String input, String pattern) {
        this.input   = input;
        this.pattern = pattern;
    }

    public List<Integer> searchNaive() {
        List<Integer> res = new ArrayList<Integer>();
        for (int i = 0; i < input.length() - pattern.length() + 1; i++) {
            if (pattern.equals(input.substring(i, i + pattern.length()))) {
                res.add(i + 1);
            }
        }
        return res;
    }

    public List<Integer> searchBorderArray() {
        List<Integer> res = new ArrayList<Integer>();

        int[] border = constructBorderArray(pattern + "$" + input);
        for (int i = 0; i < border.length; i++) {
            if (border[i] == pattern.length()) {
                res.add((i - 2*pattern.length()) + 1);
            }
        }

        return res;
    }

    private int[] constructBorderArray(String str) {
        char[] x = str.toCharArray();
        int[] border = new int[x.length];
        border[0] = 0;

        for (int i = 0; i < x.length - 1; i++) {
            int b = border[i];
            while (b > 0 && x[i+1] != x[b]) {
                b = border[b - 1];
            }
            if (x[i+1] == x[b]) border[i+1] = b+1;
            else                border[i+1] = 0;
        }

        return border;
    }

    public void printSearch(List<Integer> l) {
        System.out.print("Matches: ");
        for (int i : l) System.out.print(i + " ");
        System.out.println();
    }

}
