package entrants.pacman.dalhousie;//package pacman.controllers.osc;

import pacman.game.Constants;
import pacman.game.Constants.MOVE;
import pacman.game.Game;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.*;

//import edu.utexas.cs.nn.tasks.mspacman.facades.GameFacade;
//import edu.utexas.cs.nn.tasks.mspacman.sensors.directional.reachfirst.VariableDirectionCloserToTargetThanThreatGhostBlock;
//import edu.utexas.cs.nn.util.datastructures.ArrayUtil;
//import edu.utexas.cs.nn.util.datastructures.Pair;

/**
 * Created by happywu on 16-8-2.
 */
public class Sensors {
	private final int MaxDis = 200;
	private final int MaxPillsin30Steps_divisor = 8;
	private final int MaxJunctionsin30Steps_divisor = 5;
    private static Set<Integer> IsPills  = new HashSet<Integer>();
	private static Set<Integer> IsPowerPills  = new HashSet<Integer>();
	private static Set<Integer> IsPacmanVisited = new HashSet<Integer>();
	public Sensors(){}
	public int GetNearestPillDistance(Game game, MOVE move){
		int current=game.getPacmanCurrentNodeIndex();
		if(game.getNeighbour(current,move)==-1)return MaxDis;

		//get all active pills
		int[] activePills=game.getActivePillsIndices();

		int minDistance = MaxDis;

		for(int i = 0; i < activePills.length; ++i) {
			int distance = MaxDis;
			distance = game.getShortestPathDistance(game.getNeighbour(current,move), activePills[i],move);
			if(distance < minDistance) {
				minDistance = distance;
			}
		}
		//System.out.println(minDistance + " " +game.getShortestPathDistance(current,97));


        /* record the pills postion */
		int[] Pills = game.getPillIndices();
		for(int i=0;i<Pills.length;i++)
			IsPills.add(Pills[i]);
        Iterator iter = IsPills.iterator();
		while(iter.hasNext()){
			int pillpos = (int) iter.next();
            if(!IsPacmanVisited.contains(pillpos)) {
				int distance = MaxDis;
				distance = game.getShortestPathDistance(game.getNeighbour(current,move), pillpos ,move);
				if(distance < minDistance) {
					minDistance = distance;
				}
			}
		}
	//	System.out.println("Pill " + minDistance);
		return minDistance;
	}
	public int GetNearestPowerPillDistance(Game game, MOVE move){
		int current=game.getPacmanCurrentNodeIndex();
		if(game.getNeighbour(current,move)==-1)return MaxDis;

		//get all active pills
		int[] activePowerPills=game.getActivePowerPillsIndices();

		int minDistance = MaxDis;

		for(int i = 0; i < activePowerPills.length; ++i) {
			int distance = MaxDis;
			distance = game.getShortestPathDistance(game.getNeighbour(current,move), activePowerPills[i],move);
			if(distance < minDistance) {
				minDistance = distance;
			}
		}

        /* record the pills postion */
		int[] PowerPills = game.getPowerPillIndices();
		for(int i=0;i<PowerPills.length;i++)
			IsPowerPills.add(PowerPills[i]);
		Iterator iter = IsPowerPills.iterator();
		while(iter.hasNext()){
			int powerpillpos = (int) iter.next();
			if(!IsPacmanVisited.contains(powerpillpos)) {
				int distance = MaxDis;
				distance = game.getShortestPathDistance(game.getNeighbour(current,move), powerpillpos ,move);
				if(distance < minDistance) {
					minDistance = distance;
				}
			}
		}
		return minDistance;
	}
	public int GetNearestJunctionDistance(Game game,MOVE move){
		int current=game.getPacmanCurrentNodeIndex();

		if(game.getNeighbour(current,move)==-1)return MaxDis;
		int[] Junctions = game.getJunctionIndices();

		int minDistance = MaxDis;

		for(int i = 0; i < Junctions.length; ++i) {
			int distance = MaxDis;
			distance = game.getShortestPathDistance(game.getNeighbour(current,move), Junctions[i],move);
			if(distance < minDistance) {
				minDistance = distance;
			}
		}
		return minDistance;
	}
	public int GetNearestJunctionDistanceIndex(Game game,MOVE move){
		int current=game.getPacmanCurrentNodeIndex();

		if(game.getNeighbour(current,move)==-1)return -1;
		int[] Junctions = game.getJunctionIndices();

		int minDistance = MaxDis;
		int k = -1;

		for(int i = 0; i < Junctions.length; ++i) {
			int distance = MaxDis;
			distance = game.getShortestPathDistance(current, Junctions[i],move);
			if(distance < minDistance) {
				minDistance = distance;
				k = Junctions[i];
			}
		}
		return k;
	}

