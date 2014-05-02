package players;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.ggp.base.util.statemachine.MachineState;
import org.ggp.base.util.statemachine.Move;
import org.ggp.base.util.statemachine.Role;
import org.ggp.base.util.statemachine.StateMachine;
import org.ggp.base.util.statemachine.exceptions.GoalDefinitionException;
import org.ggp.base.util.statemachine.exceptions.MoveDefinitionException;
import org.ggp.base.util.statemachine.exceptions.TransitionDefinitionException;

public class MonteCarloTreeSearchPlayer extends BoundedDepthPlayer {

	/*
	 * INNER CLASSES
	 */
	class MonteCarloTree {
		protected MonteCarloNode root;
		public MonteCarloTree(MonteCarloNode root) {
			this.root = root;
		}

		public MonteCarloNode select() {
			return root.select();
		}

		/**
		 * From the current state (move), after gathering stats in the tree,
		 * find the most enviable child node and return the move
		 * that will get us there.
		 * @return
		 */
		public Move getBestMove() {
			int bestUtility = 0;
			Move bestMove = null;
			for (MonteCarloNode n : root.children) {
				if (bestMove == null || bestUtility < n.utility) {
					bestUtility = n.utility;
					bestMove = n.parentMove;
				}
			}
			return bestMove;
		}
	}

	class MonteCarloNode {
		/** No parent => root */
		protected MonteCarloNode parent;
		/** The move that got us to this state */
		protected Move parentMove;
		protected List<MonteCarloNode> children = new ArrayList<MonteCarloNode>();
		protected MachineState state;

		protected int visits;
		protected int utility;

		public MonteCarloNode(MachineState state) {
			this(state, null, null);
		}
		public MonteCarloNode(MachineState state, MonteCarloNode parent, Move parentMove) {
			this.state = state;
			this.parent = parent;
			this.parentMove = parentMove;
			this.visits = 0;
			this.utility = 0;
		}

		public MonteCarloNode select() {
			// If this node is new, select it
			if (this.visits <= 0 || this.children.size() <= 0)
				return this;
			else {
				float bestScore = 0;
				MonteCarloNode bestNode = null;
				for (MonteCarloNode n : children) {
					// If any of its children is new, select it
					if (n.visits <= 0)
						return n;

					// Otherwise, select the one that maximises the "score"
					// and let it select among its children
					float score = evaluateNode(n);
					if (bestNode == null || score > bestScore) {
						bestScore = score;
						bestNode = n;
					}
				}
				return bestNode.select();
			}
		}

		/**
		 *
		 * @param n The number of depth charges to perform
		 * @return
		 * @throws TransitionDefinitionException
		 * @throws MoveDefinitionException
		 * @throws GoalDefinitionException
		 */
		public int simulate(int n)
				throws TransitionDefinitionException, MoveDefinitionException, GoalDefinitionException {
			return (int)Heuristics.monteCarloSearch(getStateMachine(), this.state, getRole(), n);
		}

		/**
		 * Helper function to select the nodes
		 * @param node
		 * @return
		 */
		private float evaluateNode(MonteCarloNode node) {
			return node.utility + (float)Math.sqrt(2 * Math.log(node.parent.visits) / (float)node.visits);
		}

		// TODO: adapt to two player games (differentiate max nodes and min nodes)
		// TODO: make sure not to exceed memory limits
		public void expand() throws MoveDefinitionException, TransitionDefinitionException {
			// Gather a list of the legal moves and the resulting states
			Map<Move, List<MachineState>> nexts = getStateMachine().getNextStates(state, getRole());
			// Do not build several nodes for the same state
			Set<MachineState> seenStates = new HashSet<MachineState>();

			for (Move m : nexts.keySet()) {
				for (MachineState s : nexts.get(m)) {
					if (!seenStates.contains(s)) {
						children.add(new MonteCarloNode(s, this, m));
						seenStates.add(s);
					}
				}
			}
		}

		public void backPropagate(float score) {
			this.visits++;
			this.utility += score;
			if (this.parent != null)
				this.parent.backPropagate(score);
		}

		public int getDepth() {
			MonteCarloNode current = this;
			int depth = 0;
			while (current.parent != null) {
				depth++;
				current = current.parent;
			}
			return depth;
		}
	}


	/*
	 * PROPERTIES
	 */
	protected MonteCarloTree tree;

	/*
	 * METHODS
	 */
	@Override
	public Move stateMachineSelectMove(long timeout)
			throws TransitionDefinitionException, MoveDefinitionException, GoalDefinitionException {
		// TODO: could we keep the nodes from one turn to the next?
		tree = new MonteCarloTree(new MonteCarloNode(getCurrentState()));

		// For this player, we need the timeout parameter
		return getBestMove(getStateMachine(), getCurrentState(), getRole(), timeout);
	}

	@Override
	public Move getBestMove(StateMachine machine, MachineState current, Role role, long timeout)
			throws TransitionDefinitionException, GoalDefinitionException, MoveDefinitionException {
		// Keep enhancing the stats while there's still time
		long timeLimit = Math.min(System.currentTimeMillis() + 500, timeout - 1000);
		while (canContinue(timeLimit)) {

			// 1. Select the next node to expand
			MonteCarloNode nextNode = tree.select();

			// Bound depth
			if (nextNode.getDepth() > getMaxDepth())
				break;

			// 2. Add its descendants to the considered nodes
			// (i.e. they may get expanded next time)
			nextNode.expand();

			if (!canContinue(timeLimit))
				break;

			// 3. Run depth charge from the selected node
			int score = nextNode.simulate(6);
			// 4. Backpropagate this result from the selected node
			// all the way up to the root node
			nextNode.backPropagate(score);
		}

		// The tree now contains all the stats we need to select the best target state
		// TODO: adjust to multiplayer games!
		return tree.getBestMove();
	}

	protected boolean canContinue(long timeout) {
		// Check to see if there's time to continue.
        return (System.currentTimeMillis() < timeout);
	}

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
		return "MND_MonteCarloTreeSearchPlayer";
	}
}
