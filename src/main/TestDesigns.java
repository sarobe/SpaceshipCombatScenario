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
        double[] predatorDesign = {-298.3873627270567, -121.12687137187248, -405.26672818649956, 481.790272312112, 21.892102223759508, 594.1387737249404, -746.2835468960384, -158.39809583233364, 1318.6495601739439, 687.5856545899873, 32.47663255662939, 407.5332535924364, 265.4623814809795, 467.96939373042784, 496.84048114485375, -400.87189542636753, 163.67471383016968, -437.8231794773078, 83.69501668708816, -253.55603134801885, 77.35794390489728, 500.7446236853408, 535.2642032564287, 680.8400288682325, -69.69588566179789, -485.1831572938073, 55.885171236826196, 7.725989789181466, -127.88887926034826};
        double[] preyDesign = {336.4878035612419, 257.77801551986875, 654.3046230669203, -636.4596719766332, -406.0499705130945, 71.91027901141888, -656.2999925056665, 368.6743608076719, 77.22501317932222, 530.9702338726001, 182.46619417942338, 207.4334899424334, 26.794549889830556, -169.396319649047, 328.10772517065817, 321.31495161256544, 500.9889207026351, -494.9031602751556, -540.6748764337702, 676.2263037087341, -545.3803307090546, -536.5639893167271, -108.12098692683142, 103.52866339811771, -539.3799439133302, 190.03234292872014, 1005.1694274016462, 163.992025260633, -272.9954590110507};

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
