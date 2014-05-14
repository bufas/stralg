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
        for (int i = 0; i < input.length() - pattern.length(); i++) {
            if (pattern.equals(input.substring(i, i + pattern.length()))) {
                res.add(i + 1);
            }
        }
        return res;
    }

    public void printSearch(List<Integer> l) {
        System.out.print("Matches: ");
        for (int i : l) System.out.print(i + " ");
        System.out.println();
    }

}
