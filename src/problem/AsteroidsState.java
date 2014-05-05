package problem;

import common.math.Vector2d;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Samuel Roberts, 2014
 */
public class AsteroidsState {

    public List<Vector2d> positions;
    public List<Vector2d> velocities;

    public AsteroidsState(List<Asteroid> asteroids) {
        positions = new ArrayList<Vector2d>();
        velocities = new ArrayList<Vector2d>();

        for(Asteroid a : asteroids) {
            positions.add(a.pos.copy());
            velocities.add(a.vel.copy());
        }
    }
}
