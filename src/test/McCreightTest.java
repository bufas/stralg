package test;

import core.Edge;
import core.Node;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class McCreightTest {

    @Test
    public void testMakeEdge() throws Exception {
        String input = "abcdefghijklmnopqrstuvwxyz";
        Node n1 = new Node(input, 0, 0);
        Node n2 = new Node(input, 0, 10);

        Edge e  = new Edge(input, n1, n2, 0, 10);
        n1.addEdge(input.charAt(e.getIdx()), e);
    }

    @Test
    public void testSplitEdge() {
        String input = "abcdefghijklmnopqrstuvwxyz";
        Node n1 = new Node(input, 0, 0);
        Node n2 = new Node(input, 0, 10);
        Edge e  = new Edge(input, n1, n2, 0, 10);
        n1.addEdge(input.charAt(e.getIdx()), e);


    }
}
