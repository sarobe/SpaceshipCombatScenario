package main;

import common.Constants;
import common.utilities.JEasyFrame;
import controller.Controller;
import problem.IProblem;
import problem.entities.Asteroid;
import problem.managers.AsteroidManager;
import problem.managers.ProjectileManager;
import spaceship.ComplexSpaceship;
import spaceship.Projectile;
import spaceship.Spaceship;

import javax.swing.*;
import java.awt.*;

/**
 * Created by Samuel Roberts, 2012
 */
public class SpaceshipViewer extends JComponent {


    public static Font statFont = new Font("sans serif", Font.PLAIN, 4);
    public static Color bgColor = Color.white;
    private ComplexSpaceship ship;

    public static void main(String[] args) {
        double[] chromosome = {-102.73895913061146, 80.4536813822079, -190.07631854955284, 231.57978872973436, 116.16377119592839, 103.60301850054971, -47.789662132528015, 371.93723206898363, 91.64608832372998, -192.0701546302887, -94.52368101354523, -111.57414108483144, -192.9436604478859, 95.13536432843065, 160.19910123273706, 82.18220358588046, -113.59052059010384, -93.76034965421113, -47.5125979450776, 153.79769135748361, -96.1662443577818, 109.49010072382015, 61.34042244527505, 267.0740614751983, -14.162956707088668, -62.719014029134215, -64.72226275659085, 143.1808982044669, 73.14550087853179};

        new JEasyFrame(new SpaceshipViewer(chromosome), "Spaceship");
    }

    public SpaceshipViewer(double[] chromosome) {
        this.ship = new ComplexSpaceship(chromosome);
    }

    public void paintComponent(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;
        g2d.setColor(bgColor);
        g2d.fillRect(0, 0, Constants.screenWidth, Constants.screenHeight);

        Dimension currentSize = getSize();
        ship.pos.x = currentSize.getWidth() * 0.5;
        ship.pos.y = currentSize.getHeight() * 0.5;
        ship.draw(g2d);
    }

    public Dimension getPreferredSize() {
        return new Dimension(Constants.screenWidth, Constants.screenHeight);
    }
}
