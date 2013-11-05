package problem;

import java.awt.*;

/**
 * Created by Samuel Roberts, 2013
 */
public enum PickupType {
    FUEL (128, 128, 128),
    AMMO (96, 96, 96),
    HULL (255, 255, 255),
    MINE (255, 96, 96);

    public Color color;
    PickupType(int r, int g, int b) {
        color = new Color(r, g, b);
    }
}
