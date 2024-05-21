package Csv;


public class PingBean implements Comparable<PingBean> {

    private String deviceId;

    private long timestamp;

    private double latitude;
    private double longitude;


    // Constructors
    // ------------------------------------------------------------------------

    public PingBean(String deviceId, long timestamp, double latitude, double longitude) {
        super();

        this.deviceId = deviceId;

        this.timestamp = timestamp;

        this.latitude = latitude;
        this.longitude = longitude;
    }


    // Methods
    // ------------------------------------------------------------------------

    public static String join(String separator, Object... items) {
        if (items.length == 0) {
            return "";
        }
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < items.length - 1; ++i) {
            builder.append(items[i]);
            builder.append(separator);
        }
        builder.append(items[items.length - 1]);
        return builder.toString();
    }


    @Override
    public int compareTo(PingBean o) {
        if (this.timestamp == o.getTimestamp()) {
            return 0;
        }
        if (this.timestamp > o.getTimestamp()) {
            return 1;
        }
        return -1;
    }

    public String getCoordinateStr() {
        return join(",", this.latitude, this.longitude);
    }


    // Getters and Setters
    // ------------------------------------------------------------------------

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }


    // Object Methods
    // ------------------------------------------------------------------------

    @Override
    public String toString() {
        return "RawMobileDataBean [deviceId=" + deviceId + ", timestamp=" + timestamp + ", latitude=" + latitude
                + ", longitude=" + longitude + "]";
    }

}
