package org.apache.bookkeeper.bookie;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class BufferedChannelUtils {
    public static final String EMPTY_EXISTING_FILE_NAME = "EmptyExistingFile.log";
    public static final String NON_EMPTY_EXISTING_FILE_NAME = "NonEmptyExistingFile.log";
    public static final String NON_EXISTING_FILE_NAME = "NonExistingFile.log";

    public static final String ROOT_DIR_PATH = System.getProperty("user.dir");

    public static final String PATH_PREFIX = "src/test/resources/";

    public static void createFile(String filename){
        // Create a File object
        File file = new File(ROOT_DIR_PATH, PATH_PREFIX + filename);
        try {
            // Create the file
            if (file.createNewFile()) {
                System.out.println("File created successfully.");
            } else {
                System.out.println("File already exists.");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void writeOneByteOnFile(String filename, byte data){
        File file = new File(ROOT_DIR_PATH, PATH_PREFIX + filename);
        try (FileOutputStream fos = new FileOutputStream(file)) {
            // Write a single byte (value 42) to the file
            fos.write(data);
            System.out.println("Byte written to file.");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void deleteFile(String filename){
        File file = new File(ROOT_DIR_PATH, PATH_PREFIX + filename);

        if (file.delete()) {
            System.out.println("File deleted successfully.");
        } else {
            System.out.println("Failed to delete the file.");
        }
    }

    public static long readFileSize(String filename){

        File file = new File(ROOT_DIR_PATH, PATH_PREFIX + filename);
        long fileSize = 0;

        if (file.exists()) {
            fileSize = file.length();
            System.out.println("File size: " + fileSize + " bytes");
        } else {
            fileSize = -1;
            System.out.println("File does not exist.");
        }
        return fileSize;
    }

}
