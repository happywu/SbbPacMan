package entrants.pacman.dalhousie;

import pacman.controllers.PacmanController;
import pacman.game.Game;
import pacman.game.Constants.*;

import java.io.*;
import java.net.InetAddress;
import java.util.*;

/**
 * Created by happywu on 22/08/16.
 */
public class MyPacMan extends PacmanController {
    private static int NUM_DIR = 4;
    private static int CppAction;
    private int NUM_REWARD = 3;
    private static int NUM_TEST_GAMES = 1;

    private static double P_ADD_PROFILE_POINT = 0.0005;
    private static int MAX_BEHAVIOUR_STEPS = 5;
    private static int NUM_SENSOR_INPUTS = 94;
    private static int SBB_DIM = 28;
    private static int ATOMIC_ACCEPT = -1;
    private static double DOUBLE_DYNAMIC_RANGE = 50.0;
    private static double BOOL_DYNAMIC_RANGE = 10.0;
    private static int POINT_DIM = 8;
    private static Vector<Double> currentState = new Vector<Double>();
    private static Vector<Integer> neighbours = new Vector<Integer>();
    private static Vector<Double> rewards = new Vector<Double>();
    private static double xCoord;
    private static double yCoord;
    private static Vector<Boolean> BOOL_INPUTS = new Vector<Boolean>();
    private static boolean[] stateVarIsBool = new boolean[NUM_SENSOR_INPUTS];
    private static final int MIN_DISTANCE = 20;
    private static boolean firstflag = true;
    private static Sensors sensor = new Sensors();

    private static sbbHP sbbMain = new sbbHP();
    static PrintWriter writer;
    static int game_server_port;
    static int delay;

    static boolean moveReady;
    static boolean gameReady;
    static boolean newState;
    static boolean episodeEnd;

    static int maxLevel;

    int prevGhostScore;
    int numEatenGhosts;


    // Command-line parameters
    static boolean checkpoint = false;
    static int checkpointInMode = -1;
    static int hostFitnessMode = 1;
    static int hostToReplay = -1;
    static int phase = sbbMist._TRAIN_PHASE;
    static int port = -1;
    static int seed = -1;
    static boolean replay = false;
    static int statMod = -1;
    static long tMain = 1;
    static int tPickup = -1;
    static int tStart = 0;
    static boolean useMemory = false;
    static boolean usePoints = false;
    static int currentAction;
    private static boolean visual = false;

    public void seed(int _seed) {
        game_server_port = _seed;
    }

    public int seed() {
        return game_server_port;
    }

    public boolean visual() {
        return visual;
    }

    public int delay() {
        return delay;
    }

    public boolean gameReady() {
        return gameReady;
    }

    public void gameReady(boolean b) {
        gameReady = b;
    }

    public int maxLevel() {
        return maxLevel;
    }

    public int port() {
        return game_server_port;
    }

    private static void mspacmanDiscretizeState(Vector<Double> state, int steps) {
        for (int i = 0; i < state.size(); i++) {
            if (!stateVarIsBool[i])
                state.set(i, sbbMist.discretize(state.get(i), 0, DOUBLE_DYNAMIC_RANGE, steps));
        }
    }

    // dir must be one of 4 directions: 0-3. There are 28 inputs in total, 6 non-directed and 22 directed
    public static void getDirectedState(Vector<Double> currentState, Vector<Double> directionalState, int dir) {
        directionalState.clear();
        for (int i = 0; i < 6; i++)//6 non-directed inputs: 0-5
            directionalState.add(currentState.get(i));
        for (int i = 0; i < 22; i++)//22 directed inputs; dir=0(6-27); dir=1(28-49); dir=2(50-71); dir=3(72-93)
            directionalState.add(currentState.get((6 + (dir * 22)) + i));
    }

    // Checks if the nextAction will lead pacman into a wall
    public static boolean isTowardWall(Vector<Integer> n, int nextAction) {
        if (n.get(nextAction) >= 0)
            return false;
        else
            return true;
    }

    public static void init(Vector<Double> state, Vector<Integer> neighbours, int dim) {
        for (int i = 0; i < dim; i++) {
            if (BOOL_INPUTS.contains(i))
                stateVarIsBool[i] = true;
            else
                stateVarIsBool[i] = false;
        }
    }

