package controller.mcts;

import controller.mcts.gamestates.IGameState;

import java.util.Random;

// Taken from Simon Lucas' GGFVL project, 11/13
public class FeatureWeightedRoller implements ITunableRoller {

    public boolean uniform  = true;

    public double[] params;
    double[] bias;

    int nActions;
    int nFeatures;

    static Random rand = new Random();

    public FeatureWeightedRoller(IGameState state) {
        // assumes that all states have the same number of
        // actions and will not work for some games
        nActions = state.nActions();

        nFeatures = state.getFeatures().length;
        bias = new double[nActions];
        params = new double[nActions*nFeatures];
    }

    public int roll(IGameState gameState) {
        if (uniform) return rand.nextInt(nActions);
        double[] features = gameState.getFeatures();
        // System.out.println(Arrays.toString(features));
        int ix = 0; // used to step over params
        double tot = 0;
        for (int i=0; i<nActions; i++) {
            bias[i] = 0;
            for (int j=0; j<nFeatures; j++) {
                bias[i] += params[ix] * features[j];
                ix++;
            }
            // now replace with e^a[i]
            bias[i] = Math.exp(bias[i]);
            tot += bias[i];
        }
        // now track relative cumulative probability
        // System.out.println(Arrays.toString(bias));
        double x = rand.nextDouble();

        // an accumulator
        double acc = 0;
        int action = 0;
        for ( ; action<nActions; action++) {
            acc += bias[action] / tot;
            if (x < acc) return action;
        }
        if (action == nActions) {
//            System.out.println("Oops: Softmax Failure: " + action);
//            System.out.println(Arrays.toString(params));
            // System.out.println();
            action = rand.nextInt(nActions);
        }
        return action;
    }

    public double[] getBiases (IGameState gameState) {
        double[] biases = new double[gameState.nActions()];
        // uniform = true;
        // if (uniform || true) return biases;
        if (uniform) return biases;
        double[] features = gameState.getFeatures();
        // System.out.println(Arrays.toString(features));
        int ix = 0; // used to step over params
        double tot = 0;
        for (int i=0; i<nActions; i++) {
            bias[i] = 0;
            for (int j=0; j<nFeatures; j++) {
                bias[i] += params[ix] * features[j];
                ix++;
            }
            // now replace with e^a[i]
            bias[i] = Math.exp(bias[i]);
            tot += bias[i];
        }
        for (int i=0; i<biases.length; i++) {
            biases[i] = bias[i] / tot;
        }
        return biases;
    }

    @Override
    public int nDim() {
        return nActions * nFeatures;
    }

    @Override
    public void setParams(double[] s) {
        for (int i=0; i<nDim(); i++)
            params[i] = s[i];
    }
}