package game;

import common.Constants;
import common.utilities.JEasyFrame;
import main.KeyHandler;
import main.SpaceshipVisualiser;

/**
 * Created by Samuel Roberts, 2013
 */
public class GameRunner {

    public static void main(String[] args) {
        // set up graphical elements
        Game game = new Game();
        GameVisualiser gv = new GameVisualiser(game);
        JEasyFrame frame = new JEasyFrame(gv, "Basic Spaceship Game");
        frame.addKeyListener(new GameKeyHandler(game));

        try {
            while (true) {
                game.update();
                gv.repaint();
                Thread.sleep(Constants.delay);
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }

        frame.dispose();
    }
}
