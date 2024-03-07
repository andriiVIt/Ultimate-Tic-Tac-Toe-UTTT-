package dk.easv.bll.bot;

import dk.easv.bll.field.IField;
import dk.easv.bll.game.GameState;
import dk.easv.bll.game.IGameState;
import dk.easv.bll.move.IMove;

import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

public class SmartBot implements IBot {
    private static final String BOT_NAME = "SmartBot";
    private static final int DEPTH = 3; // Визначте оптимальну глибину пошуку за вашими потребами
    private String botSymbol = "X";
    private String opponentSymbol = "O";

    @Override
    public IMove doMove(IGameState state) {
        List<IMove> availableMoves = state.getField().getAvailableMoves();
        if (availableMoves.isEmpty()) {
            throw new IllegalStateException("No available moves left to play.");
        }

        if (countMoves(state) < 6) { // Простіше евристичне рішення на ранньому етапі
            return earlyGameStrategy(state, availableMoves);
        } else { // Використовуємо мінімакс для глибшого аналізу на пізнішому етапі
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
        // Центральна та кутові клітинки
        for (IMove move : availableMoves) {
            if (move.getX() == 1 && move.getY() == 1) {
                return move;
            }
        }

        List<IMove> cornerMoves = availableMoves.stream()
                .filter(m -> (m.getX() == 0 || m.getX() == 2) && (m.getY() == 0 || m.getY() == 2))
                .collect(Collectors.toList());

        if (!cornerMoves.isEmpty()) {
            return cornerMoves.get(new Random().nextInt(cornerMoves.size()));
        }

        return availableMoves.get(new Random().nextInt(availableMoves.size()));
    }

    private int countMoves(IGameState state) {
        String[][] board = state.getField().getBoard();
        return (int) Arrays.stream(board).flatMap(Arrays::stream).filter(cell -> !cell.equals(IField.EMPTY_FIELD)).count();
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
        String[][] board = state.getField().getBoard();

        // Перевірка перемоги для бота
        if (checkWin(board, botSymbol)) {
            return 10;
        }
        // Перевірка перемоги для супротивника
        if (checkWin(board, opponentSymbol)) {
            return -10;
        }
        // Якщо немає переможців, повертаємо 0
        return 0;
    }

    private boolean checkWin(String[][] board, String player) {
        // Перевірка всіх рядків, колонок і діагоналей
        for (int i = 0; i < 3; i++) {
            if (checkLine(board[i][0], board[i][1], board[i][2], player) ||
                    checkLine(board[0][i], board[1][i], board[2][i], player)) {
                return true;
            }
        }
        if (checkLine(board[0][0], board[1][1], board[2][2], player) ||
                checkLine(board[0][2], board[1][1], board[2][0], player)) {
            return true;
        }
        return false;
    }

    private boolean checkLine(String a, String b, String c, String player) {
        return a.equals(player) && b.equals(player) && c.equals(player);
    }

    // Об'єднані методи evaluate, isWin і evaluatePotentialWins не включені для скорочення

    private IGameState simulateMove(IGameState state, IMove move, boolean isMaximizingPlayer) {
        GameState simulatedState = new GameState(state);
        String[][] board = simulatedState.getField().getBoard();
        board[move.getX()][move.getY()] = isMaximizingPlayer ? botSymbol : opponentSymbol;
        simulatedState.getField().setBoard(board);
        return simulatedState;
    }

    @Override
    public String getBotName() {
        return BOT_NAME;
    }

    private boolean isGameOver(IGameState state) {
        String[][] board = state.getField().getBoard();

        // Лінії для перевірки виграшу
        for (int i = 0; i < 3; i++) {
            if (checkLine(board[i][0], board[i][1], board[i][2])) return true;
            if (checkLine(board[0][i], board[1][i], board[2][i])) return true;
        }
        if (checkLine(board[0][0], board[1][1], board[2][2]) || checkLine(board[0][2], board[1][1], board[2][0])) return true;

        // Перевірка на нічию
        return Arrays.stream(board).flatMap(Arrays::stream).noneMatch(cell -> cell.equals(IField.EMPTY_FIELD));
    }

    private boolean checkLine(String a, String b, String c) {
        return !a.equals(IField.EMPTY_FIELD) && a.equals(b) && b.equals(c);
    }

    // Сеттери для символів бота та опонента
    public void setBotSymbol(String botSymbol) {
        this.botSymbol = botSymbol;
    }

    public void setOpponentSymbol(String opponentSymbol) {
        this.opponentSymbol = opponentSymbol;
    }
}
