import java.util.Arrays;

/** Mission 3: find a way out with an iterative depth-first search. */
public class Escape {

    public record Result(int[] path, int visited, int deadEnds) {}

    public static Result escape(Input.Walls walls, int[] broken) {
        int rows = walls.rows();
        int cols = walls.cols();
        int cellCount = walls.cells();
        int exit = cellCount - 1;
        int[][] open = Graphs.open(walls, broken);

        boolean[] visited = new boolean[cellCount];
        int[] parent = new int[cellCount];
        int[] stack = new int[cellCount];
        int[] nextDirection = new int[cellCount];
        Arrays.fill(parent, -1);

        // An explicit stack is used so a 1000 x 1000 maze cannot overflow the call stack.
        int stackSize = 1;
        int visitedCount = 1;
        stack[0] = 0;
        visited[0] = true;

        // Direction order: up, right, down, left.
        while (stackSize > 0 && stack[stackSize - 1] != exit) {
            int top = stackSize - 1;
            int cell = stack[top];

            if (nextDirection[top] == 4) {
                stackSize--;
                continue;
            }

            int direction = nextDirection[top]++;
            int nextCell = neighbour(cell, direction, rows, cols);

            if (nextCell == -1 || visited[nextCell] || !hasNeighbour(open[cell], nextCell)) {
                continue;
            }

            visited[nextCell] = true;
            visitedCount++;
            parent[nextCell] = cell;
            stack[stackSize] = nextCell;
            nextDirection[stackSize] = 0;
            stackSize++;
        }

        int deadEnds = 0;
        for (int[] neighbours : open) {
            if (neighbours.length == 1) {
                deadEnds++;
            }
        }

        if (!visited[exit]) {
            return new Result(new int[0], visitedCount, deadEnds);
        }

        int pathLength = 0;
        for (int cell = exit; cell != -1; cell = parent[cell]) {
            pathLength++;
        }

        int[] path = new int[pathLength];
        int position = pathLength - 1;
        for (int cell = exit; cell != -1; cell = parent[cell]) {
            path[position--] = cell;
        }

        return new Result(path, visitedCount, deadEnds);
    }

    private static int neighbour(int cell, int direction, int rows, int cols) {
        int row = cell / cols;
        int col = cell % cols;

        switch (direction) {
            case 0: return row > 0 ? cell - cols : -1;       // up
            case 1: return col + 1 < cols ? cell + 1 : -1;  // right
            case 2: return row + 1 < rows ? cell + cols : -1; // down
            case 3: return col > 0 ? cell - 1 : -1;         // left
            default: return -1;
        }
    }

    private static boolean hasNeighbour(int[] neighbours, int target) {
        for (int neighbour : neighbours) {
            if (neighbour == target) {
                return true;
            }
        }
        return false;
    }
}
