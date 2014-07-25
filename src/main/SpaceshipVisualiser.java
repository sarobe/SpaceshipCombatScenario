package main;

import common.Constants;
import controller.Controller;
import problem.*;
import problem.entities.Asteroid;
import problem.managers.AsteroidManager;
import problem.managers.ProjectileManager;
import spaceship.Projectile;
import spaceship.Spaceship;

import javax.swing.*;
import java.awt.*;

/**
 * Created by Samuel Roberts, 2012
 */
public class SpaceshipVisualiser extends JComponent {

    private IProblem problem;

    public static Font statFont = new Font("sans serif", Font.PLAIN, 4);
    public static Color bgColor = new Color(0x5B8659);

    public SpaceshipVisualiser(IProblem problem) {
        this.problem = problem;
    }

    public void paintComponent(Graphics g) {
        Graphics2D g2d = (Graphics2D)g;
        g2d.setColor(bgColor);
        g2d.fillRect(0, 0, Constants.screenWidth, Constants.screenHeight);

        /*if(InfluenceMap.getMap() != null) {
            for(int y=0; y<InfluenceMap.getHeight(); y++) {
                for(int x=0; x<InfluenceMap.getWidth(); x++) {
                    double value = InfluenceMap.getValueAtCell(x, y);
                    double colorValue = InfluenceMap.getNormalisedValue(value);
                    g.setColor(Color.getHSBColor(0.3f, (float)colorValue, 0.3f));
                    g.fillRect(x*InfluenceMap.CELL_SIZE, y*InfluenceMap.CELL_SIZE, InfluenceMap.CELL_SIZE, InfluenceMap.CELL_SIZE);
                    g.setColor(Color.getHSBColor(0.3f, (float)colorValue, 0.6f));
                    g.drawString(String.format("%.2f", value), (x*InfluenceMap.CELL_SIZE) + 4, (y*InfluenceMap.CELL_SIZE) + 14);
                }
            }
        }*/

        if(Constants.worldType == Constants.WorldType.CIRCULAR) {
            g.setColor(Color.WHITE);
            int radius = Math.min(Constants.screenWidth, Constants.screenHeight)/2;
            g.drawOval(Constants.screenWidth/2 - radius, Constants.screenHeight/2 - radius, radius*2, radius*2);
        }

        synchronized(ProblemRunner.class) {

            for(Projectile p : ProjectileManager.getLivingProjectiles()) {
                p.draw(g2d);
            }

//            for(Pickup p : PickupManager.getLivingPickups()) {
//                p.draw(g2d);
//            }

            for(Asteroid a : AsteroidManager.getAsteroids()) {
                a.draw(g2d);
            }

            for(Spaceship ship : problem.getShips()) {
                if(ship.alive) ship.draw(g2d);
            }

            for(Controller cont : problem.getControllers()) {
                if(cont.ship.alive) cont.draw(g2d);
            }



            //g.setFont(statFont);
            //g.setColor(Color.WHITE);
        }
    }
    public Dimension getPreferredSize() {
        return new Dimension(Constants.screenWidth, Constants.screenHeight);
    }
}
