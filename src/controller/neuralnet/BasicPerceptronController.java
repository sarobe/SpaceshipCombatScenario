package controller.neuralnet;

import common.Constants;
import common.math.MathUtil;
import common.math.Vector2d;
import common.utilities.Picker;
import controller.Controller;
import controller.statebased.StateController;
import problem.Asteroid;
import problem.AsteroidManager;
import spaceship.ComplexSpaceship;
import spaceship.SimObject;
import spaceship.Spaceship;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.util.List;

/**
 * Created by Samuel Roberts, 2014
 */
public class BasicPerceptronController extends Controller {

    public static Font labelFont = new Font("sans serif", Font.PLAIN, 12);

    public static int NODE_RADIUS = 40;
    public static int NODE_COLUMN_SPACING = 120;
    public static int MARGIN = 20;


    // input 1  -w1-  output 1  (thrust)
    //        w2\ /
    //        w3/  \
    // input 2  -w4-  output 2  (turn)

    // HARDCODING input 1 as the output of a ranger pointed forward to SOMETHING DESIRED
    // HARDCODING input 2 as the output of a ranger pointed forward to SOMETHING TO AVOID

    // weights of synapses
    public double w1;
    public double w2;
    public double w3;
    public double w4;

    // thresholds of outputs
    public double t1;
    public double t2;

    public double rangerLength = 2000;

    public double lastDesiredDist = 0;
    public double lastDangerousDist = 0;

    // directions of rangers
    public Vector2d desiredRangerDirection;
    public Vector2d dangerousRangerDirection;

    public BasicPerceptronController(Spaceship ship) {
        super(ship);
        init();
        if(!(ship instanceof ComplexSpaceship)) {
            useDefaultParameters();
        } else {
            useChromosomeParameters(((ComplexSpaceship) ship).chromosome);
        }
    }


    public BasicPerceptronController(ComplexSpaceship ship) {
        super(ship);
        init();
        useChromosomeParameters(ship.chromosome);
    }

    public BasicPerceptronController(Spaceship ship, Spaceship antagonist, boolean flag) {
        super(ship, antagonist, flag);
        init();
        if(!(ship instanceof ComplexSpaceship)) {
            useDefaultParameters();
        } else {
            useChromosomeParameters(((ComplexSpaceship) ship).chromosome);
        }
    }

    public BasicPerceptronController(ComplexSpaceship ship, Spaceship antagonist, boolean flag) {
        super(ship, antagonist, flag);
        init();
        useChromosomeParameters(ship.chromosome);
    }

    private void init() {
        desiredRangerDirection = ship.getForward();
        dangerousRangerDirection = ship.getForward();
    }

    public void useDefaultParameters() {
        w1 = 1;
        w2 = 1;
        w3 = 1;
        w4 = 1;
        t1 = 1;
        t2 = 100;
    }

    public void useChromosomeParameters(double[] chromosome) {
        // get the six weights from the ship chromosome
        w1 = chromosome[3];
        w2 = chromosome[4];
        w3 = chromosome[5];
        w4 = chromosome[6];
        t1 = chromosome[7];
        t2 = chromosome[8];
    }

    public void think(List<SimObject> ships) {
        think();
    }

    public void think() {
        super.think();

        // adjust ranger orientations
        desiredRangerDirection = ship.getForward();
        dangerousRangerDirection = ship.getForward();

        // get DESIRED RANGER output
        double i1 = desiredRangerDistance();

        // get DANGEROUS RANGER output
        double i2 = dangerousRangerDistance();

        // do some calculation with the hardwired topology
        double o1 = i1 * w1 + i2 * w3 - t1;
        double o2 = i1 * w2 + i2 * w4 - t2;

        // sanitise the output neurons and use the action
        int thrust = (int) o1;
        int turn = (int) o2;
        ship.useRawAction(thrust, turn);
    }

    private double dangerousRangerDistance() {
        // check the asteroids
        double closestDist = rangerLength;

        for (Asteroid a : AsteroidManager.getAsteroids()) {
            double dist = MathUtil.lineDistanceToCircle(ship.pos, dangerousRangerDirection, a.pos, a.radius);
            if ( dist != -1 && dist < closestDist ) {
                closestDist = dist;
            }
        }
        lastDangerousDist = closestDist;
        return closestDist;
    }

    private double desiredRangerDistance() {
        double dist = 0;
        if ( isPredator ) {
            dist = MathUtil.lineDistanceToCircle(ship.pos, desiredRangerDirection, antagonist.pos, antagonist.radius);
            if ( dist == -1 ) dist = rangerLength; // correct for faulty result
        } else {
            // no desired things for prey
            dist = rangerLength;
        }
        lastDesiredDist = dist;
        return dist;
    }


