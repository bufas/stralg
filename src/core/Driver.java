package core;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Collections;
import java.util.List;

public class Driver {

    public static void main(String[] args) throws IOException {
        // Print usage help
        if (args.length != 2) {
            System.out.println("Please call this program with a file and a search string.");
            System.out.println("Ex. java core.McCreight.java file.txt xx");
            return;
        }

        // Read input file
        StringBuilder input = new StringBuilder();
        BufferedReader br = new BufferedReader(new FileReader(args[0]));
        int c;
//        while ((c = br.read()) != -1) if (c != '\r' && c != '\n') input.append((char) c);
        while ((c = br.read()) != -1) input.append((char) c);

        // Build and search tree for the query string
        McCreight mc = new McCreight(input.toString());
        List<Integer> search = mc.search(args[1]);
        Collections.sort(search);

        // Print result
        System.out.println();
        System.out.print("The search returned:");
        for (int i : search) {
            System.out.print(" " + i);
        }
        System.out.println();
    }

}
