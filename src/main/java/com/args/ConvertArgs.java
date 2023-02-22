package com.args;

import com.common.Annotation;

import java.io.Serializable;

public class ConvertArgs implements Serializable {
    @Annotation("input file, gz format")
    public String inputPath = "";
    @Annotation("genomic CpG file, gz format and Indexed")
    public String cpgPath = "";
    @Annotation("output directory, created in advance")
    public String outputDir = "";
    @Annotation("prefix of the output files")
    public String tag = "convert.out";

    public boolean nanopolish = false; // whether inputPath is nanopolish file

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

    public boolean isNanopolish() {
        return nanopolish;
    }

    public void setNanopolish(boolean nanopolish) {
        this.nanopolish = nanopolish;
    }
}
