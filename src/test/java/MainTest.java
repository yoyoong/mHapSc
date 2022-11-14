import junit.framework.TestCase;
import com.Main;
import org.junit.Test;

public class MainTest extends TestCase {

    @Test
    public void test_convert() throws Exception {
        Main main = new Main();
        String arg0 = "convert";
        String arg1 = "-inputPath";
        String arg2 = "methylation-log.tsv.gz";
        String arg3 = "-cpgPath";
        String arg4 = "hg38_CpG.gz";
        String arg5 = "-outputDir";
        String arg6 = "outputDir";
        String arg7 = "-tag";
        String arg8 = "nanopolish_test";
        String arg9 = "-nanopolish";
        String[] args = {arg0, arg1, arg2, arg3, arg4, arg5, arg6, arg7, arg8, arg9};

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
        String arg2 = "CRC_hg19.mhap.gz";
        String arg3 = "-cpgPath";
        String arg4 = "hg19_CpG.gz";
        String arg5 = "-region";
        String arg6 = "chrX:153715286-153715374";
        String arg7 = "-outputDir";
        String arg8 = "outputDir";
        String arg9 = "-tag";
        String arg10 = "CRC_hg19";
//        String[] args = {arg0, arg1, arg2, arg3, arg4, arg5, arg6, arg7, arg8};
        String arg11 = "-bcFile";
        String arg12 = "CRC_LN.txt";
        String[] args = {arg0, arg1, arg2, arg3, arg4, arg5, arg6, arg7, arg8, arg9, arg10};
        //String[] args = {arg0, arg1, arg2, arg3, arg4, arg5, arg6, arg7, arg8, arg9, arg10, arg11, arg12};
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
        String arg2 = "CRC_hg19.mhap.gz";
        String arg3 = "-cpgPath";
        String arg4 = "hg19_CpG.gz";
        String arg5 = "-region";
        String arg6 = "chr1:3229375-3230473";
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
        //String[] args = {arg0, arg1, arg2, arg3, arg4, arg5, arg6, arg9, arg10, arg11, arg12, arg13, arg14, arg15, arg16};
        String[] args = {arg0, arg1, arg2, arg3, arg4, arg5, arg6, arg7, arg8, arg9, arg10, arg11, arg12, arg13, arg14, arg15, arg16};

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
        String arg1 = "-mhapPath";
        String arg2 = "CRC_hg19.mhap.gz";
        String arg3 = "-region";
        String arg4 = "chrX:153715286-153715374";
//        String arg3 = "-bedFile";
//        String arg4 = "CRC_MHB_non_NC.bed";
        String arg5 = "-cpgPath";
        String arg6 = "hg19_CpG.gz";
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
        String arg19 = "-qc";
        String[] args = {arg0, arg1, arg2, arg3, arg4, arg5, arg6, arg9, arg10, arg11, arg12, arg13, arg14, arg15, arg16, arg17, arg18, arg19};
        //String[] args = {arg0, arg1, arg2, arg3, arg4, arg5, arg6, arg7, arg8, arg9, arg10, arg11, arg12, arg13, arg14, arg15, arg16, arg17, arg18, arg19};

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
        String arg5 = "-bedFile";
        String arg6 = "CRC_MHB_non_NC.bed";
//        String arg5 = "-region";
//        String arg6 = "chr1:3229375-3230473";
        String arg7 = "-bcFile";
        String arg8 = "CRC_LN.txt";
        String arg9 = "-outputDir";
        String arg10 = "outputDir";
        String arg11 = "-tag";
        String arg12 = "test";
        String[] args = {arg0, arg1, arg2, arg3, arg4, arg5, arg6, arg7, arg8, arg9, arg10, arg11, arg12};

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
        String arg2 = "CRC_hg19.mhap.gz";
        String arg3 = "-cpgPath";
        String arg4 = "hg19_CpG.gz";
        String arg5 = "-region1";
        String arg6 = "chr1:949817-949850";
//        String arg7 = "-region2";
//        String arg8 = "chr1:969429-969458";
        String arg7 = "-bedFile";
        String arg8 = "CRC_MHB_non_NC.bed";
        String arg9 = "-bcFile";
        String arg10 = "CRC_LN.txt";
        String arg11 = "-outputDir";
        String arg12 = "outputDir";
        String arg13 = "-tag";
        String arg14 = "flinkage";
        String arg15 = "-limit";
        String arg16 = "20000000";
        //String[] args = {arg0, arg1, arg2, arg3, arg4, arg5, arg6, arg7, arg8, arg9, arg10, arg11, arg12, arg13, arg14, arg15, arg16};
        String[] args = {arg0, arg1, arg2, arg3, arg4, arg7, arg8, arg9, arg10, arg11, arg12, arg13, arg14, arg15, arg16};

        String argsStr = "";
        for (int i = 0; i < args.length; i++) {
            argsStr += args[i] + " ";
        }
        System.out.println(argsStr);

        main.main(args);
    }
}