package Ignite;

import static java.nio.file.LinkOption.NOFOLLOW_LINKS;
import static java.util.stream.Collectors.toList;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.FileAttribute;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Stream;
import java.util.zip.GZIPInputStream;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public final class FileUtil {

    private static final Logger LOGGER = LoggerFactory.getLogger(FileUtil.class);


    // Constructors
    // ------------------------------------------------------------------------

    private FileUtil() {
        super();

        // Private Constructor
    }


    // Utility Methods
    // ------------------------------------------------------------------------

    public static final Path getValidPath(String pathStr) {
        // Sanity check
        if (StringUtils.isBlank(pathStr)) {
            String errMsg = String.format("Path is BLANK");
            LOGGER.error(errMsg);
            throw new ValidationException(errMsg);
        }

        return Paths.get(pathStr);
    }

    public static boolean isDirectoryExists(String directoryPathStr) {
        final Path dirPath = FileUtil.getValidPath(directoryPathStr);
        return FileUtil.isDirectoryExists(dirPath);
    }

    public static final boolean isDirectoryEmpty(Path dirPath) throws IOException {
        return !(Files.list(dirPath).count() > 0);
    }

    public static final boolean isFileExists(Path filePath) {
        return Files.exists(filePath, new LinkOption[] {LinkOption.NOFOLLOW_LINKS});
    }

    public static final List<Path> listFilePaths(Path directoryPath) throws IOException {
        try (Stream<Path> paths = Files.walk(directoryPath)) {
            return paths.filter(Files::isRegularFile).collect(toList());
        }
    }


    private static boolean isDirectoryExists(Path directoryPath) {
        return Files.exists(directoryPath, NOFOLLOW_LINKS) && Files.isDirectory(directoryPath, NOFOLLOW_LINKS);
    }


    // Clean Directory

    public static boolean cleanDirectory(String directoryPathStr) throws IOException {
        LOGGER.info("Cleaning the directory - {}", directoryPathStr);

        // Path
        final Path dirPath = FileUtil.getValidPath(directoryPathStr);

        // Directory exists?
        if (!FileUtil.isDirectoryExists(dirPath)) {
            String errMsg = String.format("INVALID directory path - %s", directoryPathStr);
            LOGGER.error(errMsg);
            throw new ValidationException(errMsg);
        }

        // List Directory Items
        final List<String> filePaths = FileUtil.listItemsRecursivelyFromPath(directoryPathStr, null);
        if (filePaths.contains(directoryPathStr)) {
            filePaths.remove(directoryPathStr);
        }
        LOGGER.info("Total items found in directory :: {} - {}", directoryPathStr, filePaths.size());

        if (filePaths.size() == 0) {
            LOGGER.info("Directory - {}, is empty", directoryPathStr);
            return true;
        }

        // Delete Items
        filePaths.stream().forEach(t -> {
            try {
                deleteFile(t);
            } catch (IOException e) {
                LOGGER.error("Failed to delete file - {}", t, e);
            }
        });

        // Is directory empty?
        if (isDirectoryEmpty(dirPath)) {
            LOGGER.info("Deleted all items from the directory - {}", directoryPathStr);
            return true;
        }
        return false;
    }


    // Listing Files

    public static List<String> listFilesRecursivelyFromPath(String pathStr) throws IOException {
        return FileUtil.listItemsRecursivelyFromPath(pathStr, Files::isRegularFile);
    }

    private static <T, R> List<String> listItemsRecursivelyFromPath(String pathStr, Function<Path, Boolean> filterFun)
            throws IOException {
        // Sanity check
        final Path path = FileUtil.getValidPath(pathStr);

        // Items Path
        final List<String> files = new ArrayList<>();
        try (Stream<Path> stream = Files.walk(path)) {
            stream.forEach(filePath -> {
                // Check path for null
                if (Objects.isNull(filePath)) {
                    String errMsg = String.format("Failed to list content of directory - %s", pathStr);
                    LOGGER.error(errMsg);
                    throw new ValidationException(errMsg);
                }

                // Apply Function
                if (Objects.isNull(filterFun)) {
                    files.add(filePath.toAbsolutePath().toString());
                    return;
                } else if (filterFun.apply(filePath)) {
                    files.add(filePath.toAbsolutePath().toString());
                } else {
                    // No operation
                }
            });
        }

        return files;
    }


    public static List<String> downloadAndListFilesFromAwsStorage(String directoryPath) {
        // TODO :: Implement downloading files from AWS
        return null;
    }


    public static List<String> downloadAndListFilesFromGoogleStorage(String directoryPath) {
        // TODO :: Implement downloading files from GCS
        return null;
    }


    // File Decompression
    // ------------------------------------------------------------------------

    public static String uncompressGzip(String sourceFilePathStr) throws IOException {
        // Sanity checks
        if (StringUtils.isBlank(sourceFilePathStr)) {
            String errMsg = String.format("Source file path is BLANK");
            LOGGER.error(errMsg);
            throw new ValidationException(errMsg);
        }

        // Target Directory Path
        final String sourceFileDir = sourceFilePathStr.substring(0, sourceFilePathStr.lastIndexOf("/"));

        return FileUtil.uncompressGzip(sourceFilePathStr, sourceFileDir);
    }

    public static String uncompressGzip(String sourceFilePathStr, String targetDirectoryPathStr) throws IOException {
        // Sanity check
        if (StringUtils.isBlank(targetDirectoryPathStr)) {
            String errMsg = String.format("Target file path is BLANK");
            LOGGER.error(errMsg);
            throw new ValidationException(errMsg);
        }
        final Path srcFilePath = FileUtil.getValidPath(sourceFilePathStr);

        // Source File
        if (Files.notExists(srcFilePath)) {
            String errMsg = String.format("File path does not exists - %s", srcFilePath);
            LOGGER.error(errMsg);
            throw new ValidationException(errMsg);
        }

        // Filename
        String fileName = srcFilePath.getFileName().toString().substring(0,
                srcFilePath.getFileName().toString().lastIndexOf('.'));

        // Clean File Path
        targetDirectoryPathStr = targetDirectoryPathStr.endsWith(File.separator)
                ? targetDirectoryPathStr.substring(0, targetDirectoryPathStr.length() - 1)
                : targetDirectoryPathStr;

        // File Path with Filename
        String trgDirPathStr = String.join(File.separator, targetDirectoryPathStr, fileName);
        final Path trgFilePath = Paths.get(trgDirPathStr);

        // Create Directory if not present
        Path parent = trgFilePath.getParent();
        if (Files.notExists(trgFilePath)) {
            Set<PosixFilePermission> permissions = PosixFilePermissions.fromString("rwxrwxrwx");
            FileAttribute<Set<PosixFilePermission>> fileAttributes = PosixFilePermissions.asFileAttribute(permissions);
            Files.createDirectories(parent, fileAttributes);
        }

        // Remove Existing File Before Copying
        Files.deleteIfExists(trgFilePath);

        // Decompression
        final FileInputStream fis = new FileInputStream(srcFilePath.toFile());
        try (GZIPInputStream gis = new GZIPInputStream(fis)) {

            // Copy
            Files.copy(gis, trgFilePath);
        }

        return trgFilePath.toString();
    }


    // Deleting File

    public static void deleteFile(String filePathStr) throws IOException {
        // Sanity check
        final Path filePath = FileUtil.getValidPath(filePathStr);

        Files.deleteIfExists(filePath);
        LOGGER.info("Deleted file :: {}", filePath);
    }

}
