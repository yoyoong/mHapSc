package com.bean;

import java.io.Serializable;

public class NanopolishInfo implements Serializable {
    public String chrom; //
    public String strand; //
    public Integer start; //
    public Integer end; //
    public String readName; //
    public Double logLikRatio; //
    public Double logLikMethylated; //
    public Double logLikUnmethylated; //
    public Integer numCallingStrands; //
    public Integer numMotifs; //
    public String sequence; //

    public String getChrom() {
        return chrom;
    }

    public void setChrom(String chrom) {
        this.chrom = chrom;
    }

    public String getStrand() {
        return strand;
    }

    public void setStrand(String strand) {
        this.strand = strand;
    }

    public Integer getStart() {
        return start;
    }

    public void setStart(Integer start) {
        this.start = start;
    }

    public Integer getEnd() {
        return end;
    }

    public void setEnd(Integer end) {
        this.end = end;
    }

    public String getReadName() {
        return readName;
    }

    public void setReadName(String readName) {
        this.readName = readName;
    }

    public Double getLogLikRatio() {
        return logLikRatio;
    }

    public void setLogLikRatio(Double logLikRatio) {
        this.logLikRatio = logLikRatio;
    }

    public Double getLogLikMethylated() {
        return logLikMethylated;
    }

    public void setLogLikMethylated(Double logLikMethylated) {
        this.logLikMethylated = logLikMethylated;
    }

    public Double getLogLikUnmethylated() {
        return logLikUnmethylated;
    }

    public void setLogLikUnmethylated(Double logLikUnmethylated) {
        this.logLikUnmethylated = logLikUnmethylated;
    }

    public Integer getNumCallingStrands() {
        return numCallingStrands;
    }

    public void setNumCallingStrands(Integer numCallingStrands) {
        this.numCallingStrands = numCallingStrands;
    }

    public Integer getNumMotifs() {
        return numMotifs;
    }

    public void setNumMotifs(Integer numMotifs) {
        this.numMotifs = numMotifs;
    }

    public String getSequence() {
        return sequence;
    }

    public void setSequence(String sequence) {
        this.sequence = sequence;
    }
}
