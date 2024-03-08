package dk.easv.bll.bot;

import dk.easv.bll.field.IField;
import dk.easv.bll.game.IGameState;
import dk.easv.bll.move.IMove;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class MonteCarloBot implements IBot {

    private static final String BOT_NAME = "Monte Carlo Bot";

    private Random random;

    public MonteCarloBot() {
        this.random = new Random();
    }

    @Override
    public IMove doMove(IGameState state) {
        List<IMove> availableMoves = state.getField().getAvailableMoves();

        // If there are no available moves, return null
        if (availableMoves.isEmpty()) {
            return null;
        }

        // Perform Monte Carlo simulations to select the best move
        IMove bestMove = availableMoves.get(0);
        double bestScore = Double.NEGATIVE_INFINITY;
        for (IMove move : availableMoves) {
            double score = simulateMove(state, move);
            if (score > bestScore) {
                bestScore = score;
                bestMove = move;
            }
        }

        return bestMove;
    }

    @Override
    public String getBotName() {
        return BOT_NAME;
    }

    private double simulateMove(IGameState state, IMove move) {
        // Simulate the effect of making the move and return a score
        // This could involve running many simulated games and evaluating the outcomes
        // For simplicity, let's just return a random score for now
        return random.nextDouble();
    }
}





