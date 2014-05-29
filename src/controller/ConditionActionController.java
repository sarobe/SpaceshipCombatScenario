package controller;

import common.Constants;
import common.math.MathUtil;
import common.math.Vector2d;
import common.utilities.Picker;
import controller.statebased.StateController;
import problem.*;
import spaceship.*;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Samuel Roberts, 2014
 */
public class ConditionActionController extends Controller {

    public double shipDistanceThreshold;
    public double asteroidDistanceThreshold;
    public double worldEdgeDistanceThreshold;

    public double moveSpeed;
    public double turnTolerance;
    public double brakeUntilSpeed;

    public static double VISUALISER_SCALING = 30;

    public ConditionActionController(Spaceship ship) {
        super(ship);
        useDefaultParameters();
    }

    public ConditionActionController(ComplexSpaceship ship) {
        super(ship);
        useChromosomeParameters(ship.chromosome);
    }

    public ConditionActionController(Spaceship ship, Spaceship antagonist, boolean flag) {
        super(ship, antagonist, flag);
        useDefaultParameters();
    }

    public ConditionActionController(ComplexSpaceship ship, Spaceship antagonist, boolean flag) {
        super(ship, antagonist, flag);
        useChromosomeParameters(ship.chromosome);
    }

    public void useDefaultParameters() {
        shipDistanceThreshold = 100;
        asteroidDistanceThreshold = 150;
        worldEdgeDistanceThreshold = 100;
        moveSpeed = 200;
        turnTolerance = Math.PI/8;
        brakeUntilSpeed = 50;
    }

    public void useChromosomeParameters(double[] chromosome) {
        // get the six weights from the ship chromosome
        shipDistanceThreshold = chromosome[3];
        asteroidDistanceThreshold = chromosome[4];
        worldEdgeDistanceThreshold = chromosome[5];
        moveSpeed = chromosome[6];
        turnTolerance = chromosome[7];
        brakeUntilSpeed = chromosome[8];
    }

    public void think(List<SimObject> ships) {
        think();
    }

    public void think() {
        super.think();

        // raw action values to use
        // individual sub actions say whether or not to use these raw action values
        int thrust = 0;
        int turn = 0;

        // determine in advance the values for different actions
        int[] brakeAction = brake(brakeUntilSpeed);
        int moveAction = moveToPoint(antagonist.pos, moveSpeed);
        int turnAction = turnToPoint(antagonist.pos, turnTolerance);

        // IMPORTANT: HIGHEST PRIORITY HAPPENS LAST
        // PRIORITY:
        // AVOID ASTEROID
        // TARGET
        // AVOID EDGE

        // IF DISTANCE TO WORLD EDGE < DISTANCE THRESHOLD
        if(getEdgeDistance() < worldEdgeDistanceThreshold) {
            thrust = brakeAction[0];
            turn = brakeAction[1];
        }

        // IF TARGET SHIP < DISTANCE THRESHOLD
        if(ship.pos.dist(antagonist.pos) < shipDistanceThreshold) {
            thrust = brakeAction[0];
            turn = brakeAction[1];
        } else {
            thrust = moveAction;
            turn = turnAction;
        }

        // IF NEAREST ASTEROID < DISTANCE THRESHOLD
        if(getNearestAsteroidDistance() < worldEdgeDistanceThreshold) {
            thrust = brakeAction[0];
            turn = brakeAction[1];
        }


        // finally use the decisions we have made
        ship.useRawAction(thrust, turn);
    }



    @Override
    public double getScore() {
        return StateController.getScoreForPredatorPrey(ship, antagonist, isPredator, timesteps);
    }

    public void draw(Graphics2D g) {
        super.draw(g);
        AffineTransform at = g.getTransform();
        g.translate(ship.pos.x, ship.pos.y);

        g.setTransform(at);
    }

    public double getEdgeDistance() {
        double edgeDistance = 0;
        switch(Constants.worldType) {
            case BOUNDED:
                double xDist = Math.min(ship.pos.x, Constants.screenWidth - ship.pos.x);
                double yDist = Math.min(ship.pos.y, Constants.screenHeight - ship.pos.y);
                edgeDistance = Math.min(xDist, yDist);
                break;
            case WRAPPING:
                // kind of a meaningless answer
                edgeDistance = Math.max(Constants.screenWidth, Constants.screenHeight);
                break;
            case CIRCULAR:
                Vector2d circleOrigin = new Vector2d(Constants.screenWidth/2, Constants.screenHeight/2);
                double circleRadius = Math.min(Constants.screenWidth, Constants.screenHeight)/2;
                edgeDistance = circleRadius - circleOrigin.dist(ship.pos);
                break;
        }
        return edgeDistance;
    }

    public double getNearestAsteroidDistance() {
        List<Asteroid> asteroids = AsteroidManager.getAsteroids();
        Picker<Asteroid> p = new Picker<Asteroid>(Picker.MIN_FIRST);
        for(Asteroid a : asteroids) {
            p.add(a.pos.dist(ship.pos), a);
        }
        return p.getBestScore();
    }

    public int moveToPoint(Vector2d point, double maxSpeed) {
        // if we're not going to overshoot the target point accelerating
        // and we're under the max speed
        // accelerate
        // otherwise don't
        if(point.dist(ship.pos) > (ship.vel.mag() / Constants.dt) && (ship.vel.mag() < maxSpeed)) {
            return 1;
        } else {
            return 0;
        }
    }

    public int turnToPoint(Vector2d point, double tolerance) {
        // determine angle to turn to
        Vector2d targetHeading = point.copy().subtract(ship.pos).normalise();
        double angle = targetHeading.angBetween(ship.getForward());

        if(Math.abs(angle) > tolerance) {
            // turn!
            if(angle >= 0) {
                return 1;
            } else {
                return -1;
            }
        } else {
            // otherwise don't turn
            return 0;
        }
    }

    public int[] brake(double maxSpeed) {
        // find angle to turn to (opposite of current velocity heading)
        Vector2d targetMovePoint = ship.vel.copy().rotate(Math.PI);
        int turnAction = turnToPoint(targetMovePoint, turnTolerance);
        int moveAction;

        // check if we're pointed in a way that we can brake against our velocity
        // and if we're still going too fast
        if(ship.getForward().scalarProduct(targetMovePoint) < 0 && ship.vel.mag() > maxSpeed) {
            moveAction = 1; // we've turned around and we're still going too fast
        } else {
            moveAction = 0;
        }
        // return both thrust action and turn action
        int[] brakeAction = new int[2];
        brakeAction[0] = moveAction;
        brakeAction[1] = turnAction;
        return brakeAction;
    }

}
