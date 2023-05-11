package com.args;

import com.common.Annotation;
import com.common.Constants;

import java.io.Serializable;

public class CSNDiscoveryArgs implements Serializable {
    @Annotation(Constants.MHAPPATH_DESCRIPTION)
    public String mHapPath = "";
    @Annotation(Constants.BCFILE_DESCRIPTION)
    public String bcFile = "";
    @Annotation(Constants.CPGPATH_DESCRIPTION)
    public String cpgPath = "";
    @Annotation(Constants.BEDPATH_DESCRIPTION)
    public String bedPath = "";
    @Annotation(Constants.BOXSIZE_DESCRIPTION)
    public Double boxSize = 0.1;
    @Annotation(Constants.ALPHA_DESCRIPTION)
    public Double alpha = 0.01;
    @Annotation(Constants.OUTPUTDIR_DESCRIPTION)
    public String outputDir = "";
    @Annotation(Constants.TAG_DESCRIPTION)
    public String tag = "CSNDiscovery.out";
    @Annotation(Constants.NDMFLAG_DESCRIPTION)
    public boolean ndmFlag = false;

    public String getmHapPath() {
        return mHapPath;
    }

    public void setmHapPath(String mHapPath) {
        this.mHapPath = mHapPath;
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

    public String getBedPath() {
        return bedPath;
    }

    public void setBedPath(String bedPath) {
        this.bedPath = bedPath;
    }

    public Double getBoxSize() {
        return boxSize;
    }

    public void setBoxSize(Double boxSize) {
        this.boxSize = boxSize;
    }

    public Double getAlpha() {
        return alpha;
    }

    public void setAlpha(Double alpha) {
        this.alpha = alpha;
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

    public boolean isNdmFlag() {
        return ndmFlag;
    }

    public void setNdmFlag(boolean ndmFlag) {
        this.ndmFlag = ndmFlag;
    }
}
