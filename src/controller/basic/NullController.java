package controller.basic;

import controller.StateController;
import spaceship.SimObject;
import spaceship.Spaceship;

import java.util.List;

/**
 * Created by Samuel Roberts, 2014
 */
public class NullController extends StateController {

    // A controller that does *NOTHING*

    public NullController(Spaceship ship) {
        super(ship);
    }

    public NullController(Spaceship ship, Spaceship antagonist, boolean flag) {
        super(ship, antagonist, flag);
    }

    @Override
    public void think(List<SimObject> ships) {
        // not used at the moment
        think();
    }

    @Override
    public void think() {
        super.think();
    }
}
