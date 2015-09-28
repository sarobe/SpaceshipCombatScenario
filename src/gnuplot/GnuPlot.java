package gnuplot;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.util.List;

public class GnuPlot {
    static String exeFile = "gnuplot/binary/pgnuplot";

    Process p;
    PrintStream ps;
    BufferedReader dis;

    public GnuPlot() throws Exception {
        p = Runtime.getRuntime().exec(exeFile);
        ps = new PrintStream(p.getOutputStream());
        dis = new BufferedReader(new InputStreamReader(p.getInputStream()));
    }

    public void splot(String filename) throws Exception {
        ps.println("set cblabel \"colour gradient\" \n" + "set pm3d implicit at s \n set pm3d depthorder");
        ps.println("set hidden3d offset 1 trianglepattern 3 undefined 1 altdiagonal bentover");
        ps.println(String.format("splot '%s' with lines", filename));
        ps.flush();
        dis.readLine();
    }

    public void plot(String filename, String title) throws Exception {
        // note: use "replot" to add multiple lines to a graph
        ps.println(String.format("plot '%s' with lines title '%s'", filename, title));
        ps.flush();
        // dis.readLine();
    }

    public void replot(String filename, String title) throws Exception {
        // note: use "replot" to add multiple lines to a graph
        ps.println(String.format("replot '%s' with lines title '%s'", filename, title));
        ps.flush();
        // dis.readLine();
    }

    public void eplotmany(List<String> filenames, List<String> legends) throws Exception {
        // note: use "replot" to add multiple lines to a graph
        ps.println(String.format("plot '%s' using 1:2 with lines title '%s', '' using 1:2:3 notitle with yerrorbars", filenames.get(0), legends.get(0)));
        if(filenames.size() > 1) {
            for(int i=1; i<filenames.size(); i++) {
                ps.println(String.format("replot '%s' using 1:2 with lines title '%s', '' using 1:2:3 notitle with yerrorbars", filenames.get(i), legends.get(i)));
            }
        }
        ps.flush();
        // dis.readLine();
    }

    public void setAxes(String xLabel, String yLabel) {
        ps.println("set xlabel \"" + xLabel + "\"");
        ps.println("set ylabel \"" + yLabel + "\"");
    }
} // Gnuplot
