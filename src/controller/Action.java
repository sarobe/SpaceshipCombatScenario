package controller;

import common.math.Vector2d;

public class Action {
    public int encoded;
    public Vector2d thrust;
    public double torque;

    public Action(int encoded, Vector2d thrust, double torque) {
        this.encoded = encoded;
        this.thrust = thrust;
        this.torque = torque;
    }
}
