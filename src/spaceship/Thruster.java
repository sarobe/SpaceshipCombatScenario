package spaceship;

import common.math.Vector2d;

import java.awt.*;
import java.awt.geom.AffineTransform;

/**
 * Created by Samuel Roberts
 * on 27/10/11
 */
public class Thruster extends SpaceshipComponent {

    double force;
    int[] thrusterX = {-10, 0, -10};
    int[] thrusterY = {-3, 0, 3};
    int[] flameX = {-12, -24, -12};
    int[] flameY = {-2, 0, 2};


    public Thruster(Spaceship parentShip, double force) {
        super(parentShip);
        this.force = force;
        mass = 12;
        moment = 0.5;
    }

    public void update() {
        if(active) {
            // calculate appropriate forces
            Vector2d thrusterOffset = attachPos.copy().subtract(parentShip.COM).rotate(parentShip.rot).normalise();
            Vector2d thrustDirection = new Vector2d(1, 0);
            thrustDirection.rotate(parentShip.rot + attachRot);

            // the following value is the Z component of a cross product of the two vectors with a Z value of 0
            double crossProd = (thrusterOffset.x * thrustDirection.y) - (thrusterOffset.y * thrustDirection.x);

            // apply push impulse
            Vector2d pushForce = thrustDirection.mul(force);
            pushForce.mul(1 / parentShip.mass);
            parentShip.vel.add(pushForce);

            // apply spin impulse
            double spinForce = crossProd * attachPos.mag();
            spinForce /= parentShip.moment;

            parentShip.rotvel += spinForce;
        }
    }

    public void draw(Graphics2D g) {
        AffineTransform at = g.getTransform();
        // draw the ship itself
        g.translate(attachPos.x, attachPos.y);
        g.rotate(attachRot);
        g.setColor(Color.GRAY);
        g.fillPolygon(thrusterX, thrusterY, 3);
        g.setColor(Color.WHITE);
        g.drawPolygon(thrusterX, thrusterY, 3);
        if(active) {
            g.setColor(Color.YELLOW);
            g.fillPolygon(flameX, flameY, 3);
        }
        g.setTransform(at);
    }

    public SpaceshipComponent copy(Spaceship newParent) {
        Thruster copy = new Thruster(newParent, force);
        copy.attachPos = attachPos.copy();
        copy.attachRot = attachRot;
        return copy;
    }

    public String toString() {
        return "(THR: " + attachPos + ", " + attachRot + ")";
    }
}
