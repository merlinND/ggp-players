package players;

import java.util.List;

import org.ggp.base.util.statemachine.MachineState;
import org.ggp.base.util.statemachine.Move;
import org.ggp.base.util.statemachine.Role;
import org.ggp.base.util.statemachine.StateMachine;
import org.ggp.base.util.statemachine.exceptions.GoalDefinitionException;
import org.ggp.base.util.statemachine.exceptions.MoveDefinitionException;
import org.ggp.base.util.statemachine.exceptions.TransitionDefinitionException;

public abstract class BoundedDepthPlayer extends BasicPlayer {

	/*
	 * PROPERTIES
	 */

	/*
	 * METHODS
	 */
	protected abstract int getHeuristic(StateMachine machine, MachineState current, Role role)
			throws GoalDefinitionException, MoveDefinitionException;

	@Override
	public Move stateMachineSelectMove(long timeout) throws TransitionDefinitionException, MoveDefinitionException, GoalDefinitionException {
		return getBestMove(getStateMachine(), getCurrentState(), getRole());
	}

	/**
	 * Determine the best move to play from the <current> state.
	 * @param machine
	 * @param state
	 * @param role
	 * @return
	 * @throws TransitionDefinitionException
	 * @throws MoveDefinitionException
	 * @throws GoalDefinitionException
	 */
	public Move getBestMove(StateMachine machine, MachineState current, Role role)
			throws TransitionDefinitionException, GoalDefinitionException, MoveDefinitionException {
		List<Move> moves = machine.getLegalMoves(current, role);

		Move bestMove = null;
		Integer alpha = MIN_GOAL, beta = MAX_GOAL;

		if (moves.size() == 1) {
			return moves.get(0);
		}

		for (Move played : moves) {
			Integer score = getBetaScore(machine, current, role, played, alpha, beta, 1);

			if (score >= MAX_GOAL)
				return played;
			if (bestMove == null || score > alpha) {
				alpha = score;
				bestMove = played;
			}
			// TODO : prune?
		}
		return bestMove;
	}


	public Integer getAlphaScore(StateMachine machine, MachineState current, Role role,
								 Integer alpha, Integer beta, Integer depth)
			throws GoalDefinitionException, TransitionDefinitionException, MoveDefinitionException {

		if (machine.isTerminal(current))
			return machine.getGoal(current, role);
		else if (depth >= getMaxDepth())
			return getHeuristic(machine, current, role);
		else {
			List<Move> moves = machine.getLegalMoves(current, role);

			for (Move played : moves) {

				Integer score = getBetaScore(machine, current, role, played, alpha, beta, depth);
				alpha = Math.max(alpha, score);

				if (score >= MAX_GOAL)
					return score;
				// Pruning: the opponent would never let us choose a move better than beta
				if (alpha >= beta)
					return beta;
			}

			return alpha;
		}
	}
	public Integer getBetaScore(StateMachine machine, MachineState current, Role role, Move action,
								Integer alpha, Integer beta, Integer depth)
			throws GoalDefinitionException, TransitionDefinitionException, MoveDefinitionException {

		if (machine.isTerminal(current))
			return machine.getGoal(current, role);
		else {
			// Find all moves the joint moves including our pre-selected move
			List<List<Move>> jointMoves = machine.getLegalJointMoves(current, role, action);

			for (List<Move> played : jointMoves) {

				MachineState nextState = machine.getNextState(current, played);
				Integer score = getAlphaScore(machine, nextState, role, alpha, beta, depth + 1);
				beta = Math.min(beta, score);

				if (score <= MIN_GOAL)
					return score;
				// Pruning: we would never let us choose a move worse than alpha
				if (beta <= alpha)
					return alpha;
			}

			return beta;
		}
	}

	/*
	 * GETTERS & SETTERS
	 */
	protected int getMaxDepth() {
		return 6;
	}
}
