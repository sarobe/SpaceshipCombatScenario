package controller;

import common.Constants;
import common.math.Vector2d;
import common.utilities.Picker;
import controller.statebased.StateController;
import problem.entities.Asteroid;
import problem.managers.AsteroidManager;
import spaceship.*;

import java.awt.*;
import java.awt.geom.AffineTransform;
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

    public int lastFwd = 0;
    public int lastTrn = 0;

    public static Font stateFont = new Font("sans serif", Font.PLAIN, 12);

    public ActionState state;
    enum ActionState {
        NONE,
        APPROACHING_TARGET,
        AVOIDING_ASTEROID,
        AVOIDING_EDGE
    }

    public static double VISUALISER_SCALING = 30;

    public ConditionActionController(Spaceship ship) {
        super(ship);
        init();
        useDefaultParameters();
    }

    public ConditionActionController(ComplexSpaceship ship) {
        super(ship);
        init();
        useChromosomeParameters(ship.chromosome);
    }

    public ConditionActionController(Spaceship ship, Spaceship antagonist, boolean flag) {
        super(ship, antagonist, flag);
        init();
        useDefaultParameters();
    }

    public ConditionActionController(ComplexSpaceship ship, Spaceship antagonist, boolean flag) {
        super(ship, antagonist, flag);
        init();
        useChromosomeParameters(ship.chromosome);
    }

    public void init() {
        state = ActionState.NONE;
    }

    public void useDefaultParameters() {
        shipDistanceThreshold = 100;
        asteroidDistanceThreshold = 150;
        worldEdgeDistanceThreshold = 100;
        moveSpeed = 500;
        turnTolerance = Math.PI/16;
        brakeUntilSpeed = 100;
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
        int moveAction; moveAction = moveToPoint(antagonist.pos, moveSpeed);
        int turnAction; turnAction = turnToPoint(antagonist.pos, turnTolerance);

        if(isPredator) {
            // APPROACH antagonist position
            moveAction = moveToPoint(antagonist.pos, moveSpeed);
            turnAction = turnToPoint(antagonist.pos, turnTolerance);
        } else {
            // FLEE antagonist position
            Vector2d antagonistDir = antagonist.pos.copy().subtract(ship.pos);
            antagonistDir.mul(-1); // flip vector
            Vector2d fleeTarget = antagonist.pos.copy().add(antagonistDir);
            moveAction = moveToPoint(fleeTarget, moveSpeed);
            turnAction = turnToPoint(fleeTarget, turnTolerance);
        }

        // IMPORTANT: HIGHEST PRIORITY HAPPENS LAST
        // PRIORITY:
        // AVOID ASTEROID
        // AVOID EDGE
        // TARGET (approach if predator, flee if prey)


        // UNUSED - MOVE TO TARGET SHIP AT ALL TIMES INSTEAD
        thrust = moveAction;
        turn = turnAction;
        state = ActionState.APPROACHING_TARGET;

        // IF DISTANCE TO WORLD EDGE < DISTANCE THRESHOLD
        if(getEdgeDistance() < worldEdgeDistanceThreshold && ship.vel.mag() > brakeUntilSpeed) {
            int[] brakeAction = brake(getClosestEdgePoint(), brakeUntilSpeed);
            thrust = brakeAction[0];
            turn = brakeAction[1];
            state = ActionState.AVOIDING_EDGE;
        }

        // UNUSED - MOVE TO TARGET SHIP AT ALL TIMES INSTEAD

//        // IF TARGET SHIP < DISTANCE THRESHOLD
//        if(ship.pos.dist(antagonist.pos) < shipDistanceThreshold) {
//            thrust = brakeAction[0];
//            turn = brakeAction[1];
//        } else {
//            thrust = moveAction;
//            turn = turnAction;
//        }



        // IF NEAREST ASTEROID < DISTANCE THRESHOLD
        Vector2d nearestAsteroidPos = getNearestAsteroidPosition();
        if(ship.pos.dist(nearestAsteroidPos) < worldEdgeDistanceThreshold) {
            int[] brakeAction = brake(nearestAsteroidPos, brakeUntilSpeed);
            thrust = brakeAction[0];
            turn = brakeAction[1];
            state = ActionState.AVOIDING_ASTEROID;
        }


        // finally use the decisions we have made
        ship.useRawAction(thrust, turn);
        lastFwd = thrust;
        lastTrn = turn;
    }



    @Override
    public double getScore() {
        return StateController.getScoreForPredatorPrey(ship, antagonist, isPredator, timesteps);
    }

    public void draw(Graphics2D g) {
        super.draw(g);
        AffineTransform at = g.getTransform();
        g.translate(ship.pos.x, ship.pos.y);

        // draw forward direction
        Vector2d fwd = ship.getForward();
        g.setColor(Color.CYAN);
        g.drawLine(0, 0, (int)fwd.x * 3, (int)fwd.y * 3);

//        // draw counter-velocity direction (braking direction)
//        Vector2d brake = ship.vel.copy().rotate(Math.PI);
//        g.setColor(Color.RED);
//        g.drawLine(0, 0, (int)brake.x, (int)brake.y);

        // draw what the controller's trying to do
        g.setColor(Color.WHITE);
        g.setFont(stateFont);
        String stateLabel = "";
        switch(state) {
            case APPROACHING_TARGET:
                stateLabel = "Goto Target";
                break;
            case AVOIDING_ASTEROID:
                stateLabel = "Avoid Asteroid";
                break;
            case AVOIDING_EDGE:
                stateLabel = "Avoid Edge";
                break;
        }
        String actionLabel = "{ Fwd: " + lastFwd + ", Trn: " + lastTrn + " }";
        g.drawString(stateLabel, -30, -45);
        g.drawString(actionLabel, -40, -25);
        g.setTransform(at);

        // draw closest edge point
        Vector2d edgePoint = getClosestEdgePoint();
        g.setColor(Color.RED);
        g.drawOval((int)(edgePoint.x - 5), (int)(edgePoint.y - 5), 10, 10);
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

    public Vector2d getClosestEdgePoint() {
        Vector2d edgePoint = new Vector2d();
        switch(Constants.worldType) {
            case BOUNDED:
                edgePoint.x =  ship.pos.x < Constants.screenWidth/2 ? 0 : Constants.screenWidth;
                edgePoint.y =  ship.pos.y < Constants.screenHeight/2 ? 0 : Constants.screenHeight;
                break;
            case WRAPPING:
                // kind of a meaningless answer
                edgePoint.set(ship.pos);
                break;
            case CIRCULAR:
                Vector2d circleOrigin = new Vector2d(Constants.screenWidth/2, Constants.screenHeight/2);
                double circleRadius = Math.min(Constants.screenWidth, Constants.screenHeight)/2;
                double dx = ship.pos.x - circleOrigin.x;
                double dy = ship.pos.y - circleOrigin.y;
                double theta = Math.atan2(dy, dx);
                edgePoint.x = circleOrigin.x + circleRadius * Math.cos(theta);
                edgePoint.y = circleOrigin.y + circleRadius * Math.sin(theta);
                break;
        }

        return edgePoint;
    }

    public Vector2d getNearestAsteroidPosition() {
        List<Asteroid> asteroids = AsteroidManager.getAsteroids();
        Picker<Asteroid> p = new Picker<Asteroid>(Picker.MIN_FIRST);
        for(Asteroid a : asteroids) {
            p.add(a.pos.dist(ship.pos), a);
        }
        return p.getBest().pos.copy();
    }

    public int moveToPoint(Vector2d point, double maxSpeed) {
        // if we're not going to overshoot the target point accelerating
        // and we're under the max speed
        // accelerate
        // otherwise don't
        if(point.dist(ship.pos) > (ship.vel.mag() * Constants.dt) && (ship.vel.mag() < maxSpeed)) {
            return 1;
        } else {
            return 0;
        }
    }

    public int turnToPoint(Vector2d point, double tolerance) {
        // determine angle to turn to
        Vector2d targetHeading = point.copy().subtract(ship.pos).normalise();
        double angleA = Math.atan2(ship.getForward().y, ship.getForward().x);
        double angleB = Math.atan2(targetHeading.y, targetHeading.x);
        double angle = angleA - angleB;
        // angle used purely for tolerance

        double crossProd = targetHeading.crossProduct(ship.getForward());

        if(Math.abs(angle) > tolerance) {
            // turn!
            if(crossProd < 0) {
                return 1;
            } else {
                return -1;
            }
        } else {
            // otherwise don't turn
            return 0;
        }
    }

    public int[] brake(Vector2d hazardPoint, double maxSpeed) {
        // find angle to turn to (opposite of current velocity heading)
        //Vector2d targetMovePoint = ship.vel.copy().rotate(Math.PI);
        Vector2d targetMovePoint = ship.pos.copy().add(hazardPoint.copy().subtract(ship.pos), -1); // move to opposite direction of hazard point
        int turnAction = turnToPoint(targetMovePoint, turnTolerance);
        int moveAction;

        // check if we're pointed in a way that we can brake against our velocity
        // and if we're still going too fast
        if(ship.getForward().scalarProduct(targetMovePoint) < 0 && ship.vel.mag() > maxSpeed) {
            moveAction = 1; // we've turned around and we're still going too fast
        } else {
            moveAction = 0;
        }
//        return both thrust action and turn action
        int[] brakeAction = new int[2];
        brakeAction[0] = moveAction;
        brakeAction[1] = turnAction;
        return brakeAction;
    }

}
