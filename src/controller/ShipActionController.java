package controller;

import common.Constants;
import common.math.MathUtil;
import common.math.Vector2d;
import problem.Pickup;
import problem.PickupManager;
import problem.PickupType;
import problem.ProjectileManager;
import spaceship.*;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Samuel Roberts, 2012
 */
public class ShipActionController extends Controller {

    List<Action> moveActions;
    List<FireAction> fireActions;

    Vector2d chaseDir;
    Vector2d chaseAllDir;
    Vector2d avoidDir;
    Vector2d pickupDir;
    Vector2d bestDirection;
    Vector2d velDirection;

    Vector2d enemyMeanPos;

    public double chaseTargetWeighting;
    public double chaseAllWeighting;
    public double evadeBulletsWeighting;
    public double moveToPickupWeighting;
    //public double approachFriendWeighting;

    //private final double THRUST_ON_TOLERANCE = Math.PI/32;
    //private final double THRUST_OFF_TOLERANCE = Math.PI/8;
    //private boolean thrust = false;

    public static double VISUALISER_SCALING = 30;

    public ShipActionController(Spaceship ship) {
        super(ship);

        chaseTargetWeighting = ship.chromosome[3] * Constants.weightScale;
        chaseAllWeighting = ship.chromosome[4] * Constants.weightScale;
        evadeBulletsWeighting = ship.chromosome[5] * Constants.weightScale;
        moveToPickupWeighting = ship.chromosome[6] * Constants.weightScale;
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
                Action action = new Action(i, ship.vel.copy(), ship.rot);
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

        chaseDir = new Vector2d();
        chaseAllDir = new Vector2d();
        avoidDir = new Vector2d();
        pickupDir = new Vector2d();
        bestDirection = new Vector2d();
        velDirection = new Vector2d();
        enemyMeanPos = new Vector2d();
    }

