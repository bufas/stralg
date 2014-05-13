package core;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;

public class Fibonacci {
    public static String generate(String a, String b, int s) {
        if (s == 0) return b;
        if (s == 1) return a;

        StringBuilder f0 = new StringBuilder(b);
        StringBuilder f1 = new StringBuilder(a);

        for (int i = 2; i <= s; i++) {
            if (i % 2 == 0) {
                f0.append(f1);
            } else {
                f1.append(f0);
            }
        }

        return (s % 2 == 0) ? f0.reverse().toString() : f1.reverse().toString();
    }

    public static void main(String[] args) throws Exception {
        int min =  (args.length > 0) ? Integer.parseInt(args[0]) : 0;
        int max = ((args.length > 1) ? Integer.parseInt(args[1]) :
                   (args.length > 0) ? min : 30) + 1;

        File dir = new File("testinput");
        for (int i = min; i < max; i++) {
            System.out.print(i + "... ");
            String fib = generate("a", "b", i);
            File file = new File(dir, "fib" + i + ".txt");
            BufferedWriter bw = new BufferedWriter(new FileWriter(file));
            bw.write(fib);
            bw.close();
            System.out.println("Done!");
        }
    }
}
