//package ea;
//
//import cma.QuadraticBowl;
//import fr.inria.optimization.cmaes.fitness.IObjectiveFunction;
//import utilities.StatSummary;
//
//public class RandomMutationHillClimberTest {
//    public static void main(String[] args) {
//        int nEvals = 1000;
//        int nTests = 100;
//        RandomMutationHillClimber.noiseFac = 1.02;
//        System.out.println(runTests(nTests, nEvals));
//    }
//
//    static StatSummary runTests(int nTests, int evalsPerTest) {
//        StatSummary ss = new StatSummary();
//        for (int i=0; i<nTests; i++) {
//            ss.add(runTest(evalsPerTest));
//        }
//        return ss;
//    }
//
//    static double runTest(int nEvals) {
//        FitVectorSource source = new RandomMutationHillClimber(2,-1);
//        IObjectiveFunction fun = new QuadraticBowl();
//        for (int i=0; i<nEvals; i++) {
//            double[] x = source.getNext();
//            double fitness = fun.valueOf(x);
//            source.returnFitness(fitness);
//            // System.out.println(i + "\t " + fitness);
//        }
//        return source.bestScore();
//    }
//}
