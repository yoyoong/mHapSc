import junit.framework.TestCase;
import com.Main;
import org.junit.Test;

public class MainTest extends TestCase {

    @Test
    public void test_convert_nanopolish() throws Exception {
        Main main = new Main();
        String arg0 = "convert";
        String arg1 = "-inputPath";
        String arg2 = "GSM5570296_tcell_t0_methylation_calls.tsv.gz";
        String arg3 = "-cpgPath";
        String arg4 = "hg38_CpG.gz";
        String arg5 = "-outputDir";
        String arg6 = "outputDir";
        String arg7 = "-tag";
        String arg8 = "nanopolish_test";
        String arg9 = "-nanopolishFlag";
        String[] args = {arg0, arg1, arg2, arg3, arg4, arg5, arg6, arg7, arg8, arg9};

        System.out.println("Work direqtory: " + System.getProperty("user.dir"));
        String argsStr = "";
        for (int i = 0; i < args.length; i++) {
            argsStr += args[i] + " ";
        }
        System.out.println(argsStr);

        main.main(args);
    }

    @Test
    public void test_convert_allc() throws Exception {
        Main main = new Main();
        String arg0 = "convert";
        String arg1 = "-inputPath";
        String arg2 = "GSM3742106_allc_180322_CEMBA_mm_P56_P63_1A_CEMBA180226_1A_1_CEMBA180226_1A_2_A10_AD001_indexed.tsv.gz";
        String arg3 = "-cpgPath";
        String arg4 = "mm10_CpG.gz";
        String arg5 = "-outputDir";
        String arg6 = "outputDir";
        String arg7 = "-tag";
        String arg8 = "allc_test";
        String arg9 = "-allcFlag";
        String[] args = {arg0, arg1, arg2, arg3, arg4, arg5, arg6, arg7, arg8, arg9};

        System.out.println("Work direqtory: " + System.getProperty("user.dir"));
        String argsStr = "";
        for (int i = 0; i < args.length; i++) {
            argsStr += args[i] + " ";
        }
        System.out.println(argsStr);

        main.main(args);
    }

    @Test
    public void test_tanghulu() throws Exception {
        Main main = new Main();
        String arg0 = "tanghulu";
        String arg1 = "-mhapPath";
        String arg2 = "/sibcb2/bioinformatics2/liaoxiqi/mhapSC/data/CRC_hg19.mhap.gz";
        String arg3 = "-cpgPath";
        String arg4 = "/sibcb2/bioinformatics/iGenome/CpG/Idx/hg19_CpG.gz";
        String arg5 = "-region";
        String arg6 = "chr1:228746134-228746234";
        String arg7 = "-outputDir";
        String arg8 = "outputDir";
        String arg9 = "-tag";
        String arg10 = "CRC_hg19";
//        String[] args = {arg0, arg1, arg2, arg3, arg4, arg5, arg6, arg7, arg8};
        String arg11 = "-bcFile";
        String arg12 = "CRC_LN.txt";
        String[] args = {arg0, arg1, arg2, arg3, arg4, arg5, arg6, arg7, arg8, arg9, arg10};
        //String[] args = {arg0, arg1, arg2, arg3, arg4, arg5, arg6, arg7, arg8, arg9, arg10, arg11, arg12};

        System.out.println("Work direqtory: " + System.getProperty("user.dir"));
        String argsStr = "";
        for (int i = 0; i < args.length; i++) {
            argsStr += args[i] + " ";
        }
        System.out.println(argsStr);


        main.main(args);
    }

    @Test
    public void test_R2() throws Exception {
        Main main = new Main();
        String arg0 = "R2";
        String arg1 = "-mhapPath";
        String arg2 = "GSM5570296_tcell_t0.mhap.gz";
        String arg3 = "-cpgPath";
        String arg4 = "hg19_CpG.gz";
        String arg5 = "-region";
        String arg6 = "chr2:233246049-233246076";
        String arg7 = "-bcFile";
        String arg8 = "CRC_LN.txt";
        String arg9 = "-outputDir";
        String arg10 = "outputDir";
        String arg11 = "-tag";
        String arg12 = "CRC_hg19";
        String arg13 = "-longrange";
        String arg14 = "-mHapView";
        String arg15 = "-bedFile";
        String arg16 = "CRC_sc_bulk.bed";
        String[] args = {arg0, arg1, arg2, arg3, arg4, arg5, arg6, arg9, arg10, arg11, arg12, arg13, arg14, arg15, arg16};
        //String[] args = {arg0, arg1, arg2, arg3, arg4, arg5, arg6, arg7, arg8, arg9, arg10, arg11, arg12, arg13, arg14, arg15, arg16};

        System.out.println("Work direqtory: " + System.getProperty("user.dir"));
        String argsStr = "";
        for (int i = 0; i < args.length; i++) {
            argsStr += args[i] + " ";
        }
        System.out.println(argsStr);

        main.main(args);
    }

