package main;

import common.Constants;
import common.RunParameters;
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


//

        // using specialised ships?
        boolean useSpecialist = true;

        // decide which controller to test
        RunParameters.runShipController = RunParameters.ShipController.MCTS;

        // Interesting co-evolved ship using MCTS, small and agile
        double[] shipDesign = {-102.73895913061146, 80.4536813822079, -190.07631854955284, 231.57978872973436, 116.16377119592839, 103.60301850054971, -47.789662132528015, 371.93723206898363, 91.64608832372998, -192.0701546302887, -94.52368101354523, -111.57414108483144, -192.9436604478859, 95.13536432843065, 160.19910123273706, 82.18220358588046, -113.59052059010384, -93.76034965421113, -47.5125979450776, 153.79769135748361, -96.1662443577818, 109.49010072382015, 61.34042244527505, 267.0740614751983, -14.162956707088668, -62.719014029134215, -64.72226275659085, 143.1808982044669, 73.14550087853179};

        // Example specialised preys and predators.
        double[] predatorDesign = {171.9495775558117, -101.69394949165228, 220.41910914547364, 68.94109357555682, -152.73079739282733, 357.3218611086253, 93.40229069029732, -580.8422295687734, 78.4311679496114, 216.48004454589034, 263.8836278861769, -254.40227949879454, -336.0904634358397, 175.48201676230886, 5.955158814100241, -219.37856641952845, 30.186953088982307, -63.245083804228926, -3.428597076345169, -210.3448257828987, 171.3019464653164, 308.8817679440293, 204.63502304332906, -79.23239174535863, 112.74639716549008, -44.021619643238154, -143.9813269069071, -147.91989968690163, -84.78936067592183};
        double[] preyDesign = {-22.90938635696061, 61.46622688664986, -127.54774979874995, -324.10354273381256, 609.035279069258, -141.78261751566305, 642.9819169710229, -138.16574553693937, 59.87801027880162, -236.9100195775502, 224.25134553594995, 136.05275271941315, -14.676673349023254, -4.863611360833534, -726.1192609807295, 247.9268007972464, -711.7510907791566, 346.9755698445058, -171.30039863343148, 227.35768941185745, -242.531198694576, 277.3174732884404, -617.1772150744822, 38.36737416705347, -153.51999650710928, 643.5682823976383, -30.609040881574213, -128.99897819102588, 138.3678789462701};

        int popSize = useSpecialist ? 2 : 1;
        double[][] pop = new double[popSize][];

        if(useSpecialist) {
            pop[0] = Arrays.copyOf(predatorDesign, problem.nDim());
            pop[1] = Arrays.copyOf(preyDesign, problem.nDim());
        } else {
            pop[0] = Arrays.copyOf(shipDesign, problem.nDim());
        }

//        for(int i=0; i<duplicates; i++) {
//            pop[i] = Arrays.copyOf(shipDesign, problem.nDim());
//            // scatter ships
////            pop[i][0] += (i - duplicates/2) * 50;
////            pop[i][1] += (i - duplicates/2) * 50;
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


        problem.demonstrationInit(pop, useSpecialist);
        // parameterless version just creates a basic lunar lander style ship
//        problem.demonstrationInit();

        Controller predatorCont = problem.getControllers().get(0);
        Controller preyCont = problem.getControllers().get(1);

        StatSummary predStats = new StatSummary("Predator");
        StatSummary preyStats = new StatSummary("Prey");

        PrintWriter pw = null;
        if(logOutput) {
            int logRunNum = 1;//ProblemRunner.getNextRunIndex();
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
                    Thread.sleep(5000);
                    // reset problem
                    problem.demonstrationInit(pop, useSpecialist);
//                    problem.demonstrationInit();
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
