package com.args;

import com.common.Annotation;
import com.common.Constants;

import java.io.Serializable;

public class R2Args implements Serializable {
    @Annotation(Constants.MHAPPATH_DESCRIPTION)
    public String mhapPath = "";
    @Annotation(Constants.CPGPATH_DESCRIPTION)
    public String cpgPath = "";
    @Annotation(Constants.REGION_DESCRIPTION)
    public String region = "";
    @Annotation(Constants.BCFILE_DESCRIPTION)
    public String bcFile = "";
    @Annotation(Constants.OUTPUTDIR_DESCRIPTION)
    public String outputDir = "";
    @Annotation(Constants.TAG_DESCRIPTION + " [R2.out]")
    public String tag = "R2.out";
    @Annotation(Constants.MHAPVIEW_DESCRIPTION)
    public boolean mhapView = false;
    @Annotation(Constants.STRAND_DESCRIPTION)
    public String strand = "both";
    @Annotation(Constants.LONGRANGE_DESCRIPTION)
    public boolean longrange = false;
    @Annotation(Constants.BEDPATH_DESCRIPTION)
    public String bedFile = "";

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

    public boolean isMhapView() {
        return mhapView;
    }

    public void setMhapView(boolean mhapView) {
        this.mhapView = mhapView;
    }

    public String getStrand() {
        return strand;
    }

    public void setStrand(String strand) {
        this.strand = strand;
    }

    public boolean isLongrange() {
        return longrange;
    }

    public void setLongrange(boolean longrange) {
        this.longrange = longrange;
    }

    public String getBedFile() {
        return bedFile;
    }

    public void setBedFile(String bedFile) {
        this.bedFile = bedFile;
    }
}