    @Test
    public void test_MHBDiscovery() throws Exception {
        Main main = new Main();
        String arg0 = "MHBDiscovery";
        String arg1 = "-mHapPath";
        String arg2 = "H103_cellline_methylationcall.mhap.gz";
//        String arg3 = "-region";
//        String arg4 = "chrX:153715286-153715374";
//        String arg3 = "-bedFile";
//        String arg4 = "CRC_MHB_non_NC.bed";
        String arg5 = "-cpgPath";
        String arg6 = "hg38_CpG.gz";
//        String arg7 = "-bcFile";
//        String arg8 = "CRC_LN.txt";
        String arg9 = "-window";
        String arg10 = "5";
        String arg11 = "-r2";
        String arg12 = "0.5";
        String arg13 = "-pvalue";
        String arg14 = "0.05";
        String arg15 = "-outputDir";
        String arg16 = "outputDir";
        String arg17 = "-tag";
        String arg18 = "CRC_hg19_MHB";
//        String arg19 = "-qc";
        String[] args = {arg0, arg1, arg2, arg5, arg6, arg9, arg10, arg11, arg12, arg13, arg14, arg15, arg16, arg17, arg18};
        //String[] args = {arg0, arg1, arg2, arg3, arg4, arg5, arg6, arg9, arg10, arg11, arg12, arg13, arg14, arg15, arg16, arg17, arg18, arg19};
        //String[] args = {arg0, arg1, arg2, arg3, arg4, arg5, arg6, arg7, arg8, arg9, arg10, arg11, arg12, arg13, arg14, arg15, arg16, arg17, arg18, arg19};

        System.out.println("Work direqtory: " + System.getProperty("user.dir"));
        String argsStr = "";
        for (int i = 0; i < args.length; i++) {
            argsStr += args[i] + " ";
        }
        System.out.println(argsStr);

        main.main(args);

    }

    @Test
    public void test_hemiM() throws Exception {
        Main main = new Main();
        String arg0 = "hemiM";
        String arg1 = "-mhapPath";
        String arg2 = "CRC_hg19.mhap.gz";
        String arg3 = "-cpgPath";
        String arg4 = "hg19_CpG.gz";
//        String arg5 = "-bedFile";
//        String arg6 = "CRC_MHB_non_NC.bed";
        String arg5 = "-region";
        String arg6 = "chr1:20000-323047";
        String arg7 = "-bcFile";
        String arg8 = "Methy_barcode.txt";
        String arg9 = "-outputDir";
        String arg10 = "outputDir";
        String arg11 = "-tag";
        String arg12 = "test";
        String arg13 = "-stat";
        String[] args = {arg0, arg1, arg2, arg3, arg4, arg5, arg6, arg7, arg8, arg9, arg10, arg11, arg12, arg13};

        System.out.println("Work direqtory: " + System.getProperty("user.dir"));
        String argsStr = "";
        for (int i = 0; i < args.length; i++) {
            argsStr += args[i] + " ";
        }
        System.out.println(argsStr);

        main.main(args);
    }

    @Test
    public void test_track() throws Exception {
        Main main = new Main();
        String arg0 = "track";
        String arg1 = "-mhapPath";
        String arg2 = "CRC_hg19.mhap.gz";
        String arg3 = "-cpgPath";
        String arg4 = "hg19_CpG.gz";
        String arg5 = "-region";
        String arg6 = "chr1:3229375-13230473";
//        String arg5 = "-bedFile";
//        String arg6 = "CRC_MHB_non_NC.bed";
        String arg7 = "-outputDir";
        String arg8 = "outputDir";
        String arg9 = "-tag";
        String arg10 = "test";
        String arg11 = "-bcFile";
        String arg12 = "CRC_LN.txt";
        String arg13 = "-metrics";
        String arg14 = "MM R2";
        String[] args = {arg0, arg1, arg2, arg3, arg4, arg5, arg6, arg7, arg8, arg9, arg10, arg8, arg9, arg10, arg11, arg12, arg13, arg14};
        //String[] args = {arg0, arg1, arg2, arg3, arg4, arg7, arg8, arg9, arg10, arg8, arg9, arg10, arg11, arg12, arg13, arg14};

        System.out.println("Work direqtory: " + System.getProperty("user.dir"));
        String argsStr = "";
        for (int i = 0; i < args.length; i++) {
            argsStr += args[i] + " ";
        }
        System.out.println(argsStr);

        main.main(args);
    }

