package main;

import common.RunParameters;
import problem.IProblem;
import problem.PredatorPreyProblem;
import strategy.CMAHandler;
import strategy.IStrategy;

import java.io.*;
import java.util.*;

public class AutomatedRunner {

    static Set<Runner> activeRuns;

    public static void main(String[] args) throws Exception {
        activeRuns = new HashSet<Runner>();
        doRuns(RunParameters.numTrials);
    }

    public static void doRuns(int numTrials) throws Exception {
        RunParameters.ShipController[] controllerEnumArray = RunParameters.ShipController.values();
        int totalControllers = controllerEnumArray.length;
        RunParameters.ShipController currentController;

        RunParameters.RunParameterEnums[] runParameterArray = RunParameters.RunParameterEnums.values();
        int totalParameters = runParameterArray.length;
        RunParameters.RunParameterEnums currentVariable;

        long startingTime = System.currentTimeMillis();

        for(int i=0; i<totalControllers; ++i) {
            // Change to specific controller
            currentController = controllerEnumArray[i];
            System.out.println("- Switching to controller " + currentController);

            for(int j=0; j<totalParameters; j++) {
                currentVariable = runParameterArray[j];
                System.out.println("-- Looking at " + currentVariable);

                for(int k=0; k<currentVariable.getNumValues(); k++) {
                    System.out.println("--- Using index " + k);

                    for(int l=0; l<numTrials; l++) {
                        System.out.println("---- Trial " + l + "/" + numTrials);
                        Runner r = startNewRun(getNextRunIndex(), currentController, currentVariable, k);
                        activeRuns.add(r);

                        // don't allow more than two concurrent runs
                        // wait for an existing run to finish
                        if(activeRuns.size() >= 2) {
                            boolean waitingForVacancy = true;
                            while(waitingForVacancy) {
                                Set<Runner> livingRuns = new HashSet<Runner>();
                                for(Runner a : activeRuns) {
                                    if(a.isRunning()) {
                                        livingRuns.add(a);
                                    }
                                }
                                activeRuns = livingRuns;
                                waitingForVacancy = activeRuns.size() < 2;
                                Thread.sleep(10000);
                            }
                        }
                    }
                }

            }
        }
        System.out.println("Everything complete, hopefully. Total time elapsed (ms): " + (System.currentTimeMillis() - startingTime));
    }

    public static Runner startNewRun(int runIndex, RunParameters.ShipController shipController, RunParameters.RunParameterEnums runParameter, int runParameterIndex) throws IOException {
        // set up values
        RunParameters.setParameter(runParameter, runParameterIndex);

        // log a file to indicate the run data being tested
        String directoryName = "data/run-" + runIndex;
        new File(directoryName).mkdir();
        PrintWriter pw = new PrintWriter(new FileWriter(directoryName + "/param.txt"));
        pw.println("Run " + runIndex + " stats:\n----------\n");
        pw.println("Controller: " + shipController);
        pw.println("Parameter adjusted: " + runParameter);
        pw.println("Variables being used:\n\n" + RunParameters.outputValues());
        pw.close();

        // go!
        Runner r = new Runner(runIndex);
        Thread t = new Thread(r);
        t.start();
        return r;
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

