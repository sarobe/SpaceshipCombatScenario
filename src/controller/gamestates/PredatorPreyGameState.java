package controller.gamestates;

import common.Constants;
import common.math.MathUtil;
import common.math.Vector2d;
import common.utilities.Picker;
import controller.ShipState;
import controller.StateController;
import controller.mcts.ShipBiasedMCTSController;
import problem.Asteroid;
import problem.AsteroidManager;
import problem.AsteroidsState;
import problem.ProjectileManager;
import spaceship.Spaceship;

public class PredatorPreyGameState implements IGameState {

    // a map of pickup states
    // true if the pickup has been collected
    // false otherwise
    int timestepsElapsed;
    int depth;
    int bounces;
    boolean hitAsteroid = false;
    boolean isPredator;
    Spaceship ship;
    ShipState shipState;
    Spaceship other;
    ShipState otherState;

    static final int BOUNCE_PENALTY = 1000;
    static final double MAX_DIST = 1280;

    public PredatorPreyGameState(Spaceship ship, ShipState shipState, Spaceship other, ShipState otherState, int timestepsElapsed, boolean isPredator) {
        this.ship = ship;
        this.shipState = shipState;
        this.other = other;
        this.otherState = otherState;
        this.isPredator = isPredator;

        this.timestepsElapsed = timestepsElapsed;
        bounces = 0;
        depth = 0;
    }

    public boolean predatorCaughtPrey() {
        return shipState.pos.dist(otherState.pos) < (ship.radius + other.radius);
    }

    public boolean isShipAsteroidCollision() {
        boolean colliding = false;
        for(Asteroid a : AsteroidManager.getAsteroids()) {
            if(ship.isColliding(a)) {
                colliding = true;
                break;
            }
        }
        return colliding;
    }

    @Override
    public boolean isTerminal() {
        return predatorCaughtPrey() || hitAsteroid ||  (depth > Constants.rolloutDepth) || (timestepsElapsed >= Constants.timesteps);
    }

    @Override
    public double value() {
        // return either the increase of distance as a good thing for the prey
        // or the decrease of distance as a good thing for the predator
        double score = 0;
//        double distanceWeighting = 1;
//        double deviationWeighting = 1;

        Vector2d ourVel = shipState.vel();
        Vector2d otherPos = otherState.pos.copy();
        Vector2d otherVel = other.vel.copy();

        // the further apart the two ships are, the more varied their velocities will be
        // the closer they are, the more they can be assumed to not change
        // therefore, assume the velocity will be a reliable predictor of future position in future states
        // with GREATER CONFIDENCE (higher scalar) when the ships are closer together
        // and LOWER CONFIDENCE (lower scalar) when the ships are farther apart
//        double predictionFactor = 1 - (shipState.pos.dist(otherPos)/MAX_DIST);
//        otherPos.add(otherState.vel(), predictionFactor * 10);

        if(isPredator) {
            // modify other position to be based on a set of circumstances as follows:

            // find the point closest to the prey we will be on current velocity course, drawing a line starting here
            Vector2d closestPredictedPoint = MathUtil.closestPointLineStart(otherPos, shipState.pos, ourVel);
            Vector2d diff = closestPredictedPoint.subtract(shipState.pos);

            // determine how many steps it will take to reach that point
            // (if we're moving away this will be zero)
            double numSteps = (diff.mag() / ourVel.mag()) / Constants.dt; // first half of equation reduces to multiples of velocity vector
            // division by time constant changes from per-frame to per-second

            // given prey maintains velocity, determine where it will end up (remember to factor in collision with walls)
            // use this as the other ship's position
            if(numSteps > 0) {
                ShipState otherStateTemp = new ShipState(other);
                for(int i=0; i<numSteps; i++) {
                    other.update();
                }
                shipState.setPredictedPoint(other.pos);
                otherPos.set(other.pos);
                other.setState(otherStateTemp);
            }
        }




        if(hitAsteroid) {
            score = 0;
        } else {
            if(!predatorCaughtPrey()) {
                double dist = shipState.pos.dist(otherPos);
                // if the world is wrapped, take the shorter of the two potential distances
                if(Constants.worldType == Constants.WorldType.WRAPPING) {
                    double dx = Constants.screenWidth - (shipState.pos.x - otherState.pos.x);
                    double dy = Constants.screenHeight - (shipState.pos.y - otherState.pos.y);
                    double wrappedDist = Math.sqrt(dx*dx + dy*dy);
                    dist = Math.min(dist, wrappedDist);
                }



                // lowest possible distance: 0, theoretically
                // maximum possible distance: max
    //            Vector2d desiredHeading;
    //            // use a vector TO the other ship if predator
    //            if(isPredator) {
    //                desiredHeading = otherState.pos.copy().subtract(shipState.pos);
    //            } else {
    //                // use a vector AWAY from the other ship if prey
    //                desiredHeading = shipState.pos.copy().subtract(otherState.pos);
    //            }
    //            desiredHeading.normalise();
    //            Vector2d currentHeading = new Vector2d(shipState.vx, shipState.vy);
    //            currentHeading.normalise();
    ////            Vector2d currentHeading = new Vector2d(1,0).rotate(shipState.rot);
    //            double headingDeviation = currentHeading.dist(desiredHeading);
    //            // lowest possible deviation: 0 (exactly moving in the desired direction)
    //            // highest possible deviation: 2 (exactly opposite)
    //            // scaling factor for making this approach the values used for distance: 1/2
    //            headingDeviation *= 0.5;

                if(isPredator) {
    //                score = (distanceWeighting * ((MAX_DIST - dist)/MAX_DIST)) + (deviationWeighting * headingDeviation);
                    score = (MAX_DIST - dist)/MAX_DIST;
                } else {
    //                score = (distanceWeighting * (dist/MAX_DIST)) + (deviationWeighting * headingDeviation);
                    score = dist/MAX_DIST;
                }
            } else {
                if(isPredator) score = 1;//10000;
                else score = 0;//-10000;
            }
        }
        return score;
    }

