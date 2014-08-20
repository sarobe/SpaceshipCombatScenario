package problem;

import controller.Controller;
import spaceship.SimObject;
import spaceship.Spaceship;

import java.util.List;

public interface IProblem {

    public void demonstrationInit(double[][] populationData, boolean useSpecialist);
    public void demonstrationInit();
    public void demonstrate();

    public List<Spaceship> getShips();
    public List<Controller> getControllers();

    public int nDim();

    public void preFitnessSim(double[][] popData); // use this for
    // anything requiring multiple population members being compared
    // stuff to do before calling fitness for a generation
    // CALLED ONCE BEFORE FITNESS FOR EACH GENERATION

    public double fitness(double[] x);

    public boolean hasEnded();

    public int getTimesteps();

}
