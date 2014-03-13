package problem;

import common.math.Vector2d;
import spaceship.SimObject;

import java.awt.*;
import java.awt.geom.AffineTransform;

public class Asteroid extends SimObject {

    public Asteroid(double radius, Vector2d pos, Vector2d vel) {
        super(pos, 0);
        this.radius = radius;
        this.vel.set(vel);
        useFriction = false;
    }

    @Override
    public void draw(Graphics2D g) {
        AffineTransform at = g.getTransform();

        g.translate(pos.x, pos.y);
        g.rotate(rot);
        g.setColor(Color.WHITE);
        g.fillOval(-(int)radius, -(int)radius, (int)radius*2, (int)radius*2);

        g.setTransform(at);
    }
}
