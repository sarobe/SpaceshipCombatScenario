package main;

import common.RunParameters;
import common.utilities.StatSummary;
import problem.PredatorPreySpecialistCoevolutionProblem;

import java.io.*;
import java.util.*;

/**
 * Created by Samuel on 26/09/2015.
 */
public class BestShipComparer {

    public static void main(String[] args) throws Exception {

        double[] testDesign = {-102.73895913061146, 80.4536813822079, -190.07631854955284, 231.57978872973436, 116.16377119592839, 103.60301850054971, -47.789662132528015, 371.93723206898363, 91.64608832372998, -192.0701546302887, -94.52368101354523, -111.57414108483144, -192.9436604478859, 95.13536432843065, 160.19910123273706, 82.18220358588046, -113.59052059010384, -93.76034965421113, -47.5125979450776, 153.79769135748361, -96.1662443577818, 109.49010072382015, 61.34042244527505, 267.0740614751983, -14.162956707088668, -62.719014029134215, -64.72226275659085, 143.1808982044669, 73.14550087853179};


        String basePath = "data/fixedPredator";
        String outputFileName = "results.txt";

        Map<RunParameters.ShipController, String> controllerPathMap = new HashMap<RunParameters.ShipController, String>();
        controllerPathMap.put(RunParameters.ShipController.CONDITION_ACTION, "condition_action");
        controllerPathMap.put(RunParameters.ShipController.FLAT_MC, "flat_mc");
        controllerPathMap.put(RunParameters.ShipController.GREEDY_SEARCH, "greedy_search");
        controllerPathMap.put(RunParameters.ShipController.MCTS, "mcts");
        RunParameters.ShipController[] allControllers = RunParameters.ShipController.values().clone();

        BufferedReader reader;
        PrintWriter pw;

        // take a controller
        for(int i=0; i<allControllers.length; i++) {
            RunParameters.ShipController currentController = allControllers[i];
            String relevantPath = basePath + "/" + controllerPathMap.get(currentController);
            pw = new PrintWriter(new FileWriter(relevantPath + "/" + outputFileName));

            // determine the other controllers to test against
            List<RunParameters.ShipController> otherControllers = new ArrayList<RunParameters.ShipController>();
            for(RunParameters.ShipController sc : allControllers) {
                if(sc != currentController) otherControllers.add(sc);
            }

            // create a statsummary for each one keyed to the controller
            Map<RunParameters.ShipController, StatSummary> comparisonMapStart = new TreeMap<RunParameters.ShipController, StatSummary>();
            Map<RunParameters.ShipController, StatSummary> comparisonMapEnd = new TreeMap<RunParameters.ShipController, StatSummary>();
            for(RunParameters.ShipController sc : otherControllers) {
                comparisonMapStart.put(sc, new StatSummary());
                comparisonMapEnd.put(sc, new StatSummary());
            }


            // fish up the best ships of the start of each run found for this controller
            File dataDirectory = new File(relevantPath);
            File directories[] = dataDirectory.listFiles();
            if(directories.length > 0) {
                for(File runDir : directories) {
                    String dirName = runDir.getName();
                    if(dirName.substring(0, 3).equals("run")) {
                        // a run directory
                        String dirNumPart = dirName.substring(4);
                        try {
                            reader = new BufferedReader(new FileReader(runDir + "/best-ship.txt"));
                            String line;
                            // get the best ship AT THE START
                            // get the best ship AT THE END
                            String bestShipStart = "";
                            String bestShipEnd = "";
                            while((line = reader.readLine()) != null) {
                                String[] tokens = line.split("\t");
                                String shipDesign = tokens[2];
                                if(bestShipStart.isEmpty()) bestShipStart = shipDesign;
                                bestShipEnd = shipDesign;
                            }
                            double[] bestShipStartChromosome = arrayStringToDoubleArray(bestShipStart);
                            double[] bestShipEndChromosome = arrayStringToDoubleArray(bestShipEnd);

                            // for each other controller not the current controller
                            for(RunParameters.ShipController otherCont : otherControllers) {
                                double resultStart = testShip(currentController, bestShipStartChromosome, otherCont, testDesign);
                                StatSummary startStats = comparisonMapStart.get(otherCont);
                                startStats.add(resultStart);
                                double resultEnd = testShip(currentController, bestShipEndChromosome, otherCont, testDesign);
                                StatSummary endStats = comparisonMapEnd.get(otherCont);
                                endStats.add(resultEnd);
                            }
                            System.out.println("Tested " + currentController + ", run " + dirNumPart);
                            reader.close();
                        } catch(IOException e) {
                            System.err.println("Couldn't load a best ship file, skipping run " + dirNumPart);
                        }
                    }
                }
            }

            // this controller has been tested against every other controller with every ship available
            for(RunParameters.ShipController otherCont : otherControllers) {
                StatSummary startStats = comparisonMapStart.get(otherCont);
                StatSummary endStats = comparisonMapEnd.get(otherCont);
                System.out.println("(START)" + currentController + " vs " + otherCont + " - Mean: " + startStats.mean() + " Std Err: " + startStats.stdErr());
                System.out.println("(END)" + currentController + " vs " + otherCont + " - Mean: " + endStats.mean() + " Std Err: " + endStats.stdErr());
            }
        }




    }

    private static double testShip(RunParameters.ShipController predatorControllerType, double[] predatorChromosome, RunParameters.ShipController preyControllerType, double[] preyChromosome) {
        PredatorPreySpecialistCoevolutionProblem problem = new PredatorPreySpecialistCoevolutionProblem();

        double[][] shipData = new double[2][];
        shipData[0] = predatorChromosome;
        shipData[1] = preyChromosome;

        problem.preFitnessSim(shipData);
        problem.competePopulations(predatorChromosome, preyChromosome, predatorControllerType, preyControllerType);
        double fitness = problem.fitness(predatorChromosome);
        return fitness;
    }


    private static double[] arrayStringToDoubleArray(String arrayString) {
        arrayString = arrayString.replaceAll("[\\[\\]]", "");
        String[] tokens = arrayString.split(", ");
        double[] dba = new double[tokens.length];
        for(int i=0; i<tokens.length; i++) {
            dba[i] = Double.parseDouble(tokens[i]);
        }
        return dba;
    }
}
