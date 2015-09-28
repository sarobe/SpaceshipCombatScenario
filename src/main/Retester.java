package main;

import common.RunParameters;
import common.utilities.JEasyFrame;
import gnuplot.Grapher;
import problem.IProblem;
import problem.PredatorPreyCoevolutionProblem;
import problem.PredatorPreySpecialistCoevolutionProblem;

import javax.swing.*;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Retester {

    public static void main(String[] args) throws IOException {
        // specify where input path is
        String inputPath = "../AUTOMATEDRUN/completearchive/";
        // specify where output path is
        String outputPath = "data/retest/";


        // TODO: SUPPORT MULTIPLE DIRECTORIES
        // specify problem directory
        String problemDirectory = "coevolution";
        // specify controller directory
        String controllerDirectory = "MCTS";
        RunParameters.runShipController = RunParameters.ShipController.MCTS;
        IProblem problem = new PredatorPreyCoevolutionProblem();

        boolean isPredator = true;

        // TODO: SUPPORT MULTIPLE RUNS
        // which run are we testing?
        int retest = 7;

        // set up file reader
        String outputFileDir = outputPath + problemDirectory + "/" + controllerDirectory + "/run-" + retest;
        String outputFileName = "gnuplot-retest.txt";
        String originalOutputFileName = "gnuplot-original.txt";
        BufferedReader reader = new BufferedReader(new FileReader(inputPath + problemDirectory + "/" + controllerDirectory + "/run-" + retest + "/best-ship.txt"));
        // set up file writer
        File outputDirectory = new File(outputFileDir);
        outputDirectory.mkdirs();
        PrintWriter pwRetest = new PrintWriter(new FileWriter(outputFileDir + "/" + outputFileName));
        PrintWriter pwOriginal = new PrintWriter(new FileWriter(outputFileDir + "/" + originalOutputFileName));

        // define fixed ship design here
        double[] testDesign = {-102.73895913061146, 80.4536813822079, -190.07631854955284, 231.57978872973436, 116.16377119592839, 103.60301850054971, -47.789662132528015, 371.93723206898363, 91.64608832372998, -192.0701546302887, -94.52368101354523, -111.57414108483144, -192.9436604478859, 95.13536432843065, 160.19910123273706, 82.18220358588046, -113.59052059010384, -93.76034965421113, -47.5125979450776, 153.79769135748361, -96.1662443577818, 109.49010072382015, 61.34042244527505, 267.0740614751983, -14.162956707088668, -62.719014029134215, -64.72226275659085, 143.1808982044669, 73.14550087853179};

        // TODO: Make this more general and less specialised
        //for(int i=0; i<problemDirectories.length; i++) {
            // inside each problem directory
                // create this directory for the output path
                // set if problem is co-evolved or specialist
        //boolean specialist = false;
                // inside each controller directory (this does matter, both ships must be tested with the same controller for fairness)

                    // create this directory for the output path
                    // set the controller type

                        // inside each run folder
                            // is this the run we're looking to retest or is retest -1?
                            // create this directory for the output path
                            // take the best ship of a generation

//        if(specialist) {
//            isPredator = true; // TODO: Figure out whether predators are odd or even, the results are confusing
//        } else {
//            isPredator = true; // TODO: Some way to vary this?
//        }
                                // if the problem is coevolved, this is simple
                                // if specialist, we need to also note what type of ship it is (predator or prey)

        // TODO: Fix this for specialists
//        IProblem problem = new PredatorPreyCoevolutionProblem();


                            // test it against fixed ship design
        Scanner scan = new Scanner(reader);
        while(scan.hasNext()) {
            String line = scan.nextLine();
            Scanner entryScan = new Scanner(line);
            int generation = entryScan.nextInt();
            double oldScore = entryScan.nextDouble();
            List<Double> shipDesign = new ArrayList<Double>();
            while(entryScan.hasNext()) {
                String nextParameterString = entryScan.next();
                // clean up parameter
                nextParameterString = nextParameterString.replace("[", "");
                nextParameterString = nextParameterString.replace("]", "");
                nextParameterString = nextParameterString.replace(",", "");
                double nextParameter = Double.parseDouble(nextParameterString);
                shipDesign.add(nextParameter);
            }
            double[] chromosome = new double[shipDesign.size()];
            for(int i=0; i<chromosome.length; i++) {
                chromosome[i] = shipDesign.get(i);
            }

            double[][] shipData = new double[2][];
            if(isPredator) {
                shipData[0] = chromosome;
                shipData[1] = testDesign;
            } else {
                shipData[0] = testDesign;
                shipData[1] = chromosome;
            }

            problem.preFitnessSim(shipData);
            if(problem instanceof PredatorPreySpecialistCoevolutionProblem) {
                PredatorPreySpecialistCoevolutionProblem specialistProblem = (PredatorPreySpecialistCoevolutionProblem)problem;
                if(isPredator) {
                    specialistProblem.competePopulations(chromosome, testDesign);
                } else {
                    specialistProblem.competePopulations(testDesign, chromosome);
                }
            }
            double fitness = problem.fitness(chromosome);
            System.out.println(fitness);

            // log result to output file (generation, tab, value)
            pwRetest.println(generation + "\t" + fitness);
            pwOriginal.println(generation + "\t" + oldScore);
        }

        pwRetest.close();
        pwOriginal.close();

        // show graph of final result
        Grapher.setMetadata("Generations", "Error", (isPredator ? "Best of Predator Population" : "Best of Prey Population"));
        Grapher.drawGraph(outputFileDir + "/" + outputFileName);
        //Grapher.drawGraph(outputFileDir + "/" + originalOutputFileName, true);

        new JEasyFrame(new JPanel(), "Keep Graph Open!");
    }
}
