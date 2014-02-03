package problem;

import common.Constants;
import common.math.Vector2d;
import spaceship.ComplexSpaceship;
import spaceship.SimObject;
import spaceship.Spaceship;

import java.awt.*;
import java.awt.geom.AffineTransform;

/**
 * Created by Samuel Roberts, 2013
 */
public class Pickup extends SimObject {

    public static Font labelFont = new Font("sans serif", Font.PLAIN, 10);
    public PickupType type;

    public Pickup(Vector2d position, PickupType type) {
        pos = position.copy();
        this.type = type;
        radius = 4;
    }

    public void update() {
        super.update();
    }

    public void dispenseReward(Spaceship ship) {
        switch(type) {
            case AMMO:
                ship.bullets = (int)Math.min(Constants.maximumBullets, ship.bullets + Constants.ammoPickupAmount);
                break;
            case HULL:
                ship.hull = Math.min(ship.maxHull, ship.hull + Constants.hullPickupAmount);
                break;
            case FUEL:
                ship.fuel = Math.min(Constants.maximumFuel, ship.fuel + Constants.fuelPickupAmount);
                break;
            case MINE:
                ship.harm(Constants.mineDamageAmount);
                break;
        }
        alive = false;
    }

    public void draw(Graphics2D g) {
        AffineTransform at = g.getTransform();

        g.translate(pos.x, pos.y);
        g.rotate(rot);
        g.setColor(type.color);
        g.fillOval(-(int)radius, -(int)radius, (int)radius*2, (int)radius*2);

        g.setFont(labelFont);
        g.drawString(type.toString(), -(int)radius - 12, -(int)radius - 5);

        g.setTransform(at);
    }

}