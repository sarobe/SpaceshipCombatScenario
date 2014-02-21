package common.utilities;
import java.util.List;

/**
 * This class is used to model the statistics
 * of a fix of numbers.  For the statistics
 * we choose here it is not necessary to store
 * all the numbers - just keeping a running total
 * of how many, the sum and the sum of the squares
 * is sufficient (plus max and min, for max and min).
 * <p/>
 * This is a simpler version of StatisticalSummary that does
 * not include statistical tests, or the Watch class.
 */

public class StatSummary {

    public String name; // defaults to ""
    private double sum;
    private double sumsq;
    private double min;
    private double max;

    private double mean;
    private double sd;

    int n;
    boolean valid;

    public StatSummary() {
        this("");
        // System.out.println("Exited default...");
    }

    public StatSummary(String name) {
        // System.out.println("Creating SS");
        this.name = name;
        n = 0;
        sum = 0;
        sumsq = 0;
        // ensure that the first number to be
        // added will fix up min and max to
        // be that number
        min = Double.POSITIVE_INFINITY;
        max = Double.NEGATIVE_INFINITY;
        // System.out.println("Finished Creating SS");
        valid = false;
    }

    public final void reset() {
        n = 0;
        sum = 0;
        sumsq = 0;
        // ensure that the first number to be
        // added will fix up min and max to
        // be that number
        min = Double.POSITIVE_INFINITY;
        max = Double.NEGATIVE_INFINITY;
    }


    public double max() {
        return max;
    }

    public double min() {
        return min;
    }

    public double mean() {
        if (!valid)
            computeStats();
        return mean;
    }

    // returns the sum of the squares of the differences
    //  between the mean and the ith values
    public double sumSquareDiff() {
        return sumsq - n * mean() * mean();
    }

    public double sumsq() {
        return sumsq;
    }


    private void computeStats() {
        if (!valid) {
            mean = sum / n;
            double num = sumsq - (n * mean * mean);
            if (num < 0) {
                // avoids tiny negative numbers possible through imprecision
                num = 0;
            }
            // System.out.println("Num = " + num);
            sd = Math.sqrt(num / (n - 1));
            // System.out.println(" Test: sd = " + sd);
            // System.out.println(" Test: n = " + n);
            valid = true;
        }
    }

    public double sd() {
        if (!valid)
            computeStats();
        return sd;
    }

    public int n() {
        return n;
    }

    public double stdErr() {
        return sd() / Math.sqrt(n);
    }

    public void add(StatSummary ss) {
        // implications for Watch?
        n += ss.n;
        sum += ss.sum;
        sumsq += ss.sumsq;
        max = Math.max(max, ss.max);
        min = Math.min(min, ss.min);
        valid = false;
    }

    public void add(double d) {
        n++;
        sum += d;
        sumsq += d * d;
        min = Math.min(min, d);
        max = Math.max(max, d);
        valid = false;
    }

    public void add(Number n) {
        add(n.doubleValue());
    }

    //    public void add(double[] d) {
//        for (int i = 0; i < d.length; i++) {
//            add(d[i]);
//        }
//    }
//
    public void add(Double... xa) {
        for (double x : xa) {
            add(x);
        }
    }

    public void add(List<Double> xa) {
        for (double x : xa) {
            add(x);
        }
    }

    public void add(double... xa) {
        for (double x : xa) {
            add(x);
        }
    }

    public String toString() {
        String s = (name == null) ? "" : name + "\n";
        s += " min = " + min() + "\n" +
                " max = " + max() + "\n" +
                " ave = " + mean() + "\n" +
                " sd  = " + sd() + "\n" +
                " se  = " + stdErr() + "\n" +
                " sum  = " + sum + "\n" +
                " sumsq  = " + sumsq + "\n" +
                " n   = " + n;
        return s;

    }

    public void copyFrom(StatSummary ss) {
        name = ss.name;
        n = ss.n;
        sum = ss.sum;
        sumsq = ss.sumsq;
        min = ss.min;
        max = ss.max;
        valid = ss.valid;
        mean = ss.mean;
        sd = ss.sd;
        // System.out.println("Copied with n = " + n);
    }
}