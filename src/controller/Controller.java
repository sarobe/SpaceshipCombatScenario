package controller;

import common.Constants;
import common.math.Vector2d;
import spaceship.Spaceship;
import spaceship.SimObject;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Samuel Roberts, 2013
 */
public abstract class Controller {

    public List<Vector2d> trace;
    public Color traceColor;
    public Spaceship ship;

    public Spaceship antagonist; // for predator vs prey
    public boolean isPredator = true; // for predator vs prey

    protected int timesteps;

    public Controller(Spaceship ship) {
        this.ship = ship;
        trace = new ArrayList<Vector2d>();
        traceColor = Color.BLUE;
        timesteps = 0;
    }

    public Controller(Spaceship ship, Spaceship antagonist, boolean flag) {
        this(ship);
        this.antagonist = antagonist;
        isPredator = flag;
        if(isPredator) {
            traceColor = Color.BLUE;
        } else {
            traceColor = Color.ORANGE;
        }
    }

    public void draw(Graphics2D g) {

        if(Constants.drawBoundingCircles) {
            AffineTransform at = g.getTransform();

            g.translate(ship.pos.x, ship.pos.y);
            g.setColor(Color.YELLOW);
            g.drawOval((int)(-ship.radius), (int)(-ship.radius), (int)(ship.radius*2), (int)(ship.radius*2));

            Vector2d shipForward = ship.getForward();
            g.drawLine(0, 0, (int)(shipForward.x * 20), (int)(shipForward.y * 20));

            g.setTransform(at);
        }

        if(Constants.drawTrails) {
            // draw ship trail
            if(trace.size() > 0) {
                g.setColor(traceColor);
                Vector2d lastP = trace.get(0);
                for(Vector2d p : trace) {
                    g.drawLine((int)p.x, (int)p.y, (int)lastP.x, (int)lastP.y);
                    lastP = p;
                }
            }
        }
    }

    abstract public void think(List<SimObject> ships);

    public void think() {
        trace.add(new Vector2d(ship.pos));
        timesteps++;
    }

    abstract public double getScore();

    public Spaceship getShip() {
        return ship;
    }
}
