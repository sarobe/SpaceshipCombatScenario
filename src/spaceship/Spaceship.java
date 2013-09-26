package spaceship;

import common.Constants;
import common.math.Vector2d;
import controller.ShipState;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Samuel Roberts
 * on 24/10/11
 */
public class Spaceship extends SimObject {

    // hull polygon
    public Polygon hullShape;

    // attached components
    public List<SpaceshipComponent> components;

    // centre of mass
    public Vector2d COM;

    // draw a hit flash?
    public boolean justHit = false;

    // just fired a projectile?
    public boolean justFired = false;

    // total bullets fired
    public int bulletsFired = 0;

    // what is the maximum hull the ship can have?
    public double maxHull = 0;

    public Spaceship() {
        super();
        mass = 100;
        hull = 30;
        maxHull = 30;
        moment = 1;
        components = new ArrayList<SpaceshipComponent>();
        COM = new Vector2d();
        hullShape = new Polygon();
    }

    public Spaceship(double[] x) {
        this();
        for(int i=0; i<x.length; i += 4) {
            // for each triple of doubles
            // the first double is the type
            SpaceshipComponent c;
            int type = (int)Math.abs((x[i])) % 2;
            if(type == 1) {
                c = new Turret(this, Constants.defaultFireVel);
            } else {
                c = new Thruster(this, Constants.defaultThrust);
            }
            // the next two doubles are the position of a thruster
            c.attachPos = new Vector2d(x[i+1], x[i+2]);
            // and the fourth double is its rotation
            c.attachRot = x[i+3];


            addComponent(c);
        }
        rebalance();
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
    }

    public void harm(double harm) {
        hull -= harm;
        justHit = true;
        if(hull < 0) {
            alive = false;
        }
    }

    public void reset() {
        pos.x = 0;
        pos.y = 0;
        vel.x = 0;
        vel.y = 0;
        rot = 0;
        for(SpaceshipComponent sc : components) {
            sc.active = false;
        }
        hull = maxHull;
        bulletsFired = 0;
        justFired = false;
        alive = true;
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
        Color shipColor = Color.WHITE;
        if(team == 1) shipColor = Color.RED;
        if(team == 2) shipColor = Color.BLUE;

        g.setColor(shipColor);
        g.fillPolygon(hullShape);
        g.setColor(Color.GRAY);
        g.drawPolygon(hullShape);

        // draw the ship components
        for(SpaceshipComponent sc : components) {
            sc.draw(g);
        }

        // draw ship "core"
        g.setColor(Color.CYAN);
        if(justHit) {
            g.fillOval(-(int)radius, -(int)radius, (int)radius*2, (int)radius*2);
            justHit = false;
        } else {
            g.drawOval(-(int)radius, -(int)radius, (int)radius*2, (int)radius*2);
        }

        // draw health bars
        g.rotate(-rot);
        g.setColor(Color.GREEN);
        // first off, the tank outline
        g.drawRect((int)(-radius), (int)(-radius*1.5), (int)(radius*2), 6);
        // then, the tank contents
        int hullIntegrity = (int)((radius*2) * (hull/maxHull));
        g.fillRect((int)(-radius), (int)(-radius*1.5), hullIntegrity, 6);

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
            maxHull += massChange/10;
            maxHull = Math.min(maxHull, Constants.maximumHull);
            hull = maxHull;

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
            moment += strutMass * 0.1 * Math.pow(strutPos.dist(COM), 2);

            // increase size of bounding radius
            if(comp.attachPos.mag()/2 > radius) {
                radius = comp.attachPos.mag()/2;
            }
        }
    }

    // Use after ALL COMPONENTS HAVE BEEN ADDED to adjust all positions such
    // that the geometric centre and centre of mass have been made equal.
    public void rebalance() {
        // translate everything from COM to origin
        for(SpaceshipComponent sc : components) {
            sc.attachPos.subtract(COM);
        }

        hullShape = new Polygon();
        mass = 100;
        maxHull = 30;
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
            maxHull += massChange/10;
            maxHull = Math.min(maxHull, Constants.maximumHull);
            hull = maxHull;

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
            moment += strutMass * 0.1 * Math.pow(strutPos.dist(COM), 2);

            // increase size of bounding radius
            if(comp.attachPos.mag() > radius) {
                radius = comp.attachPos.mag();
            }
        }
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
