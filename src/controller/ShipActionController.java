package controller;

import common.Constants;
import common.math.MathUtil;
import common.math.Vector2d;
import problem.PickupManager;
import problem.ProjectileManager;
import spaceship.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Samuel Roberts, 2012
 */
public class ShipActionController {

    public Spaceship ship; // the ship this controller controls

    List<Action> moveActions;
    List<FireAction> fireActions;

    Vector2d bestDirectionForFirepower;

    public double chaseTargetWeighting;
    public double chaseAllWeighting;
    public double evadeBulletsWeighting;
    public double moveToPickupWeighting;
    //public double approachFriendWeighting;

    //private final double THRUST_ON_TOLERANCE = Math.PI/32;
    //private final double THRUST_OFF_TOLERANCE = Math.PI/8;
    //private boolean thrust = false;

    public ShipActionController(Spaceship ship) {
        this.ship = ship;

        chaseTargetWeighting = ship.chromosome[0] * Constants.weightScale;
        chaseAllWeighting = ship.chromosome[1] * Constants.weightScale;
        evadeBulletsWeighting = ship.chromosome[2] * Constants.weightScale;
        moveToPickupWeighting = ship.chromosome[3] * Constants.weightScale;
        //approachFriendWeighting = ship.chromosome[3] * Constants.weightScale;

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

    public void think(List<Spaceship> ships) {
        if(!ship.alive) return; // no need to think when the ship is dead
        // determine closest spaceship to approach
        Spaceship target = null;
        Spaceship ally = null;
        boolean targetCanBeHit = false;
        double bestEnemyDist = Double.MAX_VALUE;
        double bestAllyDist = Double.MAX_VALUE;
        int livingEnemies = 0;
        Vector2d enemyMeanPos = new Vector2d();
        for(Spaceship s : ships) {
            if(s.alive && s != ship) {
                double dist = ship.pos.dist(s.pos);
                if(s.team != ship.team) {
                    livingEnemies++;
                    enemyMeanPos.add(s.pos);

                    if(dist < bestEnemyDist) {
                        // can we hit this target?
                        if(canHitTarget(s, 5000)) {
                            // THIS IS VERY IMPORTANT: DON'T CHOOSE A CLOSER TARGET UNLESS IT CAN ALSO BE HIT
                            targetCanBeHit = true;
                            target = s;
                            bestEnemyDist = dist;
                        } else if(!targetCanBeHit) {
                            // begrudingly just accept targets on the basis they're closer by if we haven't found a target we can aim for yet
                            target = s;
                            bestEnemyDist = dist;
                        }
                    }
                } else {
                    if(dist < bestAllyDist) {
                        ally = s;
                        bestAllyDist = dist;
                    }
                }
            }
        }
        if(livingEnemies > 0) {
            enemyMeanPos.mul(1/livingEnemies);
        } else {
            enemyMeanPos.set(ship.pos);
        }

        Vector2d bestDirection = new Vector2d();

        // CHASE TARGET
        // aim to be about 100 units in range, so back away if too close
        if(target == null) target = ship; // a terrible, terrible hack, but okay!
        Vector2d chaseDir = ship.pos.copy().add(  (ship.pos.copy().subtract(target.pos)).normalise().mul(100)  );
        chaseDir.subtract(ship.pos).normalise();
        bestDirection.add(chaseDir, chaseTargetWeighting);

        // CHASE ALL
        chaseDir = enemyMeanPos.subtract(ship.pos).normalise();
        bestDirection.add(chaseDir, chaseAllWeighting);

        // EVADE BULLETS
        Vector2d avoidanceDirection = new Vector2d();
        for(Projectile p : ProjectileManager.getLivingProjectiles()) {
            if(p.team != ship.team) {
                // this projectile is a threat perhaps
                // will it collide with the ship?
                if(willHitMe(p, 1000, ship.radius + 100)) {
                    // add the direction of the projectile to the avoidance direction, with inverse weighting based on distance
                    Vector2d dist = new Vector2d(ship.pos);
                    dist.subtract(p.pos);
                    avoidanceDirection.add(dist, 1000/dist.mag());
                }
            }
        }
        avoidanceDirection.normalise();
        bestDirection.add(avoidanceDirection, evadeBulletsWeighting);

        // APPROACH NEAREST ALLY
//        if(ally == null) ally = ship;
//        Vector2d approachDir = ally.pos.copy().subtract(ship.pos).normalise();
//        bestDirection.add(approachDir, approachFriendWeighting);

        // APPROACH NEAREST PICKUP
        Vector2d pickupDirection = new Vector2d();
        for(Pickup p : PickupManager.getLivingPickups()) {
            Vector2d dist = new Vector2d(ship.pos);
            dist.subtract(p.pos);
            dist.mul(-1);
            pickupDirection.add(dist, 10000/dist.mag());
        }
        pickupDirection.normalise();
        bestDirection.add(pickupDirection, moveToPickupWeighting);

        // normalise best direction
        bestDirection.normalise();
        // find the best thrust vector in terms of direction
        Action bestAction = moveActions.get(0);
        double bestDifference = Double.MAX_VALUE;

        for(Action a : moveActions) {
            Vector2d normalisedThrustDirection = a.thrust.copy().normalise();
            double diff = bestDirection.dist(normalisedThrustDirection);

            // aim for the best direction, but if the difference is close enough, prioritise magnitude
            if((Math.abs(diff) < bestDifference)) {
                bestAction = a;
                bestDifference = Math.abs(diff);
            }
        }


        // once we have determined what we are doing for thrust, also factor in turn to better face target with best direction of firepower


        int moveAction = bestAction.encoded;

        // determine which guns will hit a still target
        // and fire them
        int encodedFireActions = 0;
        for(FireAction fa : fireActions) {
            if(willHitStationary(fa, 1000, ships)) {
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

    protected boolean canHitTarget(Spaceship target, double range) {
        boolean canHit = false;

        // check every fire action
        for(FireAction fa : fireActions) {
            if(willHitStationary(fa, range, target)) {
                canHit = true;
                break;
            }
        }

        return canHit;
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
            if(t.pos.dist(closestLinePoint) < t.radius && t.alive && t.team != ship.team) {
                hit = true;
                break;
            }
        }
        return hit;
    }

    // as above, overload for single target
    protected boolean willHitStationary(FireAction fireAction, double range, Spaceship target) {
        boolean hit = false;
        Vector2d start = fireAction.getFireOrigin(ship).add(ship.pos);
        Vector2d direction = fireAction.getFireDir(ship);
        Vector2d end = start.copy().add(direction, range);
        Vector2d closestLinePoint = MathUtil.closestPointLineSegment(target.pos, start, end);
        if(target.pos.dist(closestLinePoint) < target.radius && target.alive && target.team != ship.team) {
            hit = true;
        }
        return hit;
    }

    // given a projectile will it hit me?
    protected boolean willHitMe(Projectile p, double range, double radius) {
        boolean hit = false;
        Vector2d start = p.pos.copy();
        Vector2d end = p.pos.copy().add(p.vel.copy().normalise().mul(range));
        Vector2d closestLinePoint = MathUtil.closestPointLineSegment(ship.pos, start, end);
        if(ship.pos.dist(closestLinePoint) <= radius) hit = true;
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





// OLD STYLE OF ALIGNING THRUST VECTOR AND TURNING
//        // rotate it to where it points based on ship's current rotation
//        bestThrust.rotate(ship.rot);
//
//        // find the angle between the rotated thrust and the best direction
//        double angleBetween = bestThrust.angBetween(bestDirection);
//
//        // amend thrusting action depending on angleBetween
//        if(Math.abs(angleBetween) < THRUST_ON_TOLERANCE) {
//            thrust = true;
//        } else if(Math.abs(angleBetween) > THRUST_OFF_TOLERANCE) {
//            thrust = false;
//        }
//
//        // if within a tolerance, use that thrust vector action
//        if(thrust) {
//            bestAction = bestThrustAction;
//        } else {
//
//            // otherwise figure out how to turn thrust vector of highest magnitude in that direction
//            // best torque impulse will factor in both the angle needed to turn and the current angular velocity
//            if(bestDirection.determinant(bestThrust) < 0) {
//                // need to turn CW, angle needs to be negative
//                angleBetween *= -1;
//            }
//            double targetAngVel =  -angleBetween - ship.rotvel;
//
//            bestDifference = Double.MAX_VALUE;
//            // find the best action that provides the closest amount of torque
//            for(Action a : moveActions) {
//                double diff = a.torque - targetAngVel;
//                if(Math.abs(diff) < bestDifference) {
//                    bestAction = a;
//                    bestDifference = Math.abs(diff);
//                }
//            }
//        }