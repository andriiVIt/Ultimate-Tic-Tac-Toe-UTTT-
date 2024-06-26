package dk.easv.bll.bot;

import dk.easv.bll.field.IField;
import dk.easv.bll.game.GameState;
import dk.easv.bll.game.IGameState;
import dk.easv.bll.move.IMove;

import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

public class BotBot implements IBot {
    private static final String BOT_NAME = "BotBot";
    private static final int DEPTH = 3; // Determine the optimal search depth for your needs

    @Override
    public IMove doMove(IGameState state) {
        List<IMove> availableMoves = state.getField().getAvailableMoves();
        if (availableMoves.isEmpty()) {

        }

        if (countMoves(state) < 6) { // A simpler heuristic solution at an early stage
            return earlyGameStrategy(state, availableMoves);
        } else { // We use minimax for deeper analysis at a later stage
            IMove bestMove = null;
            int bestValue = Integer.MIN_VALUE;
            for (IMove move : availableMoves) {
                int moveValue = minimax(state, DEPTH, false);
                if (moveValue > bestValue) {
                    bestValue = moveValue;
                    bestMove = move;
                }
            }
            return bestMove != null ? bestMove : availableMoves.get(new Random().nextInt(availableMoves.size()));
        }
    }
    private IMove earlyGameStrategy(IGameState state, List<IMove> availableMoves) {
        // If the central cell is available, select it
        for (IMove move : availableMoves) {
            if (move.getX() == 1 && move.getY() == 1) {
                return move;
            }
        }

        // If the central cell is not available, we look for the corner cells
        List<IMove> cornerMoves = availableMoves.stream()
                .filter(m -> (m.getX() == 0 || m.getX() == 2) && (m.getY() == 0 || m.getY() == 2))
                .collect(Collectors.toList());

        // If corner cells are found, select one of them randomly
        if (!cornerMoves.isEmpty()) {
            return cornerMoves.get(new Random().nextInt(cornerMoves.size()));
        }

        // If there are no central or corner cells, select any available cell
        return availableMoves.get(new Random().nextInt(availableMoves.size()));
    }
    private int countMoves(IGameState state) {
        String[][] board = state.getField().getBoard();
        int count = 0;
        for (int i = 0; i < board.length; i++) {
            for (int j = 0; j < board[i].length; j++) {
                if (!board[i][j].equals(IField.EMPTY_FIELD)) {
                    count++;
                }
            }
        }
        return count;
    }
    private int minimax(IGameState state, int depth, boolean isMaximizing) {
        if (depth == 0 || isGameOver(state)) {
            return evaluate(state, isMaximizing);
        }

        if (isMaximizing) {
            int maxEval = Integer.MIN_VALUE;
            for (IMove move : state.getField().getAvailableMoves()) {
                IGameState newState = simulateMove(state, move, true);
                int eval = minimax(newState, depth - 1, false);
                maxEval = Math.max(maxEval, eval);
            }
            return maxEval;
        } else {
            int minEval = Integer.MAX_VALUE;
            for (IMove move : state.getField().getAvailableMoves()) {
                IGameState newState = simulateMove(state, move, false);
                int eval = minimax(newState, depth - 1, true);
                minEval = Math.min(minEval, eval);
            }
            return minEval;
        }
    }

    private int evaluate(IGameState state, boolean isMaximizing) {
        int score = 0;

        // Check for a win, draw, or loss
        if (isGameOver(state)) {
            // If the game is over, check who won
            if (hasWon(state, "BotBot")) {
                score = isMaximizing ? 1000 : -1000; // Greater value for a win, less for a loss
            } else if (hasWon(state, "Opponent")) {
                score = isMaximizing ? -1000 : 1000;
            } else {

                score = 0;
            }
        } else {
            // Heuristic evaluation of potential opportunities for victory
            score += evaluatePotentialWins(state, "") * (isMaximizing ? 1 : -1);
            score -= evaluatePotentialWins(state, "") * (isMaximizing ? 1 : -1);
        }

        return score;
    }

    private int evaluatePotentialWins(IGameState state, String player) {
        int potentialWins = 0;
        String[][] board = state.getField().getBoard();

        return potentialWins;
    }

    private boolean hasWon(IGameState state, String player) {
        String[][] board = state.getField().getBoard();
        // Check rows and columns
        for (int i = 0; i < 3; i++) {
            if ((board[i][0].equals(player) && board[i][1].equals(player) && board[i][2].equals(player)) ||
                    (board[0][i].equals(player) && board[1][i].equals(player) && board[2][i].equals(player))) {
                return true;
            }
        }
        // Check diagonals
        if ((board[0][0].equals(player) && board[1][1].equals(player) && board[2][2].equals(player)) ||
                (board[0][2].equals(player) && board[1][1].equals(player) && board[2][0].equals(player))) {
            return true;
        }
        return false;
    }
    private IGameState simulateMove(IGameState state, IMove move, boolean isMaximizingPlayer) {
        // Create a copy of the game state to simulate the move so as not to change the original state
        GameState simulatedState = new GameState(state);

        // Convert the move (apply it to a copy of the playing field)
        String[][] board = simulatedState.getField().getBoard();
        if (isMaximizingPlayer) {
            board[move.getX()][move.getY()] = "X";
        } else {
            board[move.getX()][move.getY()] = "O";
        }

        // Update the state of the copy of the playing field after the move
        simulatedState.getField().setBoard(board);

        return simulatedState;
    }



    @Override
    public String getBotName() {
        return BOT_NAME;
    }
    private boolean isGameOver(IGameState state) {
        IField field = state.getField();
        String[][] board = field.getBoard();

        // Check for gains in rows, columns, and diagonals
        for (int i = 0; i < 3; i++) {
            // Horizontal lines
            if (checkLine(board[i][0], board[i][1], board[i][2])) return true;
            // Vertical lines
            if (checkLine(board[0][i], board[1][i], board[2][i])) return true;
        }
        // Diagonals
        if (checkLine(board[0][0], board[1][1], board[2][2]) || checkLine(board[0][2], board[1][1], board[2][0])) return true;

        // Check for a tie (are there any free cells left)
        for (int i = 0; i < board.length; i++) {
            for (int j = 0; j < board[i].length; j++) {
                if (board[i][j].equals(IField.EMPTY_FIELD)) return false; // If an empty cell is found, the game continues
            }
        }

        // If all cells are filled and there is no winning, then it is a draw
        return true;
    }

    private boolean checkLine(String a, String b, String c) {
        return !a.equals(IField.EMPTY_FIELD) && a.equals(b) && b.equals(c);
    }
}