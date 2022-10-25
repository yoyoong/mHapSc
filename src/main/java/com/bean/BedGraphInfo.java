package com.bean;

import java.io.Serializable;

public class BedGraphInfo implements Serializable {
    public String chrom;
    public Integer start;
    public Integer end;
    public Float MM;
    public Float PDR;
    public Float CHALM;
    public Float MHL;
    public Float MCR;
    public Float MBS;
    public Float Entropy;
    public Float R2;

    public String getChrom() {
        return chrom;
    }

    public void setChrom(String chrom) {
        this.chrom = chrom;
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

    public Float getMM() {
        return MM;
    }

    public void setMM(Float MM) {
        this.MM = MM;
    }


    public Float getR2() {
        return R2;
    }

    public void setR2(Float r2) {
        R2 = r2;
    }

    public String printMM() {
        return this.getChrom() + "\t" + this.getStart() + "\t" + this.getEnd() + "\t" + this.getMM() + "\n";
    }

    public String printR2() {
        return this.getChrom() + "\t" + this.getStart() + "\t" + this.getEnd() + "\t" + this.getR2() + "\n";
    }
}