    public static int runEval(Game game, sbbHP sbbEval, int port, int t, int phase, boolean visual, int[] timeGenTotalInGame, int hostToReplay, long[] eval) {

        Random random = new Random();
        long timeStartGame;
        Vector<Double> behaviourSequence = new Vector<Double>(); // store a discretized trajectory for diversity maintenance
        Vector<Double> tmpBehaviourSequence = new Vector<Double>();
        Vector<Double> directedState = new Vector<Double>();
        Vector<Double> selectedDirectedState = new Vector<Double>();
        int step;
        int prevAction;
        int atomicAction;
        long[] decisionInstructions = new long[1];
        long decisionInstructionsSum;
        int prevProfileId = -1;
        int newProfilePoints = 0;
        TreeMap<Integer, Integer> directedActions = new TreeMap<Integer, Integer>(Collections.reverseOrder());
        TreeMap<Double, Integer> acceptedDirectionPreferences = new TreeMap<Double, Integer>(Collections.reverseOrder());
        TreeMap<Double, Integer> rejectedDirectionPreferences = new TreeMap<Double, Integer>(Collections.reverseOrder());
        TreeSet<sbbLearner> learnersRanked = new TreeSet<sbbLearner>(new sbbLearner.LearnerBidLexicalCompare());

        Vector<sbbTeam> teams = new Vector<sbbTeam>();
        sbbEval.getTeams(teams);
        Vector<sbbPoint> points = new Vector<sbbPoint>();
        sbbEval.getPoints(points);

        int diffcnt = 0;

        //System.out.println(teams.size());
        int prevF = 0;
        for (int i = 0; i < teams.size(); i++) {
            int numEval = visual ? NUM_TEST_GAMES : (sbbEval.usePoints() ? points.size() : (hostToReplay >= 0 ? NUM_TEST_GAMES : sbbEval.episodesPerGeneration()));
            if (hostToReplay < 0 || (hostToReplay >= 0 && teams.get(i).id() == hostToReplay)) {
                for (int p = 0; p < numEval; p++) {
                    decisionInstructionsSum = 0;
                    if ((sbbEval.usePoints() && !teams.get(i).hasOutcome(points.get(p))) || (!sbbEval.usePoints() && teams.get(i).numOutcomes(phase) < sbbEval.numStoredOutcomesPerHost(phase))) {
                        if (sbbEval.usePoints()) {
                            if (false) {
                            } else {
                                Vector<Double> pState = new Vector<Double>();
                                points.get(p).pointState(pState);
                            }
                        }

                        prevAction = 0;
                        step = 0;
                        timeStartGame = System.currentTimeMillis();
                        int ccnt = 0;
                        while (!episodeEnd) {
                            acceptedDirectionPreferences.clear();
                            rejectedDirectionPreferences.clear();
                            double[] BB = new double[4];
                            for (int d = 0; d < NUM_DIR; d++) { //0:UP 1:RIGHT 2:DOWN 3:LEFT
                                if (!isTowardWall(neighbours, d)) {
                                    getDirectedState(currentState, directedState, d);
                                    learnersRanked.clear();
                                    decisionInstructions[0] = 0;
                                    atomicAction = sbbEval.getAction(teams.get(i), directedState, (phase == sbbMist._TRAIN_PHASE ? true : false), learnersRanked, decisionInstructions);
                                    if (atomicAction == ATOMIC_ACCEPT) {
                                        if (!acceptedDirectionPreferences.containsKey(learnersRanked.first().bidVal()))
                                            acceptedDirectionPreferences.put(learnersRanked.first().bidVal(), d);
                                    } else {
                                        if (!rejectedDirectionPreferences.containsKey(learnersRanked.first().bidVal()))
                                            rejectedDirectionPreferences.put(learnersRanked.first().bidVal(), d);
                                    }
                                    //   System.out.println( "atomic Action: " + atomicAction + "dir: "+ d + " learnersRanked: "+ (learnersRanked.first()).bidVal());
                                    if (random.nextDouble() < P_ADD_PROFILE_POINT && teams.get(i).id() != prevProfileId) {
                                        sbbEval.addProfilePoint(directedState, rewards, phase, t);
                                        prevProfileId = (int) teams.get(i).id();
                                        newProfilePoints++;
                                    }
                                    BB[d] = learnersRanked.first().bidVal();
                                }
                            }
                            //System.out.println(acceptedDirectionPreferences.size() + " : " + rejectedDirectionPreferences.size());
                            if (acceptedDirectionPreferences.size() > 0)
                                currentAction = acceptedDirectionPreferences.firstEntry().getValue();
                            else
                            if(rejectedDirectionPreferences.size()>0)
                                currentAction = rejectedDirectionPreferences.lastEntry().getValue();


                            behaviourSequence.add((double) ((currentAction * -1) - 1)); //actions are represented as negatives
                            getDirectedState(currentState, selectedDirectedState, currentAction);//sensor readings for the chosen direction
                            mspacmanDiscretizeState(selectedDirectedState, sbbEval.stateDiscretizationSteps());//only need the last 5!

                            for (int ii = 0; ii < selectedDirectedState.size(); ii++)
                                behaviourSequence.add(selectedDirectedState.get(ii));

                            step++;
                            decisionInstructionsSum += decisionInstructions[0];
                            prevAction = currentAction;
                            return currentAction;
                        }
                        timeGenTotalInGame[0] += (System.currentTimeMillis() - timeStartGame);
                        episodeEnd = false;
                        //get behaviour sequence for last 5 interactions
                        int start = Math.min((1 + sbbEval.dimBehavioural()) * MAX_BEHAVIOUR_STEPS, (int) behaviourSequence.size());
                        for (int b = behaviourSequence.size() - start; b < behaviourSequence.size(); b++)
                            tmpBehaviourSequence.add(behaviourSequence.get(b));

                        if (sbbEval.usePoints())
                            sbbEval.setOutcome(teams.get(i), points.get(p), tmpBehaviourSequence, rewards, phase, t);
                        else sbbEval.setOutcome(teams.get(i), tmpBehaviourSequence, rewards, phase, t);
                        eval[0]++;
                        behaviourSequence.clear();
                        tmpBehaviourSequence.clear();
                        System.out.println(" gameScore " + rewards.get(0) + " pillScore " + rewards.get(1) + " ghostScore " + rewards.get(2) + " steps " + step + " meanDecisionInst " + decisionInstructionsSum / step);
                    }
                }
            }
        }
        System.out.println(" diffnum: " + diffcnt);
        System.out.print( "mspacmanSBBHAgent::runEval t " + t + " numProfilePoints " + sbbEval.numProfilePoints());
        System.out.println(" newProfilePoints " + newProfilePoints);
        return 0;
    }

