import junit.framework.TestCase;
import com.Main;
import org.junit.Test;

public class MainTest extends TestCase {

    @Test
    public void test_convert() throws Exception {
        Main main = new Main();
        String arg0 = "convert";
        String arg1 = "-bedPath";
        String arg2 = "GSM2697488.bed";
        String arg3 = "-cpgPath";
        String arg4 = "hg19_CpG.gz";
        String arg5 = "-tag";
        String arg6 = "GSM2697488";
        String[] args = {arg0, arg1, arg2, arg3, arg4, arg5, arg6};

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
        String arg6 = "chr1:3229449-3229502";
        String arg7 = "-outputFile";
        String arg8 = "CRC_hg19.tanghulu.pdf";
//        String[] args = {arg0, arg1, arg2, arg3, arg4, arg5, arg6, arg7, arg8};
        String arg9 = "-bcFile";
        String arg10 = "CRC_LN.txt";
        String[] args = {arg0, arg1, arg2, arg3, arg4, arg5, arg6, arg7, arg8, arg9, arg10};

        main.main(args);
    }

    @Test
    public void test_R2() throws Exception {
        Main main = new Main();
        String arg0 = "R2";
        String arg1 = "-tag";
        String arg2 = "CRC_hg19";
        String arg3 = "-mhapPath";
        String arg4 = "CRC_hg19.mhap.gz";
        String arg5 = "-cpgPath";
        String arg6 = "hg19_CpG.gz";
        String arg7 = "-region";
        String arg8 = "chr1:3229375-3230473";
        String arg9 = "-bcFile";
        String arg10 = "CRC_LN.txt";
        String arg11 = "-outputDir";
        String arg12 = "outR2";
        String arg13 = "-longrange";
        String arg14 = "-mHapView";
        String arg15 = "-bedFile";
        String arg16 = "CRC_sc_bulk.bed";
        String[] args = {arg0, arg1, arg2, arg3, arg4, arg5, arg6, arg7, arg8, arg9, arg10, arg11, arg12, arg13, arg14, arg15, arg16};

        main.main(args);
    }

    @Test
    public void test_MHBDiscovery() throws Exception {
        Main main = new Main();
        String arg0 = "MHBDiscovery";
        String arg1 = "-htGZ";
        String arg2 = "CRC_hg19.mhap.gz";
//        String arg3 = "-bFile";
//        String arg4 = "";
        String arg3 = "-region";
        String arg4 = "chr1:3229375-3230473";
        String arg5 = "-cgGZ";
        String arg6 = "hg19_CpG.gz";
        String arg7 = "-bcFile";
        String arg8 = "CRC_LN.txt";
        String arg9 = "-window";
        String arg10 = "5";
        String arg11 = "-r_square";
        String arg12 = "0.5";
        String arg13 = "-p_value";
        String arg14 = "0.05";
        String arg15 = "-oFile";
        String arg16 = "CRC_hg19_MHB.bed";
        String arg17 = "-qc";
        String[] args = {arg0, arg1, arg2, arg3, arg4, arg5, arg6, arg7, arg8, arg9, arg10, arg11, arg12, arg13, arg14, arg15, arg16, arg17};

        main.main(args);
    }

    @Test
    public void test_hemiM() throws Exception {
        Main main = new Main();
        String arg0 = "hemiM";
        String arg1 = "-tag";
        String arg2 = "Hemi-methylation";
        String arg3 = "-mhapPath";
        String arg4 = "CRC_hg19.mhap.gz";
        String arg5 = "-cpgPath";
        String arg6 = "hg19_CpG.gz";
        String arg7 = "-bFile";
        String arg8 = "CRC_MHB_non_NC.bed";
//        String arg7 = "-region";
//        String arg8 = "chr1:3229375-3230473";
        String arg9 = "-bcFile";
        String arg10 = "CRC_LN.txt";
        String[] args = {arg0, arg1, arg2, arg3, arg4, arg5, arg6, arg7, arg8, arg9, arg10};

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
        String arg6 = "chr1:3229375-3230473";
//        String arg5 = "-bFile";
//        String arg6 = "CRC_MHB_non_NC.bed";
        String arg7 = "-outputDir";
        String arg8 = "outTrack";
        String arg9 = "-tag";
        String arg10 = "test";
        String arg11 = "-bcFile";
        String arg12 = "CRC_LN.txt";
        String arg13 = "-metric";
        String arg14 = "mm r2";
        String[] args = {arg0, arg1, arg2, arg3, arg4, arg5, arg6, arg7, arg8, arg9, arg10, arg8, arg9, arg10, arg11, arg12, arg13, arg14};

        main.main(args);
    }
}