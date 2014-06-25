package main;

/**
 * Created by Samuel Roberts, 2014
 */
public abstract class Runner implements Runnable {

    boolean isRunning = false;
    public boolean isRunning() {
        return isRunning;
    }

    public Runner() {
        isRunning = true;
    }

}
