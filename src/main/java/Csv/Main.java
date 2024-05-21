package Csv;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import Ignite.FileUtil;


public class Main {

    private static final Logger LOGGER = LoggerFactory.getLogger(Main.class);

    public List<PingBean> extract(final String localFilepath) {

        try {
            // Uncompress the file
            final String dataFilePath = FileUtil.uncompressGzip(localFilepath);

            // Extract Ping Data
            try {
                return this.preparePings(dataFilePath);
            } catch (Exception e) {
                String errMsg = String.format("Failed while extracting ping data for file[cloud file path] - {}",
                        "cloudFilepath");
                LOGGER.error(errMsg);
                throw new RuntimeException(errMsg, e);
            } finally {
                FileUtil.deleteFile(dataFilePath);
            }
        } catch (IOException e) {
            String errMsg = String.format("Failed to uncompress file[cloud file path] - %s", "cloudFilepath");
            LOGGER.error(errMsg, e);
            throw new RuntimeException(errMsg, e);
        }
    }

    public List<PingBean> preparePings(final String filePath) throws Exception {
        // Sanity Check
        if (filePath.isBlank()) {
            LOGGER.info("Filepath is BLANK. Exiting the ping processor.");
            return new ArrayList<>();
        }

        // Start Process
        final long startTs = System.currentTimeMillis();
        LOGGER.info("Ping processor started extracting ping data from file - {}", filePath);

        // Process Files
        final List<List<String>> rawPingDataBatchList = this.extractDataFromFile(filePath, 500, true);

        final List<PingBean> pings = new ArrayList<>(rawPingDataBatchList.size() * 500);
        rawPingDataBatchList.parallelStream() //
                .map(l -> constructPingBeans(l)) //
                .forEach(l -> {
                    pings.addAll(l);
                });

        final long endTs = System.currentTimeMillis();
        final long timeDiff = endTs - startTs;
        LOGGER.info("Ping processor finished extracting ping data in {} ms", timeDiff);

        return pings;
    }



    public List<List<String>> extractDataFromFile(String inFilePathStr, int batchSize, boolean skipHeader)
            throws Exception {
        // Sanity checks
        // Verify.notNull(inFilePathStr, "#extractDataFromFile :: File path is NULL");
        // Verify.isTrue(batchSize > 0, "#extractDataFromFile :: batch size is INVALID");

        // Prepare IO to read file
        final File file = new File(inFilePathStr);
        FileInputStream fis = null;
        try {
            fis = new FileInputStream(file);

            if (Objects.isNull(fis)) {
                String errMsg = String.format("Failed to connect to stream to read file -  %s", inFilePathStr);
                LOGGER.error(errMsg);
                throw new Exception(errMsg);
            }
        } catch (FileNotFoundException e) {
            String errMsg = String.format("Exception occurred while reading the file - %s", inFilePathStr);
            LOGGER.info(errMsg);
            throw new Exception(errMsg);
        }

        final List<List<String>> rawPingDataBatchList = new ArrayList<>();

        // Read data from file
        final InputStreamReader isr = new InputStreamReader(fis, Charset.defaultCharset());
        try (BufferedReader reader = new BufferedReader(isr);) {
            // Skip Header
            if (skipHeader) {
                reader.readLine();
            }

            // Read File Data in Batch
            int count = 0;
            final List<String> inDataLines = new ArrayList<>(batchSize);
            for (String line; (line = reader.readLine()) != null;) {
                inDataLines.add(line);

                // Save the batch
                if (++count > batchSize) {
                    List<String> data = new ArrayList<>(inDataLines);
                    rawPingDataBatchList.add(data);

                    // Clear Data Lines Collection
                    inDataLines.clear();

                    // Reset Counter
                    count = 0;
                }
            }

            // Last few records < batch size
            if (inDataLines.size() > 0) {
                final List<String> data = new ArrayList<>(inDataLines);
                rawPingDataBatchList.add(data);
            }
        } catch (IOException e) {
            LOGGER.info("Exception occurred while reading the file - {}", inFilePathStr, e);
            throw new Exception(e.getMessage());
        }
        return rawPingDataBatchList;
    }



    private List<PingBean> constructPingBeans(List<String> data) {
        // Beans
        final List<PingBean> beans = new ArrayList<>(data.size());
        for (String line : data) {
            LOGGER.debug("data line :: {}", line);
            String[] dataArray = line.split("\t"); // Split by TAB

            String deviceId = dataArray[1];
            long timestamp = Long.parseLong(dataArray[0]) * 1000; // Convert seconds to milliseconds
            double latitude = Double.parseDouble(dataArray[3]);
            double longitude = Double.parseDouble(dataArray[4]);

            // Bean
            PingBean bean = new PingBean(deviceId, timestamp, latitude, longitude);
            beans.add(bean);
        }

        return beans;
    }
}
