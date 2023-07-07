package com.common;

final public class Constants {
    // parameter description
    public static final String INPUTPATH_DESCRIPTION = "input file, gz format";
    public static final String SAMBAMPATH_DESCRIPTION = "input file, SAM/BAM format, should be sorted by samtools";
    public static final String SAMBAMPATHS_DESCRIPTION = "multiple input file, SAM/BAM format, should be sorted by samtools";
    public static final String MHAPPATH_DESCRIPTION = "input file, mhap.gz format,generated by mHapTools and indexed";
    public static final String CPGPATH_DESCRIPTION = "genomic CpG file, gz format and indexed";
    public static final String REGION_DESCRIPTION = "one region, in the format of chr:start-end";
    public static final String BEDPATH_DESCRIPTION = "a bed file, bed format, one query region per line";
    public static final String BEDPATHS_DESCRIPTION = "multi bed files path, one query region per line in a bed file";
    public static final String BCFILE_DESCRIPTION = "a barcode file, txt format, one barcode ID per line";
    public static final String NONDIRECTIONAL_DESCRIPTION = "non-directional, do not group results by the direction of reads";
    public static final String OUTPUTFILE_DESCRIPTION = "output filename(default: out.mhap.gz)";
    public static final String MODE_DESCRIPTION = "sequencing mode, TAPS | BS(default)";
    public static final String PAT_DESCRIPTION = "whether inputPath is pat file";
    public static final String OUTFORMAT_DESCRIPTION = "output format,pdf or png [pdf]";
    public static final String STRAND_DESCRIPTION = "strand type, should be plus,minus or both [both]";
    public static final String MAXREADS_DESCRIPTION = "the max number of reads to plot [50]";
    public static final String MAXLENGTH_DESCRIPTION = "the max length of region to plot [2000]";
    public static final String MERGE_DESCRIPTION = "indicates whether identical mHaps should be merged";
    public static final String SIMULATION_DESCRIPTION = "indicates whether mHaps should be simulated";
    public static final String CUTREADS_DESCRIPTION = "indicates whether only keep CpGs in the defined region";
    public static final String TAG_DESCRIPTION = "prefix of the output file(s)";
    public static final String MINK_DESCRIPTION = "minimum k-mer length for MHL [1]";
    public static final String MAXK_DESCRIPTION = "maximum k-mer length for MHL [10]";
    public static final String K_DESCRIPTION = "k-mer length for entropy, PDR, and CHALM, can be 3, 4, or 5 [4]";
    public static final String R2COV_DESCRIPTION = "minimal number of reads that cover two CpGs for R2 calculation [20]";
    public static final String METRICS_DESCRIPTION = "mHap-level metrics,including MM,PDR,CHALM,MHL,MCR,MBS,Entropy,and R2";
    public static final String METRICS_MM_DESCRIPTION = "mHap-level metrics,including MM";
    public static final String OUTPUTDIR_DESCRIPTION = "output directory, created in advance";
    public static final String CPGCOV_DESCRIPTION = "minimal number of CpG coverage for MM calculation [5]";
    public static final String K4PLUS_DESCRIPTION = "minimal number of reads that cover 4 or more CpGs for PDR, CHALM, MHL, MCR, MBS and Entropy [5]";
    public static final String WINDOW_DESCRIPTION = "Size of core window";
    public static final String R2_DESCRIPTION = "R square cutoff";
    public static final String PVALUE_DESCRIPTION = "P value cutoff";
    public static final String QCFLAG_DESCRIPTION = "whether output matrics for QC";
    public static final String STARTCHR_DESCRIPTION = "start chromosome of a genome, in the format of chrXX";
    public static final String STARTBASE_DESCRIPTION = "start base of a genome, Integer";
    public static final String ENDCHR_DESCRIPTION = "end chromosome of a genome, in the format of chrXX";
    public static final String ENDBASE_DESCRIPTION = "end base of a genome, Integer";
    public static final String BIGWIG_DESCRIPTION = "the input bigwig file of a metrics from a sample";
    public static final String BIGWIG1_DESCRIPTION = "the first input bigwig file of a metrics from a sample";
    public static final String BIGWIG2_DESCRIPTION = "the second input bigwig file of other metrics, or from other sample";
    public static final String BIGWIGS_DESCRIPTION = "multi bigwig files, split by blank character";
    public static final String UPLENGTH_DESCRIPTION = "the length of upstream region from the center point [20000]";
    public static final String DOWNLENGTH_DESCRIPTION = "the length of downstream region from the center point [20000]";
    public static final String WINDOW2_DESCRIPTION = "the length of the window [1000]";
    public static final String WINDOWNUM_DESCRIPTION = "the window number of the core region [10]";
    public static final String OPENCHROMATIN_DESCRIPTION = "a open chromatin regions file, one region per line";
    public static final String GROUPNUM_DESCRIPTION = "the group number of the metrics value [10]";
    public static final String GROUPCUTOFF_DESCRIPTION = "the minimum valid value number in the group [100]";
    public static final String NANOPOLISHFLAG_DESCRIPTION = "whether inputPath is nanopolish file";
    public static final String ALLCFLAG_DESCRIPTION = "whether inputPath is allc file";
    public static final String BOXSIZE_DESCRIPTION = "Size of neighborhood, Default = 0.1 (nx(k) = ny(k) = 0.1*n)";
    public static final String ALPHA_DESCRIPTION = "Significant level (eg. 0.001, 0.01, 0.05 ...), Default = 0.01";
    public static final String NDMFLAG_DESCRIPTION = "whether generate NDM matrix";
}