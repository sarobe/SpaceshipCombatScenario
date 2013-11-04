package game;

import common.Constants;
import problem.PickupManager;
import problem.ProjectileManager;
import problem.Pickup;
import spaceship.Projectile;
import spaceship.Spaceship;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Samuel Roberts, 2013
 */
public class Game {

    public List<Spaceship> ships;

    public PlayerAction playerActionOne;
    public PlayerAction playerActionTwo;

    public PlayerController playerControllerOne;
    public PlayerController playerControllerTwo;

    // TEMPORARY SHIP DESIGN
    double[] tempShipDesign =
            {0, 0, 0, // position and rotation (totally irrelevant here)
                    100, 100, 100, 100, // weights (totally irrelevant here)
                    0, 0.0, -200.0, 10* Math.PI/2,	// thruster (components scale down by 10)
                    0, 200.0, 0.0, 10*Math.PI, // thruster (components scale down by 10)
                    1, 80.0,  120.0, 10*(Math.PI)/2, // turret (components scale down by 10)
                    0, -80.0,  120.0, 10*(Math.PI)/2, // turret (components scale down by 10)
                    0, -200.0, 0.0, 10*0};  // thruster (components scale down by 10)

    public Game() {
        init();
    }

    public void init() {
        ships = new ArrayList<Spaceship>();
        PickupManager.placePickups(Constants.pickupPlacementSeed);
        ProjectileManager.reset();
        // create player one stuff
        playerActionOne = new PlayerAction(0, 0, false);
        Spaceship playerOneShip = new Spaceship(tempShipDesign);
        playerOneShip.pos.x = Constants.screenWidth/2 - Constants.screenWidth/3;
        playerOneShip.pos.y = Constants.screenHeight/2;
        playerOneShip.rot = 0;
        ships.add(playerOneShip);
        playerControllerOne = new PlayerController(playerOneShip);

        // create player two stuff
        playerActionTwo = new PlayerAction(0, 0, false);
        Spaceship playerTwoShip = new Spaceship(tempShipDesign);
        playerTwoShip.pos.x = Constants.screenWidth/2 + Constants.screenWidth/3;
        playerTwoShip.pos.y = Constants.screenHeight/2;
        playerTwoShip.rot = 0;
        ships.add(playerTwoShip);
        playerControllerTwo = new PlayerController(playerTwoShip);
    }

    public void update() {
        playerControllerOne.think(playerActionOne);
        playerControllerTwo.think(playerActionTwo);

        for(Spaceship s : ships) {
            s.update();
        }

        // move bullets, update collisions
        for(Projectile p : ProjectileManager.getLivingProjectiles()) {
            p.update();

            for(Spaceship s : ships) {
                if(s.alive && p.owner != s && s.isColliding(p) && (Constants.allowFriendlyFire || s.team != p.team) && p.alive) {
                    p.kill();
                    s.harm(Constants.defaultProjectileHarm);
                }
            }
        }

        // check for pickup collisions
        for(Pickup p : PickupManager.getLivingPickups()) {
            for(Spaceship s : ships) {
                if(s.alive && p.alive && s.isColliding(p)) {
                    p.dispenseReward(s);
                }
            }
        }
    }


    public List<Spaceship> getShipsToDraw() {
        return ships;
    }
}
