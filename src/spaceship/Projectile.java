package spaceship;

import common.Constants;
import common.math.Vector2d;

import java.awt.*;
import java.awt.geom.AffineTransform;

public class Projectile extends SimObject {

    public Spaceship owner;
    public double ttl;

    public Projectile(Spaceship owner) {
        this.owner = owner;
        mass = 20;
        radius = 3;
        ttl = Constants.projectileLifetime;
        useFriction = false;
    }

    public void update() {
        if(alive) super.update();
        ttl -= Constants.dt;
        if(ttl <= 0) alive = false;
    }

    public void hitSides() {
        ttl = 0;
        alive = false;
    }

    public void draw(Graphics2D g) {
        AffineTransform at = g.getTransform();

        g.translate(pos.x, pos.y);
        g.rotate(rot);
        g.setColor(owner.shipColor);
        g.fillOval(-(int)radius, -(int)radius, (int)radius*2, (int)radius*2);

        g.setTransform(at);
    }
}
