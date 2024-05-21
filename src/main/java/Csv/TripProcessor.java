package Csv;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import Ignite.Coordinate;
import Ignite.Geo;


public class TripProcessor {

    private static final Logger LOGGER = LoggerFactory.getLogger(TripProcessor.class);

    public static Map<String, List<TripBean>> tripDataMap = new HashMap<>();
    
    public boolean hasMoved(PingBean p1, PingBean p2) {
        // Device Distance Traveled in Meters
        double ddt = Geo.distance(p1.getLatitude(), p1.getLongitude(), p2.getLatitude(), p2.getLongitude());

        // Device Travel Time in Seconds
        double dtt = (p2.getTimestamp() - p1.getTimestamp()) / 1000;

        // Compare displacement and time w.r.t. Average Human Walking Speed (80 meter per minute)
        // half of the data observed has frequent ping data while traveling
        // if we do dtt > 60 we are losing so much trip data.
        if (dtt > 60d && ddt > 80d) {
            return true;
        }

        return false;
    }


    public List<TripBean> prepareTrips(final String deviceId, List<PingBean> pings) {
        // Sanity checks
        if (StringUtils.isBlank(deviceId)) {
            LOGGER.error("Device Id is BLANK. Exiting the trip processor.");
            return new ArrayList<>();
        }
        if (CollectionUtils.isEmpty(pings) || pings.size() < 2) {
            LOGGER.error("Inssufficient pings to process. Exiting the trip processor.");
            return new ArrayList<>();
        }

        // Trips
        final List<TripBean> trips = new ArrayList<>();

        // Order by Timestamp
        Collections.sort(pings);

        // Initialize Trip Based On Last Trip or Latest Ping
        PingBean curr = null;
        int i = 0;
        long lastTripTimestamp = 0l;
        long latestPingTimestamp = 0l;

        List<TripBean> exTrips = tripDataMap.get(deviceId);

        // int exTripCount = Objects.isNull(exTrips) ? 0 : tripDataMap.get(deviceId).size();
        // if (exTripCount > 0) {
        // exTrips = tripDataMap.get(deviceId);
        // }

        if (CollectionUtils.isNotEmpty(exTrips) && Objects.nonNull(exTrips.get(0))) {
            TripBean lastTrip = exTrips.get(0);
            lastTripTimestamp = lastTrip.getEndTimestamp();
            Coordinate coordinate = Coordinate.from(lastTrip.getEndCoordinateStr());

            curr = new PingBean(deviceId, lastTripTimestamp, coordinate.getLatitude(), coordinate.getLongitude());
        } else {
            do {
                curr = pings.get(i); // Latest Ping
                i++;
            } while (Objects.isNull(curr));
        }

        // Trip Preparation
        for (; i < pings.size(); i++) {
            PingBean next = pings.get(i);

            // If device has MOVED
            if (hasMoved(curr, next)) {
                // Trip
                String startLocStr = curr.getCoordinateStr();
                long startTime = curr.getTimestamp();
                String endLocStr = next.getCoordinateStr();
                long endTime = next.getTimestamp();
                final TripBean trip = new TripBean(deviceId, startLocStr, startTime, endLocStr, endTime);

                // Add to Trip Collection
                trips.add(trip);
            }

            // Swap Coordinates for next comparison
            curr = next;
        }

        // No Trips
        if (trips.isEmpty()) {
            return new ArrayList<>();
        }

        // Save Trips
        tripDataMap.put(deviceId, trips);
        // try {
        // tripService.create(trips);
        // } catch (Exception e) {
        // LOGGER.error("Failed to create trips for device - {}", deviceId);
        //
        // // Failure Case - Delete device's relevant data
        // if (lastTripTimestamp > latestPingTimestamp) {
        // tripService.purgeByTimestamp(null, lastTripTimestamp + 1);
        // } else {
        // tripService.purgeByTimestamp(null, latestPingTimestamp);
        // }
        // }

        return trips;
    }

}
