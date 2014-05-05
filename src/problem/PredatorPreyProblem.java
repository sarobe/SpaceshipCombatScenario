package problem;

import common.Constants;
import controller.Controller;
import controller.basic.GreedyController;
import controller.basic.MCController;
import controller.basic.NullController;
import controller.mcts.InfluenceMap;
import controller.basic.RandomController;
import controller.mcts.ShipBiasedMCTSController;
import main.Runner;
import spaceship.BasicSpaceship;
import spaceship.Spaceship;

import java.util.ArrayList;
import java.util.List;

public class PredatorPreyProblem implements IProblem {
    public Spaceship predator;
    public Spaceship prey;
    public List<Spaceship> ships;
    public List<Controller> conts;
    public boolean terminal;

    int timestepsElapsed = 0;

    public PredatorPreyProblem() {
        ProjectileManager.reset();
        conts = new ArrayList<Controller>();
        ships = new ArrayList<Spaceship>();
    }


    @Override
    public void demonstrationInit(double[][] populationData) {
        // TODO
        demonstrationInit();
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
//        conts.add(new GreedyController(predator, prey, true));
//        conts.add(new MCController(predator, prey, true));
        conts.add(new ShipBiasedMCTSController(predator, prey, true));
//        conts.add(new RandomController(prey, predator, false));
//        conts.add(new NullController(prey, predator, false));
        conts.add(new ShipBiasedMCTSController(prey, predator, false));
    }

    public void demonstrate() {
        synchronized (Runner.class) {

            for(Asteroid a : AsteroidManager.getAsteroids()) {
                a.update();
                if(a.isColliding(predator)) terminal = true;
                if(a.isColliding(prey)) terminal = true;
            }

            for (Controller c : conts) {
                //c.think(demoShips);
                c.think();
            }

            for (Spaceship s : ships) {
                s.update();
            }

            if(predator.isColliding(prey)) {
                terminal = true;
            }

            if(timestepsElapsed >= Constants.timesteps) {
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
        // TODO
        return 0;
    }

    @Override
    public double fitness(double[] x) {
        // TODO
        return 0;
    }

    @Override
    public boolean hasEnded() {
        return terminal;
    }


}
