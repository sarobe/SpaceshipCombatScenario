package spaceship;

import common.Constants;
import common.math.Vector2d;
import controller.Controller;
import controller.ShipState;
import problem.ProjectileManager;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.GeneralPath;
import java.awt.geom.Path2D;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by Samuel Roberts
 * on 24/10/11
 */
public class Spaceship extends SimObject {

    // chromosome
    public double[] chromosome;

    // hull polygon
    public Polygon hullShape;
    public Shape hitShape;

    // attached components
    public List<SpaceshipComponent> components;

    // centre of mass
    public Vector2d COM;

    // draw a hit flash?
    public boolean justHit = false;

    // just fired a projectile?
    public boolean justFired = false;

    // ship color (random, derived from chromosome)
    public Color shipColor;
    public Color shipHighlightColor;

    // total bullets fired
    public int bulletsFired = 0;

    // what is the maximum hull the ship can have?
    public double maxHull = 0;

    // how much fuel does the ship have?
    public double fuel = 0;

    // how many bullets does the ship have left?
    public int bullets = 0;

    // some encoded action integers relating to basic ship actions
    public int forward = 0;
    public Vector2d forwardDir;
    public int turnCW = 0;
    public int turnCCW = 0;

    public Spaceship() {
        super();
        mass = 10;
        hull = Constants.maximumHull;
        maxHull = Constants.maximumHull;
        moment = 1;
        fuel = Constants.maximumFuel;
        bullets = Constants.maximumBullets;
        components = new ArrayList<SpaceshipComponent>();
        COM = new Vector2d();
        hullShape = new Polygon();
        chromosome = null;
        shipColor = Color.WHITE;
        shipHighlightColor = Color.WHITE;
    }

    public Spaceship(double[] x) {
        this();
        chromosome = x;
        // the arbitrary +3 is for the first three parameters of the chromosome being for position and rotation
        for(int i=Constants.numWeights + 3; i<x.length; i += 4) {
            // for each triple of doubles
            // the first double is the type
            SpaceshipComponent c;
            if(x[i] < 0) {
                c = new Turret(this, Constants.defaultFireVel);
            } else {
                c = new Thruster(this, Constants.defaultThrust);
            }
            // the next two doubles are the position of a thruster
            c.attachPos = new Vector2d(x[i+1]*Constants.componentScale, x[i+2]*Constants.componentScale);
            // and the fourth double is its rotation
            c.attachRot = x[i+3]/10;


            addComponent(c);
        }
        rebalance();
        setInitialPlacement();
        calculateActions();
        shipColor = makeColorFromChromosome(x);
        shipHighlightColor = shipColor.brighter();
    }


    public Vector2d getForward() {
        // Calculate the best "forwards" direction of the ship
        // or the direction the ship can move the fastest in a linear direction
        Vector2d d = new Vector2d();
        for(SpaceshipComponent sc : components) {
            d.add(new Vector2d(1, 0).rotate(sc.attachRot));
        }
        d.normalise();
        d.rotate(rot);
        return d;
    }

    public void setState(ShipState state) {
        pos.x = state.px;
        pos.y = state.py;
        vel.x = state.vx;
        vel.y = state.vy;
        rot = state.rot;
        rotvel = state.vrot;
        hull = state.hull;
        alive = state.alive;
        justFired = state.justFired;
        bulletsFired = state.bulletsFired;
        bullets = state.bullets;
        fuel = state.fuel;
    }

    public void harm(double harm) {
        hull -= harm;
        justHit = true;
        if(hull < 0) {
            alive = false;
        }
    }

    public void reset() {
        if(chromosome == null) {
            pos.x = 0;
            pos.y = 0;
            rot = 0;
        }
        else {
            setInitialPlacement();
        }
        vel.x = 0;
        vel.y = 0;
        for(SpaceshipComponent sc : components) {
            sc.active = false;
        }
        hull = maxHull;
        bullets = Constants.maximumBullets;
        fuel = Constants.maximumFuel;
        justFired = false;
        alive = true;
    }

    private void setInitialPlacement() {
        pos.x = Constants.screenWidth/2 + chromosome[0];
        pos.y = Constants.screenHeight/2 + chromosome[1];
        rot = chromosome[2];
    }

