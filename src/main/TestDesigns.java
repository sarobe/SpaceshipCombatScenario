package main;

import common.Constants;
import common.utilities.JEasyFrame;
import common.utilities.StatSummary;
import controller.StateController;
import problem.IProblem;
import problem.PredatorPreyProblem;

import javax.annotation.processing.FilerException;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.PrintWriter;

/**
 * Created by Samuel Roberts, 2013
 */
public class TestDesigns {

    static int duplicates = 1;
    static int runs = 10;
    static int runNum = 0;

    public static boolean useGraphics = true;
    public static boolean logOutput = true;

    public static void main(String[] args) {
        HumanStateControllerKeyHandler keyHandler = new HumanStateControllerKeyHandler();
        //SpaceshipIndividualCombatProblem problem = new SpaceshipIndividualCombatProblem();

        IProblem problem = new PredatorPreyProblem(keyHandler);

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
        JEasyFrame frame = null;


        if(useGraphics) {
            sv = new SpaceshipVisualiser(problem);
            frame = new JEasyFrame(sv, "Predator Prey Problem - 0");
            frame.addKeyListener(keyHandler);
        }



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

        StateController predatorCont = (StateController)problem.getControllers().get(0);
        StateController preyCont = (StateController)problem.getControllers().get(1);

        StatSummary predStats = new StatSummary("Predator");
        StatSummary preyStats = new StatSummary("Prey");

        PrintWriter pw = null;
        if(logOutput) {
            int logRunNum = 1;//Runner.getNextRunIndex();
            try {
                String directoryName = "data/run-" + logRunNum;
                new File(directoryName).mkdir();
                FileWriter fw = new FileWriter("data/run-" + logRunNum + "/results.txt", false);
                pw = new PrintWriter(fw);
            } catch (Exception e) {
                System.out.println("Tried to set up a logging file but failed.");
                System.out.println("Reason: " + e.getMessage());
            }
        }



        // MAIN DEMONSTRATION LOOP
        try {
            while(runNum < runs) {
                problem.demonstrate();
                if(useGraphics && frame != null) {
                    sv.repaint();
                    frame.setTitle("Pickup Problem - Pred: " + predatorCont.getScore() + " Prey: " + preyCont.getScore());
                    predStats.add(predatorCont.getScore());
                    preyStats.add(preyCont.getScore());
                    Thread.sleep(Constants.delay);
                    // this is an ugly hack
                }
                if(problem.hasEnded()) {
                    runNum++;
                    if(runNum % 50 == 0) log("Completed " + runNum + " runs.", pw);
                    log("Run ended at " + problem.getTimesteps() + " timesteps, predator score: " + predatorCont.getScore() + " prey score: " + preyCont.getScore(), pw);
                    predStats.add(predatorCont.getScore());
                    preyStats.add(preyCont.getScore());
                    // reset problem
                    problem.demonstrationInit();
                    // get references to new controller instance
                    predatorCont = (StateController)problem.getControllers().get(0);
                    preyCont = (StateController)problem.getControllers().get(1);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }

        log(predStats + "", pw);
        log(preyStats + "", pw);
        log("End of run.", pw);

        if(pw != null) {
            pw.close();
        }
        if(useGraphics && frame != null) frame.dispose();

    }

    private static void log(String output, PrintWriter printer) {
        System.out.println(output);
        if(printer != null) {
            printer.println(output);
            printer.flush();
        }
    }
}
