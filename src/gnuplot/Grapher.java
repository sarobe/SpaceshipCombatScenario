package gnuplot;

import common.utilities.JEasyFrame;
import common.utilities.StatSummary;

import javax.swing.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.*;

/**
 * Created by Samuel Roberts, 2012
 */
public class Grapher {

    private static final String dataDirectory = "data/";
    private static String xLabel = "Generations";
    private static String yLabel = "Fitness";
    private static String title = "Best of Generation";


    public static void main(String[] args) throws Exception {

        final int startingIndex = 1;
        final int endingIndex = 19;

        GnuPlot gp = new GnuPlot();
//
//        for(int i=startingIndex; i<startingIndex + graphs; i++) {
//            //transformData(i, 2000);
//            drawGraph(i);
//        }

        String baseDirectory = dataDirectory + "fixedPredator/";
        String[] controllers = {"condition_action", "flat_mc", "greedy_search", "mcts"};
        String[] legends = {"Condition-Action", "Uniform MC", "Greedy Search", "MCTS"};
        setMetadata("Generations", "Error", "Best of Predator");

        List<String> filenames = new ArrayList<String>();
        for(String c : controllers) {
//            aggregateData(baseDirectory + c);
            filenames.add(baseDirectory + c + "/aggregated.txt");
        }

        gp.setAxes(xLabel, yLabel);
        gp.eplotmany(filenames, Arrays.asList(legends));

//        String controller = "flat_mc";

//
//        for(int i=startingIndex; i<endingIndex; i++) {
//            drawGraph(baseDirectory + controller + "/run-" + i + "/gnuplot-best.txt");
//            Thread.sleep(1000);
//        }


        new JEasyFrame(new JPanel(), "Keep Graph Open!");
    }

    public static void setMetadata(String newXLabel, String newYLabel, String newTitle) {
        xLabel = newXLabel;
        yLabel = newYLabel;
        title = newTitle;
    }

    public static void drawGraph(int runIndex) {
        try {
            GnuPlot gp = new GnuPlot();
            gp.setAxes(xLabel, yLabel);
            gp.plot(dataDirectory + "run-" + runIndex + "/gnuplot-best.txt", title);
            //gp.replot(dataDirectory + "run-" + runIndex + "/gnuplot-mean.txt", "Mean of Generation");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void drawGraph(String path) {
        drawGraph(path, false);
    }

    public static void drawGraph(String path, boolean drawAdditional) {
        try {
            GnuPlot gp = new GnuPlot();
            gp.setAxes(xLabel, yLabel);
            if(drawAdditional) {
                gp.replot(path, "Old " + title);
            } else {
                gp.plot(path, title);
            }
            //gp.replot(dataDirectory + "run-" + runIndex + "/gnuplot-mean.txt", "Mean of Generation");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void writeGenData(String logDirectory, int runIndex, int genIndex, double bestResult, double meanResult, double[] bestSolution) throws IOException {

        // Use to write gnuplot compatible data

        // make directory to write to

        String directoryName = logDirectory + "/run-" + runIndex;
        new File(directoryName).mkdirs();

        // write/append data to graphing files
        boolean append = true;
        if(genIndex == 1) {
            append = false;
        }
        // write the best
        PrintWriter pw = new PrintWriter(new FileWriter(directoryName + "/gnuplot-best.txt", append));
        pw.format("%f \t %f \n", (double)genIndex, bestResult);
        pw.close();

        // write the mean
        pw = new PrintWriter(new FileWriter(directoryName + "/gnuplot-mean.txt", append));
        pw.format("%f \t %f \n", (double)genIndex, meanResult);
        pw.close();

        // write the best ship
        pw = new PrintWriter(new FileWriter(directoryName + "/best-ship.txt", append));
        pw.print(genIndex + "\t" + bestResult + "\t" + Arrays.toString(bestSolution) + "\n");
        pw.close();
    }

    public static void transformData(int runIndex, double constant) throws Exception {
        String directoryName = dataDirectory + "run-" + runIndex;

        // rewrite the best
        BufferedReader reader = new BufferedReader(new FileReader(directoryName + "/gnuplot-best.txt"));
        PrintWriter pw = new PrintWriter(new FileWriter(directoryName + "/gnuplot-bestTEMP.txt", false));
        String line = "";
        while((line = reader.readLine()) != null) {
            String[] tokens = line.split(" \t ");
            double gen = Double.parseDouble(tokens[0]);
            double bestResult = constant - Double.parseDouble(tokens[1]);;
            pw.format("%f \t %f \n", gen, bestResult);
        }
        reader.close();
        pw.close();
        File fileA = new File(directoryName + "/gnuplot-best.txt");
        File fileB = new File(directoryName + "/gnuplot-bestTEMP.txt");
        fileA.delete();
        fileB.renameTo(fileA);
        fileB.delete();


        // rewrite the mean
        reader = new BufferedReader(new FileReader(directoryName + "/gnuplot-mean.txt"));
        pw = new PrintWriter(new FileWriter(directoryName + "/gnuplot-meanTEMP.txt", false));
        line = "";
        while((line = reader.readLine()) != null) {
            String[] tokens = line.split(" \t ");
            double gen = Double.parseDouble(tokens[0]);
            double bestResult = constant - Double.parseDouble(tokens[1]);
            pw.format("%f \t %f \n", gen, bestResult);
        }
        reader.close();
        pw.close();
        fileA = new File(directoryName + "/gnuplot-mean.txt");
        fileB = new File(directoryName + "/gnuplot-meanTEMP.txt");
        fileA.delete();
        fileB.renameTo(fileA);
        fileB.delete();
    }

    public static void aggregateData(String containerFolder) throws Exception {
        File dataDirectory = new File(containerFolder);
        File directories[] = dataDirectory.listFiles();

        int maxRuns = 18;
        Map<Integer, StatSummary> runValues = new HashMap<Integer, StatSummary>(); // generation - list of best values for run

        System.out.println("Aggregating " + containerFolder);
        BufferedReader reader;
        if(directories.length > 0) {
            for(File runDir : directories) {
                String dirName = runDir.getName();
                if(dirName.substring(0, 3).equals("run")) {
                    // a run directory
                    String dirNumPart = dirName.substring(4);
                    // get the best value text
                    try {
                        reader = new BufferedReader(new FileReader(runDir + "/gnuplot-best.txt"));
                        String line;
                        while((line = reader.readLine()) != null) {
                            String[] tokens = line.split(" \t ");
                            double gen = Double.parseDouble(tokens[0]);
                            int genInt = (int)gen;
                            double bestResult = Double.parseDouble(tokens[1]);

                            StatSummary values = runValues.get(genInt);
                            if(values == null) {
                                values = new StatSummary();
                                runValues.put(genInt, values);
                            }
                            values.add(bestResult);
                        }
                        System.out.println("Read run " + dirNumPart);
                        reader.close();
                    } catch(IOException e) {
                        System.err.println("Couldn't load a best file, probably absent. Skipping.");
                    }
                }
            }
        }


        // calculate mean and std err for each generation (yerrorbars)
        PrintWriter pw = new PrintWriter(new FileWriter(containerFolder + "/aggregated.txt", false));
        for(Integer generation : runValues.keySet()) {
            double genDoub = (double)generation;
            StatSummary stats = runValues.get(generation);
            pw.format("%f \t %f \t %f \n", genDoub, stats.mean(), stats.stdErr());
        }
        pw.close();
    }
}