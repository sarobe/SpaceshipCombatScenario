package game;

import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

/**
 * Created by Samuel Roberts, 2012
 */
public class GameKeyHandler extends KeyAdapter {

    public Game game;

    public GameKeyHandler(Game game) {
        this.game = game;
    }


    public void keyPressed(KeyEvent e) {
        if(e.getKeyCode() == KeyEvent.VK_W) {
            game.playerActionOne.forward = 1;
        }
        if(e.getKeyCode() == KeyEvent.VK_A) {
            game.playerActionOne.turn = 1;
        }
        if(e.getKeyCode() == KeyEvent.VK_D) {
            game.playerActionOne.turn = -1;
        }
        if(e.getKeyCode() == KeyEvent.VK_SHIFT) {
            game.playerActionOne.shoot = true;
        }

        if(e.getKeyCode() == KeyEvent.VK_I) {
            game.playerActionTwo.forward = 1;
        }
        if(e.getKeyCode() == KeyEvent.VK_J) {
            game.playerActionTwo.turn = 1;
        }
        if(e.getKeyCode() == KeyEvent.VK_L) {
            game.playerActionTwo.turn = -1;
        }
        if(e.getKeyCode() == KeyEvent.VK_SPACE) {
            game.playerActionTwo.shoot = true;
        }
    }
    
    public void keyReleased(KeyEvent e) {
        if(e.getKeyCode() == KeyEvent.VK_W) {
            game.playerActionOne.forward = 0;
        }
        if(e.getKeyCode() == KeyEvent.VK_A) {
            game.playerActionOne.turn = 0;
        }
        if(e.getKeyCode() == KeyEvent.VK_D) {
            game.playerActionOne.turn = 0;
        }
        if(e.getKeyCode() == KeyEvent.VK_SHIFT) {
            game.playerActionOne.shoot = false;
        }

        if(e.getKeyCode() == KeyEvent.VK_I) {
            game.playerActionTwo.forward = 0;
        }
        if(e.getKeyCode() == KeyEvent.VK_J) {
            game.playerActionTwo.turn = 0;
        }
        if(e.getKeyCode() == KeyEvent.VK_L) {
            game.playerActionTwo.turn = 0;
        }
        if(e.getKeyCode() == KeyEvent.VK_SPACE) {
            game.playerActionTwo.shoot = false;
        }
    }
}
