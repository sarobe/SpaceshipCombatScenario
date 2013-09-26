package spaceship;

import common.math.Vector2d;

import java.awt.*;
import java.awt.geom.AffineTransform;

public class Projectile extends SimObject {


    public Projectile() {
        mass = 20;
        radius = 3;
    }

    public void draw(Graphics2D g) {
        AffineTransform at = g.getTransform();

        // set team colours
        Color teamColor = Color.WHITE;
        if(team == 1) teamColor = Color.RED;
        if(team == 2) teamColor = Color.BLUE;

        g.translate(pos.x, pos.y);
        g.rotate(rot);
        g.setColor(teamColor);
        g.fillOval(-(int)radius, -(int)radius, (int)radius*2, (int)radius*2);

        g.setTransform(at);
    }
}
