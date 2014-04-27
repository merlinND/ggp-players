package players;

import org.ggp.base.util.statemachine.MachineState;
import org.ggp.base.util.statemachine.Role;
import org.ggp.base.util.statemachine.StateMachine;
import org.ggp.base.util.statemachine.exceptions.GoalDefinitionException;

public class BoundedGoalProximityPlayer extends BoundedDepthPlayer {

	/*
	 * PROPERTIES
	 */

	/*
	 * METHODS
	 */
	@Override
	protected int getHeuristic(StateMachine machine, MachineState current, Role role) throws GoalDefinitionException {
		return Heuristics.goalProximity(machine, current, role);
	}

	/*
	 * GETTERS & SETTERS
	 */
	@Override
	public String getName() {
		return "MND_BoundedGoalProximityPlayer";
	}
}