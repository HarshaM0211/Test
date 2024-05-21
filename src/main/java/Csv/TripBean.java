package Csv;

import Ignite.DatesUtil;


public class TripBean {

    private String deviceId;

    private String dateString;
    private long startTimestamp;
    private long endTimestamp;

    private String startCoordinateStr;
    private String endCoordinateStr;


    // Constructors
    // ------------------------------------------------------------------------

    public TripBean(String deviceId, String startCoordinateStr, long startTimestamp, String endCoordinateStr,
            long endTimestamp) {
        super();

        this.deviceId = deviceId;

        this.startTimestamp = startTimestamp;
        this.endTimestamp = endTimestamp;
        this.dateString = DatesUtil.getyyyyMMddFrom(this.startTimestamp);

        this.startCoordinateStr = startCoordinateStr;
        this.endCoordinateStr = endCoordinateStr;
    }


    // Getters and Setters
    // ------------------------------------------------------------------------

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public String getDateString() {
        return dateString;
    }

    public void setDateString(String dateString) {
        this.dateString = dateString;
    }

    public long getStartTimestamp() {
        return startTimestamp;
    }

    public void setStartTimestamp(long startTimestamp) {
        this.startTimestamp = startTimestamp;
    }

    public long getEndTimestamp() {
        return endTimestamp;
    }

    public void setEndTimestamp(long endTimestamp) {
        this.endTimestamp = endTimestamp;
    }

    public String getStartCoordinateStr() {
        return startCoordinateStr;
    }

    public void setStartCoordinateStr(String startCoordinateStr) {
        this.startCoordinateStr = startCoordinateStr;
    }

    public String getEndCoordinateStr() {
        return endCoordinateStr;
    }

    public void setEndCoordinateStr(String endCoordinateStr) {
        this.endCoordinateStr = endCoordinateStr;
    }


    // Object Methods
    // ------------------------------------------------------------------------

    @Override
    public String toString() {
        return "TripBean [deviceId=" + deviceId + ", dateString=" + dateString + ", startCoordinateStr="
                + startCoordinateStr + ", endCoordinateStr=" + endCoordinateStr + "]";
    }

}
