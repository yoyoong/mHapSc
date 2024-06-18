package com.args;

import com.common.Annotation;
import com.common.Constants;

import java.io.Serializable;

public class TanghuluArgs implements Serializable {
    @Annotation(Constants.MHAPPATH_DESCRIPTION)
    public String mhapPath = "";
    @Annotation(Constants.CPGPATH_DESCRIPTION)
    public String cpgPath = "";
    @Annotation(Constants.REGION_DESCRIPTION)
    public String region = "";
    @Annotation(Constants.OUTCUT_DESCRIPTION)
    public Integer outcut = 2000;
    @Annotation(Constants.BCFILE1_DESCRIPTION)
    public String bcFile = "";
    @Annotation(Constants.OUTPUTDIR_DESCRIPTION)
    public String outputDir = "";
    @Annotation(Constants.TAG_DESCRIPTION + " [tanghulu.out]")
    public String tag = "tanghulu.out";

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
