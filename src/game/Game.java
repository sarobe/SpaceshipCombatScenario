package game;

import common.Constants;
import problem.managers.PickupManager;
import problem.managers.ProjectileManager;
import problem.entities.Pickup;
import spaceship.ComplexSpaceship;
import spaceship.Projectile;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Samuel Roberts, 2013
 */
public class Game {

    public List<ComplexSpaceship> ships;

    public PlayerAction playerActionOne;
    public PlayerAction playerActionTwo;

    public PlayerController playerControllerOne;
    public PlayerController playerControllerTwo;

    // TEMPORARY SHIP DESIGN
    double[] tempShipDesign =
            {0, 0, 0, // position and rotation (totally irrelevant here)
                    100, 100, 100, 100, // weights (totally irrelevant here)
                    1, 0.0, -200.0, 10*(3*Math.PI/2),
                    0, 200.0, 0.0, 10*(3*Math.PI/2),
                    0, 100.0,  100.0, 10*(3*Math.PI/2),
                    0, -100.0,  100.0, 10*(3*Math.PI/2),
                    0, -200.0, 0.0, 10*(3*Math.PI/2)};

    public Game() {
        init();
    }

    public void init() {
        ships = new ArrayList<ComplexSpaceship>();
        PickupManager.placePickups(Constants.pickupPlacementSeed);
        ProjectileManager.reset();
        // create player one stuff
        playerActionOne = new PlayerAction(0, 0, false);
        ComplexSpaceship playerOneShip = new ComplexSpaceship(tempShipDesign);
        playerOneShip.pos.x = Constants.screenWidth/2 - Constants.screenWidth/3;
        playerOneShip.pos.y = Constants.screenHeight/2;
        playerOneShip.rot = 0;
        playerOneShip.team = 0;
        System.out.println(playerOneShip.hull);
        System.out.println(playerOneShip.maxHull);
        ships.add(playerOneShip);
        playerControllerOne = new PlayerController(playerOneShip);

        // create player two stuff
        playerActionTwo = new PlayerAction(0, 0, false);
        ComplexSpaceship playerTwoShip = new ComplexSpaceship(tempShipDesign);
        playerTwoShip.pos.x = Constants.screenWidth/2 + Constants.screenWidth/3;
        playerTwoShip.pos.y = Constants.screenHeight/2;
        playerTwoShip.rot = 0;
        playerTwoShip.team = 1;
        ships.add(playerTwoShip);
        playerControllerTwo = new PlayerController(playerTwoShip);
    }

    public void update() {
        playerControllerOne.think(playerActionOne);
        playerControllerTwo.think(playerActionTwo);

        for(ComplexSpaceship s : ships) {
            s.update();
        }

        // move bullets, update collisions
        for(Projectile p : ProjectileManager.getLivingProjectiles()) {
            p.update();

            for(ComplexSpaceship s : ships) {
                if(s.alive && p.owner != s && s.isColliding(p) && (Constants.allowFriendlyFire || s.team != p.team) && p.alive) {
                    p.kill();
                    s.harm(Constants.defaultProjectileHarm);
                }
            }
        }

        // check for pickup collisions
        for(Pickup p : PickupManager.getLivingPickups()) {
            for(ComplexSpaceship s : ships) {
                if(s.alive && p.alive && s.isColliding(p)) {
                    p.dispenseReward(s);
                }
            }
        }
    }


    public List<ComplexSpaceship> getShipsToDraw() {
        return ships;
    }
}
