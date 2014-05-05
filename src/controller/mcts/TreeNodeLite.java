package controller.mcts;

import common.utilities.Picker;
import controller.gamestates.IGameState;
import ea.FitVectorSource;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

// HEAVILY ADAPTED CODE TAKEN FROM SIMON LUCAS' MCTS CODE
// THIS IS AN UGLY, UGLY MODIFICATION

public class TreeNodeLite {
    // exploration term
    // 0.5 works ok for Othello
    static double k = 1.4;
    static double epsilon = 1e-6;
    IGameState initialState;
    TreeNodeLite parent;
    // that action taken to lead to this node
    Integer action;
    static int nExpansions = 0;
    static int nInstances = 0;

    static double biasInfluence = 0.0;
    static double meanInfluence = 1.0;

    // create some pickers just once to save the effort of creating each time
    private static Picker<Integer> picker = new Picker<Integer>();
    // private static Picker<Integer> rootPicker = new Picker<Integer>();

    // next thing to work out: when to expand the state...

    public TreeNodeLite[] children;
    public int nVisits;
    public double totValue;
    // need this to draw the roll-out
    public List<RollOut> rollOuts;
    public double bestRolloutValue = 0;
    public double worstRolloutValue = Double.MAX_VALUE;

    // need this to draw the roll-out
    public List<Integer> lastRollOutMax;

    public IRoller roller;

//    public TreeNodeLite(TreeNodeLite parent, IRoller roller) {
//        this.parent = parent;
//        this.roller = roller;
//        nInstances++;
//    }

    public TreeNodeLite(IRoller roller) {
        this.roller = roller;
        rollOuts = new ArrayList<RollOut>();
        nInstances++;
    }

//    public TreeNodeLite(TreeNodeLite parent, Integer action, IRoller roller) {
//        this.parent = parent;
//        this.action = action;
//        this.roller = roller;
//        nInstances++;
//    }

    public TreeNodeLite(TreeNodeLite parent, Integer action, IRoller roller, IGameState state) {
        this.parent = parent;
        this.action = action;
        this.roller = roller;
        this.initialState = state;
        rollOuts = new ArrayList<RollOut>();
        nInstances++;
    }

    public void mctsSearch(IGameState root, int its) {
        for (int i = 0; i < its; i++) {
            IGameState state = root.copy();
            initialState = root.copy();
            // System.out.println("State = " + state);
            TreeNodeLite selected = treePolicy(state);
            TreeNodeLite expanded = selected.expand(state);
            double delta = selected.rollOut(state);
            expanded.backUp(delta);
        }
    }

    public void mctsSearch(IGameState root,
                           int its,
                           ITunableRoller roller,
                           FitVectorSource source) {
        for (int i = 0; i < its; i++) {
            IGameState state = root.copy();
            initialState = root.copy();
            roller.setParams(source.getNext());

            TreeNodeLite selected = treePolicy(state);
            TreeNodeLite expanded = selected.expand(state);

            double value = selected.rollOut(state);
            source.returnFitness(value);
            expanded.backUp(value);
        }
    }

    public int mctsFastEvoSearch(IGameState root,
                                 int its,
                                 ITunableRoller roller,
                                 FitVectorSource source) {
        for (int i = 0; i < its; i++) {
            IGameState state = root.copy();
            initialState = root.copy();
            roller.setParams(source.getNext());
            TreeNodeLite selected = treePolicy(state);
            TreeNodeLite expanded = selected.expand(state);
            double value = selected.rollOut(state);
            source.returnFitness(value);
            expanded.backUp(value);
        }
        return bestRootAction(root, roller);
    }

    public void backUp(double result) {
        nVisits++;
        double v = result; // state.valueOf(result);

        // System.out.println("Adding in : " + v);
        totValue += v;
        if (parent != null) parent.backUp(result);
    }

    public TreeNodeLite treePolicy(IGameState state) {
        TreeNodeLite cur = this;
        while (cur.nonTerminal() && cur.fullyExpanded()) {
            cur = cur.bestChild();
            state.next(cur.action);
        }
        // System.out.println("TreePolicy returning node of depth: " + cur.depth());
        return cur;
    }

    boolean nonTerminal() {
        return children != null;
    }

    public TreeNodeLite bestChild() {
        return bestChild(k);
    }

