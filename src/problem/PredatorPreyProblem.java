package problem;

import common.Constants;
import common.RunParameters;
import common.math.Vector2d;
import common.utilities.Pair;
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
    public List<Spaceship> demoShips;
    public List<Controller> demoConts;
    public boolean terminal;
    public boolean testingAsPredator = true;

    // used for demonstration purposes and caching to avoid constantly resimulating in demo init
    public double[] bestChromosome;
    public double bestChromosomeScore;

    int timestepsElapsed = 0;

    public HumanStateControllerKeyHandler keyHandler;

    public PredatorPreyProblem() {
        ProjectileManager.reset();
        demoConts = new ArrayList<Controller>();
        demoShips = new ArrayList<Spaceship>();
    }

    public PredatorPreyProblem(HumanStateControllerKeyHandler keyHandler) {
        this();
        this.keyHandler = keyHandler;
    }


    @Override
    public void demonstrationInit(double[][] populationData, boolean useSpecialists) {
        ProjectileManager.reset();
        AsteroidManager.reset();
        timestepsElapsed = 0;
        demoConts.clear();
        demoShips.clear();
        terminal = false;

        AsteroidManager.placeAsteroids(Constants.asteroidPlacementSeed);


        if(populationData.length > 1) {
            if(useSpecialists) {
                predator = new ComplexSpaceship(populationData[0]);
                prey = new ComplexSpaceship(populationData[1]);
            } else {
                // get the best ship to be predator AND prey
                predator = new ComplexSpaceship(bestChromosome);
                prey = new ComplexSpaceship(bestChromosome);
            }
        } else {
            predator = new ComplexSpaceship(populationData[0]);
            prey = new ComplexSpaceship(populationData[0]);
        }


        // place the two ships apart

        predator.pos.set(Constants.predatorStartPos);
        prey.pos.set(Constants.preyStartPos);

        demoShips.add(predator);
        demoShips.add(prey);

        demoConts.add(RunParameters.getAppropriateController(RunParameters.runShipController, predator, prey, true));
        demoConts.add(RunParameters.getAppropriateController(RunParameters.runShipController, prey, predator, false));
    }

    public void demonstrationInit() {
        // use the basic ship
        ProjectileManager.reset();
        AsteroidManager.reset();
        timestepsElapsed = 0;
        demoConts.clear();
        demoShips.clear();
        terminal = false;

        AsteroidManager.placeAsteroids(Constants.asteroidPlacementSeed);

        predator = new BasicSpaceship();
        prey = new BasicSpaceship();

        // place the two ships apart

        predator.pos.set(Constants.predatorStartPos);
        prey.pos.set(Constants.preyStartPos);


        demoShips.add(predator);
        demoShips.add(prey);

        InfluenceMap.createInfluenceMap(predator, prey);

        // awareness: self, other, am i the predator
        // PREDATOR CONTROLLER
        if (Constants.humanControl == Constants.HumanControl.PREDATOR) {
            // add human
            HumanStateController humanCont = new HumanStateController(predator, prey, true);
            if(keyHandler != null) keyHandler.setController(humanCont);
            demoConts.add(humanCont);
        } else {
            // add ai
//            demoConts.add(new GreedyController(predator, prey, true));
//            demoConts.add(new MCController(predator, prey, true));
//            demoConts.add(new ShipBiasedMCTSController(predator, prey, true));
//            demoConts.add(new ConditionActionController(predator, prey, true));
            demoConts.add(new BasicPerceptronController(predator, prey, true));
        }

        // PREY CONTROLLER
        if (Constants.humanControl == Constants.HumanControl.PREY) {
            // add human
            HumanStateController humanCont = new HumanStateController(prey, predator, false);
            if(keyHandler != null) keyHandler.setController(humanCont);
            demoConts.add(humanCont);
        } else {
//            demoConts.add(new RandomController(prey, predator, false));
//            demoConts.add(new NullController(prey, predator, false));
//            demoConts.add(new ShipBiasedMCTSController(prey, predator, false));
            //demoConts.add(new ConditionActionController(predator, prey, true));
//            demoConts.add(new GreedyController(prey, predator, false));
            demoConts.add(new MCController(prey, predator, false));
        }

    }

    public void demonstrate() {
        synchronized (ProblemRunner.class) {

            for (Asteroid a : AsteroidManager.getAsteroids()) {
                a.update();
                if (predator.isColliding(a)) terminal = true;
                if (prey.isColliding(a)) terminal = true;
            }

            for (Controller c : demoConts) {
                //c.think(demoShips);
                Vector2d shipPosBefore = c.getShip().pos.copy();
                c.think();
            }

            for (Spaceship s : demoShips) {
                s.update();
            }

            if (predator.isColliding(prey)) {
                terminal = true;
            }

            if (timestepsElapsed >= Constants.timesteps) {
                terminal = true;
            }


            // update influence map periodically
//            if (timestepsElapsed % 10 == 0) {
//                InfluenceMap.createInfluenceMap(predator, prey);
//            }
            timestepsElapsed++;
        }
    }

    public List<Spaceship> getShips() {
        return demoShips;
    }

    public List<Controller> getControllers() {
        return demoConts;
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
        // Use a basic ship as the antagonist ship.
//        Spaceship antagonistShip = new BasicSpaceship();

        double[] testDesign = {-102.73895913061146, 80.4536813822079, -190.07631854955284, 231.57978872973436, 116.16377119592839, 103.60301850054971, -47.789662132528015, 371.93723206898363, 91.64608832372998, -192.0701546302887, -94.52368101354523, -111.57414108483144, -192.9436604478859, 95.13536432843065, 160.19910123273706, 82.18220358588046, -113.59052059010384, -93.76034965421113, -47.5125979450776, 153.79769135748361, -96.1662443577818, 109.49010072382015, 61.34042244527505, 267.0740614751983, -14.162956707088668, -62.719014029134215, -64.72226275659085, 143.1808982044669, 73.14550087853179};
        Spaceship antagonistShip = new ComplexSpaceship(testDesign);

        // Create ship from chromosome.
        Spaceship ship = new ComplexSpaceship(x);

        // Run once with ship as predator.
        Controller shipController = RunParameters.getAppropriateController(RunParameters.runShipController, ship, antagonistShip, true);
        Controller antagonistController = RunParameters.getAppropriateController(RunParameters.runShipController, ship, antagonistShip, false);

        double score;
        if(testingAsPredator)  {
            score = runSimulation(ship, antagonistShip, shipController, antagonistController).first();
        } else {
            // Flip the controllers.
            shipController.isPredator = false;
            antagonistController.isPredator = true;

            score = runSimulation(antagonistShip, ship, antagonistController, shipController).second();
        }

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

    // returns <predator score, prey score>
    protected Pair<Double, Double> runSimulation(Spaceship predatorShip, Spaceship preyShip, Controller predatorController, Controller preyController) {
        int timesteps = 0;

        // Reset simulation state.
        ProjectileManager.reset();
        AsteroidManager.reset();
        AsteroidManager.placeAsteroids(Constants.asteroidPlacementSeed);

        // Reset ship states.
        predatorShip.reset();
        predatorShip.pos.set(Constants.predatorStartPos);
        preyShip.reset();
        preyShip.pos.set(Constants.preyStartPos);

        // Run simulation!
        while(timesteps < Constants.timesteps) {
            for (Asteroid a : AsteroidManager.getAsteroids()) {
                a.update();
                if (predatorShip.isColliding(a)) break;
                if (preyShip.isColliding(a)) break;
            }

            predatorController.think();
            preyController.think();

            predatorShip.update();
            preyShip.update();

            if (predatorShip.isColliding(preyShip)) {
                break;
            }

            timesteps++;
        }

        Pair<Double, Double> score = new Pair<Double, Double>(predatorController.getScore(), preyController.getScore());
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
