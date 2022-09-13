package com.args;

import java.io.Serializable;

public class FlinkageArgs implements Serializable {

    public String mhapPath = ""; // input file, mhap.gz format, generated by mHapTools and indexed
    public String cpgPath = ""; // genomic CpG file, gz format and Indexed
    public String region1; // the first region, in the format of chr:start-end
    public String region2; // the second region, in the format of chr:start-end
    public String bedFile; // bed file
    public String bcFile; // barcode file
    public String outputDir = ""; // output directory, created in advance
    public String tag = ""; // prefix of the output files
    public Integer limit = 20000000; // the max length to calculate

    public String getMhapPath() {
        return mhapPath;
    }

    public void setMhapPath(String mhapPath) {
        this.mhapPath = mhapPath;
    }

    public String getCpgPath() {
        return cpgPath;
    }

    public void setCpgPath(String cpgPath) {
        this.cpgPath = cpgPath;
    }

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

    public String getBedFile() {
        return bedFile;
    }

    public void setBedFile(String bedFile) {
        this.bedFile = bedFile;
    }

    public String getBcFile() {
        return bcFile;
    }

    public void setBcFile(String bcFile) {
        this.bcFile = bcFile;
    }

    public String getOutputDir() {
        return outputDir;
    }

    public void setOutputDir(String outputDir) {
        this.outputDir = outputDir;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public Integer getLimit() {
        return limit;
    }

    public void setLimit(Integer limit) {
        this.limit = limit;
    }

}