package controller.mcts;

import common.math.Vector2d;
import controller.Controller;
import controller.ShipState;
import controller.mcts.gamestates.IGameState;
import controller.mcts.gamestates.PickupGameState;
import controller.singleMCTS.PlayoutPickupInfo;
import controller.singleMCTS.SingleMCTSController;
import problem.PickupManager;
import problem.ProjectileManager;
import spaceship.SimObject;
import spaceship.Spaceship;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.util.List;

/**
 * Created by Samuel Roberts, 2013
 * with some heavy borrowings from Simon Lucas
 */
public class SingleMCTSWrapper extends Controller {

    static int nIts = 200;
    public static int macroActionStep = 15;
    public static int rolloutDepth = 16;
    public static int timeAllowance = 1000;
    int timesteps;
    int currentAction;
    SingleMCTSController cont;
    PlayoutPickupInfo lastPlayoutInfo;

    public double bestPredictedScore = 0;

    public SingleMCTSWrapper(Spaceship ship) {
        super(ship);
        cont = new SingleMCTSController(constructState());
        think(); // get the starting action to use
        timesteps = 0;
    }

    @Override
    public void think(List<SimObject> ships) {
        // not used at the moment
        think();
    }

    @Override
    public void think() {
        // condense information about pickups into a map of pickup states
//        if(timesteps % macroActionStep == 0) {
//            //if(timesteps < Constants.timesteps) currentAction = getAction(constructState());
//            //else currentAction = 1; // do _nothing_
//
//            currentAction = getAction(constructState());
//        }
        timesteps++;

        currentAction = getAction(constructState());

        // use current action
        useSimpleAction(ship, currentAction);
    }

    public int getAction(IGameState state) {
        int action = cont.getAction(state, System.currentTimeMillis()+timeAllowance);
        lastPlayoutInfo = (PlayoutPickupInfo)cont.m_player.getPlayoutInfo();
        System.out.println(lastPlayoutInfo);
        return action;
    }

    @Override
    public void draw(Graphics2D g) {
        AffineTransform at = g.getTransform();

        g.translate(ship.pos.x, ship.pos.y);
        g.setColor(Color.YELLOW);
        g.fillOval(-5, -5, 10, 10);

        Vector2d shipForward = ship.getForward();
        g.drawLine(0, 0, (int)(shipForward.x * 20), (int)(shipForward.y * 20));

        g.setTransform(at);


        if(lastPlayoutInfo != null) {
            drawRollouts(g, lastPlayoutInfo);
        }
    }

    private void drawRollouts(Graphics2D g, PlayoutPickupInfo pInfo) {
        IGameState initialState = pInfo.startingState;
        ShipState initialShipState = new ShipState(ship);
        ShipState currentShipState = initialState.getShipState();

//        for(RollOut rollOut : pInfo.rollOuts) {
//            ship.setState(currentShipState);
            ShipState prevState = currentShipState;
            ShipState nextState = null;
            for(Integer action : pInfo.m_playoutHistory) {
                useSimpleAction(ship, action);
                for(int i=0; i<macroActionStep; i++) {
                    ship.update();
                }
                nextState = new ShipState(ship);

                // draw states
//                g.setColor(Color.getHSBColor(1.0f, 0.0f, Math.max((float)(rollOut.value/node.bestRolloutValue), 0.0f)));
                g.setColor(Color.gray);
                g.drawLine((int) prevState.px, (int) prevState.py, (int) nextState.px, (int) nextState.py);

                prevState = nextState;
            }
//        }
        ship.setState(initialShipState);
    }

    public IGameState constructState() {
        IGameState currentState;
        ProjectileManager.suppressNewProjectiles(true);

        ShipState initialState = new ShipState(ship);

        //currentState = new PickupGameState(ship, new ShipState(ship), timesteps, PickupManager.getPickupStates());
        currentState = new PickupGameState(ship, new ShipState(ship), 0, PickupManager.getPickupStates());

        // reset ship
        ship.setState(initialState);
        ProjectileManager.suppressNewProjectiles(false);
        return currentState;
    }

    public static void useSimpleAction(Spaceship ship, int action) {
        ship.useAction(action);
    }

}


