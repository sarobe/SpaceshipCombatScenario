package strategy;

import common.Constants;
import ea.RandomMutationHillClimber;
import gnuplot.Grapher;
import problem.SpaceshipCombatProblem;

import java.util.Arrays;

/**
 * Created by Samuel Roberts, 2013
 */
public class HillHandler implements IStrategy {

    public SpaceshipCombatProblem problem;
    public RandomMutationHillClimber hillClimber;
    private int funcEvals;
    private int runIndex;

    private double[] mostRecent;
    private double mostRecentFitness;


    public HillHandler(SpaceshipCombatProblem problem, int runIndex) {
        this.problem = problem;
        this.runIndex = runIndex;
        init();
    }

    public void init() {
        hillClimber = new RandomMutationHillClimber(problem.nDim(), RandomMutationHillClimber.MIN_BEST);
        funcEvals = 0;
        mostRecent = new double[problem.nDim()];
    }

    public void run() {
        // iteration loop
        if (!hasCompleted()) {
            // compute fitness/objective value
            funcEvals++;
            mostRecentFitness = problem.fitness(mostRecent); // fitness is to be minimized
            hillClimber.returnFitness(mostRecentFitness);


            try {
                Grapher.writeGenData(runIndex, funcEvals, mostRecentFitness, mostRecentFitness, mostRecent);
            } catch (Exception e) {
                System.out.println("Unsuccessful attempt to write graphing data! : " + e.getMessage());
            }

            int outmod = 20;
            if (funcEvals % (outmod) == 1)
                System.out.println(funcEvals + ": Score: " + mostRecentFitness);
        }

    }

    public void finish() {
        // final output
        System.out.println("Best solution: " + hillClimber.bestVec());
        System.out.println("Score: " + hillClimber.bestScore());
        System.out.println("Last solution: " + mostRecent);
        System.out.println("Score: " + mostRecentFitness);
    }

    public double[][] getPopulation() {
        mostRecent = hillClimber.getNext();
        double[][] pop = new double[1][];
        pop[0] = mostRecent;

        return pop;
    }

    public boolean hasCompleted() {
        return funcEvals > Constants.numEvals;
    }

    public long getIterations() {
        return funcEvals;
    }

    public int getFuncEvals() {
        return funcEvals;
    }

}
