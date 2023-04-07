package com.args;

import com.common.Annotation;
import com.common.Constants;

import java.io.Serializable;

public class ConvertArgs implements Serializable {
    @Annotation(Constants.INPUTPATH_DESCRIPTION)
    public String inputPath = "";
    @Annotation(Constants.CPGPATH_DESCRIPTION)
    public String cpgPath = "";
    @Annotation(Constants.OUTPUTDIR_DESCRIPTION)
    public String outputDir = "";
    @Annotation(Constants.TAG_DESCRIPTION)
    public String tag = "convert.out";
    @Annotation(Constants.NANOPOLISHFLAG_DESCRIPTION)
    public boolean nanopolishFlag = false;
    @Annotation(Constants.NANOPOLISHFLAG_DESCRIPTION)
    public boolean allcFlag = false;

    public String getInputPath() {
        return inputPath;
    }

    public void setInputPath(String inputPath) {
        this.inputPath = inputPath;
    }

    public String getCpgPath() {
        return cpgPath;
    }

    public void setCpgPath(String cpgPath) {
        this.cpgPath = cpgPath;
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

    public boolean isNanopolishFlag() {
        return nanopolishFlag;
    }

    public void setNanopolishFlag(boolean nanopolishFlag) {
        this.nanopolishFlag = nanopolishFlag;
    }

    public boolean isAllcFlag() {
        return allcFlag;
    }

    public void setAllcFlag(boolean allcFlag) {
        this.allcFlag = allcFlag;
    }
}
