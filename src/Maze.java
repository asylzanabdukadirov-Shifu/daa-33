import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.PriorityQueue;

/** Missions 1 and 2: build a maze and find its minimum spanning tree. */
public class Maze {

    /** Mission 1: process the walls in the same order as walls.txt. */
    public static int[] breakWalls(Input.Walls walls) {
        DisjointSet dsu = new DisjointSet(walls.cells());
        List<Integer> broken = new ArrayList<>();

        for (int i = 0; i < walls.count(); i++) {
            if (dsu.union(walls.a()[i], walls.b()[i])) {
                broken.add(i);
            }
        }

        return toIntArray(broken);
    }

    /** Mission 2: Kruskal's algorithm, processing the cheapest walls first. */
    public static int[] cheapestMaze(Input.Walls walls) {
        Integer[] order = new Integer[walls.count()];
        for (int i = 0; i < order.length; i++) {
            order[i] = i;
        }

        // The input gives every wall a different cost, so no tie-break is needed.
        Arrays.sort(order, Comparator.comparingInt(i -> walls.cost()[i]));

        DisjointSet dsu = new DisjointSet(walls.cells());
        List<Integer> chosen = new ArrayList<>();

        for (int wall : order) {
            if (dsu.union(walls.a()[wall], walls.b()[wall])) {
                chosen.add(wall);
                if (chosen.size() == walls.cells() - 1) {
                    break;
                }
            }
        }

        return toIntArray(chosen);
    }

    /** Mission 2: eager Prim, keeping the cheapest known wall for each cell. */
    public static long primCost(Input.Walls walls) {
        int cellCount = walls.cells();
        int wallCount = walls.count();
        if (cellCount == 0) {
            return 0;
        }

        // List the wall indexes touching every cell.
        int[] degree = new int[cellCount];
        for (int wall = 0; wall < wallCount; wall++) {
            degree[walls.a()[wall]]++;
            degree[walls.b()[wall]]++;
        }
        int[][] incident = new int[cellCount][];
        for (int cell = 0; cell < cellCount; cell++) {
            incident[cell] = new int[degree[cell]];
        }
        Arrays.fill(degree, 0);
        for (int wall = 0; wall < wallCount; wall++) {
            int a = walls.a()[wall];
            int b = walls.b()[wall];
            incident[a][degree[a]++] = wall;
            incident[b][degree[b]++] = wall;
        }

        boolean[] inTree = new boolean[cellCount];
        int[] best = new int[cellCount];
        Arrays.fill(best, Integer.MAX_VALUE);
        PriorityQueue<Candidate> queue = new PriorityQueue<>(
                Comparator.comparingInt(Candidate::cost));
        best[0] = 0;
        queue.add(new Candidate(0, 0));

        long totalCost = 0;
        int added = 0;
        while (!queue.isEmpty() && added < cellCount) {
            Candidate candidate = queue.poll();
            int cell = candidate.cell();
            if (inTree[cell] || candidate.cost() != best[cell]) {
                continue;
            }

            inTree[cell] = true;
            added++;
            totalCost += candidate.cost();
            for (int wall : incident[cell]) {
                int other = walls.a()[wall] == cell ? walls.b()[wall] : walls.a()[wall];
                int cost = walls.cost()[wall];
                if (!inTree[other] && cost < best[other]) {
                    best[other] = cost;
                    queue.add(new Candidate(other, cost));
                }
            }
        }

        if (added != cellCount) {
            throw new IllegalStateException("The maze graph is disconnected");
        }
        return totalCost;
    }

    private static int[] toIntArray(List<Integer> values) {
        int[] result = new int[values.size()];
        for (int i = 0; i < values.size(); i++) {
            result[i] = values.get(i);
        }
        return result;
    }

    private record Candidate(int cell, int cost) {}
}
