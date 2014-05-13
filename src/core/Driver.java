package core;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Collections;
import java.util.List;

public class Driver {

    public static void main(String[] args) throws Exception {
        tandemRepeats(args);
    }

    private static void tandemRepeats(String[] args) throws IOException {
//        // Print usage help
//        if (args.length != 1) {
//            System.out.println("Please call this program with a filename.");
//            System.out.println("Ex. java core.Driver file.txt");
//            return;
//        }

        // Read input file
        System.out.println("# bytes\ttime\titerations");
        for (int fille : new int[] {5000, 10000, 15000, 20000, 25000, 30000, 35000, 40000, 100000}) {
            StringBuilder input = new StringBuilder();
            BufferedReader br = new BufferedReader(new FileReader("testinput/lipsum"+fille+".txt"));
            int c;
            while ((c = br.read()) != -1) if (c != '\r' && c != '\n') input.append((char) c);

            // Build and search tree for the query string
            long total = 0;
            for (int i = 0; i < 5; i++) {
                McCreight mc = new McCreight(input.toString());
                long start = System.currentTimeMillis();
                mc.findTandemRepeats();
                total += System.currentTimeMillis() - start;
            }
            System.out.format("%d\t%d\t%d\n", fille, total, 5);
        }
    }

    private static void search(String[] args) throws IOException {
        // Print usage help
        if (args.length != 2) {
            System.out.println("Please call this program with a file and a search string.");
            System.out.println("Ex. java core.Driver file.txt xx");
            return;
        }

        // Read input file
        StringBuilder input = new StringBuilder();
        BufferedReader br = new BufferedReader(new FileReader(args[0]));
        int c;
        while ((c = br.read()) != -1) if (c != '\r' && c != '\n') input.append((char) c);

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
