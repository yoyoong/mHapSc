package com.bean;

import java.io.Serializable;

public class AllcInfo implements Serializable {
    public String chromosome; // with no "chr"
    public Integer position; // 1-based
    public String strand; // either + or -
    public String sequenceContext; // can be more than 3 bases
    public Integer mc; // count of reads supporting methylation
    public Integer cov; // read coverage
    public Integer methylated; // indicator of significant methylation (1 if no test is performed)
    public Integer num_matches; // number of match basecalls at context nucleotides
    public Integer num_mismatches; // number of mismatches at context nucleotides

    public String getChromosome() {
        return chromosome;
    }

    public void setChromosome(String chromosome) {
        this.chromosome = chromosome;
    }

    public Integer getPosition() {
        return position;
    }

    public void setPosition(Integer position) {
        this.position = position;
    }

    public String getStrand() {
        return strand;
    }

    public void setStrand(String strand) {
        this.strand = strand;
    }

    public String getSequenceContext() {
        return sequenceContext;
    }

    public void setSequenceContext(String sequenceContext) {
        this.sequenceContext = sequenceContext;
    }

    public Integer getMc() {
        return mc;
    }

    public void setMc(Integer mc) {
        this.mc = mc;
    }

    public Integer getCov() {
        return cov;
    }

    public void setCov(Integer cov) {
        this.cov = cov;
    }

    public Integer getMethylated() {
        return methylated;
    }

    public void setMethylated(Integer methylated) {
        this.methylated = methylated;
    }

    public Integer getNum_matches() {
        return num_matches;
    }

    public void setNum_matches(Integer num_matches) {
        this.num_matches = num_matches;
    }

    public Integer getNum_mismatches() {
        return num_mismatches;
    }

    public void setNum_mismatches(Integer num_mismatches) {
        this.num_mismatches = num_mismatches;
    }
}
