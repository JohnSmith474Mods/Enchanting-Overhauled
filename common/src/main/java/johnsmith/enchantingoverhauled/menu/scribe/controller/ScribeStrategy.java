package johnsmith.enchantingoverhauled.menu.scribe.controller;

import johnsmith.enchantingoverhauled.menu.scribe.model.ScribeGameState;
import johnsmith.enchantingoverhauled.menu.scribe.model.ScribeGridData;

import java.util.*;
import java.util.concurrent.CompletableFuture;

/**
 * Interface for the Scribe Minesweeper minigame strategy.
 * Encapsulates the core gameplay loop and ruleset variations.
 */
public sealed interface ScribeStrategy permits
        ScribeStrategy.Classic,
        ScribeStrategy.Superposition
{

    // region Classic
    /**
     * Processes an incoming interaction payload, delegating to the appropriate logic
     * based on the current game state and action type.
     *
     * @param input The decoded user interaction.
     * @return A CompletableFuture representing the execution of the input, enabling async chaining.
     */
    CompletableFuture<Void> processInput(ScribeInput input);

    /**
     * Encapsulates the pure algorithmic logic of the Classic Minesweeper mini-game.
     * <p>
     * This controller operates strictly on the abstract {@link ScribeGridData} and has zero
     * dependencies on Minecraft items, players, or levels, ensuring high testability.
     */
    final class Classic implements ScribeStrategy {
        private final ScribeGridData grid;
        private final Random random = new Random();

        public Classic(ScribeGridData grid) {
            this.grid = grid;
        }

        /**
         * Processes an incoming interaction payload, delegating to the appropriate logic
         * based on the current game state and action type.
         *
         * @param input The decoded user interaction.
         */
        @Override
        public CompletableFuture<Void> processInput(ScribeInput input) {
            ScribeGameState state = this.grid.getGameState();

            // Start Game Overlay Click
            if (input.action() == ScribeInput.ACTION_START && state == ScribeGameState.READY) {
                this.grid.setGameState(ScribeGameState.PENDING);
                return CompletableFuture.completedFuture(null);
            }

            // Validate bounds against fixed matrix
            if (input.x() < 0 || input.x() >= ScribeGridData.MAX_COLS || input.y() < 0 || input.y() >= ScribeGridData.MAX_ROWS) {
                return CompletableFuture.completedFuture(null);
            }

            // Abort the sequence manually
            if (input.action() == ScribeInput.ACTION_ABORT && (state == ScribeGameState.PENDING || state == ScribeGameState.PLAYING)) {
                this.resetGrid();
                return CompletableFuture.completedFuture(null);
            }

            // Restart on End Screen Click or Acknowledge
            if (state == ScribeGameState.WON || state == ScribeGameState.LOST) {
                if (input.action() == ScribeInput.ACTION_RETURN_TO_INDEX && state == ScribeGameState.WON) {
                    this.resetGrid();
                } else if (input.action() == ScribeInput.ACTION_ACK_LOSS && state == ScribeGameState.LOST) {
                    this.resetGrid();
                }
                return CompletableFuture.completedFuture(null);
            }

            // Generate Board on First Click
            if (state == ScribeGameState.PENDING && input.action() == ScribeInput.ACTION_REVEAL) {
                this.generateMines(input.x(), input.y());
                this.grid.setGameState(ScribeGameState.PLAYING);
            }

            // Handle Active Play Interactions
            if (input.action() == ScribeInput.ACTION_REVEAL) {
                if (this.grid.isRevealed(input.x(), input.y())) {
                    this.chordCell(input.x(), input.y());
                } else {
                    this.revealCell(input.x(), input.y());
                }
            } else if (input.action() == ScribeInput.ACTION_FLAG) {
                this.toggleFlag(input.x(), input.y());
            }

            this.checkWinCondition();
            return CompletableFuture.completedFuture(null);
        }

        // region Minesweeper Algorithms
        private void generateMines(int safeX, int safeY) {
            int minesPlaced = 0;
            int mineCount = this.grid.getMineCount();

            while (minesPlaced < mineCount) {
                int rx = this.random.nextInt(ScribeGridData.MAX_COLS);
                int ry = this.random.nextInt(ScribeGridData.MAX_ROWS);

                if (Math.abs(rx - safeX) <= 1 && Math.abs(ry - safeY) <= 1) continue;

                if (!this.grid.hasMine(rx, ry)) {
                    this.grid.setMask(rx, ry, ScribeGridData.MASK_MINE);
                    minesPlaced++;
                }
            }

            for (int y = 0; y < ScribeGridData.MAX_ROWS; y++) {
                for (int x = 0; x < ScribeGridData.MAX_COLS; x++) {
                    if (!this.grid.hasMine(x, y)) {
                        this.grid.setAdjacentMines(x, y, this.countAdjacent(x, y, ScribeGridData.MASK_MINE));
                    }
                }
            }
        }

        private void revealCell(int startX, int startY) {
            if (this.grid.isRevealed(startX, startY) || this.grid.isFlagged(startX, startY)) return;

            if (this.grid.hasMine(startX, startY)) {
                this.triggerLoss();
                return;
            }

            Queue<int[]> queue = new LinkedList<>();
            queue.add(new int[]{startX, startY});

            while (!queue.isEmpty()) {
                int[] pos = queue.poll();
                int x = pos[0];
                int y = pos[1];

                if (this.grid.isRevealed(x, y) || this.grid.isFlagged(x, y)) continue;

                this.grid.setMask(x, y, ScribeGridData.MASK_REVEALED);

                if (this.grid.getAdjacentMines(x, y) == 0) {
                    for (int dy = -1; dy <= 1; dy++) {
                        for (int dx = -1; dx <= 1; dx++) {
                            if (dx == 0 && dy == 0) continue;
                            int nx = x + dx;
                            int ny = y + dy;

                            if (nx >= 0 && nx < ScribeGridData.MAX_COLS && ny >= 0 && ny < ScribeGridData.MAX_ROWS) {
                                queue.add(new int[]{nx, ny});
                            }
                        }
                    }
                }
            }
        }

        private void chordCell(int x, int y) {
            int adj = this.grid.getAdjacentMines(x, y);
            if (adj == 0) return;

            int flagCount = this.countAdjacent(x, y, ScribeGridData.MASK_FLAGGED);

            if (flagCount >= adj) {
                for (int dy = -1; dy <= 1; dy++) {
                    for (int dx = -1; dx <= 1; dx++) {
                        if (dx == 0 && dy == 0) continue;
                        int nx = x + dx;
                        int ny = y + dy;

                        if (nx >= 0 && nx < ScribeGridData.MAX_COLS && ny >= 0 && ny < ScribeGridData.MAX_ROWS) {
                            if (!this.grid.isFlagged(nx, ny) && !this.grid.isRevealed(nx, ny)) {
                                this.revealCell(nx, ny);
                            }
                        }
                    }
                }
            }
        }

        private void toggleFlag(int x, int y) {
            if (this.grid.isRevealed(x, y)) return;

            if (this.grid.isFlagged(x, y)) {
                this.grid.clearMask(x, y, ScribeGridData.MASK_FLAGGED);
            } else {
                this.grid.setMask(x, y, ScribeGridData.MASK_FLAGGED);
            }
        }

        private void triggerLoss() {
            this.grid.setGameState(ScribeGameState.LOST);
            for (int y = 0; y < ScribeGridData.MAX_ROWS; y++) {
                for (int x = 0; x < ScribeGridData.MAX_COLS; x++) {
                    if (this.grid.hasMine(x, y)) {
                        this.grid.setMask(x, y, ScribeGridData.MASK_REVEALED);
                    }
                }
            }
        }

        private void checkWinCondition() {
            if (this.grid.getGameState() == ScribeGameState.LOST) return;

            int mineCount = this.grid.getMineCount();
            int revealedCount = 0;

            for (int y = 0; y < ScribeGridData.MAX_ROWS; y++) {
                for (int x = 0; x < ScribeGridData.MAX_COLS; x++) {
                    if (this.grid.isRevealed(x, y)) revealedCount++;
                }
            }

            if (revealedCount == ScribeGridData.TOTAL_CELLS - mineCount) {
                this.grid.setGameState(ScribeGameState.WON);
                for (int y = 0; y < ScribeGridData.MAX_ROWS; y++) {
                    for (int x = 0; x < ScribeGridData.MAX_COLS; x++) {
                        if (this.grid.hasMine(x, y)) {
                            this.grid.setMask(x, y, ScribeGridData.MASK_FLAGGED);
                        }
                    }
                }
            }
        }

        private void resetGrid() {
            for (int i = 0; i < ScribeGridData.TOTAL_CELLS; i++) {
                this.grid.setCellState(i % ScribeGridData.MAX_COLS, i / ScribeGridData.MAX_COLS, 0);
            }
            this.grid.setGameState(ScribeGameState.MISSING_SOURCE);
        }

        private int countAdjacent(int x, int y, int mask) {
            int count = 0;
            for (int dy = -1; dy <= 1; dy++) {
                for (int dx = -1; dx <= 1; dx++) {
                    if (dx == 0 && dy == 0) continue;
                    int nx = x + dx;
                    int ny = y + dy;

                    if (nx >= 0 && nx < ScribeGridData.MAX_COLS && ny >= 0 && ny < ScribeGridData.MAX_ROWS) {
                        if ((this.grid.getCellState(nx, ny) & mask) != 0) count++;
                    }
                }
            }
            return count;
        }
        // endregion
    }
    // endregion

    // region Superposition
    /**
     * Encapsulates the algorithmic logic of the "Superposition" Minesweeper mini-game variant.
     * <p>
     * This variant prevents the player from encountering 50/50 guessing situations by lazily
     * evaluating mine layouts asynchronously behind the scenes as the player clicks, only collapsing into a
     * concrete board state when necessary.
     */
    final class Superposition implements ScribeStrategy {
        private final ScribeGridData grid;
        private final int[] logicalGrid; // -1 = unknown, 0-8 = revealed number
        private final Random random = new Random();

        private boolean isCalculating = false;

        public Superposition(ScribeGridData grid) {
            this.grid = grid;
            this.logicalGrid = new int[ScribeGridData.TOTAL_CELLS];
            this.resetGrid();
        }

        @Override
        public CompletableFuture<Void> processInput(ScribeInput input) {
            ScribeGameState state = this.grid.getGameState();

            // Start Game Overlay Click
            if (input.action() == ScribeInput.ACTION_START && state == ScribeGameState.READY) {
                this.grid.setGameState(ScribeGameState.PENDING);
                return CompletableFuture.completedFuture(null);
            }

            // Validate bounds against fixed matrix
            if (input.x() < 0 || input.x() >= ScribeGridData.MAX_COLS || input.y() < 0 || input.y() >= ScribeGridData.MAX_ROWS) {
                return CompletableFuture.completedFuture(null);
            }

            // Abort the sequence manually
            if (input.action() == ScribeInput.ACTION_ABORT && (state == ScribeGameState.PENDING || state == ScribeGameState.PLAYING)) {
                this.resetGrid();
                return CompletableFuture.completedFuture(null);
            }

            // Restart on End Screen Click or Acknowledge
            if (state == ScribeGameState.WON || state == ScribeGameState.LOST) {
                if (input.action() == ScribeInput.ACTION_RETURN_TO_INDEX && state == ScribeGameState.WON) {
                    this.resetGrid();
                } else if (input.action() == ScribeInput.ACTION_ACK_LOSS && state == ScribeGameState.LOST) {
                    this.resetGrid();
                }
                return CompletableFuture.completedFuture(null);
            }

            // Drop inputs if the async solver is actively running to prevent desyncs
            if (this.isCalculating) return CompletableFuture.completedFuture(null);

            // Generate initial collapse on First Click
            if (state == ScribeGameState.PENDING && input.action() == ScribeInput.ACTION_REVEAL) {
                this.generateFirstClickBoard(input.x(), input.y());
                this.grid.setGameState(ScribeGameState.PLAYING);
                this.checkWinCondition();
                return CompletableFuture.completedFuture(null);
            }

            // Handle Active Play Interactions
            if (state == ScribeGameState.PLAYING) {
                int idx = input.y() * ScribeGridData.MAX_COLS + input.x();

                if (input.action() == ScribeInput.ACTION_REVEAL) {
                    if (this.logicalGrid[idx] >= 0) {
                        return this.chordCell(input.x(), input.y());
                    } else if (!this.grid.isFlagged(input.x(), input.y())) {
                        this.isCalculating = true;
                        return CompletableFuture.runAsync(() -> this.handleReveal(input.x(), input.y()))
                                .thenRun(() -> {
                                    this.isCalculating = false;
                                    this.checkWinCondition();
                                });
                    }
                } else if (input.action() == ScribeInput.ACTION_FLAG) {
                    this.toggleFlag(input.x(), input.y());
                }
            }

            return CompletableFuture.completedFuture(null);
        }

        // region Quantum State Management

        private void handleReveal(int cx, int cy) {
            int targetIdx = cy * ScribeGridData.MAX_COLS + cx;

            QuantumSolver solver = new QuantumSolver(this.logicalGrid, ScribeGridData.MAX_COLS, ScribeGridData.MAX_ROWS, this.grid.getMineCount());
            solver.solve();

            boolean provablySafe = solver.isProvablySafe(targetIdx);
            boolean provablyMine = solver.isProvablyMine(targetIdx);
            boolean ambiguous = !provablySafe && !provablyMine;
            boolean hasOtherSafe = solver.hasAnyProvablySafe();

            boolean survive = true;

            if (provablyMine) {
                survive = false;
            } else if (ambiguous) {
                // Quantum Savior Doctrine: Punish if they guessed lazily, forgive if forced
                if (hasOtherSafe) {
                    survive = false;
                } else {
                    survive = true;
                }
            }

            if (survive) {
                boolean[] chosenConfig = solver.getRandomConfig(targetIdx, false);
                boolean[] fullBoard;
                if (chosenConfig == null) {
                    fullBoard = this.generateFallbackBoard(cx, cy, false);
                } else {
                    fullBoard = solver.generateFullBoard(chosenConfig, targetIdx, false);
                }

                this.floodFillReveal(cx, cy, fullBoard);
            } else {
                boolean[] chosenConfig = solver.getRandomConfig(targetIdx, true);
                boolean[] fullBoard;
                if (chosenConfig == null) {
                    fullBoard = this.generateFallbackBoard(cx, cy, true);
                } else {
                    fullBoard = solver.generateFullBoard(chosenConfig, targetIdx, true);
                }

                this.triggerLoss(fullBoard);
            }
        }

        private void generateFirstClickBoard(int safeX, int safeY) {
            boolean[] fullBoard = new boolean[ScribeGridData.TOTAL_CELLS];
            int placed = 0;
            int totalMines = this.grid.getMineCount();

            while (placed < totalMines) {
                int rx = this.random.nextInt(ScribeGridData.MAX_COLS);
                int ry = this.random.nextInt(ScribeGridData.MAX_ROWS);
                int idx = ry * ScribeGridData.MAX_COLS + rx;

                // Force a 3x3 safe area around the first click to guarantee an opening
                if (!fullBoard[idx] && (Math.abs(rx - safeX) > 1 || Math.abs(ry - safeY) > 1)) {
                    fullBoard[idx] = true;
                    placed++;
                }
            }

            this.floodFillReveal(safeX, safeY, fullBoard);
        }

        private boolean[] generateFallbackBoard(int safeX, int safeY, boolean forceMine) {
            boolean[] board = new boolean[ScribeGridData.TOTAL_CELLS];
            int targetIdx = safeY * ScribeGridData.MAX_COLS + safeX;
            board[targetIdx] = forceMine;

            int placed = forceMine ? 1 : 0;
            int totalMines = this.grid.getMineCount();

            while (placed < totalMines) {
                int idx = this.random.nextInt(ScribeGridData.TOTAL_CELLS);
                if (!board[idx] && idx != targetIdx) {
                    board[idx] = true;
                    placed++;
                }
            }
            return board;
        }

        // endregion

        // region Classic Mechanics (Adapted for Superposition)

        private void floodFillReveal(int startX, int startY, boolean[] fullBoard) {
            Queue<int[]> queue = new LinkedList<>();
            queue.add(new int[]{startX, startY});

            while (!queue.isEmpty()) {
                int[] pos = queue.poll();
                int x = pos[0];
                int y = pos[1];
                int idx = y * ScribeGridData.MAX_COLS + x;

                if (this.logicalGrid[idx] >= 0) continue;
                if (fullBoard[idx]) continue;

                int adj = this.countMinesAround(x, y, fullBoard);

                // Sync to Native Grid
                this.logicalGrid[idx] = adj;
                this.grid.setMask(x, y, ScribeGridData.MASK_REVEALED);
                this.grid.setAdjacentMines(x, y, adj);

                // Auto-chord 0s
                if (adj == 0) {
                    for (int dy = -1; dy <= 1; dy++) {
                        for (int dx = -1; dx <= 1; dx++) {
                            if (dx == 0 && dy == 0) continue;
                            int nx = x + dx;
                            int ny = y + dy;
                            if (nx >= 0 && nx < ScribeGridData.MAX_COLS && ny >= 0 && ny < ScribeGridData.MAX_ROWS) {
                                int nIdx = ny * ScribeGridData.MAX_COLS + nx;
                                if (this.logicalGrid[nIdx] == -1 && !this.grid.isFlagged(nx, ny)) {
                                    queue.add(new int[]{nx, ny});
                                }
                            }
                        }
                    }
                }
            }
        }

        private CompletableFuture<Void> chordCell(int x, int y) {
            int idx = y * ScribeGridData.MAX_COLS + x;
            int adj = this.logicalGrid[idx];
            if (adj <= 0) return CompletableFuture.completedFuture(null);

            int flagCount = 0;
            List<Integer> toReveal = new ArrayList<>();

            for (int dy = -1; dy <= 1; dy++) {
                for (int dx = -1; dx <= 1; dx++) {
                    if (dx == 0 && dy == 0) continue;
                    int nx = x + dx;
                    int ny = y + dy;
                    if (nx >= 0 && nx < ScribeGridData.MAX_COLS && ny >= 0 && ny < ScribeGridData.MAX_ROWS) {
                        int nIdx = ny * ScribeGridData.MAX_COLS + nx;
                        if (this.grid.isFlagged(nx, ny)) {
                            flagCount++;
                        } else if (this.logicalGrid[nIdx] == -1) {
                            toReveal.add(nIdx);
                        }
                    }
                }
            }

            if (flagCount >= adj && !toReveal.isEmpty()) {
                this.isCalculating = true;
                return CompletableFuture.runAsync(() -> {
                    for (int tIdx : toReveal) {
                        if (this.logicalGrid[tIdx] == -1 && this.grid.getGameState() == ScribeGameState.PLAYING) {
                            this.handleReveal(tIdx % ScribeGridData.MAX_COLS, tIdx / ScribeGridData.MAX_COLS);
                        }
                    }
                }).thenRun(() -> {
                    this.isCalculating = false;
                    this.checkWinCondition();
                });
            }

            return CompletableFuture.completedFuture(null);
        }

        private void toggleFlag(int x, int y) {
            int idx = y * ScribeGridData.MAX_COLS + x;
            if (this.logicalGrid[idx] >= 0) return;

            if (this.grid.isFlagged(x, y)) {
                this.grid.clearMask(x, y, ScribeGridData.MASK_FLAGGED);
            } else {
                this.grid.setMask(x, y, ScribeGridData.MASK_FLAGGED);
            }
        }

        private void triggerLoss(boolean[] fullBoard) {
            this.grid.setGameState(ScribeGameState.LOST);

            for (int i = 0; i < ScribeGridData.TOTAL_CELLS; i++) {
                if (fullBoard[i]) {
                    int px = i % ScribeGridData.MAX_COLS;
                    int py = i / ScribeGridData.MAX_COLS;
                    this.grid.setMask(px, py, ScribeGridData.MASK_REVEALED);
                    this.grid.setMask(px, py, ScribeGridData.MASK_MINE); // Explicitly inject the mine data to render the skull
                }
            }
        }

        private void checkWinCondition() {
            if (this.grid.getGameState() == ScribeGameState.LOST) return;

            int revealedCount = 0;
            for (int i = 0; i < ScribeGridData.TOTAL_CELLS; i++) {
                if (this.logicalGrid[i] >= 0) revealedCount++;
            }

            if (revealedCount == ScribeGridData.TOTAL_CELLS - this.grid.getMineCount()) {
                this.grid.setGameState(ScribeGameState.WON);
                for (int i = 0; i < ScribeGridData.TOTAL_CELLS; i++) {
                    if (this.logicalGrid[i] == -1) {
                        this.grid.setMask(i % ScribeGridData.MAX_COLS, i / ScribeGridData.MAX_COLS, ScribeGridData.MASK_FLAGGED);
                    }
                }
            }
        }

        private void resetGrid() {
            Arrays.fill(this.logicalGrid, -1);
            this.isCalculating = false;

            for (int i = 0; i < ScribeGridData.TOTAL_CELLS; i++) {
                this.grid.setCellState(i % ScribeGridData.MAX_COLS, i / ScribeGridData.MAX_COLS, 0);
            }
            this.grid.setGameState(ScribeGameState.MISSING_SOURCE);
        }

        private int countMinesAround(int x, int y, boolean[] fullBoard) {
            int count = 0;
            for (int dy = -1; dy <= 1; dy++) {
                for (int dx = -1; dx <= 1; dx++) {
                    if (dx == 0 && dy == 0) continue;
                    int nx = x + dx;
                    int ny = y + dy;
                    if (nx >= 0 && nx < ScribeGridData.MAX_COLS && ny >= 0 && ny < ScribeGridData.MAX_ROWS) {
                        if (fullBoard[ny * ScribeGridData.MAX_COLS + nx]) count++;
                    }
                }
            }
            return count;
        }

        // endregion

        // region Quantum Async Solver

        /**
         * Internal Constraint Solver for isolating boundary configurations and determining superpositions.
         */
        private class QuantumSolver {
            private final int[] logicGrid;
            private final int cols, rows, totalMines;

            private final List<Integer> boundary = new ArrayList<>();
            private final List<Integer> constraints = new ArrayList<>();

            private final List<boolean[]> validConfigs = new ArrayList<>();
            private final List<Integer> remainingMinesList = new ArrayList<>();

            private final int[] state; // 0 = safe, 1 = mine, -1 = unassigned
            private final List<Integer>[] neighbors;

            private int voidCells;

            @SuppressWarnings("unchecked")
            public QuantumSolver(int[] logicalGrid, int cols, int rows, int totalMines) {
                this.logicGrid = logicalGrid;
                this.cols = cols;
                this.rows = rows;
                this.totalMines = totalMines;
                this.state = new int[cols * rows];
                Arrays.fill(this.state, -1);

                this.neighbors = new ArrayList[cols * rows];
                for (int i = 0; i < cols * rows; i++) {
                    this.neighbors[i] = new ArrayList<>();
                    int cx = i % cols;
                    int cy = i / cols;
                    for (int dy = -1; dy <= 1; dy++) {
                        for (int dx = -1; dx <= 1; dx++) {
                            if (dx == 0 && dy == 0) continue;
                            int nx = cx + dx;
                            int ny = cy + dy;
                            if (nx >= 0 && nx < cols && ny >= 0 && ny < rows) {
                                this.neighbors[i].add(ny * cols + nx);
                            }
                        }
                    }
                }
            }

            public void solve() {
                Set<Integer> bSet = new HashSet<>();
                int revealedCount = 0;

                for (int i = 0; i < this.cols * this.rows; i++) {
                    if (this.logicGrid[i] >= 0) {
                        this.constraints.add(i);
                        revealedCount++;
                        for (int n : this.neighbors[i]) {
                            if (this.logicGrid[n] == -1) bSet.add(n);
                        }
                    }
                }

                this.boundary.addAll(bSet);
                this.voidCells = (this.cols * this.rows) - revealedCount - this.boundary.size();

                this.backtrack(0, 0);
            }

            private void backtrack(int bIdx, int currentMines) {
                if (this.validConfigs.size() >= 5000) return; // Hard execution cap to guarantee < 5ms operation

                if (bIdx == this.boundary.size()) {
                    for (int c : this.constraints) {
                        if (this.countMinesAround(c) != this.logicGrid[c]) return;
                    }

                    int remainingMines = this.totalMines - currentMines;
                    if (remainingMines >= 0 && remainingMines <= this.voidCells) {
                        boolean[] config = new boolean[this.cols * this.rows];
                        for (int i : this.boundary) config[i] = (this.state[i] == 1);

                        this.validConfigs.add(config);
                        this.remainingMinesList.add(remainingMines);
                    }
                    return;
                }

                int cell = this.boundary.get(bIdx);

                // Branch 1: Assume Safe
                this.state[cell] = 0;
                if (this.checkPartial(cell)) this.backtrack(bIdx + 1, currentMines);

                // Branch 2: Assume Mine
                this.state[cell] = 1;
                if (this.checkPartial(cell)) this.backtrack(bIdx + 1, currentMines + 1);

                // Restore
                this.state[cell] = -1;
            }

            private boolean checkPartial(int cell) {
                for (int c : this.neighbors[cell]) {
                    if (this.logicGrid[c] >= 0) {
                        int req = this.logicGrid[c];
                        int placed = 0;
                        int possible = 0;

                        for (int n : this.neighbors[c]) {
                            if (this.state[n] == 1) placed++;
                            else if (this.state[n] == -1 && this.logicGrid[n] == -1) possible++;
                        }

                        if (placed > req) return false;
                        if (placed + possible < req) return false;
                    }
                }
                return true;
            }

            private int countMinesAround(int cell) {
                int count = 0;
                for (int n : this.neighbors[cell]) if (this.state[n] == 1) count++;
                return count;
            }

            public boolean isProvablySafe(int idx) {
                if (this.validConfigs.isEmpty()) return false;
                if (this.boundary.contains(idx)) {
                    for (boolean[] cfg : this.validConfigs) {
                        if (cfg[idx]) return false;
                    }
                    return true;
                } else {
                    for (int rem : this.remainingMinesList) {
                        if (rem > 0) return false;
                    }
                    return true;
                }
            }

            public boolean isProvablyMine(int idx) {
                if (this.validConfigs.isEmpty()) return false;
                if (this.boundary.contains(idx)) {
                    for (boolean[] cfg : this.validConfigs) {
                        if (!cfg[idx]) return false;
                    }
                    return true;
                } else {
                    for (int rem : this.remainingMinesList) {
                        if (rem < this.voidCells) return false;
                    }
                    return true;
                }
            }

            public boolean hasAnyProvablySafe() {
                for (int b : this.boundary) {
                    if (this.isProvablySafe(b)) return true;
                }
                if (this.voidCells > 0) {
                    boolean allVoidSafe = true;
                    for (int rem : this.remainingMinesList) {
                        if (rem > 0) {
                            allVoidSafe = false;
                            break;
                        }
                    }
                    if (allVoidSafe) return true;
                }
                return false;
            }

            public boolean[] getRandomConfig(int targetIdx, boolean wantMine) {
                List<Integer> filteredIndices = new ArrayList<>();
                boolean isBoundary = this.boundary.contains(targetIdx);

                for (int i = 0; i < this.validConfigs.size(); i++) {
                    boolean[] cfg = this.validConfigs.get(i);
                    if (isBoundary) {
                        if (cfg[targetIdx] == wantMine) filteredIndices.add(i);
                    } else {
                        int rem = this.remainingMinesList.get(i);
                        if (wantMine && rem > 0) filteredIndices.add(i);
                        else if (!wantMine && rem < this.voidCells) filteredIndices.add(i);
                    }
                }

                if (filteredIndices.isEmpty()) return null;
                return this.validConfigs.get(filteredIndices.get(Superposition.this.random.nextInt(filteredIndices.size())));
            }

            public boolean[] generateFullBoard(boolean[] boundaryConfig, int targetIdx, boolean isTargetMine) {
                boolean[] full = new boolean[this.cols * this.rows];
                int configIndex = this.validConfigs.indexOf(boundaryConfig);
                if (configIndex == -1) configIndex = 0;

                for (int i : this.boundary) {
                    full[i] = boundaryConfig[i];
                }

                int rem = this.validConfigs.isEmpty() ? 0 : this.remainingMinesList.get(configIndex);
                List<Integer> voidList = new ArrayList<>();

                for (int i = 0; i < this.cols * this.rows; i++) {
                    if (this.logicGrid[i] == -1 && !this.boundary.contains(i)) {
                        if (i == targetIdx) {
                            if (isTargetMine) {
                                full[i] = true;
                                rem--;
                            }
                        } else {
                            voidList.add(i);
                        }
                    }
                }

                Collections.shuffle(voidList, Superposition.this.random);
                for (int i = 0; i < rem && i < voidList.size(); i++) {
                    full[voidList.get(i)] = true;
                }

                return full;
            }
        }

        // endregion
    }
    // endregion
}