    @Override
    public int nActions() {
        return Constants.actions.length;    // Constants.numComponents;
    }

    @Override
    public IGameState next(int action) {
        ShipState initialState = new ShipState(ship);
        ShipState initialOtherState = new ShipState(other);
        ProjectileManager.suppressNewProjectiles(true);
        AsteroidsState initialAsteroidsState = AsteroidManager.saveState();
        //binaryToActions(ship, action);
//        for(int i=0; i<ship.components.size(); i++) {
//            if(i == action) ship.components.get(i).active = true;
//            else ship.components.get(i).active = false;
//        }

        ship.setState(shipState);
        other.setState(otherState);
        StateController.useSimpleAction(ship, action);

        // ASSUME OTHER SHIP WILL ACT RANDOMLY
        // space for improvement here, there could be some sort of minimax-style estimation of what the best immediate macro-action for the other ship would be
        // until then assume best
//        int otherAction = (int)(Constants.rand.nextDouble() * Constants.actions.length);
//        StateController.useSimpleAction(other, otherAction);


        // determine what the most obvious action for the other ship would be based on 1-ply greedy search
        // GET BEST ACTION FOR NEXT MACRO ACTION STEP (this is going to be slow)
        // assume within this sub-simulation that the current ship isn't changing its action
        Picker<Integer> actionPicker = new Picker<Integer>();
        for(int i =0; i< Constants.actions.length; i++) {
            ship.setState(shipState);
            other.setState(otherState);
            StateController.useSimpleAction(other, i);
            for(int j=0; j<Constants.macroActionStep; j++) {
                ship.update();
                other.update();
            }
            double stateScore = new PredatorPreyGameState(ship, shipState, other, otherState, timestepsElapsed + Constants.macroActionStep, isPredator).value();
            actionPicker.add(stateScore, i);
        }
        // use best action for other ship
        StateController.useSimpleAction(other, actionPicker.getBest());

        ship.setState(shipState);
        other.setState(otherState);

        for(int i =0; i < Constants.macroActionStep; i++) {
            ship.update();
            for(Asteroid a : AsteroidManager.getAsteroids()) {
                a.update();
            }
            other.update();
            timestepsElapsed++;
            depth++;
            if(ship.bounced) bounces++;
            if(isShipAsteroidCollision()) hitAsteroid = true;
        }
        shipState = new ShipState(ship);
        otherState = new ShipState(other);

        ship.setState(initialState);
        other.setState(initialOtherState);
        ProjectileManager.suppressNewProjectiles(false);
        AsteroidManager.loadState(initialAsteroidsState);

        return this;
    }

    @Override
    public IGameState copy() {
        return new PredatorPreyGameState(ship, new ShipState(shipState), other, new ShipState(other), timestepsElapsed, isPredator);
    }

    @Override
    public double heuristicValue() {
        return 0;
        //if(isPredator) return (MAX_DIST - shipState.pos.dist(otherState.pos)) - (bounces * BOUNCE_PENALTY);
        //else return shipState.pos.dist(otherState.pos) - (bounces * BOUNCE_PENALTY);
//        if(isPredator) return InfluenceMap.getValue(shipState.pos.x, shipState.pos.y) - (bounces * BOUNCE_PENALTY);
//        else return (InfluenceMap.getHeight() - InfluenceMap.getValue(shipState.pos.x, shipState.pos.y)) - (bounces * BOUNCE_PENALTY);
    }

    public ShipState getShipState() {
        return shipState;
    }

    @Override
    public double[] getFeatures() {
        // use two features, the distance to the other ship
        double shipDist = otherState.pos.dist(shipState.pos);

        // and the distance to the nearest edge
        double edgeDist = MAX_DIST;
        // leave as max dist for wrapped world, otherwise calculate actual distance
        if(Constants.worldType == Constants.WorldType.BOUNDED) {
            double edgeX = Math.min(shipState.pos.x, Constants.screenWidth - shipState.pos.x);
            double edgeY = Math.min(shipState.pos.y, Constants.screenHeight - shipState.pos.y);
            edgeDist = Math.min(edgeX, edgeY);
        }

        return new double[]{shipDist, edgeDist};
    }

    @Override
    public int getTotalTime() {
        return timestepsElapsed;
    }

    @Override
    public boolean mustBePruned() {
        return shipState.bounced || hitAsteroid;
    }
}
