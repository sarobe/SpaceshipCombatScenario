package problem;

import common.Constants;
import controller.ConditionActionController;
import controller.Controller;
import controller.statebased.HumanStateController;
import controller.statebased.StateController;
import controller.mcts.InfluenceMap;
import controller.mcts.ShipBiasedMCTSController;
import controller.statebased.basic.GreedyController;
import controller.statebased.basic.MCController;
import main.HumanStateControllerKeyHandler;
import main.Runner;
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

        // get the best ship to be predator
        if(populationData.length > 1) {
            predator = new ComplexSpaceship(bestChromosome);
        } else {
            predator = new ComplexSpaceship(populationData[0]);
        }


        // set up a basic prey ship
        prey = new BasicSpaceship();

        // place the two ships apart

        predator.pos.set(Constants.predatorStartPos);
        prey.pos.set(Constants.preyStartPos);

        ships.add(predator);
        ships.add(prey);

//        conts.add(new GreedyController(predator, prey, true));
//        conts.add(new MCController(predator, prey, true));
//        conts.add(new ShipBiasedMCTSController(predator, prey, true));
        conts.add(new ConditionActionController(predator, prey, true));

//        conts.add(new RandomController(prey, predator, false));
//        conts.add(new NullController(prey, predator, false));
//        conts.add(new ShipBiasedMCTSController(prey, predator, false));
//        conts.add(new ConditionActionController(predator, prey, true));
        conts.add(new GreedyController(prey, predator, false));
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
            conts.add(new ConditionActionController(predator, prey, true));
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
            conts.add(new GreedyController(prey, predator, false));
//            conts.add(new MCController(prey, predator, false));
        }

    }

    public void demonstrate() {
        synchronized (Runner.class) {

            for (Asteroid a : AsteroidManager.getAsteroids()) {
                a.update();
                if (a.isColliding(predator)) terminal = true;
                if (a.isColliding(prey)) terminal = true;
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
    public double fitness(double[] x) {
        // ONLY EVOLVES PREDATOR AT THE MOMENT
        // TODO: MEANS OF DETERMINING WHETHER TO EVALUATE CHROMOSOME AS PREY OR NOT (VALUE AT THE END OF THE ARRAY IGNORED BY OTHER PROBLEMS?)

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

//        StateController shipController = new ShipBiasedMCTSController(ship, antagonistShip, true);
//        StateController antagonistController = new ShipBiasedMCTSController(antagonistShip, ship, false);
        Controller shipController = new ConditionActionController(ship, antagonistShip, true);
//        Controller antagonistController = new ConditionActionController(antagonistShip, ship, false);
        Controller antagonistController = new GreedyController(antagonistShip, ship, false);
        // Simulate.
        double score = 0;
        int timesteps = 0;

        while(timesteps < Constants.timesteps) {
            for (Asteroid a : AsteroidManager.getAsteroids()) {
                a.update();
                if (a.isColliding(ship)) break;
                if (a.isColliding(antagonistShip)) break;
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

        // Return score.
        score = shipController.getScore();

        if(score >= bestChromosomeScore) {
            // use >= so that if score is inexplicably identical, we at least prefer novelty
            bestChromosomeScore = score;
            bestChromosome = Arrays.copyOf(x, x.length);
        }

        // Flip for CMA!
        score = 2 - score;

        return score;
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
