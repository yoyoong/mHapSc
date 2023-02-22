package com.File;

import com.bean.Region;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class BedFile {
    public static final Logger log = LoggerFactory.getLogger(BedFile.class);

    File bedFile;

    public BedFile(String bedPath) {
        bedFile = new File(bedPath);
    }

    public List<Region> parseWholeFile() throws Exception {
        List<Region> regionList = new ArrayList<>();
        BufferedReader bufferedReader = new BufferedReader(new FileReader(bedFile));
        String bedLine = "";
        while ((bedLine = bufferedReader.readLine()) != null && !bedLine.equals("")) {
            Region region = new Region();
            if (bedLine.split("\t").length < 3) {
                log.error("Interval not in correct format.");
                break;
            }
            region.setChrom(bedLine.split("\t")[0]);
            region.setStart(Integer.valueOf(bedLine.split("\t")[1]) + 1);
            region.setEnd(Integer.valueOf(bedLine.split("\t")[2]));
            regionList.add(region);
        }
        return regionList;
    }


}
