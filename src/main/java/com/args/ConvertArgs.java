package com.args;

import java.io.Serializable;

public class ConvertArgs implements Serializable {
    public String bedPath = ""; // input bed file, gz format
    public String cpgPath = ""; // genomic CpG file, gz format and Indexed
    public String outputDir = ""; // output directory, created in advance
    public String tag = ""; // prefix of the output files

    public String getBedPath() {
        return bedPath;
    }

    public void setBedPath(String bedPath) {
        this.bedPath = bedPath;
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
}
