package org.apache.bookkeeper.bookie;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class BufferedChannelUtils {
    public static final String EMPTY_EXISTING_FILE_NAME = "EmptyExistingFile.log";
    public static final String NON_EMPTY_EXISTING_FILE_NAME = "NonEmptyExistingFile.log";
    public static final String NON_EXISTING_FILE_NAME = "NonExistingFile.log";

    public static void createFile(String filename){
        String filePath = "../resources/" + filename;
        // Create a File object
        File file = new File(filePath);
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

        try (FileOutputStream fos = new FileOutputStream(filename)) {
            // Write a single byte (value 42) to the file
            fos.write(data);
            System.out.println("Byte written to file.");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void deleteFile(String filename){
        String filePath = "../resources/" + filename;
        File file = new File(filePath);

        if (file.delete()) {
            System.out.println("File deleted successfully.");
        } else {
            System.out.println("Failed to delete the file.");
        }
    }

}
