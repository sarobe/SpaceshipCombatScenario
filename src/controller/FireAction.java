package controller;

import common.math.Vector2d;
import spaceship.Spaceship;

public class FireAction {
    public int encoded;
    public Vector2d fireOrigin;
    public Vector2d fireDir;

    public FireAction(int encoded, Vector2d fireOrigin, Vector2d fireDir) {
        this.encoded = encoded;
        this.fireOrigin = fireOrigin;
        this.fireDir = fireDir;
    }

    public Vector2d getFireOrigin(Spaceship ship) {
        return fireOrigin.copy().rotate(ship.rot);
    }

    public Vector2d getFireDir(Spaceship ship) {
        return fireDir.copy().rotate(ship.rot);
    }
}
