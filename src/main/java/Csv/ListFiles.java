package Csv;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;


public class ListFiles {
    
    

    public static List<TripBean> getTrips(String rootDirectory) {
        // Specify the root directory here
        // String rootDirectory = "/home/sreeharsha/Documents/01";
        TripProcessor tripProcessor = new TripProcessor();

        List<PingBean> pings = new ArrayList<>();
        List<TripBean> tripss = new ArrayList<>();
        try {
            // Start the directory traversal
            pings = iterateDirectory(Paths.get(rootDirectory));
        } catch (IOException e) {
            e.printStackTrace();
        }


        final Map<String, List<PingBean>> devicePingsMap = pings.stream() //
                .filter(Objects::nonNull) //
                .collect(Collectors.groupingBy(c -> c.getDeviceId()));

        pings = null; // Garbage Collectible

        devicePingsMap.entrySet().parallelStream().forEach(e -> {
            String deviceId = e.getKey();
            List<PingBean> devicePings = e.getValue();

            final long startTs = System.currentTimeMillis();
            List<TripBean> trips = tripProcessor.prepareTrips(deviceId, devicePings);
            System.out.println("Trips for a Device :" + trips.size());
            tripss.addAll(trips);
            System.out.println("Trips:" + tripss.size());
        });
        return tripss;
    }

    public static List<PingBean> iterateDirectory(Path path) throws IOException {
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(path)) {
            Iterator<Path> iterator = stream.iterator();

            List<PingBean> pings = new ArrayList<>();
            while (iterator.hasNext()) {
                Path entry = iterator.next();
                if (Files.isDirectory(entry)) {
                    // Recursively iterate through subdirectories
                    iterateDirectory(entry);
                } else {
                    // Print the file name
                    System.out.println(entry.toAbsolutePath() + "Extracting pings from here : ");
                    Main main = new Main();
                    pings.addAll(main.extract(entry.toAbsolutePath().toString()));
                    System.out.println(pings.size());
                    System.out.println("Done collecting Pings.");
                }
            }
            return pings;
        }
    }



}

