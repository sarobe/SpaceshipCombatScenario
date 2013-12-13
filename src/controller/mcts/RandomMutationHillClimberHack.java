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

    double[] fourDirHack = {1, 0, 0, 1, 0, 0, 1, 0, 0, 1, 0, 0};
    double[] lunarHack = {0, -1, 1, 0, 0, 0, 0, 1, -1, 1, -1, 1, 1, 0, 0, 1, -1, 1};
    double[] noHack = {1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1};
    double[] evoHack = {0.8952032594417709, 0.5355876020169748, 0.171792027397131, -1.8653047387380446, -0.8141103570936311, -0.17170701240088626, 0.41782760954626125, 0.32702415271720137, 0.2972977550061861, -0.331012943529391, -0.979543914098989, -2.2057513084767804, 1.102629100090829, -0.5033905297309863, 0.40953967480672654, -0.3459936731498821, -0.6860667667216647, 1.8685212144940766};

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
        return lunarHack;
        //return proposed;
        //return evoHack;
        //return noHack;
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