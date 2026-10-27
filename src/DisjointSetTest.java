/** Run with: java -cp out DisjointSetTest */
public class DisjointSetTest {
    private static int failed = 0;

    private static void check(boolean condition, String description) {
        System.out.println((condition ? "ok    " : "FAIL  ") + description);
        if (!condition) {
            failed++;
        }
    }

    public static void main(String[] args) {
        DisjointSet dsu = new DisjointSet(6);
        check(dsu.count() == 6, "six sets at the start");
        check(!dsu.connected(0, 1), "0 and 1 start in different sets");
        check(dsu.union(0, 1), "union joins two different sets");
        check(dsu.connected(0, 1), "0 and 1 are now connected");
        check(!dsu.union(1, 0), "union returns false for an existing connection");
        check(dsu.count() == 5, "the set count decreases after a union");

        dsu.union(2, 3);
        dsu.union(1, 3);
        check(dsu.connected(0, 2), "cells can be connected through other cells");
        check(dsu.count() == 3, "three sets remain after three successful unions");
        check(dsu.find(4) == 4 && dsu.find(5) == 5, "single-cell sets keep their own roots");
        check(!dsu.union(4, 4), "union of a cell with itself returns false");

        // Join many cells to check the implementation on a larger input.
        int n = 10_000;
        DisjointSet large = new DisjointSet(n);
        for (int i = 1; i < n; i++) {
            large.union(i - 1, i);
        }
        check(large.connected(0, n - 1), "a large chain connects the first and last cells");
        check(large.count() == 1, "one set remains after many unions");
        check(!large.union(123, 9_876), "repeated union on a large set changes nothing");

        System.out.println(failed == 0 ? "All checks passed." : failed + " checks failed.");
        if (failed > 0) {
            System.exit(1);
        }
    }
}
