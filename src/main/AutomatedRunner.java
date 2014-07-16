package main;

import common.Constants;
import common.RunParameters;

import java.io.*;
import java.util.*;

public class AutomatedRunner {

    static Set<Runner> activeRuns;

    public static void main(String[] args) throws Exception {
        RunParameters.ShipController currentController = RunParameters.ShipController.GREEDY_SEARCH;
        if(args.length > 0) {
             currentController = RunParameters.ShipController.valueOf(args[0]);
        }

        activeRuns = new HashSet<Runner>();
        doRuns(RunParameters.numTrials, currentController);
    }

    public static void doRuns(int numTrials, RunParameters.ShipController currentController) throws Exception {
        RunParameters.RunParameterEnums[] runParameterArray = RunParameters.RunParameterEnums.values();
        int totalParameters = runParameterArray.length;
        RunParameters.RunParameterEnums currentVariable;

        RunParameters.runShipController = currentController;
        long startingTime = System.currentTimeMillis();
        System.out.println("Starting " + currentController + " experiment");
        for(int i=0; i<totalParameters; i++) {
            currentVariable = runParameterArray[i];
            System.out.println("-- Looking at " + currentVariable);

            for(int j=0; j<currentVariable.getNumValues(); j++) {
                System.out.println("--- Using index " + j);

                for(int k=0; k<numTrials; k++) {
                    System.out.println("---- Trial " + (k+1) + "/" + numTrials);
                    Runner r = startNewRun(getNextRunIndex(currentController), currentController, currentVariable, j);
                    while(r.isRunning()) {
                        Thread.sleep(100);
                    }
//                        activeRuns.add(r);
//
//                        // don't allow more than two concurrent runs
//                        // wait for an existing run to finish
//                        if(activeRuns.size() >= 1) {
//                            boolean waitingForVacancy = true;
//                            while(waitingForVacancy) {
//                                Set<ProblemRunner> livingRuns = new HashSet<ProblemRunner>();
//                                for(ProblemRunner a : activeRuns) {
//                                    if(a.isRunning()) {
//                                        livingRuns.add(a);
//                                    }
//                                }
//                                activeRuns = livingRuns;
//                                waitingForVacancy = activeRuns.size() < 2;
//                                Thread.sleep(10000);
//                            }
//                        }
                }
            }

        }
        System.out.println("Everything complete, hopefully. Total time elapsed (ms): " + (System.currentTimeMillis() - startingTime));
    }

    public static Runner startNewRun(int runIndex, RunParameters.ShipController shipController, RunParameters.RunParameterEnums runParameter, int runParameterIndex) throws IOException {
        // set up values
        RunParameters.setParameter(runParameter, runParameterIndex);

        // do not run if default parameters
        // check to see these are a unique set of parameters
        if(RunParameters.usingDefaultParameters) {
            System.out.println("Skipping default parameter run.");
            return new NullRunner();
        } else {
            // log a file to indicate the run data being tested
            String directoryName = "data/" + shipController.toString().toLowerCase() + "/run-" + runIndex;
            boolean madeDirectory = new File(directoryName).mkdirs();
            if(madeDirectory) {
                PrintWriter pw = new PrintWriter(new FileWriter(directoryName + "/param.txt"));

                pw.println("Run " + runIndex + " stats:\n----------\n");
                pw.println("Controller: " + shipController);
                pw.println("Parameter adjusted: " + runParameter);
                pw.println("Variables being used:\n\n" + RunParameters.outputValues());
                pw.println();
                pw.println("MC/MCTS iterations: " + Constants.nIts);
                pw.println("Function evaluations: " + Constants.numEvals);
                pw.println("Max timesteps per sim: " + Constants.timesteps);
                pw.close();

                // go!
                Runner r = new ProblemRunner(runIndex, "data/" + shipController.toString().toLowerCase());
                Thread t = new Thread(r);
                t.start();
                return r;
            } else {
                System.out.println("Couldn't make directory.");
                return new NullRunner();
            }
        }
    }

    public static int getNextRunIndex(RunParameters.ShipController shipController) {
        File dataDirectory = new File("data/" + shipController.toString().toLowerCase() + "/");
        File directories[] = dataDirectory.listFiles();
        int highestRunNum = 0;
        if(directories == null || directories.length == 0) {
            // no folders present, the highest run num remains 0
            highestRunNum = 0;
        } else {
            // folders present, find latest run folder
            for(File dir : directories) {
                String dirName = dir.getName();
                if(dirName.substring(0, 3).equals("run")) {
                    // get run number of run directory
                    String dirNumPart = dirName.substring(4);
                    int dirNum = 0;
                    try {
                        dirNum = Integer.parseInt(dirNumPart);
                        if(dirNum > highestRunNum) highestRunNum = dirNum;
                    } catch(NumberFormatException e) {
                        // do nothing, just skip
                    }
                }
            }
        }
        return highestRunNum + 1;
    }

}

