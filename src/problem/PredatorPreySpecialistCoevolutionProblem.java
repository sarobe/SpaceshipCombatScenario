package problem;

import common.RunParameters;
import common.utilities.Pair;
import controller.Controller;
import spaceship.ComplexSpaceship;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PredatorPreySpecialistCoevolutionProblem extends PredatorPreyProblem implements ITwoPopProblem {

    public Map<double[], Pair<Integer, Double>> fitnessScores;
    // chromosome = key
    // integer = number of times data is logged
    // double = cumulative score of chromosome

    public PredatorPreySpecialistCoevolutionProblem() {
        super();
        fitnessScores = new HashMap<double[], Pair<Integer, Double>>();
    }

    // much like preFitnessSim in the other class, except takes two populations
    public void competePopulations(double[][] predatorData, double[][] preyData) {
        fitnessScores.clear();
        List<ComplexSpaceship> predatorShips = new ArrayList<ComplexSpaceship>();
        List<ComplexSpaceship> preyShips = new ArrayList<ComplexSpaceship>();

        // reify all ships etc etc
        for(double[] shipDatum : predatorData) {
            ComplexSpaceship ship = new ComplexSpaceship(shipDatum);
            predatorShips.add(ship);
            Pair<Integer, Double> initData = new Pair<Integer, Double>(0, 0.0);
            fitnessScores.put(ship.chromosome, initData);
        }
        for(double[] shipDatum : preyData) {
            ComplexSpaceship ship = new ComplexSpaceship(shipDatum);
            preyShips.add(ship);
            Pair<Integer, Double> initData = new Pair<Integer, Double>(0, 0.0);
            fitnessScores.put(ship.chromosome, initData);
        }

        // begin tournament!!
        List<ComplexSpaceship> winnerPredators = new ArrayList<ComplexSpaceship>();
        List<ComplexSpaceship> winnerPreys = new ArrayList<ComplexSpaceship>();
        winnerPredators.addAll(predatorShips);
        winnerPreys.addAll(preyShips);

        while(winnerPredators.size() >= 1 && winnerPreys.size() >= 1) {
            List<ComplexSpaceship> remainingPredators = new ArrayList<ComplexSpaceship>();
            List<ComplexSpaceship> remainingPreys = new ArrayList<ComplexSpaceship>();

            // determine the number of trials
            int numOfTrials = Math.min(winnerPredators.size(), winnerPreys.size());
            for(int i=0; i<numOfTrials; ++i) {



                ComplexSpaceship predatorShip = winnerPredators.get(i);
                ComplexSpaceship preyShip = winnerPreys.get(i);
                Controller predatorCont = RunParameters.getAppropriateController(RunParameters.runShipController, predatorShip, preyShip, true);
                Controller preyCont = RunParameters.getAppropriateController(RunParameters.runShipController, preyShip, predatorShip, false);

                // get scores
                Pair<Double, Double> contest = runSimulation(predatorShip, preyShip, predatorCont, preyCont);

                double predatorScore = contest.first();
                double preyScore = contest.second();

                // store scores
                Pair<Integer, Double> predatorStats = fitnessScores.get(predatorShip.chromosome);
                Pair<Integer, Double> preyStats = fitnessScores.get(preyShip.chromosome);
                // increment number of trials
                predatorStats.first++;
                preyStats.first++;
                // add both scores
                predatorStats.second += predatorScore;
                preyStats.second += preyScore;

                // add winner to remaining winners
                if(predatorScore > preyScore) {
                    remainingPredators.add(predatorShip);
                } else {
                    remainingPreys.add(preyShip);
                }


            }

            winnerPredators = remainingPredators;
            winnerPreys = remainingPreys;
        }
    }

    public double fitness(double[] x) {
        Pair<Integer, Double> scoreData = fitnessScores.get(x);
        // mean of all trials
        double score = scoreData.second() / (double)scoreData.first();

        // Flip for CMA!
        assert(score >= 0);
        assert(score < 1);
        score = 1 - score;

        return score;
    }

}
