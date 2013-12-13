package main;

import common.Constants;
import common.utilities.JEasyFrame;
import controller.mcts.ShipBiasedMCTSController;
import problem.SpaceshipIndividualCombatProblem;

import java.util.Arrays;

/**
 * Created by Samuel Roberts, 2013
 */
public class TestDesigns {

    static int duplicates = 1;

    public static void main(String[] args) {
        SpaceshipIndividualCombatProblem problem = new SpaceshipIndividualCombatProblem();

//        double[] shipDesignA =
//                {0, 0, 0, // position and rotation
//                        100, 100, 100, 100, // weights, default (weights scale down by 100)
//                        1, 0.0, -200.0, 10* Math.PI/2,	// thruster (components scale down by 10)
//                        1, 200.0, 0.0, 10*(3*Math.PI)/4, // thruster (components scale down by 10)
//                        1, 80.0,  120.0, 10*(3*Math.PI)/2, // turret (components scale down by 10)
//                        1, -80.0,  120.0, 10*(3*Math.PI)/2, // turret (components scale down by 10)
//                        1, -200.0, 0.0, 10*(1*Math.PI)/4};  // thruster (components scale down by 10)
//        double[] shipDesignB =
//                {0, 0, 0, // position and rotation (totally irrelevant here)
//                        100, 100, 100, 300, // weights (totally irrelevant here)
//                        -1, 0.0, -200.0, 10*(3*Math.PI/2),
//                        1, 200.0, 0.0, 10*(5*Math.PI/4),
//                        1, 100.0,  100.0, 10*(3*Math.PI/2),
//                        1, -100.0,  100.0, 10*(3*Math.PI/2),
//                        1, -200.0, 0.0, 10*(7*Math.PI/4)};
//        double[] shipDesignC =
//                {0, 0, 0, // position and rotation (totally irrelevant here)
//                        100, 100, 100, 300, // weights (totally irrelevant here)
//                        1, -200.0, 0.0, 0,
//                        1, 0.0,  -200.0, 10*(Math.PI/2),
//                        1, 200.0,  0.0, 10*(Math.PI),
//                        1, 0.0, 200.0, 10*(3*Math.PI/2)};
//
//        double[][] pop = new double[duplicates][];
//
//        for(int i=0; i<duplicates; i++) {
//            pop[i] = Arrays.copyOf(shipDesignA, problem.nDim());
//            // scatter ships
//            pop[i][0] += (i - duplicates/2) * 50;
//            pop[i][1] += (i - duplicates/2) * 50;
//        }

        SpaceshipVisualiser sv = new SpaceshipVisualiser(problem);
        JEasyFrame frame = new JEasyFrame(sv, "Pickup Problem - 0");


        // print out the best fitness
        //problem.runCombat(pop);
        //problem.printBestScoreOfRecentSim();
//        double bestFitness = Double.MAX_VALUE;
//        for(int i=0; i<duplicates; i++) {
//            double fitness = problem.fitness(pop[i]);
//            System.out.println("Fitness: " + fitness);
//            if(fitness < bestFitness) bestFitness = fitness;
//        }
//        System.out.println("Best Fitness: " + bestFitness);


        //problem.demonstrationInit(pop);
        // parameterless version just creates a basic lunar lander style ship
        problem.demonstrationInit();
        ShipBiasedMCTSController cont = (ShipBiasedMCTSController)problem.getControllers().get(0);


        // MAIN DEMONSTRATION LOOP
        try {
            while(true) {
                problem.demonstrate();
                sv.repaint();
                frame.setTitle("Pickup Problem - " + cont.bestPredictedScore);
                Thread.sleep(Constants.delay);
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }

        frame.dispose();
    }
}
