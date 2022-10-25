package com.args;

import java.io.Serializable;

public class MHBDiscoveryArgs implements Serializable {
    public String mHapPath = ""; // Indexed haplotype file
    public String bcFile = ""; // Barcode ID file
    public String cpgPath = ""; // CpG position file
    public String region = ""; // One interval [chr1:1000-2000]
    public String bedFile = ""; // BED file
    public Integer window = 5; // Size of core window
    public Double r2 = 0.5; // R square cutoff
    public Double pvalue = 0.05; // P value cutoff
    public String outputDir = ""; // output directory, created in advance
    public String tag = "MHBDiscovery.out"; // prefix of the output files
    public boolean qcFlag = false; // whether output matrics for QC

    public String getmHapPath() {
        return mHapPath;
    }

    public void setmHapPath(String mHapPath) {
        this.mHapPath = mHapPath;
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

    public Integer getWindow() {
        return window;
    }

    public void setWindow(Integer window) {
        this.window = window;
    }

    public Double getR2() {
        return r2;
    }

    public void setR2(Double r2) {
        this.r2 = r2;
    }

    public Double getPvalue() {
        return pvalue;
    }

    public void setPvalue(Double pvalue) {
        this.pvalue = pvalue;
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

    public boolean isQcFlag() {
        return qcFlag;
    }

    public void setQcFlag(boolean qcFlag) {
        this.qcFlag = qcFlag;
    }
}
