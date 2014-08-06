package main;

import common.Constants;
import common.utilities.JEasyFrame;
import problem.IProblem;
import problem.ITwoPopProblem;
import problem.PredatorPreySpecialistCoevolutionProblem;
import strategy.CMAHandler;
import strategy.IStrategy;

import java.io.File;

/**
 * Created by Samuel Roberts
 * on 09/02/12
 */
public class TwoPopProblemRunner extends Runner {

    ITwoPopProblem problem;
    IStrategy popAHandler;
    IStrategy popBHandler;
    boolean runDemo;
    boolean showDemos;
    int runIndex;


//    public static void main(String[] args) {
//
////        final int repeats = 1;
//        final int startingIndex = getNextRunIndex();
//
////        for(int i=0; i<repeats; ++i) {
//            TwoPopProblemRunner r = new TwoPopProblemRunner(startingIndex, "data"); //+ i
//            Thread t = new Thread(r);
//            t.start();
////        }
//    }

    public TwoPopProblemRunner(int runIndex, String logDirectory) {
        super();
        this.runIndex = runIndex;

        //problem = new SpaceshipCombatProblem();
        //problem = new PredatorPreyProblem();
        //problem = new PredatorPreyCoevolutionProblem();
        //handler = new CMAHandler(problem, runIndex, logDirectory);

        problem = new PredatorPreySpecialistCoevolutionProblem();
        popAHandler = new CMAHandler(problem, runIndex, logDirectory);
        popBHandler = new CMAHandler(problem, runIndex+1, logDirectory);


        showDemos = false;
        isStarted = false;
    }

    public void run() {
        isStarted = true;

        while(!popAHandler.hasCompleted() && !popBHandler.hasCompleted()) {
            problem.competePopulations(popAHandler.getPopulation(), popBHandler.getPopulation());
            popAHandler.run();
            popBHandler.run();
        }
        popAHandler.finish();
        popBHandler.finish();
        System.out.println("Function evaluations: PopA - " + popAHandler.getFuncEvals() + " PopB - " + popBHandler.getFuncEvals());
        isRunning = false;
    }
}
