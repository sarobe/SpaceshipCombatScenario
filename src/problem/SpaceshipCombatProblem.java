package problem;

import common.Constants;
import common.math.Vector2d;
import controller.ShipActionController;
import main.ProblemRunner;
import spaceship.ComplexSpaceship;
import spaceship.Projectile;

import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Samuel Roberts, 2012
 */
public class SpaceshipCombatProblem {

    public Map<double[], Double> fitnessScores;

    public List<ComplexSpaceship> demoShips;
    public List<ShipActionController> demoConts;

    public SpaceshipCombatProblem() {
        ProjectileManager.reset();
        PickupManager.reset();
        fitnessScores = new HashMap<double[], Double>();
        demoShips = new ArrayList<ComplexSpaceship>();
        demoConts = new ArrayList<ShipActionController>();
    }

    public int nDim() {
        return Constants.numWeights + (4 * Constants.numComponents) + 3;
    }

    public void runCombat(double[][] shipData) {
        ////////////////////////////////////
        // INITIALISING COMBAT
        ////////////////////////////////////

        // initialise simulation
        ProjectileManager.reset();
        PickupManager.reset();
        List<ComplexSpaceship> ships = new ArrayList<ComplexSpaceship>();
        List<ShipActionController> conts = new ArrayList<ShipActionController>();
        fitnessScores.clear();

        // populate simulation
        for(int i = 0; i < shipData.length; i++) {
            ComplexSpaceship ship = getInstance(shipData[i]);
            ship.setTeam(i);
            ships.add(ship);
            conts.add(new ShipActionController(ship));
            fitnessScores.put(ship.chromosome, 0.0);
        }

        // initialise scores
        for(ComplexSpaceship s : ships) {
            fitnessScores.put(s.chromosome, 0.0);
        }


        for(int i = 0; i<Constants.combatRepeats; i++) {
            ////////////////////////////////////
            // INITIALISING COMBAT SIMULATION
            ////////////////////////////////////
            ProjectileManager.reset();
            PickupManager.placePickups(Constants.pickupPlacementSeed);
            for(ComplexSpaceship s : ships) {
                // initialise properties
                s.reset();
                //s.pos = getRandStartPos(Constants.startRect);
                //s.rot = 0;

                if(s.radius >= Math.min(Constants.screenWidth, Constants.screenHeight)) {
                    s.alive = false;
                }
            }

            ////////////////////////////////////
            // RUNNING COMBAT SIMULATION
            ////////////////////////////////////

            // simulate ships for the determined duration
            int j = 0;
    //        double[] teamScores = new double[2];
    //        teamScores[0] = 0;
    //        teamScores[1] = 0;
            boolean allShipsDestroyed = false;
            while((j < Constants.timesteps) && !allShipsDestroyed) {
                allShipsDestroyed = true;

                // simulate all ships
                for(ShipActionController c : conts) {
                    //c.think(ships);
                }
                for(ComplexSpaceship s : ships) {
                    if(s.alive) {
                        s.update();
                        allShipsDestroyed = false;

                        // penalise bounce
                        double bouncePenalty = 0;
                        if(s.bounced) {
                            bouncePenalty = 20;
                        }

                        double oldScore = fitnessScores.get(s.chromosome);
                        fitnessScores.put(s.chromosome, oldScore - bouncePenalty);
                    }
                }

                // simulate bullets and resolve collisions
                for(Projectile p : ProjectileManager.getLivingProjectiles()) {
                    p.update();

                    for(ComplexSpaceship s : ships) {
                        if(s.alive && p.owner != s && s.isColliding(p) && (Constants.allowFriendlyFire || s.team != p.team) && p.alive) {
                            p.kill();
                            s.harm(Constants.defaultProjectileHarm);

                            double hitScore = 0;
                            if(!s.alive) hitScore = Constants.killReward;
                            else hitScore = Constants.hitReward;

                            // award ship
                            ComplexSpaceship attacker = p.owner;
                            double oldScore = fitnessScores.get(attacker.chromosome);
                            fitnessScores.put(attacker.chromosome, oldScore + hitScore);
                            // and team
    //                        teamScores[attacker.team] += hitScore;
                        }
                    }
                }

                // check for pickup collisions
                for(Pickup p : PickupManager.getLivingPickups()) {
                    for(ComplexSpaceship s : ships) {
                        if(s.alive && p.alive && s.isColliding(p)) {
                            p.dispenseReward(s);
                            // award ship for collecting non-mine pickup
                            double oldScore = fitnessScores.get(s.chromosome);
                            if(p.type != PickupType.MINE) {
                                fitnessScores.put(s.chromosome, oldScore + Constants.pickupReward);
                            } else {
                                fitnessScores.put(s.chromosome, oldScore - Constants.minePenalty);
                            }
                        }
                    }
                }

                j++;
            }

            ////////////////////////////////////
            // ASSIGNING END OF ROUND SCORES
            ////////////////////////////////////

            for(ComplexSpaceship s : ships) {
                double score = fitnessScores.get(s.chromosome);

                // reward hull integrity
                score += s.hull * Constants.hullRewardMul;
                // penalise for too many shots
                score -= s.bulletsFired * Constants.bulletPenaltyMul;


                // and put it back
                fitnessScores.put(s.chromosome, score);
            }
        }

        ////////////////////////////////////
        // DETERMINING FINAL SCORES
        ////////////////////////////////////
        for(ComplexSpaceship s : ships) {
            double score = fitnessScores.get(s.chromosome);
            // get average performance
            score /= Constants.combatRepeats;

            // penalise ship for having components further away
//            for(SpaceshipComponent sc : s.components) {
//                score -= 100 * Math.max((sc.attachPos.mag() - 50), 0);
//            }

            // if ship is too big, penalise it heavily
            if(s.radius >= Math.min(Constants.screenWidth, Constants.screenHeight)) {
                score = -Double.MAX_VALUE;
            }

            // flip score to error
            score *= -1;
            // and put it back
            fitnessScores.put(s.chromosome, score);
        }
    }

