package spaceship;

import java.awt.*;

// Exists purely as a dummy object for simple physics calculation purposes.

public class DummyObject extends SimObject {

    public DummyObject(SimObject other) {
        super(other);
    }

    @Override
    public void draw(Graphics2D g) {
        // do nothing;
    }
}
