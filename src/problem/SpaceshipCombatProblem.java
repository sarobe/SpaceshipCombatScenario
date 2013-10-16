package problem;

import common.Constants;
import common.math.Vector2d;
import controller.ShipActionController;
import main.Runner;
import main.SpaceshipVisualiser;
import spaceship.Projectile;
import spaceship.Spaceship;
import spaceship.SpaceshipComponent;

import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Samuel Roberts, 2012
 */
public class SpaceshipCombatProblem {

    public double demoScoreLeft = 0;
    public double demoScoreRight = 0;

    public Map<double[], Double> fitnessScores;

    public List<Spaceship> demoShips;
    public List<ShipActionController> demoConts;

    public SpaceshipCombatProblem() {
        ProjectileManager.reset();
        fitnessScores = new HashMap<double[], Double>();
        demoShips = new ArrayList<Spaceship>();
        demoConts = new ArrayList<ShipActionController>();
    }

    public int nDim() {
        return Constants.numWeights + (4 * Constants.numComponents);
    }

    public void runCombat(double[][] teamA, double[][] teamB) {
        ////////////////////////////////////
        // INITIALISING COMBAT
        ////////////////////////////////////

        // initialise simulation
        ProjectileManager.reset();
        List<Spaceship> ships = new ArrayList<Spaceship>();
        List<ShipActionController> conts = new ArrayList<ShipActionController>();
        fitnessScores.clear();

        // populate simulation
        for(int i = 0; i < teamA.length; i++) {
            Spaceship ship = getInstance(teamA[i]);
            ship.reset();
            ship.setTeam(Constants.TEAM_LEFT);
            ship.pos = getRandStartPos(Constants.leftTeamStartRect);
            ship.rot = 0;
            ships.add(ship);
            conts.add(new ShipActionController(ship));
            fitnessScores.put(ship.chromosome, 0.0);
        }

        // set up other side
        for(int i = 0; i < teamB.length; i++) {
            Spaceship ship = getInstance(teamB[i]);
            ship.reset();
            ship.setTeam(Constants.TEAM_RIGHT);
            ship.pos = getRandStartPos(Constants.rightTeamStartRect);
            ship.rot = 0;
            ships.add(ship);
            conts.add(new ShipActionController(ship));
            fitnessScores.put(ship.chromosome, 0.0);
        }

        // initialise scores and assign any initial penalties for designs
        for(Spaceship s : ships) {
            double score = 0;

            // penalise ship for having components further away
            for(SpaceshipComponent sc : s.components) {
                score -= 100 * Math.max((sc.attachPos.mag() - 50), 0);
            }

            // if ship is too big, destroy it
            if(s.radius >= Math.min(Constants.screenWidth, Constants.screenHeight)) {
                s.alive = false;
                score -= 999999999;
            }

            // assign initial score
            fitnessScores.put(s.chromosome, 0.0);
        }

        // kill ships that are initially overlapping others
//        for(int i = 0; i < ships.size(); i++) {
//            for(int j=i; j < ships.size(); j++) {
//                if(i == j) continue;
//                Spaceship shipA = ships.get(i);
//                Spaceship shipB = ships.get(j);
//                double dist = shipA.pos.dist(shipB.pos);
//                if(dist < shipA.radius + shipB.radius) {
//                    // kill a ship for overlapping
//                    //shipA.alive = false;
//                    //fitnessScores.put(shipA.chromosome, fitnessScores.get(shipA.chromosome) - 999999);
//                }
//            }
//        }

        ////////////////////////////////////
        // RUNNING COMBAT SIMULATION
        ////////////////////////////////////

        // simulate ships for the determined duration
        int i = 0;
        double[] teamScores = new double[2];
        teamScores[0] = 0;
        teamScores[1] = 0;
        boolean allShipsDestroyed = false;
        while((i < Constants.timesteps) && !allShipsDestroyed) {
            allShipsDestroyed = true;

            // simulate all ships
            for(ShipActionController c : conts) {
                c.think(ships);
            }
            for(Spaceship s : ships) {
                if(s.alive) {
                    s.update();
                    allShipsDestroyed = false;
                }
            }

            // simulate bullets and resolve collisions
            for(Projectile p : ProjectileManager.getLivingProjectiles()) {
                p.update();

                for(Spaceship s : ships) {
                    if(s.alive && s.isColliding(p) && s.team != p.team) {
                        p.kill();
                        s.harm(Constants.defaultProjectileHarm);

                        double hitScore = 0;
                        if(!s.alive) hitScore = Constants.defaultProjectileHarm * 10;
                        else hitScore = Constants.defaultProjectileHarm;

                        // award ship
                        Spaceship attacker = p.owner;
                        double oldScore = fitnessScores.get(attacker.chromosome);
                        fitnessScores.put(attacker.chromosome, oldScore + hitScore);
                        // and team
                        teamScores[attacker.team] += hitScore;
                    }
                }
            }

            i++;
        }

        ////////////////////////////////////
        // ASSIGNING POST SIMULATION SCORES
        ////////////////////////////////////

        for(Spaceship s : ships) {
            double score = fitnessScores.get(s.chromosome);

            // reward for life left
            score += s.hull * 200;
            // penalise for too many shots
            score -= s.bulletsFired;
            // penalise for death
            if(!s.alive) score -= Constants.defaultProjectileHarm * 100;
            // grant a bonus based on team score
            score += teamScores[s.team] * Constants.teamScoreWeight;

            // invert scores to error by negation
            score *= -1;
            // and put it back
            fitnessScores.put(s.chromosome, score);
        }
    }

