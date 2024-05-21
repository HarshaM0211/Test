package Ignite;

import static java.lang.Math.PI;
import static java.lang.Math.asin;
import static java.lang.Math.atan2;
import static java.lang.Math.cos;
import static java.lang.Math.sin;
import static java.lang.Math.sqrt;
import static java.lang.Math.toDegrees;
import static java.lang.Math.toRadians;

import java.util.List;

import org.apache.commons.collections4.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Geo specific utilities.
 * 
 * <p>
 * Units :
 * <li>Angle : <b>Radians</b></li>
 * <li>Distance : <b>Meters</b></li>
 * </p>
 */
public final class Geo {

    private static final Logger LOGGER = LoggerFactory.getLogger(Geo.class);

    private Geo() {
        // Utility Class
    }


    // Constants
    // ------------------------------------------------------------------------

    public static final double RADIUS_EARTHS_MTRS = 6378_137;

    public static final double DEG_TO_MTRS = 111.2;



    // Distance
    // ------------------------------------------------------------------------

    /**
     * Calculates distance between two Geo Coordinates...
     * 
     * <p>
     * Ref:: <a href="https://andrew.hedges.name/experiments/haversine/">Haversine Method</a>
     * <p>
     * 
     * @return distance in meters
     */
    public static double distance(double lat1, double lng1, double lat2, double lng2) {
        // Differences
        double dLng = toRadians(lng2 - lng1);
        double dLat = toRadians(lat2 - lat1);

        // Haversine formula
        double a =
                (sin(dLat / 2) * sin(dLat / 2)) + cos(toRadians(lat1)) * cos(toRadians(lat2))
                        * (sin(dLng / 2) * sin(dLng / 2));
        double c = 2 * atan2(sqrt(a), sqrt(1 - a));
        double distInMtrs = RADIUS_EARTHS_MTRS * c;

        LOGGER.debug("Calculated Distance between {},{} and {},{} is #{} meters", lat1, lng1, lat2, lng2, distInMtrs);
        return distInMtrs;
    }

    /**
     * Calculates distance between two Geo Coordinates...
     * 
     * <p>
     * Ref:: <a href="https://andrew.hedges.name/experiments/haversine/">Haversine Method</a>
     * <p>
     * 
     * @return distance in meters
     */
    public static double distance(Coordinate c1, Coordinate c2) {
        return distance(c1.getLatitude(), c1.getLongitude(), c2.getLatitude(), c2.getLongitude());
    }


    // Length
    // ------------------------------------------------------------------------

    /**
     * Calculates the length of a trace/line-string
     * 
     * @param trace
     * @return
     */
    public static double length(List<Coordinate> trace) {
        // Sanity checks
        if (trace == null || trace.size() < 2) {
            return 0d;
        }

        Double length = 0d;
        for (int i = 1; i < trace.size(); i++) {
            Coordinate c1 = trace.get(i - 1);
            Coordinate c2 = trace.get(i);

            length += distance(c1, c2);
        }

        return length;
    }


    /**
     * Given a geo coordinate, the bearing and the distance, the method computes the geo coordinate
     * of the end point.
     * 
     * <p>
     * References:
     * <li><a href="https://www.fcc.gov/media/radio/find-terminal-coordinates">Link 1</a></li>
     * <li><a href="http://www.gpsvisualizer.com/calculators#distance">GPS Visualizer</a></li>
     * <li><a href= "https://stackoverflow.com/questions/877524/calculating-coordinates-given-a-bearing-and-a-distance">Stackoverflow</a></li>
     * </p>
     * 
     * @param origin Starting {@link Coordinate}
     * @param bearingAngle Bearing Angle (i.e. angle with north) in Degrees
     * @param distanceMtrs distance to travel on Meters
     * 
     * @return The Destination/Terminal {@link Coordinate}.
     */
    public static Coordinate terminalCoordinate(Coordinate origin, double bearingAngle, double distanceMtrs) {
        // Lat & Lng in radians
        double origLat = toRadians(origin.getLatitude());
        double origLng = toRadians(origin.getLongitude());

        double radialDistance = distanceMtrs / RADIUS_EARTHS_MTRS;
        double bearingInRads = 2 * PI - bearingAngle;

        double lat = asin(sin(origLat) * cos(radialDistance) + cos(origLat) * sin(radialDistance) * cos(bearingInRads));

        double lng;
        if (cos(lat) == 0) {
            // Endpoint a pole
            lng = origLng;
        } else {
            lng = ((origLng - asin(sin(bearingInRads) * sin(radialDistance) / cos(lat)) + PI) % (2 * PI)) - PI;
        }

        return Coordinate.from(toDegrees(lat), toDegrees(lng));
    }




