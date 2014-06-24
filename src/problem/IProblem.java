package problem;

import controller.Controller;
import spaceship.SimObject;
import spaceship.Spaceship;

import java.util.List;

public interface IProblem {

    public void demonstrationInit(double[][] populationData);
    public void demonstrationInit();
    public void demonstrate();

    public List<Spaceship> getShips();
    public List<Controller> getControllers();

    public int nDim();

    public double fitness(double[] x);

    public boolean hasEnded();

    public int getTimesteps();

}
