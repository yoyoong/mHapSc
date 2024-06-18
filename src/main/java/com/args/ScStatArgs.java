package com.args;

import com.common.Annotation;
import com.common.Constants;

import java.io.Serializable;

public class ScStatArgs implements Serializable {
    @Annotation(Constants.MHAPPATH_DESCRIPTION)
    public String mhapPath = "";
    @Annotation(Constants.CPGPATH_DESCRIPTION)
    public String cpgPath = "";
    @Annotation(Constants.REGION_DESCRIPTION)
    public String region = "";
    @Annotation(Constants.BEDPATH_DESCRIPTION)
    public String bedFile = "";
    @Annotation(Constants.BCFILE2_DESCRIPTION)
    public String bcFile = "";
    @Annotation(Constants.OUTPUTDIR_DESCRIPTION)
    public String outputDir = "";
    @Annotation(Constants.TAG_DESCRIPTION + " [ScStat.out]")
    public String tag = "ScStat.out";
    @Annotation(Constants.METRICS_MM_DESCRIPTION)
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

    public String getMetrics() {
        return metrics;
    }

    public void setMetrics(String metrics) {
        this.metrics = metrics;
    }

}
