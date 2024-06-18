package com.args;

import com.common.Annotation;
import com.common.Constants;

import java.io.Serializable;

public class HemiMArgs implements Serializable {
    @Annotation(Constants.MHAPPATH_DESCRIPTION)
    public String mhapPath = "";
    @Annotation(Constants.CPGPATH_DESCRIPTION)
    public String cpgPath = "";
    @Annotation(Constants.BEDPATH_DESCRIPTION)
    public String bedFile = "";
    @Annotation(Constants.REGION_DESCRIPTION)
    public String region = "";
    @Annotation(Constants.BCFILE2_DESCRIPTION)
    public String bcFile = "";
    @Annotation(Constants.OUTPUTDIR_DESCRIPTION)
    public String outputDir = "";
    @Annotation(Constants.TAG_DESCRIPTION + " [hemiM.out]")
    public String tag = "hemiM.out";
    @Annotation(Constants.STAT_DESCRIPTION)
    public Boolean stat = false;

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

    public String getBedFile() {
        return bedFile;
    }

    public void setBedFile(String bedFile) {
        this.bedFile = bedFile;
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
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

    public Boolean getStat() {
        return stat;
    }

    public void setStat(Boolean stat) {
        this.stat = stat;
    }
}
