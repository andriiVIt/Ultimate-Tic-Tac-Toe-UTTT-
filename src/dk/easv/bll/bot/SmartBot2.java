package dk.easv.bll.bot;

import dk.easv.bll.field.IField;
import dk.easv.bll.game.IGameState;
import dk.easv.bll.move.IMove;

import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

public class SmartBot2 implements IBot {
    private static final String BOT_NAME = "SmartBot2";
    private static final int DEPTH = 3; // Визначте оптимальну глибину пошуку за вашими потребами

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
        // Перший пріоритет: якщо центральна клітинка доступна, вибираємо її
        IMove centerMove = availableMoves.stream()
                .filter(move -> move.getX() == 1 && move.getY() == 1)
                .findFirst()
                .orElse(null);

        if (centerMove != null) {
            return centerMove;
        }

        // Другий пріоритет: блокування переможного ходу супротивника або створення "вилки"
        IMove blockingOrForkMove = findBlockingOrForkMove(state, availableMoves);
        if (blockingOrForkMove != null) {
            return blockingOrForkMove;
        }

        // Третій пріоритет: вибір кутової клітинки, якщо вона доступна
        List<IMove> cornerMoves = availableMoves.stream()
                .filter(move -> (move.getX() == 0 || move.getX() == 2) && (move.getY() == 0 || move.getY() == 2))
                .toList();

        if (!cornerMoves.isEmpty()) {
            return cornerMoves.get(new Random().nextInt(cornerMoves.size()));
        }

        // Останній варіант: вибір будь-якої доступної клітинки
        return availableMoves.get(new Random().nextInt(availableMoves.size()));
    }

    private IMove findBlockingOrForkMove(IGameState state, List<IMove> availableMoves) {
        String currentPlayerId = state.getMoveNumber() % 2 == 0 ? "X" : "O";
        for (IMove move : availableMoves) {
            IGameState simulatedState = simulateMove(state, move, currentPlayerId.equals("X"));
            if (canWinNextMove(simulatedState, currentPlayerId) || canCreateFork(simulatedState, move, currentPlayerId)) {
                return move; // Цей хід створює "вилку" або блокує переможний хід суперника
            }
        }

        for (IMove move : availableMoves) {
            IGameState simulatedState = simulateMove(state, move, currentPlayerId.equals("X"));
            String opponentId = currentPlayerId.equals("X") ? "O" : "X";
            if (canWinNextMove(simulatedState, opponentId)) {
                return move; // Блокуємо переможний хід суперника
            }
        }

        return null; // Якщо немає критичних ходів, повертаємо null
    }

    private boolean canCreateFork(IGameState simulatedState, IMove move, String currentPlayerId) {
        return false;
    }

    private boolean canWinNextMove(IGameState simulatedState, String opponentId) {
        return false;
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

        // Перевірка на виграш, нічию чи програш
        if (isGameOver(state)) {
            // Якщо гра закінчилася, потрібно перевірити, хто переміг
            if (hasWon(state, "BotBot")) { // Припускаємо, що "BotBot" - це мітка вашого бота
                score = isMaximizing ? 1000 : -1000; // Більше значення для перемоги, менше для програшу
            } else if (hasWon(state, "Opponent")) { // Припускаємо, що "Opponent" - мітка супротивника
                score = isMaximizing ? -1000 : 1000;
            } else {
                // Нічия
                score = 0;
            }
        } else {
            // Евристична оцінка потенційних можливостей для перемоги
            score += evaluatePotentialWins(state, "BotBot") * (isMaximizing ? 1 : -1);
            score -= evaluatePotentialWins(state, "Opponent") * (isMaximizing ? 1 : -1);
        }

        return score;
    }

    private int evaluatePotentialWins(IGameState state, String player) {
        int potentialWins = 0;
        String[][] board = state.getField().getBoard();
        // Оцінка можливих рядків, колонок і діагоналей, де може бути перемога.
        // Це може включати перевірку кількості символів гравця в кожному рядку/колонці/діагоналі
        // і оцінку на основі цього. Наприклад, рядок з двома символами гравця і одним пустим полем
        // має вищу оцінку, ніж рядок з одним символом гравця і двома пустими полями.

        // Приклад реалізації такої логіки може бути дуже специфічним для структури ігрового поля
        // і правил гри, тому наведено лише загальний підхід.

        return potentialWins;
    }

    private boolean hasWon(IGameState state, String player) {
        String[][] board = state.getField().getBoard();
        // Перевірка рядків і колонок
        for (int i = 0; i < 3; i++) {
            if ((board[i][0].equals(player) && board[i][1].equals(player) && board[i][2].equals(player)) ||
                    (board[0][i].equals(player) && board[1][i].equals(player) && board[2][i].equals(player))) {
                return true;
            }
        }
        // Перевірка діагоналей
        if ((board[0][0].equals(player) && board[1][1].equals(player) && board[2][2].equals(player)) ||
                (board[0][2].equals(player) && board[1][1].equals(player) && board[2][0].equals(player))) {
            return true;
        }
        return false;
    }
    private IGameState simulateMove(IGameState state, IMove move, boolean b) {
        return state;
    }



    @Override
    public String getBotName() {
        return BOT_NAME;
    }
    private boolean isGameOver(IGameState state) {
        IField field = state.getField();
        String[][] board = field.getBoard();

        // Перевірка на виграш у рядах, колонках і діагоналях
        for (int i = 0; i < 3; i++) {
            // Горизонтальні лінії
            if (checkLine(board[i][0], board[i][1], board[i][2])) return true;
            // Вертикальні лінії
            if (checkLine(board[0][i], board[1][i], board[2][i])) return true;
        }
        // Діагоналі
        if (checkLine(board[0][0], board[1][1], board[2][2]) || checkLine(board[0][2], board[1][1], board[2][0])) return true;

        // Перевірка на нічию (чи залишились вільні клітинки)
        for (int i = 0; i < board.length; i++) {
            for (int j = 0; j < board[i].length; j++) {
                if (board[i][j].equals(IField.EMPTY_FIELD)) return false; // Якщо знайдено пусту клітинку, гра триває
            }
        }

        // Якщо всі клітинки заповнені і немає виграшу, то нічия
        return true;
    }

    private boolean checkLine(String a, String b, String c) {
        return !a.equals(IField.EMPTY_FIELD) && a.equals(b) && b.equals(c);
    }
}