package game;

import common.Constants;
import main.ProblemRunner;
import problem.managers.PickupManager;
import problem.managers.ProjectileManager;
import problem.entities.Pickup;
import spaceship.ComplexSpaceship;
import spaceship.Projectile;

import javax.swing.*;
import java.awt.*;

/**
 * Created by Samuel Roberts, 2012
 */
public class GameVisualiser extends JComponent {

    private Game game;

    public static Font statFont = new Font("sans serif", Font.BOLD, 16);

    public GameVisualiser(Game game) {
        this.game = game;
    }

    public void paintComponent(Graphics g) {
        Graphics2D g2d = (Graphics2D)g;
        g2d.setColor(Color.BLACK);
        g2d.fillRect(0, 0, Constants.screenWidth, Constants.screenHeight);

        synchronized(ProblemRunner.class) {

            for(Projectile p : ProjectileManager.getLivingProjectiles()) {
                p.draw(g2d);
            }

            for(Pickup p : PickupManager.getLivingPickups()) {
                p.draw(g2d);
            }

            for(ComplexSpaceship ship : game.getShipsToDraw()) {
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
