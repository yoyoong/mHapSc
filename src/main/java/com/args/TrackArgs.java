package com.args;

import java.io.Serializable;

public class TrackArgs implements Serializable {
    public String mhapPath = ""; // input file, mhap.gz format, generated by mHapTools and indexed
    public String cpgPath = ""; // genomic CpG file, gz format and Indexed
    public String region = ""; // one region, in the format of chr:start-end
    public String bedFile = ""; // BED file
    public String outputDir = ""; // output directory, created in advance
    public String tag = "track.out"; // prefix of the output files
    public String bcFile = "";
    public String metrics = "";

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

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public String getBedFile() {
        return bedFile;
    }

    public void setBedFile(String bedFile) {
        this.bedFile = bedFile;
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

    public String getBcFile() {
        return bcFile;
    }

    public void setBcFile(String bcFile) {
        this.bcFile = bcFile;
    }

    public String getMetrics() {
        return metrics;
    }

    public void setMetrics(String metrics) {
        this.metrics = metrics;
    }
}
