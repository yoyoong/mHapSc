package com.File;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class CSNOutputFile extends OutputFile {
    public CSNOutputFile(String directory, String fileName) throws IOException {
        super(directory, fileName);
    }

    public String barCode;
    public String region1;
    public String region2;
    public String statistic;

    public String getRegion1() {
        return region1;
    }

    public void setRegion1(String region1) {
        this.region1 = region1;
    }

    public String getRegion2() {
        return region2;
    }

    public void setRegion2(String region2) {
        this.region2 = region2;
    }

    public String getBarCode() {
        return barCode;
    }

    public void setBarCode(String barCode) {
        this.barCode = barCode;
    }

    public String getStatistic() {
        return statistic;
    }

    public void setStatistic(String statistic) {
        this.statistic = statistic;
    }

    @Override
    public void writeHead(String head) throws Exception {
        bufferedWriter.write("barCode\tregion1\tregion2\tstatistic\n");
    }

    @Override
    public void writeLine(String line) throws Exception {
        bufferedWriter.write(barCode + "\t" + region1 + "\t" + region2 + "\t" + statistic + "\n");
    }

}