    private static void OutPutState() {
        try {

            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream("A.txt",true)));
            for (int i = 0; i < 3; i++)
                writer.write(rewards.get(i) + ",");
            writer.write(xCoord + ",");
            writer.write(yCoord + ",");

            for (int i = 0; i < 4; i++)
                writer.write(neighbours.get(i) + ",");

            for (int j = 0; j < NUM_SENSOR_INPUTS; j++)
                writer.write(currentState.get(j) + ",");

            writer.write(currentAction + "\n");
            writer.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void GetState(String line) {
        String[] Out;
        Out = line.split(",");
        int cnt = 0;
        for(int i=0;i<3;i++)
            rewards.set(i,Double.parseDouble(Out[cnt++]));
        xCoord = Double.parseDouble(Out[cnt++]);
        yCoord = Double.parseDouble(Out[cnt++]);
        for(int i=0;i<4;i++)
            neighbours.set(i,Integer.parseInt(Out[cnt++]));
        for(int i = 0;i<NUM_SENSOR_INPUTS;i++)
            currentState.set(i,Double.parseDouble(Out[cnt++]));
        // CppAction = Integer.parseInt(Out[cnt++]);
    }

    public static void init() {
        rewards.clear();
        neighbours.clear();
        currentState.clear();
        for (int i = 0; i < 3; i++) rewards.add(0.0);
        for (int i = 0; i < 4; i++) neighbours.add(-1);
        for (int i = 0; i < NUM_SENSOR_INPUTS; i++)
            currentState.add(0.0);
        checkpoint = true;
        checkpointInMode = 0;
        hostFitnessMode = 0;
        statMod = 5;
        useMemory = false;
        replay = true;
        int Run = 3;
        tMain = 1000;
        switch (Run){
            case 1:
                //3915
                hostToReplay = 8980520;
                tStart = 709;
                seed = 8900;
                break;
            case 2:
                //
                hostToReplay = 8546085;
                tStart = 892;
                seed = 8400;
                break;
            case 3:
                // 4800
                hostToReplay = 9019146;
                tStart = 736;
                seed = 8900;
                break;
            case 4:
                //full obseravtion 9334
                hostToReplay = 7175533;
                tStart = 516;
                seed = 7100;
                useMemory = true;
            default:break;
        }
        tPickup = tStart;
        visual = true;
    }

    public MOVE IntegerToMove(int move){
        switch (move){
            case 0: return MOVE.UP;
            case 1: return MOVE.RIGHT;
            case 2: return MOVE.DOWN;
            case 3: return MOVE.LEFT;
        }
        return MOVE.NEUTRAL;
    }
//    public static void main(String args[]) throws FileNotFoundException {

