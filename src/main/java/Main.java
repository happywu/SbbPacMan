import entrants.pacman.dalhousie.MyPacMan;
import pacman.Executor;
import pacman.controllers.examples.po.POCommGhosts;
import pacman.controllers.examples.po.POPacMan;
import pacman.game.util.Stats;

/**
 * Created by pwillic on 06/05/2016.
 */
public class Main {

    public static void main(String[] args) {

        Executor executor = new Executor(true, true);

       // Stats[] stats = executor.runExperiment(new MyPacMan(), new POCommGhosts(50),10,"score");
        executor.runGameTimedSpeedOptimised(new MyPacMan(), new POCommGhosts(50),false,true,"F");
        //System.out.println(stats[0].getAverage());

    }
}
