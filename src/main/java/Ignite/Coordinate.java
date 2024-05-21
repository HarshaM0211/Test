package Ignite;

import org.apache.commons.lang3.StringUtils;


public final class Coordinate {

    private final double latitude;
    private final double longitude;


    // Constructor
    // ------------------------------------------------------------------------

    private Coordinate(double latitude, double longitude) {
        super();

        this.latitude = latitude;
        this.longitude = longitude;
    }


    // Getters and Setters
    // ------------------------------------------------------------------------

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }


    // Object Methods
    // ------------------------------------------------------------------------

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }

        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }

        Coordinate other = (Coordinate) obj;
        if (latitude != other.latitude || longitude != other.longitude) {
            return false;
        }

        return true;
    }

    @Override
    public String toString() {
        return "Coordinate [" + latitude + ", " + longitude + "]";
    }



    // Factory Methods
    // ------------------------------------------------------------------------

    public static Coordinate from(double latitude, double longitude) {
        // Sanity checks
        if (-90 > latitude || latitude > 90) {
            throw new IllegalArgumentException("Coordinate :: Latitude should belong to [-90, 90] : " + latitude);
        }

        if (-180 > longitude || longitude > 180) {
            throw new IllegalArgumentException("Coordinate :: Longitude should belong to [-180, 180] : " + longitude);
        }

        return new Coordinate(latitude, longitude);
    }


    // String

    public static Coordinate from(String inLatStr, String inLngStr) {
        // Sanity checks
        if (StringUtils.isBlank(inLatStr)) {
            throw new IllegalArgumentException("Coordinate :: input Latitude Str is blank");
        }

        if (StringUtils.isBlank(inLngStr)) {
            throw new IllegalArgumentException("Coordinate :: input Longitude Str is blank");
        }

        final String latStr = inLatStr.replaceAll("[^\\.0-9]", "");
        final String lngStr = inLngStr.replaceAll("[^\\.0-9]", "");

        double latitude = Double.valueOf(latStr);
        double longitude = Double.valueOf(lngStr);

        return from(latitude, longitude);
    }

    public static Coordinate from(String latLng) {
        // Sanity checks
        if (StringUtils.isBlank(latLng)) {
            throw new IllegalArgumentException("Coordinate :: latLng string should not be blank");
        }

        String[] splits = latLng.replaceAll("\\s+", "").split(",");
        if (splits.length != 2) {
            String errMsg = String.format("Invalid latLng string passed to construct Coordinate : %s", latLng);
            throw new IllegalArgumentException(errMsg);
        }

        return from(splits[0], splits[1]);
    }

}