    public TreeNodeLite bestChild(double k) {

        TreeNodeLite selected = null;
        double bestValue = -Double.MAX_VALUE;
        for (TreeNodeLite child : children) {
            // addition of epsilon used primarily to avoid cases of dividing by zero (can't divide by zero if a fixed positive constant is added)
            double uctValue =
                    child.totValue / (child.nVisits + epsilon) +
                            k * Math.sqrt(Math.log(nVisits + 1) / (child.nVisits + epsilon)) +
                            r.nextDouble() * epsilon;
            if(uctValue == Double.NEGATIVE_INFINITY) {
                // aaaaa
                System.out.println("HALT");
            }
            // small random numbers: break ties in unexpanded nodes
            if (uctValue > bestValue) {
                selected = child;
                bestValue = uctValue;
            }
        }
        if (selected == null)
            throw new RuntimeException("Warning! returning null: " + bestValue + " : " + children.length);
        return selected;
    }

    public boolean fullyExpanded() {
        if (children == null) return false;
        for (TreeNodeLite tn : children) {
            if (tn == null) {
                return false;
            }
        }
        return true;
    }

    static int nTerminalExpansions = 0;

    public TreeNodeLite expand(IGameState state) {
        // if the state is terminal then simply return the current node
        if (state.isTerminal()) {
            // System.out.println("Called expand on terminal state: " + ++nTerminalExpansions);
            return this;
        }


        // if the child array has not yet been made then make it
        if (children == null) children = new TreeNodeLite[state.nActions()];

        // choose a random unused action and add a new node for that
        picker.reset();
        for (int i = 0; i < children.length; i++) {
            if (children[i] == null) {
                double x = r.nextDouble();
                picker.add(x, i);
            }
        }
        // if (unused == 0) throw new RuntimeException("Should not have zero null children; state terminal? " + state.isTerminal());
        int bestAction = picker.getBest();
        TreeNodeLite tn = new TreeNodeLite(this, bestAction, this.roller, state);
        nExpansions++;
        children[bestAction] = tn;
        state.next(bestAction);
        return tn;
    }

    public int bestRootAction(IGameState state, IRoller roller) {
        if (roller instanceof FeatureWeightedRoller)
            return biasedRootAction(state, (FeatureWeightedRoller) roller);
        Picker<Integer> p = new Picker<Integer>();

        for (int i=0; i<children.length; i++) {
            if (children[i] != null) {
                p.add(children[i].meanValue() + r.nextDouble() * epsilon, i);
            }
        }
        return p.getBest();
    }

    public int biasedRootAction(IGameState state, FeatureWeightedRoller roller) {
        Picker<Integer> p = new Picker<Integer>();
        double[] biases = roller.getBiases(state);
        // System.out.println(Arrays.toString(biases));
        for (int i=0; i<children.length; i++) {
            if (children[i] != null) {
                p.add(children[i].meanValue() * meanInfluence + biases[i] * biasInfluence + r.nextDouble() * epsilon, i);
            }
        }
        return p.getBest();
    }

    public boolean isLeaf() {
        return children == null;
    }


    public double rollOut(IGameState state) {
        // System.out.println(state + " : " + action);
        List<Integer> lastRollOut = new ArrayList<Integer>();
        double bestHeuristic = 0;
        //double totalHeuristic = 0;
        RollOut rollOutData = new RollOut();
        while (!state.isTerminal()) {
            int numActions = state.nActions();
            int action = r.nextInt(numActions);
            action = roller.roll(state);
            if (action >= 0)             //action == -1 is PASS.
            {
                state.next(action);
                if(state.heuristicValue() > bestHeuristic) {
                    bestHeuristic = state.heuristicValue();
                }
                //totalHeuristic += state.heuristicValue() * 0.1;
            }
            rollOutData.addAction(action);
            rollOutData.addPosition(state.getShipState().pos.copy());
            rollOutData.addVelocity(state.getShipState().vel().copy());
            rollOutData.addGameState(state);
        }

        // one last check
        //if(state.heuristicValue() > bestHeuristic) bestHeuristic = state.heuristicValue();


        double value = state.value() + bestHeuristic;//state.value() + totalHeuristic;
        rollOutData.setScore(value);
        rollOuts.add(rollOutData);
        if(value > bestRolloutValue) bestRolloutValue = value;
        if(value < worstRolloutValue) worstRolloutValue = value;
        return value;
    }

    public double meanValue() {
        return totValue / nVisits + epsilon;
    }

    public int arity() {
        return children == null ? 0 : children.length;
    }

    public double totValue() {
        return totValue;
    }

    public int nVisits() {
        return nVisits;
    }

    public List<Integer> lastRollOut() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public static Random r = new Random();

    public int treeSize() {
        if (children == null) return 1;
        int tot = 1; // count this node
        for (TreeNodeLite child : children) {
            // count child nodes
            if (child != null) {
                tot += child.treeSize();
                // tot += 1;
            }
        }
        return tot;
    }
}
