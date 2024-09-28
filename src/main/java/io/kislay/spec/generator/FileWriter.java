package io.kislay.spec.generator;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class FileWriter {

    public static void writeToFile(String fileName, String content) {
        System.out.println("Writing to disk: " + fileName);
        Path filePath = Paths.get(fileName);

        try {
            // Check if the file exists
            if (Files.exists(filePath)) {
                // If the file exists, delete it
                Files.delete(filePath);
            }

            // Write the content to the file (this will create a new file or overwrite the existing one)
            Files.writeString(filePath, content);
            System.out.println("File " + fileName + " written successfully!");

        } catch (IOException e) {
            System.err.println("An error occurred while writing to the file: " + e.getMessage());
        }
    }
}