	public int GetMaxPillsin30Steps(Game game,MOVE move){
		if(game.getNeighbour(game.getPacmanCurrentNodeIndex(),move)==-1)return 0;
		//get all active pills
		int[] activePills=game.getActivePillsIndices();
		Map isPill = new HashMap();

		for(int i = 0; i < activePills.length; ++i) isPill.put(activePills[i],1);

		Queue<BFSnode<Integer,Integer,Integer,MOVE,Integer>> queue = new LinkedList<BFSnode<Integer,Integer,Integer,MOVE,Integer>>();
		int firstpos = game.getCurrentMaze().graph[game.getPacmanCurrentNodeIndex()].neighbourhood.get(move).intValue();
		//queue.add(new BFSnode<Integer, Integer, Integer, MOVE>(current,0,0,MOVE.NEUTRAL));
		queue.add(new BFSnode<Integer, Integer, Integer, MOVE,Integer>(firstpos,1,(isPill.containsKey(firstpos)==true?1:0),move,-1));

		MOVE[] possibleMoves;
		int MaxPill = 0;
		while(!queue.isEmpty()){
			BFSnode<Integer,Integer,Integer,MOVE,Integer> now = queue.remove();
			if(now.trd > MaxPill)MaxPill = now.trd;
			if(now.snd == 30)continue;
			possibleMoves = game.getPossibleMoves(now.fst);
			for(MOVE dir : MOVE.values()){
				if(!Arrays.asList(possibleMoves).contains(dir))continue;
				if(dir.opposite() == now.fth|| dir == MOVE.NEUTRAL) continue;
				int newPos = game.getCurrentMaze().graph[now.fst].neighbourhood.get(dir).intValue();
				queue.add(new BFSnode<Integer, Integer, Integer, MOVE,Integer>(newPos,now.snd+1,
						now.trd+(isPill.containsKey(newPos)==true?1:0),dir,-1));
			}

		}
		return MaxPill;
	}
	public int GetMaxJunctionsin30Steps(Game game,MOVE move){
		if(game.getNeighbour(game.getPacmanCurrentNodeIndex(),move)==-1)return 0;
		//get all active pills
		int[] junctionIndices =game.getJunctionIndices();
		Map isJunction = new HashMap();

		for(int i = 0; i < junctionIndices.length; ++i) isJunction.put(junctionIndices[i],1);

		Queue<BFSnode<Integer,Integer,Integer,MOVE,Integer>> queue = new LinkedList<BFSnode<Integer,Integer,Integer,MOVE,Integer>>();
		int firstpos = game.getCurrentMaze().graph[game.getPacmanCurrentNodeIndex()].neighbourhood.get(move).intValue();
		//queue.add(new BFSnode<Integer, Integer, Integer, MOVE>(current,0,0,MOVE.NEUTRAL));
		queue.add(new BFSnode<Integer, Integer, Integer, MOVE,Integer>(firstpos,1,(isJunction.containsKey(firstpos)==true?1:0),move,-1));
		// queue.add(new BFSnode<Integer, Integer, Integer, MOVE>(current,0,0,MOVE.NEUTRAL));

		MOVE[] possibleMoves;

		int MaxJunctions = 0;
		while(!queue.isEmpty()){
			BFSnode<Integer,Integer,Integer,MOVE,Integer> now = queue.remove();
			if(now.trd > MaxJunctions)MaxJunctions = now.trd;
			if(now.snd == 30)continue;
			possibleMoves = game.getPossibleMoves(now.fst);
			for(MOVE dir : MOVE.values()){
				if(!Arrays.asList(possibleMoves).contains(dir))continue;
				if(dir.opposite() == now.fth|| dir == MOVE.NEUTRAL) continue;
				int newPos = game.getCurrentMaze().graph[now.fst].neighbourhood.get(dir).intValue();
				// System.out.format("%d %d\n",now.fst,newPos);
				queue.add(new BFSnode<Integer, Integer, Integer, MOVE,Integer>(newPos,now.snd+1,
						now.trd+(isJunction.containsKey(newPos)==true?1:0),dir,-1));
			}

		}
		return MaxJunctions;
	}
	
