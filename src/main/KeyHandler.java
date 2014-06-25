package main;

import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

/**
 * Created by Samuel Roberts, 2012
 */
public class KeyHandler extends KeyAdapter {

    ProblemRunner r;
    
    public KeyHandler(ProblemRunner r) {
        this.r = r;
    }
    
    
    public void keyReleased(KeyEvent e) {
        if(e.getKeyCode() == KeyEvent.VK_SPACE) {
            r.runDemo = false;
        }
    }
}
