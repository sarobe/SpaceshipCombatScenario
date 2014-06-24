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

    // Modification of the above, for lines with a finite start and an infinite length
    public static Vector2d closestPointLineStart(Vector2d point, Vector2d start, Vector2d direction) {
        if(direction.mag() == 0) {
            return start.copy(); // there's no line, it's a point
        }

        Vector2d v = direction.copy();
        v.normalise();
        Vector2d w = point.copy();
        w.subtract(start);
        double t = w.scalarProduct(v) / v.scalarProduct(v);
        if(t < 0) {
            t = 0;
        }
        return start.copy().add(v.mul(t));
    }

    // Gives the distance as a double from start in the direction of a line where the line intersects a circle of radius
    // Returns the distance to the circle centre
    // Returns -1 if there is no intersection
    public static double lineDistanceToCircle(Vector2d start, Vector2d direction, Vector2d circlePos, double circleRadius) {
        Vector2d closestPointOnLine = closestPointLineStart(circlePos, start, direction);
        if(circlePos.dist(closestPointOnLine) <= circleRadius) {
            // the circle is intersecting the line
            // return distance to projected point on line
            return closestPointOnLine.dist(start);
        } else {
            // circle not intersecting line
            return -1;
        }
    }
}
