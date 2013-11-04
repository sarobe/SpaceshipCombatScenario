package game;

import common.Constants;
import common.math.Vector2d;
import controller.Action;
import controller.Controller;
import controller.FireAction;
import controller.ShipState;
import problem.ProjectileManager;
import spaceship.Spaceship;
import spaceship.SpaceshipComponent;
import spaceship.Turret;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Samuel Roberts, 2013
 */
public class PlayerController extends Controller {

    int forward = 0;
    int turnCW = 0;
    int turnCCW = 0;
    int fire = 0;

    public PlayerController(Spaceship ship) {
        super(ship);

        // construct lookup table for each possible action
        int numActions = (int)Math.pow(2, Constants.numComponents);

        // get the outcome of every possible action
        ShipState initialState = new ShipState(ship);
        ProjectileManager.suppressNewProjectiles(true);

        double highestThrust = 0;
        double highestCCWTorque = 0;
        double highestCWTorque = Double.MAX_VALUE;

        for(int i = 0; i < numActions; i++) {
            ship.rot = 0;
            binaryToActions(ship, i);
            ship.update();

            // ignore any actions that turn on a turret
            if(!ship.justFired) {

                // if it produces the most thrust, use it as forwards
                if(ship.vel.mag() > highestThrust) {
                    forward = i;
                    highestThrust = ship.vel.mag();
                }
                // if it produces the most CW torque, use it as CW turn
                if(ship.rotvel > highestCCWTorque) {
                    turnCCW = i;
                    highestCCWTorque = ship.rotvel;
                }
                if(ship.rotvel < highestCWTorque) {
                    turnCW = i;
                    highestCWTorque = ship.rotvel;
                }
            }

            ship.setState(initialState);
        }

        // construct shot lookup table
        for(int i = 0; i < ship.components.size(); i++) {
            SpaceshipComponent sc = ship.components.get(i);
            if(sc instanceof Turret) {
                // add turret activation to fire encoded integer
                fire |= 1 << i;
            }
        }


        ProjectileManager.suppressNewProjectiles(false);
    }

    public void think(PlayerAction action) {
        int actionToExecute = 0;

        // if the player intent is to move, move
        if(action.forward > 0) {
            actionToExecute |= forward;
        }

        // if the player intent is to turn, turn
        if(action.turn > 0) {
            actionToExecute |= turnCCW;
        } else if(action.turn < 0) {
            actionToExecute |= turnCW;
        }

        // if the player wants to fire, fire
        if(action.shoot) {
            actionToExecute |= fire;
        }

        // tell ship to carry out specified action
        binaryToActions(ship, actionToExecute);
    }


}
