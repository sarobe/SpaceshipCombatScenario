package main;

import gnuplot.Grapher;
import controller.*;
import strategy.*;
import common.Constants;
import common.utilities.JEasyFrame;
import problem.*;
import spaceship.Spaceship;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Samuel Roberts
 * on 09/02/12
 */
public class Runner implements Runnable {

    SpaceshipCombatProblem problem;
    IStrategy handler;
    boolean runDemo;
    boolean showDemos;
    int runIndex;

    boolean shipsOnLeft = true;

    public static void main(String[] args) {

//        final int repeats = 1;
        final int startingIndex = 0;

//        for(int i=0; i<repeats; ++i) {
            Runner r = new Runner(startingIndex); //+ i
            Thread t = new Thread(r);
            t.start();
//        }
    }
    
    public Runner(int runIndex) {
        this.runIndex = runIndex;

        problem = new SpaceshipCombatProblem();
        handler = new CMAHandler(problem, runIndex);

        showDemos = true;
    }

    public void run() {
        int demonstrationInterval = 20;//(int)Math.floor(Constants.numEvals / 50);

//        if(showDemos) demonstrate();
        while(!handler.hasCompleted()) {
            problem.runCombat(handler.getPopulation());
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
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }

        frame.dispose();
    }
}
