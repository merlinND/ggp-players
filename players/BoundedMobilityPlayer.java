package players;

import org.ggp.base.util.statemachine.MachineState;
import org.ggp.base.util.statemachine.Role;
import org.ggp.base.util.statemachine.StateMachine;
import org.ggp.base.util.statemachine.exceptions.GoalDefinitionException;
import org.ggp.base.util.statemachine.exceptions.MoveDefinitionException;
import org.ggp.base.util.statemachine.exceptions.TransitionDefinitionException;

public class BoundedMobilityPlayer extends BoundedDepthPlayer {

	/*
	 * PROPERTIES
	 */
	protected int averageNumberOfMoves = 1;

	/*
	 * METHODS
	 */
	@Override
	public void stateMachineMetaGame(long timeout) throws TransitionDefinitionException, MoveDefinitionException, GoalDefinitionException {
		super.stateMachineMetaGame(timeout);

		// Simulate n games and count the average number of available moves
		int n = 4;
		float averages = 0;
		for (int i = 0; i < n; ++i) {
			int numberOfStates = 0;
			int avg = 0;
			MachineState current = getCurrentState();
			while (!getStateMachine().isTerminal(current)) {
				avg = getStateMachine().getLegalMoves(current, getRole()).size();
				numberOfStates++;
				current = getStateMachine().getRandomNextState(current);
			}
			averages += (avg / (float)numberOfStates);
		}

		// Average over n games
		averageNumberOfMoves = (int)(averages / (float)n);
	}

	@Override
	protected int getHeuristic(StateMachine machine, MachineState current, Role role)
			throws GoalDefinitionException, MoveDefinitionException {
		return Heuristics.mobility(machine, current, role, averageNumberOfMoves);
	}

	/*
	 * GETTERS & SETTERS
	 */
	@Override
	public String getName() {
		return "MND_BoundedMobilityPlayer";
	}
}