    public void update() {
        if(!alive) return;
        if(justFired) justFired = false;
        for(SpaceshipComponent sc : components) {
            sc.update();
        }
        super.update();
    }

    public void draw(Graphics2D g) {
        AffineTransform at = g.getTransform();

        // draw the ship itself
        g.translate(pos.x, pos.y);
        g.rotate(rot);

        // set team colours
        // Color shipColor = Color.WHITE;
        //if(team == Constants.TEAM_LEFT) shipColor = Color.RED;
        //if(team == Constants.TEAM_RIGHT) shipColor = Color.BLUE;

        g.setColor(shipColor);
        g.fillPolygon(hullShape);
        g.setColor(Color.GRAY);
        g.drawPolygon(hullShape);

        // draw the ship components
        for(SpaceshipComponent sc : components) {
            sc.draw(g);
        }

        if(justHit) {
            g.setColor(Color.WHITE);
            g.fillPolygon(hullShape);
            justHit = false;
        }

        // draw HEALTH
        g.rotate(-rot);
        g.setColor(shipHighlightColor);
        // first off, the tank outline
        g.drawRect((int)(-radius), (int)(-radius*1.5), (int)(radius*2), 6);
        // then, the tank contents
        int hullIntegrity = (int)((radius*2) * (hull/maxHull));
        g.fillRect((int)(-radius), (int)(-radius*1.5), hullIntegrity, 6);

        // draw FUEL
        g.setColor(Color.GRAY);
        g.drawRect((int)(-radius), (int)(-radius*1.5 + 12), (int)(radius*2), 3);
        int fuelContained = (int)((radius*2) * (fuel/Constants.maximumFuel));
        g.fillRect((int)(-radius), (int)(-radius*1.5 + 12), fuelContained, 3);

        // draw BULLETS
        g.setColor(Color.WHITE);
        g.drawRect((int)(-radius), (int)(-radius*1.5 + 17), (int)(radius*2), 2);
        int bulletsHeld = (int)((radius*2) * (bullets/(float)Constants.maximumBullets));
        g.fillRect((int)(-radius), (int)(-radius*1.5 + 17), bulletsHeld, 2);


        g.setTransform(at);
    }

    public Spaceship copyShip() {
        Spaceship copy = new Spaceship();
        copy.pos = pos.copy();
        copy.rot = rot;
        copy.vel = vel.copy();
        copy.rotvel = rotvel;
        for(SpaceshipComponent sc : components) {
            SpaceshipComponent copiedComponent = sc.copy(copy);
            copy.addComponent(copiedComponent);
        }
        copy.rebalance();
        copy.calculateActions();
        return copy;
    }

    public void addComponent(SpaceshipComponent comp) {
        // check component is valid
        boolean valid = true;
        for(SpaceshipComponent sc : components) {
            if(sc.attachPos.dist(comp.attachPos) < Constants.thrusterRadiusLimit) {
                valid = false;
            }
        }

        // add component if valid
        if(valid) {
            double massChange = 0;
            components.add(comp);
            massChange += comp.mass;

            // add a strut (makes components further away have more effect on mass)
            double strutMass = comp.attachPos.mag() * 2;
            massChange += strutMass;

            // update hull shape
            hullShape.addPoint((int)comp.attachPos.x, (int)comp.attachPos.y);

            // update hull integrity based on mass
            mass += massChange;
            //maxHull += massChange/10;
            //maxHull = Math.min(maxHull, Constants.maximumHull);
            //hull = maxHull;

            // calculate centre of mass
            COM.set(0, 0);
            for(SpaceshipComponent sc : components) {
                COM.add(sc.attachPos, sc.mass);
            }
            COM.mul(1/mass);

            // calculate moment based on component and strut
            // component
            moment += comp.moment * Math.pow(comp.attachPos.dist(COM), 2);
            // strut
            Vector2d strutPos = comp.attachPos.copy().mul(0.5);
            moment += strutMass * 0.005  * Math.pow(strutPos.dist(COM), 2);

            // increase size of bounding radius
            if(comp.attachPos.mag()/2 > radius) {
                radius = comp.attachPos.mag()/2;
            }
        }
    }

