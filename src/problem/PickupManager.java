package problem;

import common.Constants;
import common.math.Vector2d;
import spaceship.Pickup;
import spaceship.PickupType;
import spaceship.Projectile;
import spaceship.Spaceship;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class PickupManager {

    public static List<Pickup> pickupList;


    public static void reset() {
        pickupList = new ArrayList<Pickup>();
    }

    public static void placePickups(int seed) {
        reset();
        Random rand = new Random(seed);

        for(int i = 0; i < Constants.numPickups; i++) {

            Vector2d placement = new Vector2d(rand.nextDouble() * Constants.screenWidth, rand.nextDouble() * Constants.screenHeight);

            int typeSelection = (int)(Math.random() * 3);
            PickupType type = PickupType.values()[typeSelection];

            Pickup pickup = new Pickup(placement, type);
            pickupList.add(pickup);
        }
    }

    public static List<Pickup> getLivingPickups() {
        List<Pickup> livingPickups = new ArrayList<Pickup>();
        for(Pickup p : pickupList) {
            if(p.alive) {
                livingPickups.add(p);
            }
        }
        return livingPickups;
    }

}
