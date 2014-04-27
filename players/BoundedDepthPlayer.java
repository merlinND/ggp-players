package players;

import java.util.ArrayList;
import java.util.List;

import org.ggp.base.player.gamer.Gamer;
import org.ggp.base.util.statemachine.MachineState;
import org.ggp.base.util.statemachine.Move;
import org.ggp.base.util.statemachine.Role;
import org.ggp.base.util.statemachine.StateMachine;
import org.ggp.base.util.statemachine.exceptions.GoalDefinitionException;
import org.ggp.base.util.statemachine.exceptions.MoveDefinitionException;
import org.ggp.base.util.statemachine.exceptions.TransitionDefinitionException;

public class BoundedDepthPlayer extends BasicPlayer {
	/*
	 * PROPERTIES
	 */
	protected int averageNumberOfActions = 0;

	/*
	 * METHODS
	 */
	@Override
	public void stateMachineMetaGame(long timeout)
			throws TransitionDefinitionException, MoveDefinitionException, GoalDefinitionException {
		// Determine the average number of available actions by playing random games
		List<Float> averages = new ArrayList<Float>();
		List<Integer> samples = new ArrayList<Integer>();
		System.out.println("Trying to estimate average number of legal moves per state");
		int n = 10;
//		for (int i = 0; i < n; i++) {
//			MachineState current = getCurrentState();
//			while (!getStateMachine().isTerminal(current)) {
//				samples.add(getStateMachine().getLegalMoves(current, getRole()).size());
//				current = getStateMachine().getRandomNextState(current);
//			}
//
//			float avg = 0;
//			for (Integer s : samples)
//				avg += s;
//			avg /= (float)samples.size();
//			averages.add(avg);
//
//			System.out.println("This simulation yielded an average of " + avg);
//		}

		float avg = 0;
		for (Float s : averages)
			avg += s;
		avg /= samples.size();
		System.out.println("The " + n + " simulations gave an average of " + avg);
		this.averageNumberOfActions = (int)avg;
	}

	@Override
	public Move stateMachineSelectMove(long timeout) throws TransitionDefinitionException, MoveDefinitionException, GoalDefinitionException {

		return getBestMove(getStateMachine(), getCurrentState(), getRole(), 6);
	}

	protected Move getBestMove(StateMachine machine, MachineState current, Role role, int maxDepth)
			throws TransitionDefinitionException, GoalDefinitionException, MoveDefinitionException {
		List<Move> moves = machine.getLegalMoves(current, role);

		Move bestMove = moves.get(0);
		Integer bestScore = 0;

		for (Move move : moves) {
			ArrayList<Move> played = new ArrayList<Move>();
			played.add(move);
			MachineState next = machine.getNextState(current, played);
			Integer score = getBoundedMiniMaxScore(machine, next, role, false, 1, maxDepth);

			if (score >= Gamer.MAX_GOAL)
				return move;
			if (score > bestScore) {
				bestScore = score;
				bestMove = move;
			}
		}

		return bestMove;
	}

	public Integer getBoundedMiniMaxScore(StateMachine machine, MachineState current, Role role,
										  Boolean isMax, int depth, int maxDepth)
			throws GoalDefinitionException, TransitionDefinitionException, MoveDefinitionException {

		if (machine.isTerminal(current))
			return machine.getGoal(current, role);
		else if (depth >= maxDepth)
			return Heuristics.goalProximity(machine, current, role);
		else {
			List<List<Move>> jointMoves = machine.getLegalJointMoves(current);
			Integer extrema = null;

			for (List<Move> played : jointMoves) {

				MachineState nextState = machine.getNextState(current, played);

				// Only count depth for turns that are actually ours to play
				int nextDepth = depth;
				if (!isMax)
					nextDepth++;

				Integer score = getBoundedMiniMaxScore(machine, nextState, role, !isMax, nextDepth, maxDepth);

				if (extrema == null)
					extrema = score;

				if (isMax) {
					if (score >= Gamer.MAX_GOAL)
						return score;
					if (score > extrema)
						extrema = score;
				}
				else {
					if (score <= Gamer.MIN_GOAL)
						return score;
					if (score < extrema)
						extrema = score;
				}
			}

			return extrema;
		}
	}

	/*
	 * GETTERS & SETTERS
	 */
	@Override
	public String getName() {
		return "MND_BoundedDepthPlayer";
	}
}