    public double testSingleShip(double[] x) {
        double score = 0;
        double[][] substitutePopulation = new double[1][];
        substitutePopulation[0] = x;
        runCombat(substitutePopulation);
        score = fitnessScores.get(x);
        return score;
    }

    public double fitness(double[] x) {
        //double score = 0;
        //score = fitnessScores.get(x);
        double score = testSingleShip(x);
        return score;
    }

    public ComplexSpaceship getInstance(double[] x) {
        return new ComplexSpaceship(x);
    }

    public void demonstrationInit(double[][] shipData) {

        ProjectileManager.reset();
        PickupManager.placePickups(Constants.pickupPlacementSeed);
        demoShips.clear();
        demoConts.clear();

        // set up sides
        for(int i = 0; i < shipData.length; i++) {
            ComplexSpaceship ship = getInstance(shipData[i]);
            ship.reset();
            ship.setTeam(i);
            //ship.pos = getRandStartPos(Constants.startRect);
            //ship.rot = 0;
            demoShips.add(ship);
            demoConts.add(new ShipActionController(ship));
        }

        // kill ships that are too big
        for(ComplexSpaceship s : demoShips) {
            // if ship is too big, destroy it
            if(s.radius >= Math.min(Constants.screenWidth, Constants.screenHeight)) {
                s.alive = false;
                System.out.println("Killed ship with radius of " + s.radius);
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
        synchronized (ProblemRunner.class) {
            for(ShipActionController c : demoConts) {
                //c.think(demoShips);
                c.think();
            }

            for(ComplexSpaceship s : demoShips) {
                s.update();
            }

            // move bullets, update collisions
            for(Projectile p : ProjectileManager.getLivingProjectiles()) {
                p.update();

                for(ComplexSpaceship s : demoShips) {
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
                for(ComplexSpaceship s : demoShips) {
                    if(s.alive && p.alive && s.isColliding(p)) {
                        p.dispenseReward(s);
                    }
                }
            }
        }
    }

    public Vector2d getRandStartPos(Rectangle startRect) {
        Vector2d startPos = new Vector2d();
        startPos.set(startRect.getX(), startRect.getY());
        startPos.x += Math.random() * startRect.getWidth();
        startPos.y += Math.random() * startRect.getHeight();
        return startPos;
    }

    public List<ComplexSpaceship> getShipsToDraw() {
        return demoShips;
    }

    public void printBestScoreOfRecentSim() {
        double best = Double.MAX_VALUE;
        for(Double d : fitnessScores.values()) {
            System.out.println("Score: " + d);
            if(d < best) best = d;
        }
        System.out.println("Best score (negative is better): " + best);
    }

    public List<ShipActionController> getControllersToDraw() {
        return demoConts;
    }
}
