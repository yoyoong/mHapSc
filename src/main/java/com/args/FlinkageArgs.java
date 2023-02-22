package com.args;

import com.common.Annotation;

import java.io.Serializable;

public class FlinkageArgs implements Serializable {
    @Annotation("input file, mhap.gz format, generated by mHapTools and indexed")
    public String mhapPath = "";
    @Annotation("genomic CpG file, gz format and Indexed")
    public String cpgPath = "";
    @Annotation("the first region, in the format of chr:start-end")
    public String region1 = "";
    @Annotation("the second region, in the format of chr:start-end")
    public String region2 = "";
    @Annotation("input BED file")
    public String bedFile = "";
    @Annotation("barcode ID file")
    public String bcFile = "";
    @Annotation("output directory, created in advance")
    public String outputDir = "";
    @Annotation("prefix of the output file(s)")
    public String tag = "flinkage.out"; 
    @Annotation("the max length to calculate")
    public Integer limit = 20000000; 
    @Annotation("minimal number of reads that cover two CpGs for R2 calculation [5]")
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
