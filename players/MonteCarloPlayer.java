package players;

import org.ggp.base.util.statemachine.MachineState;
import org.ggp.base.util.statemachine.Role;
import org.ggp.base.util.statemachine.StateMachine;
import org.ggp.base.util.statemachine.exceptions.GoalDefinitionException;
import org.ggp.base.util.statemachine.exceptions.MoveDefinitionException;
import org.ggp.base.util.statemachine.exceptions.TransitionDefinitionException;

public class MonteCarloPlayer extends BoundedDepthPlayer {

	/*
	 * PROPERTIES
	 */

	/*
	 * METHODS
	 */
	@Override
	protected int getHeuristic(StateMachine machine, MachineState current, Role role)
			throws GoalDefinitionException, MoveDefinitionException {
		try {
			return (int)Heuristics.monteCarloSearch(machine, current, role, 3);
		} catch (TransitionDefinitionException e) {
			e.printStackTrace();
			return 0;
		}
	}



	/*
	 * GETTERS & SETTERS
	 */
	@Override
	protected int getMaxDepth() {
		return 3;
	}

	@Override
	public String getName() {
		return "MND_MonteCarloPlayer";
	}
}
