package dk.easv.bll.bot;
import dk.easv.bll.bot.IBot;
import dk.easv.bll.field.IField;
import dk.easv.bll.game.IGameState;
import dk.easv.bll.move.IMove;
import dk.easv.bll.move.Move;

public class BotBot implements IBot{
    private String BOT_NAME = getClass().getSimpleName();
    final int moveTimeMs = 1000;
    @Override
    public IMove doMove(IGameState state) {
        return null;
    }

    @Override
    public String getBotName() {
        return BOT_NAME;
    }
}
