package ea;

public interface FitVectorSource {

    void returnFitness(double fitness);

    double[] getNext();

    double[] bestVec();
    double bestScore();
}