	public int GetOptionsFromNextJunction(Game game, MOVE move){
        if(game.getNeighbour(game.getPacmanCurrentNodeIndex(),move)==-1)return 0;
        int[] Junctions = game.getJunctionIndices();
        int NearestJunctionIndex = this.closestSafeTargetInDir(game, Junctions, move);
        if(NearestJunctionIndex < 0) return 0;
        int cnt = 0;
        for(int i=0;i<Junctions.length;i++){
            if(Junctions[i]==NearestJunctionIndex)continue;
            if(!game.isNodeObservable(Junctions[i]))continue;
            boolean flag = true;
            for(Constants.GHOST ghost : Constants.GHOST.values()){
                if(game.getShortestPathDistance(game.getPacmanCurrentNodeIndex(),game.getGhostCurrentNodeIndex(ghost))==-1)continue;
            if(game.getShortestPathDistance(NearestJunctionIndex,Junctions[i])
                >=game.getShortestPathDistance(game.getGhostCurrentNodeIndex(ghost),Junctions[i])&&
                    game.isNodeObservable(game.getGhostCurrentNodeIndex(ghost))
                    )
                flag = false;
            }
            if(flag)cnt++;
        }
        return cnt;
    }

	public int closestSafeTargetInDir(Game game, int[] targets, MOVE dir) {
		int currentLocation = game.getPacmanCurrentNodeIndex();
		int distToClosestTargetInDir = 2147483647;
		int closestSafeTargetInDir = -1;
		if(game.getNeighbour(currentLocation,dir) == -1) {
			return closestSafeTargetInDir;
		} else {
			for(int i = 0; i < targets.length; ++i) {
				int[] path = game.getShortestPath(game.getNeighbour(game.getPacmanCurrentNodeIndex(),dir), targets[i], dir);
				boolean flag = true;
				for(Constants.GHOST  ghost : Constants.GHOST.values()) {
					int ghostidx = game.getGhostCurrentNodeIndex(ghost);
					if (ghostidx == -1 || game.getShortestPathDistance(game.getNeighbour(game.getPacmanCurrentNodeIndex(),dir), ghostidx) == -1) continue;
					if( game.isGhostEdible(ghost) != null && game.isGhostEdible(ghost) == true)continue;
					if (this.PathContainsTargets(path, ghostidx)) {
						flag = false;
						break;
					}
				}
				if(!flag)continue;
				if(path.length < distToClosestTargetInDir && this.isPacManCloserThanAnyThreatGhost(game, currentLocation, targets[i])) {
					distToClosestTargetInDir = path.length;
					closestSafeTargetInDir = targets[i];
				}
			}


			/*
            for(Constants.GHOST ghost: Constants.GHOST.values()){
                System.out.format(" DIS: %s %d %d\n",dir,game.getShortestPathDistance(currentLocation, closestSafeTargetInDir),
                        game.getShortestPathDistance(game.getGhostCurrentNodeIndex(ghost),closestSafeTargetInDir));
            }*/
			return closestSafeTargetInDir;
		}
	}

	public boolean PathContainsTargets(int[] path, int target){
		for(int i=0;i<path.length;i++)
			if(path[i]==target)return true;
		return false;
	}
	public boolean isPacManCloserThanAnyThreatGhost(Game game, int pacmanLocation, int target) {
		for(Constants.GHOST ghost : Constants.GHOST.values()){

			if(game.getGhostLairTime(ghost) == 0 && !game.isGhostEdible(ghost) &&
					game.getShortestPathDistance(pacmanLocation, target) >
			game.getShortestPathDistance(game.getGhostCurrentNodeIndex(ghost), target
					)) return false;
		}

		return true;
	}

