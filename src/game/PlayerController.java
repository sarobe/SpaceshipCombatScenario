package game;

import common.Constants;
import controller.Controller;
import controller.ShipState;
import problem.ProjectileManager;
import spaceship.SimObject;
import spaceship.ComplexSpaceship;
import spaceship.SpaceshipComponent;
import spaceship.Turret;

import java.awt.*;
import java.util.List;

/**
 * Created by Samuel Roberts, 2013
 */
public class PlayerController extends Controller {

    int forward = 0;
    int turnCW = 0;
    int turnCCW = 0;
    int fire = 0;

    public PlayerController(ComplexSpaceship ship) {
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
            ship.useAction(i);
            ship.update();

            // ignore any actions that turn on a turret
            if(!ship.justFired) {

                // if it produces the most thrust, use it as forwards
                if(ship.vel.mag() > highestThrust) {
                    forward = i;
                    highestThrust = ship.vel.mag();
                }
                // if it produces the most CW torque, use it as CW turn
                if(ship.rot > highestCCWTorque) {
                    turnCCW = i;
                    highestCCWTorque = ship.rot;
                }
                if(ship.rot < highestCWTorque) {
                    turnCW = i;
                    highestCWTorque = ship.rot;
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

    @Override
    public void draw(Graphics2D g) {
        // nothing
    }

    @Override
    public void think(List<SimObject> ships) {
        // should never be called, do nothing
    }

    @Override
    public void think() {
       // this needs a massive overhaul
    }

    @Override
    public double getScore() {
        return 0;  //To change body of implemented methods use File | Settings | File Templates.
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
        ship.useAction(actionToExecute);
    }


}
