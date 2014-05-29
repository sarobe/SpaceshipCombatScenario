package main;

import common.Constants;
import common.utilities.JEasyFrame;
import common.utilities.StatSummary;
import controller.Controller;
import controller.statebased.StateController;
import problem.IProblem;
import problem.PredatorPreyProblem;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.Arrays;

/**
 * Created by Samuel Roberts, 2013
 */
public class TestDesigns {

    static int duplicates = 1;
    static int runs = 10;
    static int runNum = 0;

    public static boolean useGraphics = true;
    public static boolean logOutput = false;

    public static void main(String[] args) {
        HumanStateControllerKeyHandler keyHandler = new HumanStateControllerKeyHandler();
        //SpaceshipIndividualCombatProblem problem = new SpaceshipIndividualCombatProblem();

        IProblem problem = new PredatorPreyProblem(keyHandler);

//        double[] shipDesign =
//                {0, 0, 0, // position and rotation
//                        100, 100, 100, 100, // weights, default (weights scale down by 100)
//                        1, 0.0, -200.0, 10* Math.PI/2,	// thruster (components scale down by 10)
//                        1, 200.0, 0.0, 10*(3*Math.PI)/4, // thruster (components scale down by 10)
//                        1, 80.0,  120.0, 10*(3*Math.PI)/2, // turret (components scale down by 10)
//                        1, -80.0,  120.0, 10*(3*Math.PI)/2, // turret (components scale down by 10)
//                        1, -200.0, 0.0, 10*(1*Math.PI)/4};  // thruster (components scale down by 10)
//        double[] shipDesign =
//                {0, 0, 0, // position and rotation (totally irrelevant here)
//                        100, 100, 100, 300, // weights (totally irrelevant here)
//                        1, 0.0, -200.0, 10*(3*Math.PI/2),
//                        1, 200.0, 0.0, 10*(5*Math.PI/4),
//                        1, 100.0,  100.0, 10*(3*Math.PI/2),
//                        1, -100.0,  100.0, 10*(3*Math.PI/2),
//                        1, -200.0, 0.0, 10*(7*Math.PI/4)};
//        double[] shipDesign =
//                {0, 0, 0, // position and rotation (totally irrelevant here)
//                        100, 100, 100, 300, // weights (totally irrelevant here)
//                        1, -200.0, 0.0, 0,
//                        1, 0.0,  -200.0, 10*(Math.PI/2),
//                        1, 200.0,  0.0, 10*(Math.PI),
//                        1, 0.0, 200.0, 10*(3*Math.PI/2)};

        double[] shipDesign = {1714.376246407137, 440.7681690487486, 24.120106750927576, 773.6988284118912, -93.95104959123547, -398.41055735086917, -1276.5776667396824, -676.1199940584609, 183.62343342877057, -233.38613199244963, 1620.4486537381783, -980.3022456262279, -256.4972674902297, -262.8090045089018, 232.67707343868787, -1284.6309713210167, -64.75315767407336, 285.35007520532974, 2333.199396408405, -442.6466684898229, -209.36378762287922, 280.05972291773594, 172.18916807552074, 2228.248293546724, 194.14629734245344, 318.0819331787881, -1338.78131547564};
//
        double[][] pop = new double[duplicates][];

        for(int i=0; i<duplicates; i++) {
            pop[i] = Arrays.copyOf(shipDesign, problem.nDim());
            // scatter ships
            pop[i][0] += (i - duplicates/2) * 50;
            pop[i][1] += (i - duplicates/2) * 50;
        }



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


//        problem.demonstrationInit(pop);
        // parameterless version just creates a basic lunar lander style ship
        problem.demonstrationInit();

        Controller predatorCont = problem.getControllers().get(0);
        Controller preyCont = problem.getControllers().get(1);

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
//                    problem.demonstrationInit(pop);
                    problem.demonstrationInit();
                    // get references to new controller instance
                    predatorCont = problem.getControllers().get(0);
                    preyCont = problem.getControllers().get(1);
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
