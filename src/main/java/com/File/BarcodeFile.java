package com.File;

import com.bean.MHapInfo;
import com.bean.Region;
import htsjdk.tribble.readers.TabixReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

public class BarcodeFile {
    public static final Logger log = LoggerFactory.getLogger(BarcodeFile.class);

    File bcFile;

    public BarcodeFile(String bcPath) {
        bcFile = new File(bcPath);
    }

    public List<String> parseBcFile() throws Exception {
        List<String> barcodeList = new ArrayList<>();
        BufferedReader bufferedReader = new BufferedReader(new FileReader(bcFile));
        String bcLine = "";
        while ((bcLine = bufferedReader.readLine()) != null && !bcLine.equals("")) {
            barcodeList.add(bcLine.split("\t")[0]);
        }
        return barcodeList;
    }
}