	public int[] GetNearestGhostDistance(Game game){
		int current = game.getPacmanCurrentNodeIndex();
		int dis[] = new int[4];
		Arrays.fill(dis,MaxDis);
		int i = 0;
		for(Constants.GHOST ghost : Constants.GHOST.values()){
			if(game.isNodeObservable(game.getGhostCurrentNodeIndex(ghost)))
				dis[i++] = game.getShortestPathDistance(current,game.getGhostCurrentNodeIndex(ghost));
		}
		Arrays.sort(dis);
		return dis;
	}
	public int GetGhostDistance(Game game, Constants.GHOST ghost){
		if(!game.isNodeObservable(game.getGhostCurrentNodeIndex(ghost)))return MaxDis;
		return game.getShortestPathDistance(game.getPacmanCurrentNodeIndex(),game.getGhostCurrentNodeIndex(ghost));
	}

	public int IsGhostApproaching(Game game, Constants.GHOST ghost){
		if(!game.isNodeObservable(game.getGhostCurrentNodeIndex(ghost)))return 0;
		int current = game.getPacmanCurrentNodeIndex();
		int ghostcurrent = game.getGhostCurrentNodeIndex(ghost);
		game.getGhostLastMoveMade(ghost);
		int ghostlastPos = game.getCurrentMaze().graph[ghostcurrent].neighbourhood.get(
				game.getGhostLastMoveMade(ghost).opposite()).intValue();

		if(game.getShortestPathDistance(current,ghostcurrent)<game.getShortestPathDistance(current,ghostlastPos))
			return 1;
		else return 0;
	}

	public int IsGhostTrapped(Game game, MOVE move, Constants.GHOST ghost){
		if (!game.isNodeObservable(game.getGhostCurrentNodeIndex(ghost))) return 0;
		if (game.getGhostLairTime(ghost) > 0) return 0;
		if(game.getNeighbour(game.getPacmanCurrentNodeIndex(),move)==-1)return 0;
		int[] paths = game.getShortestPath(game.getNeighbour(game.getPacmanCurrentNodeIndex(),move),game.getGhostCurrentNodeIndex(ghost),move);
		for(int i=0;i<paths.length;i++)
			if(game.isJunction(paths[i]))
				return 0;
		return 1;
	}

	public int IsGhostEdible(Game game, Constants.MOVE move, Constants.GHOST ghost){
		if(!game.isNodeObservable(game.getGhostCurrentNodeIndex(ghost)))return 0;
		if (game.getGhostLairTime(ghost) > 0) return 0; 
		int current = game.getPacmanCurrentNodeIndex();
		if(game.getNeighbour(current,move)==-1)return 0;
		return game.isGhostEdible(ghost) ? 1: 0;
	}

	public int GetProportionPills(Game game){
		return game.getNumberOfActivePills();
	}

	public int GetProportionPowerPills(Game game){
		return game.getNumberOfActivePowerPills();
	}

	public int GetProportionEdibleGhosts(Game game){
		int cnt = 0;
		for(Constants.GHOST ghost : Constants.GHOST.values()){
			if(game.isNodeObservable(game.getGhostCurrentNodeIndex(ghost)))
				if(game.isGhostEdible(ghost))cnt++;
		}
		return cnt;
	}

	public int GetProportionEdibleTime(Game game){
		int time = 0;
		for(Constants.GHOST ghost : Constants.GHOST.values()){
			if(game.isNodeObservable(game.getGhostCurrentNodeIndex(ghost))&&game.isGhostEdible(ghost)&&game.getGhostEdibleTime(ghost)>time)
				time = game.getGhostEdibleTime(ghost);
		}
		return time;
	}

	public int IsAnyGhostsEdible(Game game){
		for(Constants.GHOST ghost : Constants.GHOST.values()){
			if(game.isNodeObservable(game.getGhostCurrentNodeIndex(ghost)))
				if(game.isGhostEdible(ghost))return 1;
		}
		return 0;
	}

	public int IsAllThreatGhostsPresent(Game game){
		for(Constants.GHOST ghost : Constants.GHOST.values()){
			if(game.isNodeObservable(game.getGhostCurrentNodeIndex(ghost)))
				if(game.getGhostLairTime(ghost)>0)return 0;
		}

		return 1;
	}