    @Test
    public void test_Flinkage() throws Exception {
        Main main = new Main();
        String arg0 = "flinkage";
        String arg1 = "-mhapPath";
        String arg2 = "/sibcb2/bioinformatics2/liaoxiqi/mhapSC/data/CRC_hg19.mhap.gz";
        String arg3 = "-cpgPath";
        String arg4 = "/sibcb1/bioinformatics/dataupload/iGenome/CpGs/hg19/hg19_CpG.gz";
        String arg5 = "-region1";
        String arg6 = "chr1:949817-949850";
//        String arg7 = "-region2";
//        String arg8 = "chr1:969429-969458";
        String arg7 = "-bedFile";
        String arg8 = "/sibcb2/bioinformatics2/liaoxiqi/mhapSC/data/chr/CRC_MHB_chr10.bed";
//        String arg9 = "-bcFile";
//        String arg10 = "CRC_LN.txt";
        String arg11 = "-outputDir";
        String arg12 = "flinkage_500Mb_results";
        String arg13 = "-tag";
        String arg14 = "flinkage_chr10";
        String arg15 = "-limit";
        String arg16 = "5000000";
        String arg17 = "-r2Cov";
        String arg18 = "5";
        //String[] args = {arg0, arg1, arg2, arg3, arg4, arg5, arg6, arg7, arg8, arg9, arg10, arg11, arg12, arg13, arg14, arg15, arg16};
        String[] args = {arg0, arg1, arg2, arg3, arg4, arg7, arg8, arg11, arg12, arg13, arg14, arg15, arg16, arg17, arg18};

        System.out.println("Work direqtory: " + System.getProperty("user.dir"));
        String argsStr = "";
        for (int i = 0; i < args.length; i++) {
            argsStr += args[i] + " ";
        }
        System.out.println(argsStr);

        main.main(args);
    }

    @Test
    public void test_stat() throws Exception {
        Main main = new Main();
        String arg0 = "stat";
        String arg1 = "-metrics";
        String arg2 = "MHL";
        String arg3 = "-mhapPath";
        String arg4 = "CRC_hg19.mhap.gz";
        String arg5 = "-cpgPath";
        String arg6 = "hg19_CpG.gz";
//        String arg7 = "-region";
//        String arg8 = "chr1:566520-566816";
        String arg7 = "-bedPath";
        String arg8 = "CRC_MHB111.bed";
        String arg9 = "-bcFile";
        String arg10 = "";
        String arg11 = "-outputFile";
        String arg12 = "outStat.tsv";
        String arg13 = "-minK";
        String arg14 = "1";
        String arg15 = "-maxK";
        String arg16 = "10";
        String arg17 = "-K";
        String arg18 = "4";
        String arg19 = "-strand";
        String arg20 = "both";
        String arg21 = "-r2Cov";
        String arg22 = "20";

        // lack of bcFile and strand
        String[] args = {arg0, arg1, arg2, arg3, arg4, arg5, arg6, arg7, arg8, arg11, arg12, arg13, arg14, arg15, arg16, arg17, arg18, arg21, arg22};
        // all parameter
        // String[] args = {arg0, arg1, arg2, arg3, arg4, arg5, arg6, arg7, arg8, arg9, arg10, arg11, arg12, arg13, arg14, arg15, arg16, arg17, arg18, arg19, arg20, arg21, arg22};

        System.out.println("Work direqtory: " + System.getProperty("user.dir"));
        String argsStr = "";
        for (int i = 0; i < args.length; i++) {
            argsStr += args[i] + " ";
        }
        System.out.println(argsStr);

        main.main(args);
    }

    @Test
    public void test_scStat() throws Exception {
        Main main = new Main();
        String arg0 = "scStat";
        String arg1 = "-metrics";
        String arg2 = "MM";
        String arg3 = "-mhapPath";
        String arg4 = "CRC_hg19.mhap.gz";
        String arg5 = "-cpgPath";
        String arg6 = "hg19_CpG.gz";
//        String arg7 = "-region";
//        String arg8 = "chr1:566520-566816";
        String arg7 = "-bedPath";
        String arg8 = "CRC_MHB.bed";
        String arg9 = "-bcFile";
        String arg10 = "Methy_barcode.txt";
        String arg11 = "-outputDir";
        String arg12 = "ScStatOutput";
        String arg13 = "-tag";
        String arg14 = "promoter";

        String[] args = {arg0, arg1, arg2, arg3, arg4, arg5, arg6, arg7, arg8, arg9, arg10, arg11, arg12, arg13, arg14};
        System.out.println("Work direqtory: " + System.getProperty("user.dir"));
        String argsStr = "";
        for (int i = 0; i < args.length; i++) {
            argsStr += args[i] + " ";
        }
        System.out.println(argsStr);

        main.main(args);
    }

    @Test
    public void test_CSN() throws Exception {
        Main main = new Main();
        String arg0 = "CSN";
        String arg1 = "-mHapPath";
        String arg2 = "CRC_hg19.mhap.gz";
        String arg3 = "-cpgPath";
        String arg4 = "hg19_CpG.gz";
        String arg5 = "-bedPath";
        String arg6 = "CRC_MHB111.bed";
        String arg7 = "-bcFile";
        String arg8 = "Methy_barcode.txt";
        String arg9 = "-boxSize";
        String arg10 = "0.1";
        String arg15 = "-alpha";
        String arg16 = "0.01";
        String arg11 = "-outputDir";
        String arg12 = "CSN";
        String arg13 = "-tag";
        String arg14 = "CSN";
        String arg17 = "-ndmFlag";

        String[] args = {arg0, arg1, arg2, arg3, arg4, arg5, arg6, arg7, arg8, arg9, arg10, arg11, arg12, arg13, arg14, arg15, arg16, arg17};
        System.out.println("Work direqtory: " + System.getProperty("user.dir"));
        String argsStr = "";
        for (int i = 0; i < args.length; i++) {
            argsStr += args[i] + " ";
        }
        System.out.println(argsStr);

        main.main(args);
    }
}