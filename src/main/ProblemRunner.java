package main;

import common.RunParameters;
import strategy.*;
import common.Constants;
import common.utilities.JEasyFrame;
import problem.*;

import java.io.File;

/**
 * Created by Samuel Roberts
 * on 09/02/12
 */
public class ProblemRunner extends Runner {

    IProblem problem;
    IStrategy handler;
    boolean runDemo;
    boolean showDemos;
    int runIndex;


    public static void main(String[] args) {

//        final int repeats = 1;
        final int startingIndex = getNextRunIndex();

//        for(int i=0; i<repeats; ++i) {
            ProblemRunner r = new ProblemRunner(startingIndex, "data"); //+ i
            Thread t = new Thread(r);
            t.start();
//        }
    }
    
    public ProblemRunner(int runIndex, String logDirectory) {
        super();
        this.runIndex = runIndex;

        problem = RunParameters.getAppropriateProblem(RunParameters.problem);
        handler = new CMAHandler(problem, runIndex, logDirectory);


        showDemos = false;
        isStarted = false;
    }

    public void run() {
        isStarted = true;
        int demonstrationInterval = 100;//(int)Math.floor(Constants.numEvals / 50);

//        if(showDemos) demonstrate();
        while(!handler.hasCompleted()) {
            problem.preFitnessSim(handler.getPopulation());
            handler.run();
//            if((handler.getIterations()% demonstrationInterval == 2) && showDemos) {  //% demonstrationInterval
//                demonstrate();
//            }
        }
        handler.finish();
        System.out.println("Function evaluations: Predator - " + handler.getFuncEvals());
        isRunning = false;
        // show a graph after finishing
        //Grapher.drawGraph(runIndex);

        //demonstrate();
    }



    public void demonstrate() {
        double[][] pop = handler.getPopulation();

        // set up graphical elements
        SpaceshipVisualiser sv = new SpaceshipVisualiser(problem);
        JEasyFrame frame = new JEasyFrame(sv, "Demonstration at Iteration " + handler.getIterations());
        frame.addKeyListener(new KeyHandler(this));

        problem.demonstrationInit(pop);

        runDemo = true;
        // MAIN DEMONSTRATION LOOP
        try {
            while(runDemo) {
                problem.demonstrate();
                sv.repaint();
                Thread.sleep(Constants.delay);
                if(problem.hasEnded()) runDemo = false;
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }

        frame.dispose();
    }



    public static int getNextRunIndex() {
        File dataDirectory = new File("data/");
        File directories[] = dataDirectory.listFiles();
        int highestRunNum = 0;
        for(File dir : directories) {
            String dirNumPart = dir.getName().substring(4);
            int dirNum = 0;
            try {
                dirNum = Integer.parseInt(dirNumPart);
                if(dirNum > highestRunNum) highestRunNum = dirNum;
            } catch(NumberFormatException e) {
                // do nothing, just skip
            }
        }
        int startingIndex = highestRunNum + 1;
        return startingIndex;
    }
}