	public int IsClosetoPowerPill(Game game){
		int current = game.getPacmanCurrentNodeIndex();

		int[] activePowerPills=game.getActivePowerPillsIndices();

		for(int i=0;i<activePowerPills.length;i++)
			if(game.getShortestPathDistance(current,activePowerPills[i])<10)
				return 1;
		return 0;
	}

	public int ghostApproachingPacman(Game game, Constants.GHOST ghost) {
		if(!game.isNodeObservable(game.getGhostCurrentNodeIndex(ghost)))return 0;
		int current = game.getPacmanCurrentNodeIndex();
		if(game.getShortestPathDistance(current,game.getGhostCurrentNodeIndex(ghost))==-1)return 0;
		int[] ghostPath = game.getShortestPath(game.getGhostCurrentNodeIndex(ghost), current, game.getGhostLastMoveMade(ghost));

		//  System.out.format("Ghost: %d\n",game.getGhostCurrentNodeIndex(ghost));
		int[] shortestPath = game.getShortestPath(game.getGhostCurrentNodeIndex(ghost),current);
		// Paths could be different if two equal-length paths exist
		return (ghostPath.length == shortestPath.length) == true ? 1 : 0;
	}
	public double[] GetConflictDirected(Game game, MOVE move){
		double[] ans = new double[16];
		// use BFSnode as Four-element node

		BFSnode<Integer,Integer,Integer,Integer,Constants.GHOST>[] ghosts = new BFSnode[5];
		// List<Pair<Integer, Integer>> A  = new ArrayList<Pair<Integer, Integer>>();
		List<Map.Entry<Integer, Integer>> A  = new ArrayList<>();
		int current = game.getPacmanCurrentNodeIndex();
		int i = 0;
		for(Constants.GHOST ghost : Constants.GHOST.values()){

			if(!game.isNodeObservable(game.getGhostCurrentNodeIndex(ghost))) {
			//if(game.getShortestPathDistance(game.getPacmanCurrentNodeIndex(),game.getGhostCurrentNodeIndex(ghost))==-1){
				ghosts[i] = new BFSnode<Integer, Integer, Integer, Integer,Constants.GHOST>((Integer)MaxDis,0,0,0,ghost);
			}else{
				ghosts[i] = new BFSnode<Integer, Integer, Integer, Integer,Constants.GHOST>(
						game.getNeighbour(current, move)==-1 || game.getGhostCurrentNodeIndex(ghost) == -1 || game.getGhostLairTime(ghost) > 0 ? MaxDis:
							game.getShortestPathDistance(game.getNeighbour(game.getPacmanCurrentNodeIndex(),move),
									game.getGhostCurrentNodeIndex(ghost),move),
									isGhostIncoming(game, move, ghost),//ghostApproachingPacman(game,ghost),
									IsGhostTrapped(game,move,ghost),
									IsGhostEdible(game,move,ghost),
									ghost);
			}
			// A.add(new Pair<Integer, Integer>(ghosts[i].fst,i));
			A.add(new AbstractMap.SimpleEntry<Integer, Integer>(ghosts[i].fst,i));
			i++;
		}

		/*
        Collections.sort(A, new Comparator<Pair<Integer, Integer>>() {
            @Override
            public int compare(Pair<Integer, Integer> o1, Pair<Integer, Integer> o2) {
                if(o1.getKey()<o2.getKey())return 1;
                return 0;
            }
        });*/

		Collections.sort(A, new Comparator<Map.Entry<Integer, Integer>>() {
			@Override
			public int compare(Map.Entry<Integer, Integer> o1, Map.Entry<Integer, Integer> o2) {
				return o1.getKey()-o2.getKey();
			}
		});
		int num = 0;

		for(int j=0;j<A.size();j++){
			int k = A.get(j).getValue();
			ans[num++] = (double)ghosts[k].fst/MaxDis;
			ans[num++] = (double)ghosts[k].snd;
			ans[num++] = (double)ghosts[k].trd;
			ans[num++] = (double)ghosts[k].fth;
//			            if (game.getNeighbour(game.getPacmanCurrentNodeIndex(),move) != -1 && // is valid
//			            		              //j==0 && //closest
//			            		              //move == MOVE.UP && //UP only
//			            		              game.isNodeObservable(game.getGhostCurrentNodeIndex(ghosts[k].sth)) &&
//			            		              game.getGhostLairTime(ghosts[k].sth) == 0 //not in lair
//			            		              ){
//			            GameView.addPoints(game, 
//			            		(IsGhostEdible(game,move,ghosts[k].sth) > 0 ? Color.GREEN :  Color.RED),
//			            		           game.getShortestPath(game.getGhostCurrentNodeIndex(ghosts[k].sth),
//			            		        		                game.getNeighbour(game.getPacmanCurrentNodeIndex(),move),     		                                
//			            		                                game.getGhostLastMoveMade(ghosts[k].sth)));
//			            }
		}
		return ans;
	}

