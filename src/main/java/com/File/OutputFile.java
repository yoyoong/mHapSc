package com.File;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

public class OutputFile {
    public static final Logger log = LoggerFactory.getLogger(OutputFile.class);

    BufferedWriter bufferedWriter = null;

    public OutputFile(String directory, String fileName) throws IOException {
        String filePath = "";
        if (directory != null && !directory.equals("")) {
            // create the output directory
            java.io.File outputDir = new java.io.File(directory);
            if (!outputDir.exists()){
                if (!outputDir.mkdirs()){
                    log.error("create" + outputDir.getAbsolutePath() + "fail");
                    return;
                }
            }
            filePath = directory + "/" + fileName;
        } else {
            filePath = fileName;
        }

        // create the output file
        java.io.File file = new java.io.File(filePath);
        if (!file.exists()) {
            if (!file.createNewFile()) {
                log.error("create" + file.getAbsolutePath() + "fail");
                return;
            }
        } else {
            FileWriter fileWriter =new FileWriter(file.getAbsoluteFile());
            fileWriter.write("");  //写入空
            fileWriter.flush();
            fileWriter.close();
        }
        FileWriter fileWriter = new FileWriter(file.getAbsoluteFile(), true);
        bufferedWriter = new BufferedWriter(fileWriter);
    }

    public void writeHead(String head) throws Exception {
        bufferedWriter.write(head);
    }

    public void writeLine(String line) throws Exception {
        bufferedWriter.write(line);
    }

    public void close() throws IOException {
        bufferedWriter.close();
    }
}