    public boolean isColliding(SimObject other) {
        // use basic detection first to see if fine detection is needed
        boolean colliding = super.isColliding(other);
        if(colliding) {
            // then use polygon detection instead of radius detection
            // treat other object as a point
            AffineTransform tr = new AffineTransform();
            tr.translate(pos.x, pos.y);
            tr.rotate(rot);
            Path2D path = new GeneralPath(hullShape);
            hitShape = path.createTransformedShape(tr);
            colliding = hitShape.contains(other.pos.x, other.pos.y);
        }
        return colliding;
    }

    // Use after ALL COMPONENTS HAVE BEEN ADDED to adjust all positions such
    // that the geometric centre and centre of mass have been made equal.
    public void rebalance() {
        // translate everything from COM to origin
        for(SpaceshipComponent sc : components) {
            sc.attachPos.subtract(COM);
        }

        hullShape = new Polygon();
        mass = 10;
        maxHull = Constants.maximumHull;
        moment = 1;

        // recalculate everything
        for(SpaceshipComponent comp : components) {
            double massChange = 0;
            massChange += comp.mass;

            // add a strut (makes components further away have more effect on mass)
            double strutMass = comp.attachPos.mag() * 2;
            massChange += strutMass;

            // update hull shape
            hullShape.addPoint((int)comp.attachPos.x, (int)comp.attachPos.y);

            // update hull integrity based on mass
            mass += massChange;
            //maxHull += massChange/10;
            //maxHull = Math.min(maxHull, Constants.maximumHull);
            //hull = maxHull;

            // calculate centre of mass
            COM.set(0, 0);
            for(SpaceshipComponent sc : components) {
                COM.add(sc.attachPos, sc.mass);
            }
            COM.mul(1/mass);

            // calculate moment based on component and strut
            // component
            moment += comp.moment * Math.pow(comp.attachPos.dist(COM), 2);
            // strut
            Vector2d strutPos = comp.attachPos.copy().mul(0.5);
            moment += strutMass * 0.005 * Math.pow(strutPos.dist(COM), 2);

            // increase size of bounding radius
            if(comp.attachPos.mag() > radius) {
                radius = comp.attachPos.mag();
            }
        }
    }

    public void calculateActions() {
        int numActions = (int)Math.pow(2, Constants.numComponents);
        ShipState initialState = new ShipState(this);
        ProjectileManager.suppressNewProjectiles(true);

        double highestThrust = 0;
        double highestCCWTorque = 0;
        double highestCWTorque = Double.MAX_VALUE;

        for(int i = 0; i < numActions; i++) {
            rot = 0;
            Controller.binaryToActions(this, i);
            update();

            // ignore any actions that turn on a turret
            if(!justFired) {

                // if it produces the most thrust, use it as forwards
                if(vel.mag() > highestThrust) {
                    forward = i;
                    forwardDir = vel.copy().normalise();
                    highestThrust = vel.mag();
                }
                // if it produces the most CW torque, use it as CW turn
                if(rot > highestCCWTorque) {
                    turnCCW = i;
                    highestCCWTorque = rot;
                }
                if(rot < highestCWTorque) {
                    turnCW = i;
                    highestCWTorque = rot;
                }
            }

            setState(initialState);
        }
        ProjectileManager.suppressNewProjectiles(false);
    }

    public Color makeColorFromChromosome(double[] x) {
        // mess around with chromosome and make it into a colour
        double r = 0;
        double g = 0;
        double b = 0;
        for(int i=0; i<x.length; i++) {
            if(i%3 == 0) r += Math.abs(x[i]);
            if(i%3 == 1) g += Math.abs(x[i]);
            if(i%3 == 2) b += Math.abs(x[i]);
        }
        double tot = r+g+b;
        r = (r/tot) * 255;
        g = (g/tot) * 255;
        b = (b/tot) * 255;

        return new Color((int)r, (int)g, (int)b);
    }

    public String toString() {
        String result = "";
        for(SpaceshipComponent sc : components) {
            result += sc + " ";
        }
        return result;
    }

    public void fired() {
        justFired = true;
        bulletsFired++;
    }
}
