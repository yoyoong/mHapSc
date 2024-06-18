package com.args;

import com.common.Annotation;
import com.common.Constants;

import java.io.Serializable;

public class FlinkageArgs implements Serializable {
    @Annotation(Constants.MHAPPATH_DESCRIPTION)
    public String mhapPath = "";
    @Annotation(Constants.CPGPATH_DESCRIPTION)
    public String cpgPath = "";
    @Annotation(Constants.REGION1_DESCRIPTION)
    public String region1 = "";
    @Annotation(Constants.REGION2_DESCRIPTION)
    public String region2 = "";
    @Annotation(Constants.BEDPATH_DESCRIPTION)
    public String bedFile = "";
    @Annotation(Constants.BCFILE2_DESCRIPTION)
    public String bcFile = "";
    @Annotation(Constants.OUTPUTDIR_DESCRIPTION)
    public String outputDir = "";
    @Annotation(Constants.TAG_DESCRIPTION + " [flinkage.out]")
    public String tag = "flinkage.out"; 
    @Annotation(Constants.LIMIT_DESCRIPTION)
    public Integer limit = 20000000; 
    @Annotation(Constants.R2COV2_DESCRIPTION)
    public Integer r2Cov = 5; 

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

    public Integer getR2Cov() {
        return r2Cov;
    }

    public void setR2Cov(Integer r2Cov) {
        this.r2Cov = r2Cov;
    }

}
