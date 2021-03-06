package problem.managers;

import common.Constants;
import common.math.Vector2d;
import problem.entities.Pickup;

import java.util.*;

public class PickupManager {

    public static List<Pickup> pickupList;


    public static void reset() {
        pickupList = new ArrayList<Pickup>();
    }

    public static void placePickups(int seed) {
        reset();
        Random rand = new Random(seed);

        for(int i = 0; i < Constants.numPickups + Constants.numMines; i++) {

            Vector2d placement = new Vector2d(rand.nextDouble() * Constants.screenWidth, rand.nextDouble() * Constants.screenHeight);

            PickupType type;
            if(i >= Constants.numPickups) {
                type = PickupType.MINE;
            } else {
                int typeSelection = (int)(rand.nextDouble() * PickupType.values().length - 1);
                type = PickupType.values()[typeSelection];
            }


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

    public static int getTotalPickups() {
        int totalNum = 0;
        List<Pickup> livingPickups = new ArrayList<Pickup>();
        for(Pickup p : pickupList) {
            if(p.type != PickupType.MINE) {
                totalNum++;
            }
        }
        return totalNum;
    }

    public static List<Pickup> getLivingMines() {
        List<Pickup> livingMines = new ArrayList<Pickup>();
        for(Pickup p : pickupList) {
            if(p.type == PickupType.MINE && p.alive) {
                livingMines.add(p);
            }
        }
        return livingMines;

    }

    public static int pickupsRemaining() {
        int numRemaining = 0;
        for(Pickup p : pickupList) {
            if(p.alive) {
                numRemaining++;
            }
        }
        return numRemaining;
    }

    public static Map<Pickup, Boolean> getPickupStates() {
        Map<Pickup, Boolean> pickupStates = new HashMap<Pickup, Boolean>();
        for(Pickup p : PickupManager.pickupList) {
            pickupStates.put(p, !p.alive);
        }
        return pickupStates;
    }

    public static void setPickupStates(Map<Pickup,Boolean> states) {
        Map<Pickup, Boolean> pickupStates = new HashMap<Pickup, Boolean>();
        for(Pickup p : states.keySet()) {
            pickupStates.put(p, states.get(p));
        }
    }
}
