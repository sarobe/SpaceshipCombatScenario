package problem;

import common.RunParameters;
import common.utilities.Pair;
import controller.Controller;
import spaceship.ComplexSpaceship;

import java.util.*;

public class PredatorPreyCoevolutionProblem extends PredatorPreyProblem {

    public Map<double[], Pair<Integer, Double>> fitnessScores;
    // chromosome = key
    // integer = number of times data is logged
    // double = cumulative score of chromosome

    public PredatorPreyCoevolutionProblem() {
        super();
        fitnessScores = new HashMap<double[], Pair<Integer, Double>>();
    }

    // handle the knockout tournament and simulation here
    public void preFitnessSim(double[][] shipData) {
        List<ComplexSpaceship> ships = new ArrayList<ComplexSpaceship>();
        fitnessScores.clear();

        // reify all ships and initialise score information
        for(double[] shipDatum : shipData) {
            ComplexSpaceship ship = new ComplexSpaceship(shipDatum);
            ships.add(ship);
            Pair<Integer, Double> initData = new Pair<Integer, Double>(0, 0.0);
            fitnessScores.put(ship.chromosome, initData);
        }

        // begin tournament!!
        List<ComplexSpaceship> winners = new ArrayList<ComplexSpaceship>();
        winners.addAll(ships);

        while(winners.size() > 1) {
            List<ComplexSpaceship> remainingWinners = new ArrayList<ComplexSpaceship>();
            for(int i=0; i<winners.size()/2; ++i) {
                ComplexSpaceship shipA = winners.get(i*2);
                ComplexSpaceship shipB = winners.get((i*2) + 1);
                Controller contA = RunParameters.getAppropriateController(RunParameters.runShipController, shipA, shipB, true);
                Controller contB = RunParameters.getAppropriateController(RunParameters.runShipController, shipB, shipA, false);

                // get scores
                Pair<Double, Double> firstContest = runSimulation(shipA, shipB, contA, contB);
                contA.isPredator = false;
                contB.isPredator = true;
                Pair<Double, Double> secondContest = runSimulation(shipB, shipA, contB, contA);

                double shipAScore = (firstContest.first() + secondContest.second()) / 2;
                double shipBScore = (firstContest.second() + secondContest.first()) / 2;

                // store scores
                Pair<Integer, Double> shipAStats = fitnessScores.get(shipA.chromosome);
                Pair<Integer, Double> shipBStats = fitnessScores.get(shipB.chromosome);
                // increment number of trials
                shipAStats.first++;
                shipBStats.first++;
                // add both scores
                shipAStats.second += shipAScore;
                shipBStats.second += shipBScore;

                // add winner to remaining winners
                if(shipAScore > shipBScore) {
                    remainingWinners.add(shipA);
                } else {
                    remainingWinners.add(shipB);
                }
            }
            winners = remainingWinners;
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
