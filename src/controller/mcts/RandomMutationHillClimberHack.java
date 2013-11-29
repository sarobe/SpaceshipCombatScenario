package controller.mcts;

import ea.FitVectorSource;

import java.util.Random;

public class RandomMutationHillClimberHack implements FitVectorSource {

    static Random rand = new Random();

    double sDev = 1;
    double noiseDev = 0.1;
    static double noiseFac = 1.02;
    int nDim;
    double[] bestYet;
    double bestScore;
    double[] proposed;
    int nEvals = 0;
    int order;

    public static int MAX_BEST = 1;
    public static int MIN_BEST = -1;

    //double[] fourDirHack = {1, 0, 0, 1, 0, 0, 1, 0, 0, 1, 0, 0};
    double[] lunarHack = {0, -1, 1, 0, 0, 0, 0, 1, -1, 1, -1, 1, 1, 0, 0, 1, -1, 1};

    public RandomMutationHillClimberHack(int nDim, int order) {
        this.nDim = nDim;
        this.order = order;
        bestYet = new double[nDim];
        for (int i=0; i<nDim; i++) bestYet[i] = rand.nextGaussian() * sDev;
        bestScore = Double.NEGATIVE_INFINITY * order;
    }

    @Override
    public void returnFitness(double fitness) {
        nEvals++;
        if (order * fitness >= bestScore * order) {
            bestYet = proposed;
            bestScore = fitness;
            // success so increase the noiseDev
            noiseDev *= noiseFac;
        } else {
            // failure so decrease noiseDev
            noiseDev /= noiseFac;
        }

        // System.out.println(nEvals + "\t improved to: " + bestScore);
        // System.out.println(noiseDev);

    }

    @Override
    public double[] getNext() {
        proposed = new double[nDim];
        for (int i=0; i<nDim; i++) {
            proposed[i] = bestYet[i] + rand.nextGaussian() * noiseDev;
        }
        // System.out.println("Returning: " + Arrays.toString(hack));
        //return proposed;
        return lunarHack;
    }

    @Override
    public double[] bestVec() {
        return bestYet;
    }

    @Override
    public double bestScore() {
        return bestScore;
    }
}