package Csv;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public class CsvWriter {

    
    public static void writeToCSV(List<TripBean> trips, String filePath) {
        
        System.out.println("trips size in #writeToCSV" + trips.size());
        try (FileWriter writer = new FileWriter(filePath)) {
            // Write CSV header
            writer.append("DeviceId,DateString,Start-TS,Start-Latitude,Start-Longitude,End-Ts,End-Latitude,End-Longitude\n");

            // Write each person to the CSV file
            for (TripBean trip : trips) {
                writer.append(trip.getDeviceId())
                        .append(",")
                        .append(trip.getDateString())
                        .append(",")
                        .append(trip.getStartTimestamp()+"")
                        .append(",")
                        .append(trip.getStartCoordinateStr())
                        .append(",")
                        .append(trip.getEndTimestamp()+"")
                        .append(",")
                        .append(trip.getEndCoordinateStr())
                        .append("\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    

    public static void main(String[] args) {
        // Create some sample Person objects
        
        List<TripBean> trips = ListFiles.getTrips("/home/sreeharsha/Documents/01");

        // Write the list of persons to a CSV file
        String filePath = "/home/sreeharsha/MyFiles/Jan01Trips.csv";
        writeToCSV(trips, filePath);

        System.out.println("CSV file created: " + filePath);
        

//        Path startDir = Paths.get("/home/sreeharsha/Documents/01");
//
//        try {
//            Files.walk(startDir)
//                .filter(Files::isRegularFile)
//                .forEach(System.out::println);
//        } catch (IOException ex) {
//            System.err.println("Error reading directory: " + ex.getMessage());
//        }

    }
}
