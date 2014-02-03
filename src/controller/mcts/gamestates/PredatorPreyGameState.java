package controller.mcts.gamestates;

import common.Constants;
import common.math.Vector2d;
import controller.ShipState;
import controller.mcts.InfluenceMap;
import controller.mcts.ShipBiasedMCTSController;
import problem.ProjectileManager;
import spaceship.Spaceship;

import java.util.Map;

public class PredatorPreyGameState implements IGameState {

    // a map of pickup states
    // true if the pickup has been collected
    // false otherwise
    int timestepsElapsed;
    int depth;
    int bounces;
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

    @Override
    public boolean isTerminal() {
        return predatorCaughtPrey() || (depth > ShipBiasedMCTSController.rolloutDepth) || (timestepsElapsed >= Constants.timesteps);
    }

    @Override
    public double value() {
        // return either the increase of distance as a good thing for the prey
        // or the decrease of distance as a good thing for the predator
        double score = 0;
        if(!predatorCaughtPrey()) {
            double dist = shipState.pos.dist(otherState.pos);
            if(isPredator) {
                score = (MAX_DIST - dist);
            } else {
                score = dist;
            }
        } else {
            if(isPredator) score = 99999;
            else score = -99999;
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
        //binaryToActions(ship, action);
//        for(int i=0; i<ship.components.size(); i++) {
//            if(i == action) ship.components.get(i).active = true;
//            else ship.components.get(i).active = false;
//        }
        ShipBiasedMCTSController.useSimpleAction(ship, action);

        // ASSUME OTHER SHIP WILL ACT RANDOMLY
        // space for improvement here, there could be some sort of minimax-style estimation of what the best immediate macro-action for the other ship would be
        // until then assume best
        int otherAction = (int)(Constants.rand.nextDouble() * Constants.actions.length);
        ShipBiasedMCTSController.useSimpleAction(other, otherAction);

        for(int i =0; i < ShipBiasedMCTSController.macroActionStep; i++) {
            ship.update();
            other.update();
            timestepsElapsed++;
            depth++;
            if(ship.bounced) bounces++;
        }
        shipState = new ShipState(ship);
        ship.setState(initialState);
        otherState = new ShipState(other);
        other.setState(initialOtherState);
        ProjectileManager.suppressNewProjectiles(false);
        return this;
    }

    @Override
    public IGameState copy() {
        return new PredatorPreyGameState(ship, new ShipState(shipState), other, new ShipState(other), timestepsElapsed, isPredator);
    }

    @Override
    public double heuristicValue() {
        if(isPredator) return InfluenceMap.getValue(shipState.pos.x, shipState.pos.y) - (bounces * BOUNCE_PENALTY);
        else return (InfluenceMap.getHeight() - InfluenceMap.getValue(shipState.pos.x, shipState.pos.y)) - (bounces * BOUNCE_PENALTY);
    }

    public ShipState getShipState() {
        return shipState;
    }

    @Override
    public double[] getFeatures() {
        // use dot product features
        Vector2d s;
        // use a vector TO the other ship if predator
        if(isPredator) {
            s = otherState.pos.copy().subtract(shipState.pos);
        } else {
            // use a vector AWAY from the other ship if prey
            s = shipState.pos.copy().subtract(otherState.pos);
        }

        Vector2d forward = ship.getForward();
        Vector2d left = forward.copy();
        left.rotate(Math.PI/2);
        Vector2d right = forward.copy();
        right.rotate(-Math.PI/2);

        // calculate the features
        double f = forward.scalarProduct(s);
        double l = left.scalarProduct(s);
        double r = right.scalarProduct(s);



        // feature four: velocity divided by distance (becomes larger as velocity increases and distance decreases)
        // negated because the larger this number is, the worse scenario it is
        //double vs = -(ship.v.mag()/s.mag());

        return new double[]{f,l,r};
    }

    @Override
    public int getTotalTime() {
        return timestepsElapsed;
    }

    @Override
    public boolean mustBePruned() {
        return shipState.bounced;
    }
}
