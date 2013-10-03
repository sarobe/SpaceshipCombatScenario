package spaceship;

import common.Constants;
import common.math.Vector2d;
import problem.ProjectileManager;

import java.awt.*;
import java.awt.geom.AffineTransform;

/**
 * Created by Samuel Roberts
 * on 27/10/11
 */
public class Turret extends SpaceshipComponent {

    double fireVel;
    int[] turretX = {15, 0, 0, 15};
    int[] turretY = {-3, -5, 5, 3};
    int[] flameX = {17, 22, 22, 17};
    int[] flameY = {-4, -7, 7, 4};

    double counter = 0;


    public Turret(Spaceship parentShip, double fireVel) {
        super(parentShip);
        this.fireVel = fireVel;
        mass = 20;
        moment = 0.6;
    }

    public void update() {
        if(active && counter <= 0) {
            Projectile p = ProjectileManager.getNewProjectile(parentShip);
            if(p != null) {

                // calculate appropriate forces
                Vector2d fireOffset = attachPos.copy();
                fireOffset.rotate(parentShip.rot);
                Vector2d fireDirection = new Vector2d(1, 0);
                fireDirection.rotate(parentShip.rot + attachRot);


                // set projectile speed
                fireOffset.add(fireDirection, 15);
                Vector2d fireForce = fireDirection.mul(fireVel);

                p.pos = parentShip.pos.copy().add(fireOffset);
                p.vel = fireForce.mul(1/p.mass);
                p.vel.add(parentShip.vel);

                // set turret cooldown
                counter = Constants.weaponCooldown;

                // inform ship we just fired
                parentShip.fired();
            }
        }

        counter -= Constants.dt;
    }

    public void draw(Graphics2D g) {
        AffineTransform at = g.getTransform();
        g.translate(attachPos.x, attachPos.y);
        g.rotate(attachRot);
        g.setColor(Color.GRAY);
        g.fillPolygon(turretX, turretY, 4);
        g.setColor(Color.WHITE);
        g.drawPolygon(turretX, turretY, 4);
        if(active) {
            g.setColor(Color.GREEN);
            g.fillPolygon(flameX, flameY, 4);
        }
        g.setTransform(at);
    }

    public SpaceshipComponent copy(Spaceship newParent) {
        Turret copy = new Turret(newParent, fireVel);
        copy.attachPos = attachPos.copy();
        copy.attachRot = attachRot;
        return copy;
    }

    public String toString() {
        return "(GUN: " + attachPos + ", " + attachRot + ")";
    }
}