    @Override
    public double getScore() {
        return StateController.getScoreForPredatorPrey(ship, antagonist, isPredator, timesteps);
    }

    public void draw(Graphics2D g) {
        super.draw(g);
        AffineTransform at = g.getTransform();
        g.translate(ship.pos.x, ship.pos.y);

        // draw sensors
        g.setColor(Color.GREEN);
        g.drawLine(0, 0, (int) (desiredRangerDirection.x * rangerLength), (int) (desiredRangerDirection.y * rangerLength));
        g.setColor(Color.RED);
        g.drawLine(0, 0, (int) (dangerousRangerDirection.x * rangerLength), (int) (dangerousRangerDirection.y * rangerLength));

        g.setTransform(at);


        // draw neural network topology
        g.setColor(Color.WHITE);
        g.setFont(labelFont);

        // synapses

        int LINE_MIDPOINT_X = MARGIN + NODE_RADIUS + ((MARGIN + NODE_COLUMN_SPACING) - (MARGIN + NODE_RADIUS))/2;
        int LINE_MIDPOINT_Y = MARGIN + (int)(NODE_RADIUS * 1.5);
        // w1
        g.drawLine(MARGIN + NODE_RADIUS, MARGIN + NODE_RADIUS/2, MARGIN + NODE_COLUMN_SPACING, MARGIN + NODE_RADIUS/2);
        g.drawString((int)w1 + "", LINE_MIDPOINT_X - 10, MARGIN + NODE_RADIUS/2 - 10);
        // w2
        g.drawLine(MARGIN + NODE_RADIUS, MARGIN + NODE_RADIUS/2, MARGIN + NODE_COLUMN_SPACING, (int)(MARGIN + (2.5 * NODE_RADIUS)));
        g.drawString((int)w2 + "", LINE_MIDPOINT_X - 10, LINE_MIDPOINT_Y - 20);
        // w3
        g.drawLine(MARGIN + NODE_RADIUS, (int)(MARGIN + (2.5 * NODE_RADIUS)), MARGIN + NODE_COLUMN_SPACING, MARGIN + NODE_RADIUS/2);
        g.drawString((int)w3 + "", LINE_MIDPOINT_X - 10, LINE_MIDPOINT_Y + 20);
        // w4
        g.drawLine(MARGIN + NODE_RADIUS, (int)(MARGIN + (2.5 * NODE_RADIUS)), MARGIN + NODE_COLUMN_SPACING, (int)(MARGIN + (2.5 * NODE_RADIUS)));
        g.drawString((int)w4 + "", LINE_MIDPOINT_X - 10, (int)(MARGIN + (2.5 * NODE_RADIUS)) + 20);

        // inputs
        // i1
        g.drawOval(MARGIN, MARGIN, NODE_RADIUS, NODE_RADIUS);
        g.drawString((int)lastDesiredDist + "", MARGIN + NODE_RADIUS/2 - 12, MARGIN + NODE_RADIUS/2 + 5);
        // i2
        g.drawOval(MARGIN, MARGIN + NODE_RADIUS * 2, NODE_RADIUS, NODE_RADIUS);
        g.drawString((int)lastDangerousDist + "", MARGIN + NODE_RADIUS/2 - 12, MARGIN + 5 * NODE_RADIUS/2 + 5);

        // outputs
        double o1 = lastDesiredDist * w1 + lastDangerousDist * w3 - t1;
        double o2 = lastDesiredDist * w2 + lastDangerousDist * w4 - t2;
        // o1
        g.drawOval(MARGIN + NODE_COLUMN_SPACING, MARGIN, NODE_RADIUS, NODE_RADIUS);
        g.drawString((int)o1 + "", MARGIN + NODE_COLUMN_SPACING + NODE_RADIUS/2 - 12, MARGIN + NODE_RADIUS/2 + 5);
        g.drawString(" -> THRUST", MARGIN + NODE_COLUMN_SPACING + NODE_RADIUS + 10, MARGIN + NODE_RADIUS/2 + 5);
        // o2
        g.drawOval(MARGIN + NODE_COLUMN_SPACING, MARGIN + NODE_RADIUS * 2, NODE_RADIUS, NODE_RADIUS);
        g.drawString((int)o2 + "", MARGIN + NODE_COLUMN_SPACING + NODE_RADIUS/2 - 12, MARGIN + 5 * NODE_RADIUS/2 + 5);
            g.drawString(" -> TURN", MARGIN + NODE_COLUMN_SPACING + NODE_RADIUS + 10, MARGIN + 5 * NODE_RADIUS/2 + 5);


//        String actionLabel = "{ Fwd: " + lastFwd + ", Trn: " + lastTrn + " }";
//        g.drawString(stateLabel, -30, -45);
//        g.drawString(actionLabel, -40, -25);


        // draw neural network topology
    }


}
