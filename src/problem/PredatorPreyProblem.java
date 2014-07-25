package problem;

import common.Constants;
import common.RunParameters;
import controller.Controller;
import controller.neuralnet.BasicPerceptronController;
import controller.statebased.HumanStateController;
import controller.mcts.InfluenceMap;
import controller.statebased.basic.MCController;
import main.HumanStateControllerKeyHandler;
import main.ProblemRunner;
import problem.entities.Asteroid;
import problem.managers.AsteroidManager;
import problem.managers.ProjectileManager;
import spaceship.BasicSpaceship;
import spaceship.ComplexSpaceship;
import spaceship.Spaceship;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class PredatorPreyProblem implements IProblem {
    public Spaceship predator;
    public Spaceship prey;
    public List<Spaceship> ships;
    public List<Controller> conts;
    public boolean terminal;

    // used for demonstration purposes and caching to avoid constantly resimulating in demo init
    public double[] bestChromosome;
    public double bestChromosomeScore;

    int timestepsElapsed = 0;

    public HumanStateControllerKeyHandler keyHandler;

    public PredatorPreyProblem() {
        ProjectileManager.reset();
        conts = new ArrayList<Controller>();
        ships = new ArrayList<Spaceship>();
    }

    public PredatorPreyProblem(HumanStateControllerKeyHandler keyHandler) {
        this();
        this.keyHandler = keyHandler;
    }


    @Override
    public void demonstrationInit(double[][] populationData) {
        ProjectileManager.reset();
        AsteroidManager.reset();
        timestepsElapsed = 0;
        conts.clear();
        ships.clear();
        terminal = false;

        AsteroidManager.placeAsteroids(Constants.asteroidPlacementSeed);

        // get the best ship to be predator AND prey
        if(populationData.length > 1) {
            predator = new ComplexSpaceship(bestChromosome);
            prey = new ComplexSpaceship(bestChromosome);
        } else {
            predator = new ComplexSpaceship(populationData[0]);
            prey = new ComplexSpaceship(populationData[0]);
        }


        // place the two ships apart

        predator.pos.set(Constants.predatorStartPos);
        prey.pos.set(Constants.preyStartPos);

        ships.add(predator);
        ships.add(prey);

        conts.add(RunParameters.getAppropriateController(RunParameters.runShipController, predator, prey, true));
        conts.add(RunParameters.getAppropriateController(RunParameters.runShipController, prey, predator, false));
    }

    public void demonstrationInit() {
        // use the basic ship
        ProjectileManager.reset();
        AsteroidManager.reset();
        timestepsElapsed = 0;
        conts.clear();
        ships.clear();
        terminal = false;

        AsteroidManager.placeAsteroids(Constants.asteroidPlacementSeed);

        predator = new BasicSpaceship();
        prey = new BasicSpaceship();

        // place the two ships apart

        predator.pos.set(Constants.predatorStartPos);
        prey.pos.set(Constants.preyStartPos);


        ships.add(predator);
        ships.add(prey);

        InfluenceMap.createInfluenceMap(predator, prey);

        // awareness: self, other, am i the predator
        // PREDATOR CONTROLLER
        if (Constants.humanControl == Constants.HumanControl.PREDATOR) {
            // add human
            HumanStateController humanCont = new HumanStateController(predator, prey, true);
            if(keyHandler != null) keyHandler.setController(humanCont);
            conts.add(humanCont);
        } else {
            // add ai
//            conts.add(new GreedyController(predator, prey, true));
//            conts.add(new MCController(predator, prey, true));
//            conts.add(new ShipBiasedMCTSController(predator, prey, true));
//            conts.add(new ConditionActionController(predator, prey, true));
            conts.add(new BasicPerceptronController(predator, prey, true));
        }

        // PREY CONTROLLER
        if (Constants.humanControl == Constants.HumanControl.PREY) {
            // add human
            HumanStateController humanCont = new HumanStateController(prey, predator, false);
            if(keyHandler != null) keyHandler.setController(humanCont);
            conts.add(humanCont);
        } else {
//            conts.add(new RandomController(prey, predator, false));
//            conts.add(new NullController(prey, predator, false));
//            conts.add(new ShipBiasedMCTSController(prey, predator, false));
            //conts.add(new ConditionActionController(predator, prey, true));
//            conts.add(new GreedyController(prey, predator, false));
            conts.add(new MCController(prey, predator, false));
        }

    }

    public void demonstrate() {
        synchronized (ProblemRunner.class) {

            for (Asteroid a : AsteroidManager.getAsteroids()) {
                a.update();
                if (predator.isColliding(a)) terminal = true;
                if (prey.isColliding(a)) terminal = true;
            }

            for (Controller c : conts) {
                //c.think(demoShips);
                c.think();
            }

            for (Spaceship s : ships) {
                s.update();
            }

            if (predator.isColliding(prey)) {
                terminal = true;
            }

            if (timestepsElapsed >= Constants.timesteps) {
                terminal = true;
            }


            // update influence map periodically
            if (timestepsElapsed % 10 == 0) {
                InfluenceMap.createInfluenceMap(predator, prey);
            }
            timestepsElapsed++;
        }
    }

    public List<Spaceship> getShips() {
        return ships;
    }

    public List<Controller> getControllers() {
        return conts;
    }

    @Override
    public int nDim() {
        return Constants.numWeights + (4 * Constants.numComponents) + 3;
    }

    @Override
    public void preFitnessSim(double[][] popData) {
        // do nothing
    }

    @Override
    public double fitness(double[] x) {
        // Reset simulation state.
        ProjectileManager.reset();
        AsteroidManager.reset();
        AsteroidManager.placeAsteroids(Constants.asteroidPlacementSeed);

        // Use a basic ship as the antagonist ship.
        Spaceship antagonistShip = new BasicSpaceship();

        // Create ship from chromosome.
        Spaceship ship = new ComplexSpaceship(x);

        // Set up properties and controllers for both.
        ship.pos.set(Constants.predatorStartPos);
        antagonistShip.pos.set(Constants.preyStartPos);

        // Run once with ship as predator.
        Controller shipController = RunParameters.getAppropriateController(RunParameters.runShipController, ship, antagonistShip, true);
        Controller antagonistController = RunParameters.getAppropriateController(RunParameters.runShipController, ship, antagonistShip, false);
        // Simulate.
        double predScore = runSimulation(ship, antagonistShip, shipController, antagonistController);

        // Reset state.
        ProjectileManager.reset();
        AsteroidManager.reset();
        AsteroidManager.placeAsteroids(Constants.asteroidPlacementSeed);

        // Reset and swap the ships.
        ship.pos.set(Constants.preyStartPos);
        ship.vel.zero();
        ship.rot = 0;
        ship.rotvel = 0;

        antagonistShip.pos.set(Constants.predatorStartPos);
        antagonistShip.vel.zero();
        antagonistShip.rot = 0;
        antagonistShip.rotvel = 0;

        // Flip the controllers.
        shipController.isPredator = false;
        antagonistController.isPredator = true;

        // Simulate.
        double preyScore = runSimulation(ship, antagonistShip, shipController, antagonistController);

        // Take the average of both scores.
        double score = (predScore + preyScore) / 2;

        if(score >= bestChromosomeScore) {
            // use >= so that if score is inexplicably identical, we at least prefer novelty
            bestChromosomeScore = score;
            bestChromosome = Arrays.copyOf(x, x.length);
        }

        // Flip for CMA!
        assert(score >= 0);
        assert(score < 1);
        score = 1 - score;

        return score;
    }

    private double runSimulation(Spaceship ship, Spaceship antagonistShip, Controller shipController, Controller antagonistController) {
        double score = 0;
        int timesteps = 0;

        while(timesteps < Constants.timesteps) {
            for (Asteroid a : AsteroidManager.getAsteroids()) {
                a.update();
                if (ship.isColliding(a)) break;
                if (antagonistShip.isColliding(a)) break;
            }

            shipController.think();
            antagonistController.think();

            ship.update();
            antagonistShip.update();

            if (ship.isColliding(antagonistShip)) {
                break;
            }

            timesteps++;
        }
        return shipController.getScore();
    }

    @Override
    public boolean hasEnded() {
        return terminal;
    }

    @Override
    public int getTimesteps() {
        return timestepsElapsed;
    }

}
