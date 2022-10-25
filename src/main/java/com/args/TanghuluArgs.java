package com.args;

import java.io.Serializable;

public class TanghuluArgs implements Serializable {
    public String mhapPath = "";
    public String cpgPath = "";
    public String region = "";
    public Integer outcut = 2000;
    public String bcFile = "";
    public String outputDir = ""; // output directory, created in advance
    public String tag = "tanghulu.out"; // prefix of the output files

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

    public Integer getOutcut() {
        return outcut;
    }

    public void setOutcut(Integer outcut) {
        this.outcut = outcut;
    }

    public String getBcFile() { return bcFile; }

    public void setBcFile(String bcFile) { this.bcFile = bcFile; }

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
}
