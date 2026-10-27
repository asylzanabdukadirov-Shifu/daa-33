import java.util.Arrays;

/** Missions 4 and 5: strongly connected components and the cheapest route. */
public class Zones {

    /**
     * Mission 4: Kosaraju's algorithm.
     * The iterative DFS first records finishing order in the original graph,
     * then visits the reversed graph in the opposite order to label each zone.
     */
    public static int[] kosaraju(Input.Slides slides) {
        int n = slides.n();
        int[][] outgoing = Graphs.slidesFrom(slides);
        int[][] incoming = Graphs.slidesInto(slides);

        boolean[] visited = new boolean[n];
        int[] finishOrder = new int[n];
        int finished = 0;
        int[] stack = new int[n];
        int[] nextEdge = new int[n];

        // First pass: DFS in the original slide graph and save finish order.
        for (int start = 0; start < n; start++) {
            if (visited[start]) {
                continue;
            }

            int stackSize = 1;
            stack[0] = start;
            nextEdge[0] = 0;
            visited[start] = true;

            while (stackSize > 0) {
                int top = stackSize - 1;
                int cell = stack[top];

                if (nextEdge[top] < outgoing[cell].length) {
                    int edge = outgoing[cell][nextEdge[top]++];
                    int nextCell = slides.to()[edge];

                    if (!visited[nextCell]) {
                        visited[nextCell] = true;
                        stack[stackSize] = nextCell;
                        nextEdge[stackSize] = 0;
                        stackSize++;
                    }
                } else {
                    finishOrder[finished++] = cell;
                    stackSize--;
                }
            }
        }

        // Second pass: visit the reversed graph in decreasing finish order.
        int[] zoneOf = new int[n];
        Arrays.fill(zoneOf, -1);
        int zoneCount = 0;

        for (int i = finished - 1; i >= 0; i--) {
            int start = finishOrder[i];
            if (zoneOf[start] != -1) {
                continue;
            }

            int stackSize = 1;
            stack[0] = start;
            zoneOf[start] = zoneCount;

            while (stackSize > 0) {
                int cell = stack[--stackSize];

                for (int edge : incoming[cell]) {
                    int previous = slides.from()[edge];
                    if (zoneOf[previous] == -1) {
                        zoneOf[previous] = zoneCount;
                        stack[stackSize++] = previous;
                    }
                }
            }

            zoneCount++;
        }

        return zoneOf;
    }

    /**
     * Mission 5: process the zone graph in topological order.
     * Returns [longest chain length, cheapest start-to-exit route cost].
     */
    public static long[] routes(Input.Slides slides, int[] zoneOf, int zoneCount) {
        int[][] outgoing = Graphs.slidesFrom(slides);
        long[] longest = new long[zoneCount];
        long[] distance = new long[zoneCount];
        Arrays.fill(longest, 1); // A chain containing one zone has length 1.
        Arrays.fill(distance, Long.MAX_VALUE);

        // Group cells by zone so each cell is processed only once.
        int[] firstCell = new int[zoneCount];
        int[] nextCell = new int[slides.n()];
        Arrays.fill(firstCell, -1);
        for (int cell = 0; cell < slides.n(); cell++) {
            int zone = zoneOf[cell];
            nextCell[cell] = firstCell[zone];
            firstCell[zone] = cell;
        }

        int startZone = zoneOf[0];
        int exitZone = zoneOf[slides.n() - 1];
        distance[startZone] = 0;
        long flowStages = 1;

        // Kosaraju numbers zones in topological order: edges go from low to high.
        for (int zone = 0; zone < zoneCount; zone++) {
            for (int cell = firstCell[zone]; cell != -1; cell = nextCell[cell]) {
                for (int edge : outgoing[cell]) {
                    int nextZone = zoneOf[slides.to()[edge]];
                    if (nextZone == zone) {
                        continue; // Movement inside one zone is free.
                    }

                    longest[nextZone] = Math.max(longest[nextZone], longest[zone] + 1);

                    if (distance[zone] != Long.MAX_VALUE) {
                        long newCost = distance[zone] + slides.time()[edge];
                        if (newCost < distance[nextZone]) {
                            distance[nextZone] = newCost;
                        }
                    }
                }
            }
            flowStages = Math.max(flowStages, longest[zone]);
        }

        return new long[]{flowStages, distance[exitZone]};
    }
}