    @Override
    public MOVE getMove(Game game, long timeDue) {

        if(game.getCurrentLevelTime()==0)init();
        sensor = new Sensors();
        double[] sense = sensor.read(game);

        rewards.set(0,(double)game.getScore());
        rewards.set(1,(double)game.getNumberOfPills() - game.getNumberOfActivePills());
        rewards.set(2,(double)game.getNumGhostsEaten());
        xCoord = game.getNodeXCood(game.getPacmanCurrentNodeIndex());
        yCoord = game.getNodeYCood(game.getPacmanCurrentNodeIndex());

        neighbours.set(0,game.getNeighbour(game.getPacmanCurrentNodeIndex(),MOVE.UP));
        neighbours.set(1,game.getNeighbour(game.getPacmanCurrentNodeIndex(),MOVE.RIGHT));
        neighbours.set(2,game.getNeighbour(game.getPacmanCurrentNodeIndex(),MOVE.DOWN));
        neighbours.set(3,game.getNeighbour(game.getPacmanCurrentNodeIndex(),MOVE.LEFT));

        for(int i=0;i<NUM_SENSOR_INPUTS;i++)
            currentState.set(i,sense[i]);

        int[] timeTotalInGame = new int[1];
        long[] totalEval = new long[1];
        totalEval[0] = 0;

        if(currentState.get(9)<=0.10&&currentState.get(12)==0){
            neighbours.set(0,-1);
        }
        if(currentState.get(31)<=0.10&&currentState.get(34)==0){
            neighbours.set(1,-1);
        }
        if(currentState.get(53)<=0.10&&currentState.get(56)==0){
            neighbours.set(2,-1);
        }
        if(currentState.get(76)<=0.10&&currentState.get(79)==0){
            neighbours.set(3,-1);
        }
        int current = game.getPacmanCurrentNodeIndex();

        /// Strategy 2: Find nearest edible ghost and go after them
        int minDistance = Integer.MAX_VALUE;
        GHOST minGhost = null;
        for (GHOST ghost : GHOST.values()) {
            // If it is > 0 then it is visible so no more PO checks
            if (game.getGhostEdibleTime(ghost) > 0) {
                int distance = game.getShortestPathDistance(current, game.getGhostCurrentNodeIndex(ghost));

                if (distance < minDistance) {
                    minDistance = distance;
                    minGhost = ghost;
                }
            }
        }

        if (minGhost != null) {
            return game.getNextMoveTowardsTarget(current, game.getGhostCurrentNodeIndex(minGhost), DM.PATH);
        }
        /*
        System.out.println(currentState.get(9) + " "  + currentState.get(12) + " , "+
                currentState.get(28) + " "  + currentState.get(31) + " , "
                + currentState.get(50) + " "  + currentState.get(53) + " , "
                + currentState.get(73) + " "  + currentState.get(76));*/
        if(game.getCurrentLevelTime()!=0) return IntegerToMove(runEval(game,sbbMain,port,tPickup,sbbMist._TRAIN_PHASE,visual,timeTotalInGame,hostToReplay,totalEval));

        totalEval[0] = 0;
        sbbMain = new sbbHP();
        firstflag = false;

        //SBB Parameter Setup
        sbbMain.id(-1);
        sbbMain.useMemory(useMemory);
        sbbMain.usePoints(usePoints);
        sbbMain.seed(seed);
        sbbMain.dimPoint(POINT_DIM);
        sbbMain.dimBehavioural(SBB_DIM);
        sbbMain.hostFitnessMode(hostFitnessMode);
       //   sbbMain.setParams();
        sbbMain.numAtomicActions(2); //Binary action, yes or no for direction
        sbbMain.numStoredOutcomesPerHost(sbbMist._TRAIN_PHASE, 10);
        init(currentState, neighbours, NUM_SENSOR_INPUTS);

        //loading populations from a chackpoint file
        String dir = "checkpoints";
        String prefix = "cp";
        String filepath = dir + "/" + prefix + "." + tPickup + "." + sbbMain.id()+ "." + sbbMain.seed() + "." + checkpointInMode +".rslt";
        sbbMain.readCheckpoint(checkpointInMode,filepath);
        if (sbbMain.usePoints()) sbbMain.initPoints();
        return IntegerToMove(runEval(game,sbbMain,port,tPickup,sbbMist._TRAIN_PHASE,visual,timeTotalInGame,hostToReplay,totalEval));
    }
}
