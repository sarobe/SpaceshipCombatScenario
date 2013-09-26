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

    private List<Spaceship> ships;
    private SpaceshipCombatProblem problem;

    public static Font statFont = new Font("sans serif", Font.BOLD, 16);

    public SpaceshipVisualiser(List<Spaceship> ships, SpaceshipCombatProblem problem) {
        this.ships = ships;
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

            for(Spaceship ship : problem.otherShips) {
                if(ship.alive) ship.draw(g2d);
            }

            for(Spaceship ship : ships) {
                if(ship.alive) ship.draw(g2d);
            }

            g.setFont(statFont);
            g.setColor(Color.WHITE);
            g.drawString("Left Team Score: " + (int)problem.demoScoreLeft, 10, 20);
            g.drawString("Right Team Score: " + (int)problem.demoScoreRight, 10, 40);
        }
    }

    public Dimension getPreferredSize() {
        return new Dimension(Constants.screenWidth, Constants.screenHeight);
    }
}
