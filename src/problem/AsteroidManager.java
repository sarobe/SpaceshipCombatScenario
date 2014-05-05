package problem;

import common.Constants;
import common.math.Vector2d;

import java.util.*;

public class AsteroidManager {
    public static List<Asteroid> asteroidList;

    public static void reset() {
        asteroidList = new ArrayList<Asteroid>();
    }

    public static void placeAsteroids(int seed) {
        reset();
        Random rand = new Random(seed);

        for (int i = 0; i < Constants.numAsteroids; i++) {

            Vector2d placement = new Vector2d(rand.nextDouble() * Constants.screenWidth, rand.nextDouble() * Constants.screenHeight);
            Vector2d velocity = new Vector2d(rand.nextGaussian() * Constants.maxVelocityRange, rand.nextGaussian() * Constants.maxVelocityRange);
            double radius = Constants.minAsteroidRadius + (rand.nextDouble() * (Constants.maxAsteroidRadius - Constants.minAsteroidRadius));

            Asteroid asteroid = new Asteroid(radius, placement, velocity);
            asteroidList.add(asteroid);
        }
    }

    public static List<Asteroid> getAsteroids() {
        return asteroidList;
    }

    public static AsteroidsState saveState() {
        return new AsteroidsState(asteroidList);
    }

    public static void loadState(AsteroidsState state) {
        for (int i = 0; i < asteroidList.size(); i++) {
            Asteroid a = asteroidList.get(i);
            a.pos.set(state.positions.get(i));
            a.vel.set(state.velocities.get(i));
        }
    }
}
