package com.File;

import com.bean.Region;
import htsjdk.tribble.readers.TabixReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class CpgFile {
    public static final Logger log = LoggerFactory.getLogger(CpgFile.class);

    TabixReader tabixReader;
    TabixReader.Iterator cpgIterator;

    public CpgFile(String cpgPath) throws IOException {
        tabixReader = new TabixReader(cpgPath);
    }

    public List<Integer> parseByRegion(Region region) throws Exception {
        List<Integer> cpgPosList = new ArrayList<>();
        String osName = System.getProperty("os.name");
        if(osName.startsWith("Linux")) { // windows系统end要加1？linux系统的end值与某行第二列相同时，该行不能扫描进来？
            cpgIterator = tabixReader.query(region.getChrom(), region.getStart(), region.getEnd() + 1);
        } else {
            cpgIterator = tabixReader.query(region.getChrom(), region.getStart(), region.getEnd());
        }

        String cpgLine = "";
        while((cpgLine = cpgIterator.next()) != null) {
            if (cpgLine.split("\t").length < 3) {
                continue;
            } else {
                cpgPosList.add(Integer.valueOf(cpgLine.split("\t")[1]));
            }
        }
        return cpgPosList;
    }

    public List<Integer> parseByRegionWithShift(Region region, Integer shift) throws Exception {
        List<Integer> cpgPosList = new ArrayList<>();
        Integer start = region.getStart() - shift > 1 ? region.getStart() - shift : 1;
        cpgIterator = tabixReader.query(region.getChrom(), start, region.getEnd() + shift);
        String cpgLine = "";
        while((cpgLine = cpgIterator.next()) != null) {
            if (cpgLine.split("\t").length < 3) {
                continue;
            } else {
                cpgPosList.add(Integer.valueOf(cpgLine.split("\t")[1]));
            }
        }

        return cpgPosList;
    }

    public void close() {
        tabixReader.close();
    }
}