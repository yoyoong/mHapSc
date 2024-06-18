package com.common;

final public class Constants {
    // parameter description
    public static final String INPUTPATH_DESCRIPTION = "input file, Single-cell methylation sequencing file is bed.gz format, Nanopore methylation sequencin is tsv.gz format\n";
    public static final String MHAPPATH_DESCRIPTION = "input file in sc-mHap format and indexed";
    public static final String CPGPATH_DESCRIPTION = "genomic CpG file, gz format and indexed";
    public static final String REGION_DESCRIPTION = "one region, in the format of chr:start-end";
    public static final String REGION1_DESCRIPTION = "the first region, in the format of chr:start-end";
    public static final String REGION2_DESCRIPTION = "the second region, in the format of chr:start-end";
    public static final String BEDPATH_DESCRIPTION = "a bed file, bed format, one query region per line";
    public static final String BCFILE1_DESCRIPTION = "barcode file to specify the cell to be plotted, in txt format, one barcode per line";
    public static final String BCFILE2_DESCRIPTION = "barcode file to specify the cell to be used, in txt format, one barcode per line";
    public static final String STRAND_DESCRIPTION = "strand type, should be plus,minus or both [both]";
    public static final String MHAPVIEW_DESCRIPTION = "indicates whether to draw the mhapView plot [false]";
    public static final String LONGRANGE_DESCRIPTION = "indicates whether generate a file in longrange format [false]";
    public static final String OUTCUT_DESCRIPTION = "the max length of region to plot [2000]";
    public static final String TAG_DESCRIPTION = "prefix of the output file(s)";
    public static final String MINK_DESCRIPTION = "minimum k-mer length for MHL [1]";
    public static final String MAXK_DESCRIPTION = "maximum k-mer length for MHL [10]";
    public static final String K_DESCRIPTION = "k-mer length for entropy, PDR, and CHALM, can be 3, 4, or 5 [4]";
    public static final String R2COV_DESCRIPTION = "minimal number of reads that cover two CpGs for R2 calculation [20]";
    public static final String R2COV2_DESCRIPTION = "minimal number of reads that cover two CpGs for R2 calculation [5]";
    public static final String METRICS_DESCRIPTION = "mHap-level metrics,including MM,PDR,CHALM,MHL,MCR,MBS,Entropy,and R2";
    public static final String METRICS_MM_DESCRIPTION = "mHap-level metrics,including MM";
    public static final String METRICS_MMR2_DESCRIPTION = "mHap-level metrics,including MM,R2";
    public static final String OUTPUTDIR_DESCRIPTION = "output directory, created in advance";
    public static final String WINDOW_DESCRIPTION = "Size of core window";
    public static final String R2_DESCRIPTION = "R square cutoff";
    public static final String PVALUE_DESCRIPTION = "P value cutoff";
    public static final String QCFLAG_DESCRIPTION = "whether output matrics for QC";
    public static final String LIMIT_DESCRIPTION = "the max length to calculate";
    public static final String STAT_DESCRIPTION = "whether stat the hemiM count of barcodes [false]";
    public static final String NANOPOLISHFLAG_DESCRIPTION = "indicates whether input file is nanopore methylation sequencing file";
    public static final String ALLCFLAG_DESCRIPTION = "indicates whether inputPath is allc methylation sequencing file";
    public static final String BOXSIZE_DESCRIPTION = "Size of neighborhood, Default = 0.1 (nx(k) = ny(k) = 0.1*n)";
    public static final String ALPHA_DESCRIPTION = "Significant level (eg. 0.001, 0.01, 0.05 ...), Default = 0.01";
    public static final String NDMFLAG_DESCRIPTION = "whether generate NDM matrix";
}
