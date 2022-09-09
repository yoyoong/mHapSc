package com.args;

import java.io.Serializable;

public class MHBDiscoveryArgs implements Serializable {
    public String mHapPath = ""; // Indexed haplotype file
    public String bedFile; // BED file
    public String bcFile; // Barcode ID file
    public String cpgPath = ""; // CpG position file
    public String region; // One interval [chr1:1000-2000]
    public Integer window = 5; // Size of core window
    public Double rSquare = 0.5; // R square cutoff
    public Double pValue = 0.05; // P value cutoff
    public String outFile; // Output file
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

    public Double getrSquare() {
        return rSquare;
    }

    public void setrSquare(Double rSquare) {
        this.rSquare = rSquare;
    }

    public Double getpValue() {
        return pValue;
    }

    public void setpValue(Double pValue) {
        this.pValue = pValue;
    }

    public String getOutFile() {
        return outFile;
    }

    public void setOutFile(String outFile) {
        this.outFile = outFile;
    }

    public boolean isQcFlag() {
        return qcFlag;
    }

    public void setQcFlag(boolean qcFlag) {
        this.qcFlag = qcFlag;
    }
}
