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
import java.util.List;

/**
 * Created by Samuel Roberts, 2012
 */
public class SpaceshipCombatProblem {

    public List<Spaceship> otherShips;
    public List<ShipActionController> otherConts;
    public boolean shipOnLeft;


    public double demoScoreLeft = 0;
    public double demoScoreRight = 0;

    public SpaceshipCombatProblem() {
        ProjectileManager.reset();
    }

    public int nDim() {
        return 4 * Constants.numComponents;
    }

    public void constructEnemyShips(double[][] otherChromosomes) {
        otherShips = new ArrayList<Spaceship>();
        otherConts = new ArrayList<ShipActionController>();
        for(double[] otherChromosome : otherChromosomes) {
            Spaceship otherShip = getInstance(otherChromosome);
            otherShip.setTeam(2);
            ShipActionController otherCont = new ShipActionController(otherShip);
            otherShips.add(otherShip);
            otherConts.add(otherCont);
        }
    }

    public void setShipsOnLeft(boolean shipOnLeft) {
        this.shipOnLeft = shipOnLeft;
    }

    public double fitness(double[] x) {
        ProjectileManager.reset();
        Spaceship ship = getInstance(x);
        ship.setTeam(1);
        ShipActionController cont = new ShipActionController(ship);

        double score = 0;

        // penalise ship for having components further away
        for(SpaceshipComponent sc : ship.components) {
            score += 10 * Math.max((sc.attachPos.mag() - 50), 0);
        }

        ship.rot = 0;
        if(shipOnLeft) ship.pos.set(Constants.leftStartPos);
        else ship.pos.set(Constants.rightStartPos);

        // initialise enemy ships
        for(Spaceship s : otherShips) {
            s.reset();
            s.setTeam(2);
            if(!shipOnLeft) s.pos.set(Constants.leftStartPos);
            else s.pos.set(Constants.rightStartPos);
        }

        List<Spaceship> thisShip = new ArrayList<Spaceship>();
        thisShip.add(ship);

        int i = 0;
        while(i < Constants.timesteps) {

            // simulate enemy ships
            for(ShipActionController sc : otherConts) {
                sc.think(thisShip);
            }
            for(Spaceship s : otherShips) {
                s.update();
            }

            // simulate this ship
            cont.think(otherShips);
            ship.update();

            // simulate bullets and resolve collisions
            for(Projectile p : ProjectileManager.getLivingProjectiles()) {
                p.update();

                for(Spaceship s : otherShips) {
                    if(s.alive && p.isColliding(s)) {
                        // for every hit on an enemy ship, reward the ship
                        p.kill();
                        s.harm(Constants.defaultProjectileHarm);
                        if(s.team == 2) {
                            score -= Constants.defaultProjectileHarm;
                            // bonus if the ship is destroyed
                            if(!s.alive) score -= Constants.defaultProjectileHarm * 10;
                        }
                    }
                }
            }

            // if our ship is dead, end run
            if(!ship.alive) i = Constants.timesteps;
            i++;
        }

        // reward for life left
        score -= ship.hull * 200;
        // penalise for too many shots
        score += ship.bulletsFired;
        // penalise for death
        score += (Constants.timesteps - i) * 300;

        ship.reset();


        return score;
    }

    public Spaceship getInstance(double[] x) {
        return new Spaceship(x);
    }

    public void demonstrationInit(List<Spaceship> ships, List<ShipActionController> conts, boolean givenShipsOnLeft) {

        ProjectileManager.reset();

        // set up given side
        for(int i = 0; i < ships.size(); i++) {
            Spaceship ship = ships.get(i);
            ship.reset();
            ship.setTeam(1);
            if(givenShipsOnLeft) ship.pos = Constants.leftStartPos.copy();
            else ship.pos = Constants.rightStartPos.copy();
            ship.rot = 0;
        }

        // set up other side
        for(int i = 0; i < otherShips.size(); i++) {
            Spaceship ship = otherShips.get(i);
            ship.reset();
            ship.setTeam(2);
            if(!givenShipsOnLeft) ship.pos = Constants.leftStartPos.copy();
            else ship.pos = Constants.rightStartPos.copy();
            ship.rot = 0;
        }

        demoScoreLeft = 0;
        demoScoreRight = 0;

    }

    public void demonstrate(List<Spaceship> ships, List<ShipActionController> conts) {
        synchronized (Runner.class) {
            for(ShipActionController c : conts) {
                c.think(otherShips);
            }
            for(ShipActionController otherC : otherConts) {
                otherC.think(ships);
            }

            for(Spaceship s : ships) {
                s.update();
            }
            for(Spaceship otherS : otherShips) {
                otherS.update();
            }

            // move bullets, update collisions
            for(Projectile p : ProjectileManager.getLivingProjectiles()) {
                p.update();

                for(Spaceship s : ships) {
                    if(s.alive && p.isColliding(s)) {
                        p.kill();
                        s.harm(Constants.defaultProjectileHarm);
                        if(shipOnLeft) demoScoreRight += Constants.defaultProjectileHarm;
                        else demoScoreLeft += Constants.defaultProjectileHarm;
                        if(!s.alive) {
                            if(shipOnLeft) demoScoreRight += Constants.defaultProjectileHarm * 10;
                            else demoScoreLeft += Constants.defaultProjectileHarm * 10;
                            System.out.println("Ship on team " + s.team + " destroyed!");
                        }
                    }
                }

                for(Spaceship s : otherShips) {
                    if(s.alive && p.isColliding(s)) {
                        p.kill();
                        s.harm(Constants.defaultProjectileHarm);
                        if(!shipOnLeft) demoScoreRight += Constants.defaultProjectileHarm;
                        else demoScoreLeft += Constants.defaultProjectileHarm;
                        if(!s.alive) {
                            if(!shipOnLeft) demoScoreRight += Constants.defaultProjectileHarm;
                            else demoScoreLeft += Constants.defaultProjectileHarm;
                            System.out.println("Ship on team " + s.team + " destroyed!");
                        }
                    }
                }

            }
        }
    }

}
