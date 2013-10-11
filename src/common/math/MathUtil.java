package common.math;

import spaceship.SimObject;

import java.util.List;

/**
 * Created by Samuel Roberts, 2013
 */
public class MathUtil {

    // get the sign of a number
    public static int sgn(double value) {
        if(value < 0) {
            return -1;
        } else if(value > 0) {
            return 1;
        } else {
            return 0;
        }
    }

    // Code adapated from
    // http://blog.generalrelativity.org/actionscript-30/collision-detection-circleline-segment-circlecapsule/
    // Accessed 27/02/10
    // Returns the closest point on a line segment to the test point
    public static Vector2d closestPointLineSegment(Vector2d point, Vector2d start, Vector2d end) {
        Vector2d v = end.copy();
        v.subtract(start);
        Vector2d w = point.copy();
        w.subtract(start);
        double t = w.scalarProduct(v) / v.scalarProduct(v);
        if(t < 0) {
            t = 0;
        } else if(t > 1) {
            t = 1;
        }
        return start.copy().add(v.mul(t));
    }
}
