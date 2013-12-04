package problem;

import common.Constants;
import common.math.Vector2d;
import controller.Controller;
import controller.ShipActionController;
import controller.mcts.ShipBiasedMCTSController;
import controller.mcts.ShipMCTSController;
import main.Runner;
import spaceship.Projectile;
import spaceship.Spaceship;

import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Samuel Roberts, 2012
 */
public class SpaceshipIndividualCombatProblem {

    public List<Spaceship> demoShips;
    public List<Controller> demoConts;

    public SpaceshipIndividualCombatProblem() {
        ProjectileManager.reset();
        PickupManager.reset();
        demoShips = new ArrayList<Spaceship>();
        demoConts = new ArrayList<Controller>();
    }

    public int nDim() {
        return Constants.numWeights + (4 * Constants.numComponents) + 3;
    }

    public double fitness(double[] x) {

        double score = 0;

        // initialise simulation
        ProjectileManager.reset();
        PickupManager.reset();

        Spaceship ship = getInstance(x);
        Controller cont = new ShipBiasedMCTSController(ship);
        ship.setTeam(1);

        for(int i = 0; i<Constants.combatRepeats; i++) {
            ProjectileManager.reset();
            PickupManager.placePickups(Constants.pickupPlacementSeed);
            ship.reset();

            if(ship.radius >= Math.min(Constants.screenWidth, Constants.screenHeight)) {
                ship.alive = false;
            }

            int j = 0;
            while((j < Constants.timesteps) && ship.alive && PickupManager.pickupsRemaining() > 0) {

                // simulate all ships
                cont.think();
                ship.update();
                // penalise bounce
                double bouncePenalty = 0;
                if(ship.bounced) {
                    bouncePenalty = 100;
                }
                score -= bouncePenalty;

                // simulate bullets and resolve collisions
                for(Projectile p : ProjectileManager.getLivingProjectiles()) {
                    p.update();

//                    for(Spaceship s : ships) {
//                        if(s.alive && p.owner != s && s.isColliding(p) && (Constants.allowFriendlyFire || s.team != p.team) && p.alive) {
//                            p.kill();
//                            s.harm(Constants.defaultProjectileHarm);
//
//                            double hitScore = 0;
//                            if(!s.alive) hitScore = Constants.killReward;
//                            else hitScore = Constants.hitReward;
//
//                            // award ship
//                            Spaceship attacker = p.owner;
//                            double oldScore = fitnessScores.get(attacker.chromosome);
//                            fitnessScores.put(attacker.chromosome, oldScore + hitScore);
//                        }
//                    }
                }

                // check for pickup collisions
                for(Pickup p : PickupManager.getLivingPickups()) {
                    if(ship.alive && p.alive && ship.isColliding(p)) {
                        p.dispenseReward(ship);
                        // award ship for collecting non-mine pickup
                        if(p.type != PickupType.MINE) {
                            score += Constants.pickupReward;
                        } else {
                            score -= Constants.minePenalty;
                        }
                    }
                }

                j++;
            }

            ////////////////////////////////////
            // ASSIGNING END OF ROUND SCORES
            ////////////////////////////////////
            // reward hull integrity
            score += ship.hull * Constants.hullRewardMul;
            // penalise for too many shots
            score -= ship.bulletsFired * Constants.bulletPenaltyMul;
            // reward on time left if all collected
            if(PickupManager.pickupsRemaining() == 0) score += (Constants.timesteps - j) * 10;
        }

        ////////////////////////////////////
        // DETERMINING FINAL SCORES
        ////////////////////////////////////
        // get average performance
        score /= Constants.combatRepeats;

        // if ship is too big, penalise it heavily
        if(ship.radius >= Math.min(Constants.screenWidth, Constants.screenHeight)) {
            score = -Double.MAX_VALUE;
        }

        // flip score to error
        score *= -1;
        return score;
    }

    public Spaceship getInstance(double[] x) {
        return new Spaceship(x);
    }

    public void demonstrationInit(double[][] shipData) {

        ProjectileManager.reset();
        PickupManager.placePickups(Constants.pickupPlacementSeed);
        demoShips.clear();
        demoConts.clear();

        // determine the best ship to show
//        double bestFitness = Double.MAX_VALUE;
//        double[] bestShip = null;
//        for(double[] individual : shipData) {
//            double fitness = fitness(individual);
//            if(fitness < bestFitness) {
//                bestFitness = fitness;
//                bestShip = individual;
//            }
//        }

        // set up sides
        int numShips = shipData.length;

        for(int i = 0; i < numShips; i++) {
            Spaceship ship = getInstance(shipData[i]);
            ship.reset();
            ship.setTeam(1);
            //ship.pos = getRandStartPos(Constants.startRect);
            //ship.rot = 0;
            demoShips.add(ship);
            demoConts.add(new ShipBiasedMCTSController(ship));
        }

        // kill ships that are too big
        for(Spaceship ship : demoShips) {
            // if ship is too big, destroy it
            if(ship.radius >= Math.min(Constants.screenWidth, Constants.screenHeight)) {
                ship.alive = false;
                System.out.println("Killed ship with radius of " + ship.radius);
            }
        }

        // kill ships that are initially overlapping others
//        for(int i = 0; i < demoShips.size(); i++) {
//            for(int j=i; j < demoShips.size(); j++) {
//                if(i == j) continue;
//                Spaceship shipA = demoShips.get(i);
//                Spaceship shipB = demoShips.get(j);
//                double dist = shipA.pos.dist(shipB.pos);
//                if(dist < shipA.radius + shipB.radius) {
//                    // kill a ship for overlapping
//                    //shipA.alive = false;
//                }
//            }
//        }
    }

    public void demonstrate() {
        synchronized (Runner.class) {
            for(Controller c : demoConts) {
                //c.think(demoShips);
                c.think();
            }

            for(Spaceship s : demoShips) {
                s.update();
            }

            // move bullets, update collisions
            for(Projectile p : ProjectileManager.getLivingProjectiles()) {
                p.update();

                for(Spaceship s : demoShips) {
                    if(s.alive && p.owner != s && s.isColliding(p) && (Constants.allowFriendlyFire || s.team != p.team) && p.alive) {
                        p.kill();
                        s.harm(Constants.defaultProjectileHarm);
//                        if(s.team == Constants.TEAM_LEFT) demoScoreRight += Constants.defaultProjectileHarm;
//                        else demoScoreLeft += Constants.defaultProjectileHarm;
//                        if(!s.alive) {
//                            if(s.team == Constants.TEAM_LEFT) demoScoreRight += Constants.defaultProjectileHarm * 10;
//                            else demoScoreLeft += Constants.defaultProjectileHarm * 10;
//                        }
                    }
                }
            }

            // check for pickup collisions
            for(Pickup p : PickupManager.getLivingPickups()) {
                for(Spaceship s : demoShips) {
                    if(s.alive && p.alive && s.isColliding(p)) {
                        p.dispenseReward(s);
                    }
                }
            }
        }
    }

    public List<Spaceship> getShips() {
        return demoShips;
    }

    public List<Controller> getControllers() {
        return demoConts;
    }
}