    public double fitness(double[] x) {
        return fitnessScores.get(x);
    }

    public Spaceship getInstance(double[] x) {
        return new Spaceship(x);
    }

    public void demonstrationInit(double[][] teamA, double[][] teamB) {

        ProjectileManager.reset();
        demoShips.clear();
        demoConts.clear();

        // set up sides
        for(int i = 0; i < teamA.length; i++) {
            Spaceship ship = getInstance(teamA[i]);
            ship.reset();
            ship.setTeam(Constants.TEAM_LEFT);
            ship.pos = getRandStartPos(Constants.leftTeamStartRect);
            ship.rot = 0;
            demoShips.add(ship);
            demoConts.add(new ShipActionController(ship));
        }

        // set up other side
        for(int i = 0; i < teamB.length; i++) {
            Spaceship ship = getInstance(teamB[i]);
            ship.reset();
            ship.setTeam(Constants.TEAM_RIGHT);
            ship.pos = getRandStartPos(Constants.rightTeamStartRect);
            ship.rot = 0;
            demoShips.add(ship);
            demoConts.add(new ShipActionController(ship));
        }

        // kill ships that are too big
        for(Spaceship s : demoShips) {
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

        demoScoreLeft = 0;
        demoScoreRight = 0;

    }

    public void demonstrate() {
        synchronized (Runner.class) {
            for(ShipActionController c : demoConts) {
                c.think(demoShips);
            }

            for(Spaceship s : demoShips) {
                s.update();
            }

            // move bullets, update collisions
            for(Projectile p : ProjectileManager.getLivingProjectiles()) {
                p.update();

                for(Spaceship s : demoShips) {
                    if(s.alive && s.isColliding(p) && s.team != p.team) {
                        p.kill();
                        s.harm(Constants.defaultProjectileHarm);
                        if(s.team == Constants.TEAM_LEFT) demoScoreRight += Constants.defaultProjectileHarm;
                        else demoScoreLeft += Constants.defaultProjectileHarm;
                        if(!s.alive) {
                            if(s.team == Constants.TEAM_LEFT) demoScoreRight += Constants.defaultProjectileHarm * 10;
                            else demoScoreLeft += Constants.defaultProjectileHarm * 10;
                        }
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

    public List<Spaceship> getShipsToDraw() {
        return demoShips;
    }
}
