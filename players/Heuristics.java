package players;

import org.ggp.base.util.statemachine.MachineState;
import org.ggp.base.util.statemachine.Role;
import org.ggp.base.util.statemachine.StateMachine;
import org.ggp.base.util.statemachine.exceptions.GoalDefinitionException;
import org.ggp.base.util.statemachine.exceptions.MoveDefinitionException;

import sun.reflect.generics.reflectiveObjects.NotImplementedException;

public final class Heuristics {
	/*
	 * METHODS
	 */
	/**
	 *
	 * @param machine
	 * @param state
	 * @param role
	 * @return If the state is terminal, return its goal. Otherwise, return 0.
	 * @throws GoalDefinitionException
	 */
	public static int evalNull(StateMachine machine, MachineState state, Role role) throws GoalDefinitionException {
		if (machine.isTerminal(state))
			return machine.getGoal(state, role);
		else
			return 0;
	}

	/**
	 *
	 * @param machine
	 * @param state
	 * @param role
	 * @return A measure of mobility from 0 to 100
	 * @throws GoalDefinitionException
	 * @throws MoveDefinitionException
	 */
	public static int mobility(StateMachine machine, MachineState state, Role role, int averageNumberOfMoves)
			throws GoalDefinitionException, MoveDefinitionException {
		int legal = machine.getLegalMoves(state, role).size();
		return (int)((legal / (float)averageNumberOfMoves) * 100);
	}

	/**
	 * Simplistic version: return the current goal for this state
	 * @param machine
	 * @param state
	 * @param role
	 * @return
	 * @throws GoalDefinitionException
	 */
	public static int goalProximity(StateMachine machine, MachineState state, Role role) throws GoalDefinitionException {
		return machine.getGoal(state, role);
	}

	/**
	 * TODO: Advanced version: measure the similarity of this state to a winning state
	 * @param machine
	 * @param state
	 * @param role
	 * @return
	 */
	public static int goalSimilarity(StateMachine machine, MachineState state, Role role) throws NotImplementedException {
		throw new NotImplementedException();
	}

	/*
	 * GETTERS & SETTERS
	 */
}
