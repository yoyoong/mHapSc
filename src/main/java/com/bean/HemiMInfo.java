package com.bean;

import java.io.Serializable;

public class HemiMInfo implements Serializable {
    public String chrom;
    public Integer pos;
    public String cpg;
    public String barCode;

    public String getChrom() {
        return chrom;
    }

    public void setChrom(String chrom) {
        this.chrom = chrom;
    }

    public Integer getPos() {
        return pos;
    }

    public void setPos(Integer pos) {
        this.pos = pos;
    }

    public String getCpg() {
        return cpg;
    }

    public void setCpg(String cpg) {
        this.cpg = cpg;
    }

    public String getBarCode() {
        return barCode;
    }

    public void setBarCode(String barCode) {
        this.barCode = barCode;
    }

    public String index() {
        return this.chrom + "\t" + this.pos;
    }

    public String printPlusStrand() {
        return this.chrom + "\t" + this.pos + "\t" + this.cpg + "\t" + "+" + "\t" + this.barCode + "\n";
    }

    public String printMinusStrand() {
        return this.chrom + "\t" + this.pos + "\t" + this.cpg + "\t" + "-" + "\t" + this.barCode + "\n";
    }
}