    public void think(List<SimObject> ships) {
        if(!ship.alive) return; // no need to think when the ship is dead

        bestDirection.zero();
        // determine closest spaceship to approach
        SimObject target = null;
        SimObject ally = null;
        boolean targetCanBeHit = false;
        double bestEnemyDist = Double.MAX_VALUE;
        double bestAllyDist = Double.MAX_VALUE;
        double livingEnemies = 0;
        enemyMeanPos.zero();
        for(SimObject s : ships) {
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



        // CHASE TARGET
        chaseDir.zero();
        if(target == null) target = ship; // a terrible, terrible hack, but okay!
        chaseDir = target.pos.copy().subtract(ship.pos);
        chaseDir.normalise();
        bestDirection.add(chaseDir, chaseTargetWeighting);

        // CHASE ALL
        chaseAllDir = enemyMeanPos.copy().subtract(ship.pos).normalise();
        bestDirection.add(chaseAllDir, chaseAllWeighting);

        // EVADE BULLETS AND MINES
        avoidDir.zero();
        for(Projectile p : ProjectileManager.getLivingProjectiles()) {
            if(p.team != ship.team) {
                // this projectile is a threat perhaps
                // will it collide with the ship?
                if(willHitMe(p, 1000, ship.radius + 100)) {
                    // add the direction of the projectile to the avoidance direction, with inverse weighting based on distance
                    Vector2d dist = new Vector2d(ship.pos);
                    dist.subtract(p.pos);
                    avoidDir.add(dist, 1000/dist.mag());
                }
            }
        }
        for(Pickup p : PickupManager.getLivingMines()) {
            Vector2d dist = new Vector2d(ship.pos);
            dist.subtract(p.pos);
            avoidDir.add(dist, 1000/dist.mag());
        }
        avoidDir.normalise();
        bestDirection.add(avoidDir, evadeBulletsWeighting);

        // APPROACH NEAREST ALLY
//        if(ally == null) ally = ship;
//        Vector2d approachDir = ally.pos.copy().subtract(ship.pos).normalise();
//        bestDirection.add(approachDir, approachFriendWeighting);

        // APPROACH NEAREST PICKUP
        pickupDir.zero();
        double closestDist = Double.MAX_VALUE;
        for(Pickup p : PickupManager.getLivingPickups()) {
            if(p.type != PickupType.MINE) {
                Vector2d dist = new Vector2d(p.pos).subtract(ship.pos);
                if(dist.mag() < closestDist) {
                    closestDist = dist.mag();
                    pickupDir.set(dist);
                }
                //pickupDir.add(dist, 1000/dist.mag());
            }
        }
        pickupDir.normalise();
        bestDirection.add(pickupDir, moveToPickupWeighting);

        // normalise best direction
        bestDirection.normalise();
        // find the best thrust vector in terms of direction
        // ALSO FACTOR IN VELOCITY
        velDirection = ship.vel.copy();
        velDirection.normalise();
        Action bestAction = moveActions.get(0);
        double bestDifference = Double.MAX_VALUE;
        double bestThrust = 0;

        for(Action a : moveActions) {
            Vector2d newVelDirection = a.thrust.copy().rotate(ship.rot);
            newVelDirection.add(velDirection);
            double diff = bestDirection.angBetween(newVelDirection);

            // aim for the best direction, but if the difference is close enough, prioritise magnitude
            if((Math.abs(diff) < bestDifference) && a.thrust.mag() > 0) {
                //if(a.thrust.mag() > bestThrust) {
                    bestAction = a;
                    bestDifference = Math.abs(diff);
                    bestThrust = a.thrust.mag();
                //}
            }
        }


        // once we have determined what we are doing for thrust, also factor in turn to better face target with best direction of firepower
        // find shortest distance to turn to get turret in range

        Action bestTurnAction = bestAction;
        if(target != null) {
            double smallestTurnDiff = Double.MAX_VALUE;
            for(SpaceshipComponent sc : ship.components) {
                if(sc instanceof Turret) {
                    Vector2d fireDir = sc.attachPos.copy().rotate(ship.rot);
                    fireDir.add(new Vector2d(1,0).rotate(ship.rot + sc.attachRot));
                    fireDir.normalise();
                    double turnDiff = chaseDir.angBetween(fireDir);
                    if(Math.abs(turnDiff) < Math.abs(smallestTurnDiff)) {
                        smallestTurnDiff = turnDiff;
                    }
                }
            }

            // now that we have a direction to turn, find what will turn that way
            double actionTurnDiff = Double.MAX_VALUE;
            for(Action a : moveActions) {
                if(Math.abs(a.torque - smallestTurnDiff) < actionTurnDiff) {
                    actionTurnDiff = Math.abs(a.torque - smallestTurnDiff);
                    bestTurnAction = a;
                }
            }
        }

        Action backupTurnAction = bestAction;
        double bestTorque = 0;
        for(Action a : moveActions) {
            if(Math.abs(a.torque) > bestTorque) {
                backupTurnAction = a;
                bestTorque = Math.abs(a.torque);
            }
        }

        int moveAction = bestTurnAction.encoded;
        if(moveAction == 0) moveAction = bestTurnAction.encoded;
        // desperation measures
        //if(moveAction == 0) moveAction = backupTurnAction.encoded;

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

    public void think() {
        List<SimObject> ships = new ArrayList<SimObject>();
        ships.add(ship);
        think(ships);
    }

    public void draw(Graphics2D g) {
        AffineTransform at = g.getTransform();
        g.translate(ship.pos.x, ship.pos.y);

        // DRAW CHASE DIR
        g.setColor(Color.RED);
        g.drawLine(0, 0, (int)(chaseDir.x * chaseTargetWeighting * VISUALISER_SCALING), (int)(chaseDir.y * chaseTargetWeighting * VISUALISER_SCALING));

        // DRAW CHASE ALL DIR
        g.setColor(Color.ORANGE);
        g.drawLine(0, 0, (int)(chaseAllDir.x * chaseAllWeighting * VISUALISER_SCALING), (int)(chaseAllDir.y * chaseAllWeighting * VISUALISER_SCALING));


        // DRAW AVOID DIR
        g.setColor(Color.BLUE);
        g.drawLine(0, 0, (int)(avoidDir.x * evadeBulletsWeighting * VISUALISER_SCALING), (int)(avoidDir.y * evadeBulletsWeighting * VISUALISER_SCALING));

        // DRAW PICKUP DIR
        g.setColor(Color.GREEN);
        g.drawLine(0, 0, (int)(pickupDir.x * moveToPickupWeighting * VISUALISER_SCALING), (int)(pickupDir.y * moveToPickupWeighting * VISUALISER_SCALING));

        // DRAW BEST DIR
        g.setColor(Color.WHITE);
        g.drawLine(0, 0, (int)(bestDirection.x * VISUALISER_SCALING), (int)(bestDirection.y* VISUALISER_SCALING));

        // DRAW COMPENSATING DIR
        g.setColor(Color.GRAY);
        g.drawLine(0, 0, (int)(velDirection.x * VISUALISER_SCALING), (int)(velDirection.y* VISUALISER_SCALING));

        g.setTransform(at);

        // DRAW CHASE ALL TARGET
        g.translate(enemyMeanPos.x, enemyMeanPos.y);
        g.setColor(Color.ORANGE);
        g.drawOval((int)(-VISUALISER_SCALING/2.0), (int)(-VISUALISER_SCALING/2.0), (int)VISUALISER_SCALING, (int)VISUALISER_SCALING);

        g.setTransform(at);
    }



    protected boolean canHitTarget(SimObject target, double range) {
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
    protected boolean willHitStationary(FireAction fireAction, double range, List<SimObject> targets) {
        boolean hit = false;
        Vector2d start = fireAction.getFireOrigin(ship).add(ship.pos);
        Vector2d direction = fireAction.getFireDir(ship);
        Vector2d end = start.copy().add(direction, range);
        for(SimObject t : targets) {
            Vector2d closestLinePoint = MathUtil.closestPointLineSegment(t.pos, start, end);
            if(t.pos.dist(closestLinePoint) < t.radius && t.alive && t.team != ship.team) {
                hit = true;
                break;
            }
        }
        return hit;
    }

    // as above, overload for single target
    protected boolean willHitStationary(FireAction fireAction, double range, SimObject target) {
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