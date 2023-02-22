package com.File;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class StatOutputFile extends OutputFile {
    public StatOutputFile(String directory, String fileName) throws IOException {
        super(directory, fileName);
    }

    private List<String> metricsList = new ArrayList<>();
    public List<String> getMetricsList() {
        return metricsList;
    }
    public void setMetricsList(List<String> metricsList) {
        this.metricsList = metricsList;
    }

    @Override
    public void writeHead(String head) throws IOException, IllegalAccessException {
        String headString = head;
        for (String metrics : metricsList) { // joint the fields generate line string
            headString += "\t" + metrics;
        }
        headString += "\n";
        bufferedWriter.write(headString);
    }

    @Override
    public void writeLine(String line) throws IOException, IllegalAccessException {

    }
}
