package players;

import java.util.List;

import org.ggp.base.player.gamer.Gamer;
import org.ggp.base.util.gdl.grammar.GdlPool;
import org.ggp.base.util.statemachine.MachineState;
import org.ggp.base.util.statemachine.Move;
import org.ggp.base.util.statemachine.Role;
import org.ggp.base.util.statemachine.StateMachine;
import org.ggp.base.util.statemachine.exceptions.GoalDefinitionException;
import org.ggp.base.util.statemachine.exceptions.MoveDefinitionException;
import org.ggp.base.util.statemachine.exceptions.TransitionDefinitionException;

public class MiniMaxPlayer extends BasicPlayer {

	/*
	 * PROPERTIES
	 */

	/*
	 * METHODS
	 */
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
		Move bestMove = new Move(GdlPool.getConstant("NOOP"));
		Integer bestScore = Gamer.MIN_GOAL;

		for (Move played : moves) {
			Integer score = getMinScore(machine, current, role, played);

			if (score >= Gamer.MAX_GOAL)
				return played;
			if (bestMove == null || score > bestScore) {
				bestScore = score;
				bestMove = played;
			}
		}

		return bestMove;
	}

	public Integer getMaxScore(StateMachine machine, MachineState current, Role role)
			throws GoalDefinitionException, TransitionDefinitionException, MoveDefinitionException {

		List<Move> moves = machine.getLegalMoves(current, role);
		Integer max = null;

		for (Move played : moves) {
			Integer score = getMinScore(machine, current, role, played);

			if (score >= Gamer.MAX_GOAL)
				return score;
			if (max == null || score > max)
				max = score;
		}

		return max;
	}
	public Integer getMinScore(StateMachine machine, MachineState current, Role role, Move action)
			throws GoalDefinitionException, TransitionDefinitionException, MoveDefinitionException {

		if (machine.isTerminal(current))
			return machine.getGoal(current, role);
		else {
			List<List<Move>> jointMoves = machine.getLegalJointMoves(current, role, action);
			Integer min = null;

			for (List<Move> played : jointMoves) {

				MachineState nextState = machine.getNextState(current, played);
				Integer score = getMaxScore(machine, nextState, role);

				if (score <= Gamer.MIN_GOAL)
					return score;
				if (min == null || score < min)
					min = score;
			}

			return min;
		}
	}

	/*
	 * GETTERS & SETTERS
	 */
	@Override
	public String getName() {
		return "MND_MiniMaxPlayer";
	}
}
