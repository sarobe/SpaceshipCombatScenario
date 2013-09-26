package controller;

import common.Constants;
import common.math.MathUtil;
import common.math.Vector2d;
import problem.ProjectileManager;
import spaceship.SimObject;
import spaceship.Spaceship;
import spaceship.SpaceshipComponent;
import spaceship.Turret;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Samuel Roberts, 2012
 */
public class ShipActionController {

    public Spaceship ship; // the ship this controller controls

    List<Action> moveActions;
    List<FireAction> fireActions;

    private final double THRUST_ON_TOLERANCE = Math.PI/32;
    private final double THRUST_OFF_TOLERANCE = Math.PI/8;
    private boolean thrust = false;

    public ShipActionController(Spaceship ship) {
        this.ship = ship;

        // construct lookup table for each possible action
        int numActions = (int)Math.pow(2, Constants.numComponents);
        moveActions = new ArrayList<Action>();
        fireActions = new ArrayList<FireAction>();

        // get the outcome of every possible action
        ShipState initialState = new ShipState(ship);
        ProjectileManager.suppressNewProjectiles(true);
        for(int i = 0; i < numActions; i++) {
            ship.rot = 0;
            binaryToActions(ship, i);
            ship.update();

            // ignore any actions that turn on a turret
            if(!ship.justFired) {
                Action action = new Action(i, ship.vel.copy(), ship.rotvel);
                moveActions.add(action);
            }

            ship.setState(initialState);
        }

        // construct shot lookup table
        for(int i = 0; i < ship.components.size(); i++) {
            SpaceshipComponent sc = ship.components.get(i);
            if(sc instanceof Turret) {
                FireAction fireAction = new FireAction(1 << i, sc.attachPos, (new Vector2d(1,0)).rotate(sc.attachRot));
                fireActions.add(fireAction);
            }
        }


        ProjectileManager.suppressNewProjectiles(false);
    }

    public void think(List<Spaceship> targets) {
        if(!ship.alive) return; // no need to think when the ship is dead
        // determine closest spaceship to approach
        Spaceship target = null;
        double bestDist = Double.MAX_VALUE;
        for(Spaceship s : targets) {
            if(s.alive) {
                double dist = ship.pos.dist(s.pos);
                if(dist < bestDist) {
                    target = s;
                    bestDist = dist;
                }
            }
        }

        Action bestAction = moveActions.get(0);

        // with no target, no need to think
        if(target != null) {

            // aim to be about 100 units in range
            Vector2d targetPos = ship.pos.copy().add(  (ship.pos.copy().subtract(target.pos)).normalise().mul(100)  );
            Vector2d distance = targetPos.subtract(ship.pos);
            Vector2d bestDirection = distance.copy().normalise();


            // find the best thrust vector in terms of magnitude
            Action bestThrustAction = moveActions.get(0);
            Vector2d bestThrust = new Vector2d();

    //        Vector2d targetVel = distance.copy();//.add(bestDirection, 10);
    //        Vector2d targetThrust = targetVel;//.subtract(ship.vel.copy());
    //        double bestDifference = Double.MAX_VALUE;
    //
    //        for(Action a : moveActions) {
    //            //Vector2d diff = a.thrust.copy().subtract(targetThrust);
    //            if(a.thrust.dist(targetThrust) < bestDifference) {
    //                bestThrustAction = a;
    //                bestThrust = a.thrust.copy();
    //                bestDifference = a.thrust.dist(targetThrust);
    //            }
    //        }

            double targetThrust = distance.mag();//  - ship.vel.mag();
            double bestDifference = Double.MAX_VALUE;

            for(Action a : moveActions) {
                double diff = a.thrust.mag() - targetThrust;
                if(Math.abs(diff) < bestDifference) {
                    bestThrustAction = a;
                    bestThrust = a.thrust.copy();
                    bestDifference = Math.abs(diff);
                }
            }

            // rotate it to where it points based on ship's current rotation
            bestThrust.rotate(ship.rot);

            // find the angle between the rotated thrust and the best direction
            double angleBetween = bestThrust.angBetween(bestDirection);

            // amend thrusting action depending on angleBetween
            if(Math.abs(angleBetween) < THRUST_ON_TOLERANCE) {
                thrust = true;
            } else if(Math.abs(angleBetween) > THRUST_OFF_TOLERANCE) {
                thrust = false;
            }

            // if within a tolerance, use that thrust vector action
            if(thrust) {
                bestAction = bestThrustAction;
            } else {

                // otherwise figure out how to turn thrust vector of highest magnitude in that direction
                // best torque impulse will factor in both the angle needed to turn and the current angular velocity
                if(bestDirection.determinant(bestThrust) < 0) {
                    // need to turn CW, angle needs to be negative
                    angleBetween *= -1;
                }
                double targetAngVel =  -angleBetween - ship.rotvel;

                bestDifference = Double.MAX_VALUE;
                // find the best action that provides the closest amount of torque
                for(Action a : moveActions) {
                    double diff = a.torque - targetAngVel;
                    if(Math.abs(diff) < bestDifference) {
                        bestAction = a;
                        bestDifference = Math.abs(diff);
                    }
                }
            }
        }


        int moveAction = bestAction.encoded;

        // determine which guns will hit a still target
        // and fire them
        int encodedFireActions = 0;
        for(FireAction fa : fireActions) {
            if(willHitStationary(fa, 1000, targets)) {
                encodedFireActions |= fa.encoded;
            }
        }

        // bitwise OR the move and fire actions together
        moveAction |= encodedFireActions;

        // use suitable action
        binaryToActions(ship, moveAction);
    }

    protected void binaryToActions(Spaceship target, int encodedActions) {
        int j = 1;
        int actionNum = 0;
        int totalPossibleActions = target.components.size();
        while(actionNum < totalPossibleActions) {
            if((encodedActions & j) != 0) {
                target.components.get(actionNum).active = true;
            } else {
                target.components.get(actionNum).active = false;
            }
            actionNum++;
            j *= 2;
        }
    }

    // given a projectile at starting position start
    // and its predicted movement in direction
    // scanning along range
    // will the projectile hit a stationary target?
    protected boolean willHitStationary(FireAction fireAction, double range, List<Spaceship> targets) {
        boolean hit = false;
        Vector2d start = fireAction.getFireOrigin(ship).add(ship.pos);
        Vector2d direction = fireAction.getFireDir(ship);
        Vector2d end = start.copy().add(direction, range);
        for(Spaceship t : targets) {
            Vector2d closestLinePoint = MathUtil.closestPointLineSegment(t.pos, start, end);
            if(t.pos.dist(closestLinePoint) < t.radius && t.alive) {
                hit = true;
                break;
            }
        }
        return hit;
    }

    public Spaceship getShip() {
        return ship;
    }
}

class Action {
    int encoded;
    Vector2d thrust;
    double torque;

    public Action(int encoded, Vector2d thrust, double torque) {
        this.encoded = encoded;
        this.thrust = thrust;
        this.torque = torque;
    }
}

class FireAction {
    int encoded;
    Vector2d fireOrigin;
    Vector2d fireDir;

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
