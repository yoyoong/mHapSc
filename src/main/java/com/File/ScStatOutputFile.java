package com.File;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ScStatOutputFile extends OutputFile {
    public ScStatOutputFile(String directory, String fileName) throws IOException {
        super(directory, fileName);
    }

    private List<String> barcodeList = new ArrayList<>();
    public List<String> getBarcodeList() {
        return barcodeList;
    }
    public void setBarcodeList(List<String> barcodeList) {
        this.barcodeList = barcodeList;
    }

    @Override
    public void writeHead(String head) throws IOException, IllegalAccessException {
        String headString = head;
        for (String barcode : this.barcodeList) { // joint the fields generate line string
            headString += "\t" + barcode;
        }
        headString += "\n";
        bufferedWriter.write(headString);
    }

}
