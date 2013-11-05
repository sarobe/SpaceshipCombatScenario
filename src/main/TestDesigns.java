package main;

import common.Constants;
import common.utilities.JEasyFrame;
import problem.SpaceshipCombatProblem;

import java.util.Arrays;

/**
 * Created by Samuel Roberts, 2013
 */
public class TestDesigns {

    static int duplicates = 2;

    public static void main(String[] args) {
        SpaceshipCombatProblem problem = new SpaceshipCombatProblem();

        double[] shipDesignA =
                {0, 0, 0, // position and rotation
                        100, 100, 100, 100, // weights, default (weights scale down by 100)
                        0, 0.0, -200.0, 10* Math.PI/2,	// thruster (components scale down by 10)
                        0, 200.0, 0.0, 10*(3*Math.PI)/4, // thruster (components scale down by 10)
                        1, 80.0,  120.0, 10*(Math.PI)/2, // turret (components scale down by 10)
                        1, -80.0,  120.0, 10*(Math.PI)/2, // turret (components scale down by 10)
                        0, -200.0, 0.0, 10*(1*Math.PI)/4};  // thruster (components scale down by 10)
        double[] shipDesignB =
                {0, 0, 0, // position and rotation (totally irrelevant here)
                        000, 0, 200, 300, // weights (totally irrelevant here)
                        1, 0.0, -200.0, 10*(3*Math.PI/2),
                        0, 200.0, 0.0, 10*(5*Math.PI/4),
                        0, 100.0,  100.0, 10*(3*Math.PI/2),
                        0, -100.0,  100.0, 10*(3*Math.PI/2),
                        0, -200.0, 0.0, 10*(7*Math.PI/4)};

        double[][] pop = new double[duplicates][];

        for(int i=0; i<duplicates; i++) {
            pop[i] = Arrays.copyOf(shipDesignB, shipDesignB.length);
            // scatter ships
            pop[i][0] += (i - duplicates/2) * 50;
            pop[i][1] += (i - duplicates/2) * 50;
        }

        SpaceshipVisualiser sv = new SpaceshipVisualiser(problem);
        JEasyFrame frame = new JEasyFrame(sv, "Demonstration of Handmade Ships");

        // print out the best fitness
        problem.runCombat(pop);
        problem.printBestScoreOfRecentSim();

        problem.demonstrationInit(pop);

        // MAIN DEMONSTRATION LOOP
        try {
            while(true) {
                problem.demonstrate();
                sv.repaint();
                Thread.sleep(Constants.delay);
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }

        frame.dispose();
    }
}