    // Orientation (Turn/Side of the third point from the first Two)
    // ------------------------------------------------------------------------

    /**
     * Find out the orientation/side of a {@link Coordinate} from line (that connects point one to two).
     * 
     * @param c1 first {@link Coordinate} of the line
     * @param c2 second/last {@link Coordinate} of the line
     * @param p the third point {@link Coordinate} which gets checked for the orientation
     * 
     * @return -1 if the third {@link Coordinate} lies on the left side of the line (counter-clockwise orientation).
     *         0 if the third {@link Coordinate} lies on the line (collinear).
     *         1 if the third {@link Coordinate} lies on the right side of the line (clockwise orientation).
     */
    public static int orientation(Coordinate c1, Coordinate c2, Coordinate p) {
        // Distances
        double px = p.getLongitude();
        double py = p.getLatitude();

        double c1x = c1.getLongitude();
        double c1y = c1.getLatitude();
        double c2x = c2.getLongitude();
        double c2y = c2.getLatitude();

        // Slope difference
        double slopeDiffNumerator = (c2y - c1y) * (px - c2x) - (c2x - c1x) * (py - c2y);
        if (slopeDiffNumerator == 0) {
            return 0; // Colinear
        }

        // Clock or Counterclock wise
        return (slopeDiffNumerator > 0) ? 1 : -1;
    }




    // Nearest
    // ------------------------------------------------------------------------

    /**
     * Given the list of coordinates, get the coordinate which is closest to the given point.
     * 
     * @param point {@link Coordinate} from which the nearest needs to be found.
     * @param coordinates {@link Coordinate}s in which the nearest needs to be found.
     * 
     * @return the nearest {@link Coordinate} in the collection.
     */
    public static Coordinate nearest(Coordinate point, List<Coordinate> coordinates) {
        // Sanity checks
        if (point == null) {
            throw new IllegalArgumentException("Coordinate from which nearest to be found shouldn't be null");
        }

        if (CollectionUtils.isEmpty(coordinates)) {
            return null;
        }

        Coordinate nearest = null;
        Double dist = Double.MAX_VALUE;
        for (int i = 0; i < coordinates.size(); i++) {
            Coordinate c = coordinates.get(i);
            Double distance = distance(point, c);
            if (distance < dist) {
                dist = distance;
                nearest = c;
            }
        }

        return nearest;
    }

    /**
     * Finds the nearest point projection on the line that connects the given two points. Note that
     * the nearest point is returned only if it lies on line segment, otherwise null is returned.
     * 
     * @param from {@link Coordinate} from which the nearest coordinate needs to be found.
     * @param c1 first end {@link Coordinate} of the line segment.
     * @param c2 other end {@link Coordinate} of the line segment.
     * 
     * @return the nearest {@link Coordinate} on the line segment.
     */
    public static Coordinate nearestProjection(Coordinate from, Coordinate c1, Coordinate c2) {
        // Distances
        double ap = distance(c1, from);
        double bp = distance(c2, from);
        double ab = distance(c1, c2);

        double lambda = (ap * ap - bp * bp) / (2 * ab * ab) + .5;
        if (lambda > 1 || lambda < 0) {
            return null;
        }

        double latitude = c1.getLatitude() + lambda * (c2.getLatitude() - c1.getLatitude());
        double longitude = c1.getLongitude() + lambda * (c2.getLongitude() - c1.getLongitude());
        if (Double.isNaN(latitude) || Double.isNaN(longitude)) {
            LOGGER.error("NaN occured while finding nearest point to {} between {} and {}", from, c1, c2);
            return null;
        }

        final Coordinate result = Coordinate.from(latitude, longitude);
        LOGGER.debug("A: {}, B: {}, p: {}, o: {}", c1, c2, from, result);
        return result;
    }

    /**
     * Finds the nearest point projection on the lineString/trace.
     * 
     * @param from {@link Coordinate} from which the nearest coordinate needs to be found.
     * @param trace {@link Coordinate}s trace
     * 
     * @return
     */
    public static Coordinate nearestProjection(Coordinate from, List<Coordinate> trace) {
        // Sanity checks
        if (CollectionUtils.isEmpty(trace)) {
            return null;
        }

        Coordinate nearestProj = null;
        Double nearestProjLength = Double.MAX_VALUE;
        for (int i = 1; i < trace.size(); i++) {
            Coordinate c1 = trace.get(i - 1);
            Coordinate c2 = trace.get(i);
            Coordinate proj = nearestProjection(from, c1, c2);
            if (proj == null) {
                continue;
            }

            Double distance = distance(from, proj);
            if (distance < nearestProjLength) {
                nearestProjLength = distance;
                nearestProj = proj;
            }
        }

        return nearestProj;
    }

}
