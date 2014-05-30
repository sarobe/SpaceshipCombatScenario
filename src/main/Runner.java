package main;

import strategy.*;
import common.Constants;
import common.utilities.JEasyFrame;
import problem.*;

import java.io.File;

/**
 * Created by Samuel Roberts
 * on 09/02/12
 */
public class Runner implements Runnable {

    IProblem problem;
    IStrategy handler;
    boolean runDemo;
    boolean showDemos;
    int runIndex;

    public static void main(String[] args) {

//        final int repeats = 1;
        final int startingIndex = getNextRunIndex();

//        for(int i=0; i<repeats; ++i) {
            Runner r = new Runner(startingIndex); //+ i
            Thread t = new Thread(r);
            t.start();
//        }
    }
    
    public Runner(int runIndex) {
        this.runIndex = runIndex;

        //problem = new SpaceshipCombatProblem();
        problem = new PredatorPreyProblem();
        handler = new CMAHandler(problem, runIndex);

        showDemos = true;
    }

    public void run() {
        int demonstrationInterval = 100;//(int)Math.floor(Constants.numEvals / 50);

//        if(showDemos) demonstrate();
        while(!handler.hasCompleted()) {
            //problem.runCombat(handler.getPopulation());
            handler.run();
            if((handler.getIterations()% demonstrationInterval == 2) && showDemos) {  //% demonstrationInterval
                demonstrate();
            }
        }
        handler.finish();
        System.out.println("Function evaluations: " + handler.getFuncEvals());
        // show a graph after finishing
        //Grapher.drawGraph(runIndex);

        demonstrate();
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
