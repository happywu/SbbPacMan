package pacman.controllers.examples;

import pacman.controllers.Controller;
import pacman.game.Constants;
import pacman.game.Game;

import java.util.EnumMap;

import static pacman.game.Constants.*;
/**
 * Created by pwillic on 09/06/2016.
 */
public class DudGhosts extends Controller<EnumMap<GHOST, MOVE>> {

    @Override
    public EnumMap<GHOST, MOVE> getMove(Game game, long timeDue) {
        return null;
    }
}
