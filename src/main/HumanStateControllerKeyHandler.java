package main;

import controller.HumanStateController;

import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

/**
 * Created by Samuel Roberts, 2014
 */
public class HumanStateControllerKeyHandler extends KeyAdapter {

    HumanStateController cont;

    public void keyPressed(KeyEvent e) {
        if (cont != null) {
            if (e.getKeyCode() == KeyEvent.VK_W) {
                cont.forward = 1;
            }
            if (e.getKeyCode() == KeyEvent.VK_A) {
                cont.turn = -1;
            }
            if (e.getKeyCode() == KeyEvent.VK_D) {
                cont.turn = 1;
            }
        }
    }

    public void keyReleased(KeyEvent e) {
        if (cont != null) {
            if (e.getKeyCode() == KeyEvent.VK_W) {
                cont.forward = 0;
            }
            if (e.getKeyCode() == KeyEvent.VK_A) {
                cont.turn = 0;
            }
            if (e.getKeyCode() == KeyEvent.VK_D) {
                cont.turn = 0;
            }
        }
    }

    public void setController(HumanStateController humanCont) {
        this.cont = humanCont;
    }
}