	public double[] read(Game game){
		double[] ans = new double[94];
		int num = 0;
        if(game.getCurrentLevelTime()==0){
			IsPills.clear();
			IsPowerPills.clear();
			IsPacmanVisited.clear();
		}

		IsPacmanVisited.add(game.getPacmanCurrentNodeIndex());

        //System.out.println(game.getShortestPathDistance(game.getPacmanCurrentNodeIndex(),97));
		// 6 undirected
		ans[num++] = (double)GetProportionPowerPills(game)/game.getNumberOfPowerPills();
		ans[num++] = (double)GetProportionPills(game)/game.getNumberOfPills();
		ans[num++] = (double)GetProportionEdibleGhosts(game)/4;
		ans[num++] = (double)GetProportionEdibleTime(game)/200;
		ans[num++] = (double)IsAnyGhostsEdible(game);
		ans[num++] = (double)IsClosetoPowerPill(game);

		// 22 directed
		for(MOVE dir : MOVE.values()){
			if(dir == MOVE.NEUTRAL)continue;
			ans[num++] = (double)GetNearestPillDistance(game,dir)/MaxDis;
			//System.out.format("%s %d ",dir, GetNearestPillDistance(game,dir));

			ans[num++] = (double)GetNearestPowerPillDistance(game,dir)/MaxDis;
			//  System.out.format(" %d", GetNearestPowerPillDistance(game,dir));

			ans[num++] = (double)GetNearestJunctionDistance(game,dir)/MaxDis;
			// System.out.format(" %d\n", GetNearestJunctionDistance(game,dir));



			double[] tmp2 = GetConflictDirected(game, dir);
			for(int i=0;i<tmp2.length;i++)
				ans[num++] = tmp2[i];

			ans[num++] = (double)GetMaxPillsin30Steps(game,dir)/MaxPillsin30Steps_divisor;
			//System.out.format("  %d ", GetMaxPillsin30Steps(game,dir));

			ans[num++] = (double)GetMaxJunctionsin30Steps(game,dir)/MaxJunctionsin30Steps_divisor;
			//  System.out.format(" %d ", GetMaxJunctionsin30Steps(game,dir));

			/********* NOTICE: if the path to the first nearest junction contains any threat ghosts , return 0 ****************/
			ans[num++] = (double)GetOptionsFromNextJunction(game,dir)/38;
			// System.out.format("OFNJ : %d  \n", GetOptionsFromNextJunction(game,dir));

		}

		// method1("output.xls",ans);
		return ans;
	}
	public static void method1(String file, double[] ans) {
		BufferedWriter out = null;
		try {
			out = new BufferedWriter(new OutputStreamWriter(
					new FileOutputStream(file, true)));
			for(int i=0;i<ans.length;i++)
				out.write(String.format("%.3f ",ans[i]));
			out.write("\n");
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				out.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	public int isGhostIncoming(Game game, Constants.MOVE move, Constants.GHOST ghost) {
		if(!game.isNodeObservable(game.getGhostCurrentNodeIndex(ghost)))return 0;
		if(game.getNeighbour(game.getPacmanCurrentNodeIndex(),move)==-1)return 0;
		int[] ghostPath = game.getShortestPath(game.getGhostCurrentNodeIndex(ghost), game.getPacmanCurrentNodeIndex(), game.getGhostLastMoveMade(ghost));
		return member(game.getNeighbour(game.getPacmanCurrentNodeIndex(),move), ghostPath) ? 1 : 0;
	}

	public static boolean member(int x, int[] set) {
		for (int j = 0; j < set.length; j++) {
			if (x == set[j]) {
				return true;
			}
		}
		return false;
	}
}
