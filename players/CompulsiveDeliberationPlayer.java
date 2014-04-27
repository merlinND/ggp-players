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

public class CompulsiveDeliberationPlayer extends BasicPlayer {

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

		Move bestMove = moves.get(0);
		Integer bestScore = 0;

		for (Move move : moves) {
			ArrayList<Move> played = new ArrayList<Move>();
			played.add(move);
			MachineState next = machine.getNextState(current, played);
			Integer score = getBestScore(machine, next, role);
			if (score > bestScore) {
				bestScore = score;
				bestMove = move;
			}
		}

		return bestMove;
	}

	/**
	 * Search tree recursively to determine the best accessible score from <current> state
	 * @param machine
	 * @param current
	 * @param role
	 * @return
	 * @throws GoalDefinitionException
	 * @throws TransitionDefinitionException
	 * @throws MoveDefinitionException
	 */
	public Integer getBestScore(StateMachine machine, MachineState current, Role role) throws GoalDefinitionException, TransitionDefinitionException, MoveDefinitionException {
		if (machine.isTerminal(current))
			return machine.getGoal(current, role);
		else {
			List<Move> moves = machine.getLegalMoves(current, role);
			Integer best = machine.getGoal(current, role);

			for (Move move : moves) {
				ArrayList<Move> played = new ArrayList<Move>();
				played.add(move);
				MachineState nextState = machine.getNextState(current, played);
				Integer score = getBestScore(machine, nextState, role);

				if (score >= Gamer.MAX_GOAL)
					return score;
				if (score > best)
					best = score;
			}

			return best;
		}
	}


	/*
	 * GETTERS & SETTERS
	 */
	@Override
	public String getName() {
		return "MND_CompulsiveDeliberationPlayer";
	}
}
