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

    SpaceshipCombatProblem leftProblem;
    IStrategy leftHandler;
    SpaceshipCombatProblem rightProblem;
    IStrategy rightHandler;
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

        leftProblem = new SpaceshipCombatProblem();
        leftProblem.setShipsOnLeft(true);
        leftHandler = new CMAHandler(leftProblem, runIndex);
        rightProblem = new SpaceshipCombatProblem();
        rightProblem.setShipsOnLeft(false);
        rightHandler = new CMAHandler(rightProblem, runIndex);

        showDemos = true;
    }

    public void run() {
        int demonstrationInterval = 20;//(int)Math.floor(Constants.numEvals / 50);

//        if(showDemos) demonstrate();
        while(!leftHandler.hasCompleted() && !rightHandler.hasCompleted()) {
            leftProblem.constructEnemyShips(rightHandler.getPopulation());
            rightProblem.constructEnemyShips(leftHandler.getPopulation());
            leftHandler.run();
            rightHandler.run();
            if((rightHandler.getIterations()% demonstrationInterval == 2) && showDemos) {  //% demonstrationInterval
                demonstrate();
            }
        }
        leftHandler.finish();
        rightHandler.finish();
        System.out.println("Lefthand Function evaluations: " + leftHandler.getFuncEvals());
        System.out.println("Righthand Function evaluations: " + rightHandler.getFuncEvals());
        // show a graph after finishing
        //Grapher.drawGraph(runIndex);

        demonstrate();
    }

    public void demonstrate() {
        // Create some ships from the population
        List<Spaceship> leftShips = new ArrayList<Spaceship>();
        List<ShipActionController> leftConts = new ArrayList<ShipActionController>();
        List<Spaceship> rightShips = new ArrayList<Spaceship>();
        List<ShipActionController> rightConts = new ArrayList<ShipActionController>();
        double[][] leftPop = leftHandler.getPopulation();
        double[][] rightPop = rightHandler.getPopulation();

        // set up graphical elements
        SpaceshipVisualiser sv = new SpaceshipVisualiser(leftShips, leftProblem);
        JEasyFrame frame = new JEasyFrame(sv, "Demonstration at Iteration " + leftHandler.getIterations());
        frame.addKeyListener(new KeyHandler(this));

        for(int i = 0; i < leftPop.length; i++) {
            double[] s = leftPop[i];
            Spaceship ship = new Spaceship(s);
            leftShips.add(ship);

            ShipActionController sc = new ShipActionController(ship);
            leftConts.add(sc);
        }
        for(int i = 0; i < rightPop.length; i++) {
            double[] s = rightPop[i];
            Spaceship ship = new Spaceship(s);
            rightShips.add(ship);

            ShipActionController sc = new ShipActionController(ship);
            rightConts.add(sc);
        }
        leftProblem.demonstrationInit(leftShips, leftConts, true);

        runDemo = true;
        // MAIN DEMONSTRATION LOOP
        try {
            while(runDemo) {
                leftProblem.demonstrate(leftShips, leftConts);
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
