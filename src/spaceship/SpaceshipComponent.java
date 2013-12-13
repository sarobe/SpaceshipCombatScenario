package spaceship;

import common.math.Vector2d;

import java.awt.*;

/**
 * Created by Samuel Roberts
 * on 24/10/11
 */
public abstract class SpaceshipComponent {

    public ComplexSpaceship parentShip;
    public Vector2d attachPos;
    public double attachRot;
    public boolean active;

    public double mass;
    public double moment;

    public SpaceshipComponent(ComplexSpaceship parentShip) {
        this.parentShip = parentShip;
    }

    public abstract void draw(Graphics2D g);
    public abstract void update();
    public abstract SpaceshipComponent copy(ComplexSpaceship newParent);

    public void setActive(boolean active) {
        this.active = active;
    }
}
