package players;

import java.util.List;

import org.ggp.base.util.statemachine.Move;
import org.ggp.base.util.statemachine.exceptions.GoalDefinitionException;
import org.ggp.base.util.statemachine.exceptions.MoveDefinitionException;
import org.ggp.base.util.statemachine.exceptions.TransitionDefinitionException;

public class LegalPlayer extends BasicPlayer {

	/*
	 * PROPERTIES
	 */

	/*
	 * METHODS
	 */
	@Override
	public Move stateMachineSelectMove(long timeout) throws TransitionDefinitionException, MoveDefinitionException, GoalDefinitionException {
		List<Move> moves = getStateMachine().getLegalMoves(getCurrentState(), getRole());
		return moves.get(0);
	}

	/*
	 * GETTERS & SETTERS
	 */
	@Override
	public String getName() {
		return "MND_LegalPlayer";
	}
}
