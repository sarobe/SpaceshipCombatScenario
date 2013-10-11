package strategy;

import common.Constants;
import fr.inria.optimization.cmaes.CMAEvolutionStrategy;
import gnuplot.Grapher;
import problem.SpaceshipCombatProblem;

import java.util.Arrays;

/**
 * Created by Samuel Roberts
 * on 08/02/12
 */
public class CMAHandler implements IStrategy {

    public SpaceshipCombatProblem problem;
    public CMAEvolutionStrategy cma;
    private int funcEvals;
    private int runIndex;

    private double[] fitness;


    public CMAHandler(SpaceshipCombatProblem problem, int runIndex) {
        this.problem = problem;
        this.runIndex = runIndex;
        init();
    }

    public void init() {
        cma = new CMAEvolutionStrategy();

        cma.setDimension(problem.nDim()); // overwrite some loaded properties
        //cma.setInitialX(0.0); // in each dimension, also setTypicalX can be used
        cma.setInitialStandardDeviation(Constants.startingStdDev); // also a mandatory setting
        // testing handmade seed!
//        double[] testShip = {-16.0, -14.0, 0,
//                              0.0,  0.0, (3*Math.PI)/2,
//                              16.0, -14.0, Math.PI,
//                              10.0,  10.0, (3*Math.PI)/2,
//                             -10.0,  10.0, (3*Math.PI)/2};
//        double[] testShip = {0.0, -18.0, Math.PI/2,
//                             10.0, 0.0, (5*Math.PI)/4,
//                             4.0,  12.0, (3*Math.PI)/2,
//                            -4.0,  12.0, (3*Math.PI)/2,
//                            -10.0, 0.0, (7*Math.PI)/4};
        cma.setInitialX(0.0);

        cma.options.stopMaxFunEvals = Constants.numEvals;
        cma.options.stopFitness = -100000;

        // initialize cma and get fitness array to fill in later
        fitness = cma.init();  // new double[cma.parameters.getPopulationSize()];

        funcEvals = 0;
    }

    public void run() {
        // iteration loop
        if (!hasCompleted()) {

            // --- core iteration step ---
            double[][] pop = cma.samplePopulation(); // get a new population of solutions



            // calculate mean for graphing
            double mean = 0;

            for (int i = 0; i < pop.length; ++i) {    // for each candidate solution i
                // compute fitness/objective value
                funcEvals++;
                double fitnessValue = problem.fitness(pop[i]); // fitness is to be minimized
                fitness[i] = fitnessValue;
                mean += fitnessValue;
            }
            cma.updateDistribution(fitness);         // pass fitness array to update search distribution
            // --- end core iteration step ---

            cma.writeToDefaultFiles();

            mean /= pop.length;
            try {
                Grapher.writeGenData(runIndex, (int) cma.getCountIter(), cma.getBestRecentFunctionValue(), mean, cma.getBestRecentX());
            } catch (Exception e) {
                System.out.println("Unsuccessful attempt to write graphing data! : " + e.getMessage());
            }

            int outmod = 20;
            if (cma.getCountIter() % (15 * outmod) == 1)
                cma.printlnAnnotation(); // might write file as well
            if (cma.getCountIter() % outmod == 1)
                cma.println();
        }

    }

    public void finish() {
        // final output
        cma.writeToDefaultFiles(1);
        cma.println();
        cma.println("Terminated due to");
        for (String s : cma.stopConditions.getMessages())
            cma.println("  " + s);
        cma.println("best function value " + cma.getBestFunctionValue()
                + " at evaluation " + cma.getBestEvaluationNumber());
        cma.println("Population size: " + cma.population.length);
        cma.println("Best solution: " + Arrays.toString(cma.getBestSolution().getX()));
    }

    public double[][] getPopulation() {
        return cma.population;
    }

    public boolean hasCompleted() {
        return cma.stopConditions.getNumber() != 0;
    }

    public long getIterations() {
        return cma.getCountIter();
    }

    public int getFuncEvals() {
        return funcEvals;
    }

}
