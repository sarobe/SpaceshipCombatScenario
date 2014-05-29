package controller.statebased;

import common.math.Vector2d;
import spaceship.Spaceship;

import java.awt.*;
import java.awt.geom.AffineTransform;

/**
 * Created by Samuel Roberts, 2014
 */
public class HumanStateController extends StateController {

    // THESE VALUES ARE DIRECTLY ACCESSED AND MANIPULATED BY HumanStateControllerKeyHandler
    // THIS IS PROBABLY POOR JAVA PRACTICE
    public int forward = 0;
    public int turn = 0;

    public HumanStateController(Spaceship ship, Spaceship antagonist, boolean flag) {
        super(ship, antagonist, flag);
    }

    public void think() {
        super.think();
        // just use the raw values to move the ship
        ship.useRawAction(forward, turn);
    }

    public void draw(Graphics2D g) {
        super.draw(g);
        AffineTransform at = g.getTransform();

        g.translate(ship.pos.x, ship.pos.y);
        g.setColor(Color.YELLOW);
        g.drawOval((int)(-ship.radius), (int)(-ship.radius), (int)(ship.radius*2), (int)(ship.radius*2));

        Vector2d shipForward = ship.getForward();
        g.drawLine(0, 0, (int)(shipForward.x * 20), (int)(shipForward.y * 20));

        g.setTransform(at);

        // draw ship trail
        if(isPredator) g.setColor(Color.BLUE);
        else g.setColor(Color.ORANGE);

        Vector2d lastP = trace.get(0);
        for(Vector2d p : trace) {
            g.drawLine((int)p.x, (int)p.y, (int)lastP.x, (int)lastP.y);
            lastP = p;
        }
    }
}
