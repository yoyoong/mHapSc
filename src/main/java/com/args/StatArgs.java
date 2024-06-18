package com.args;

import com.common.Annotation;
import com.common.Constants;

import java.io.Serializable;

public class StatArgs implements Serializable {
    @Annotation(Constants.METRICS_DESCRIPTION)
    public String metrics = "";
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
    @Annotation(Constants.TAG_DESCRIPTION + " [stat.out]")
    public String tag = "stat.out";
    @Annotation(Constants.MINK_DESCRIPTION)
    public Integer minK = 1;
    @Annotation(Constants.MAXK_DESCRIPTION)
    public Integer maxK = 10;
    @Annotation(Constants.K_DESCRIPTION)
    public Integer K = 4;
    @Annotation(Constants.STRAND_DESCRIPTION)
    public String strand = "both";
    @Annotation(Constants.R2COV_DESCRIPTION)
    public Integer r2Cov = 20;

    public String getMetrics() {
        return metrics;
    }

    public void setMetrics(String metrics) {
        this.metrics = metrics;
    }

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

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public Integer getMinK() {
        return minK;
    }

    public void setMinK(Integer minK) {
        this.minK = minK;
    }

    public Integer getMaxK() {
        return maxK;
    }

    public void setMaxK(Integer maxK) {
        this.maxK = maxK;
    }

    public Integer getK() {
        return K;
    }

    public void setK(Integer k) {
        K = k;
    }

    public String getStrand() {
        return strand;
    }

    public void setStrand(String strand) {
        this.strand = strand;
    }

    public Integer getR2Cov() {
        return r2Cov;
    }

    public void setR2Cov(Integer r2Cov) {
        this.r2Cov = r2Cov;
    }
}
