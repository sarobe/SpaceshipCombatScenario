package main;

import common.Constants;
import problem.ProjectileManager;
import problem.SpaceshipCombatProblem;
import spaceship.Projectile;
import spaceship.Spaceship;

import javax.swing.*;
import java.awt.*;
import java.util.List;

/**
 * Created by Samuel Roberts, 2012
 */
public class SpaceshipVisualiser extends JComponent {

    private SpaceshipCombatProblem problem;

    public static Font statFont = new Font("sans serif", Font.BOLD, 16);

    public SpaceshipVisualiser(SpaceshipCombatProblem problem) {
        this.problem = problem;
    }

    public void paintComponent(Graphics g) {
        Graphics2D g2d = (Graphics2D)g;
        g2d.setColor(Color.BLACK);
        g2d.fillRect(0, 0, Constants.screenWidth, Constants.screenHeight);

        synchronized(Runner.class) {

            for(Projectile p : ProjectileManager.getLivingProjectiles()) {
                p.draw(g2d);
            }

            for(Spaceship ship : problem.getShipsToDraw()) {
                if(ship.alive) ship.draw(g2d);
            }

            //g.setFont(statFont);
            //g.setColor(Color.WHITE);
        }
    }

    public Dimension getPreferredSize() {
        return new Dimension(Constants.screenWidth, Constants.screenHeight);
    }
}
