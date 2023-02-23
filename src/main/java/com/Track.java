package com;

import com.args.TrackArgs;
import com.bean.BedGraphInfo;
import com.bean.MHapInfo;
import com.bean.R2Info;
import com.bean.Region;
import com.common.Util;
import htsjdk.tribble.readers.TabixReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.util.*;

public class Track {
    public static final Logger log = LoggerFactory.getLogger(Track.class);

    Util util = new Util();
    TrackArgs args = new TrackArgs();

    BufferedWriter bufferedWriterMM = null;
    BufferedWriter bufferedWriterR2 = null;

    public void track(TrackArgs trackArgs) throws Exception {
        log.info("Track start!");
        args = trackArgs;

        // 校验命令正确性
        boolean checkResult = checkArgs();
        if (!checkResult) {
            log.error("Checkargs fail, please check the command.");
            return;
        }

        if (args.getMetrics().contains("MM")) {
            bufferedWriterMM = util.createOutputFile(args.getOutputDir(), args.getTag() + ".MM.bedGraph");
        }
        if (args.getMetrics().contains("R2")) {
            bufferedWriterR2 = util.createOutputFile(args.getOutputDir(), args.getTag() + ".R2.bedGraph");
        }

        // parse the barcodefile
        List<String> barcodeList = util.parseBcFile(args.getBcFile());

        // get regionList, from region or bedfile
        if (args.getRegion() != null && !args.getRegion().equals("")) {
            Region region = util.parseRegion(args.getRegion());
            // parse the mhap file
            Map<String, List<MHapInfo>> mHapListMap = util.parseMhapFile(args.getMhapPath(), barcodeList, args.getBcFile(), region);

            // parse the cpg file
            List<Integer> cpgPosList = util.parseCpgFileWithShift(args.getCpgPath(), region, 2000);

            Boolean calculateRegionResult = calculateRegion(cpgPosList, region, barcodeList);
            if (!calculateRegionResult) {
                log.error("Calculate " + region.toHeadString() + " fail!");
                return;
            }
        } else if (args.getBedFile() != null && !args.getBedFile().equals("")){
            List<Region> regionList = util.parseBedFile(args.getBedFile());
            for (Region region : regionList) {
                // parse the mhap file
                Map<String, List<MHapInfo>> mHapListMap = util.parseMhapFile(args.getMhapPath(), barcodeList, args.getBcFile(), region);

                // parse the cpg file
                List<Integer> cpgPosList = util.parseCpgFileWithShift(args.getCpgPath(), region, 2000);

                Boolean calculateRegionResult = calculateRegion(cpgPosList, region, barcodeList);
                if (!calculateRegionResult) {
                    log.error("Calculate " + region.toHeadString() + " fail!");
                    return;
                }
            }
        } else {
            Boolean calculateResult = calculateWholeGenome(barcodeList);
            if (!calculateResult) {
                log.error("calculateResult fail!");
                return;
            }
        }

        if (args.getMetrics().contains("MM")) {
            bufferedWriterMM.close();
        }
        if (args.getMetrics().contains("R2")) {
            bufferedWriterR2.close();
        }

        log.info("Track end!");
    }

    private boolean checkArgs() {
        if (args.getMhapPath().equals("")) {
            log.error("mhapPath can not be null.");
            return false;
        }
        if (args.getCpgPath().equals("")) {
            log.error("cpgPath can not be null.");
            return false;
        }
        if (args.getMetrics().equals("")) {
            log.error("metrics can not be null.");
            return false;
        }
        if (!args.getRegion().equals("") && !args.getBedFile().equals("")) {
            log.error("Can not input region and bedFile at the same time.");
            return false;
        }
        return true;
    }

    private boolean calculateWholeGenome(List<String> barcodeList) throws Exception {
        // parse whole cpg file
        Map<String, List<Integer>> cpgPosListMapRaw = util.parseWholeCpgFile(args.getCpgPath());

        // sort the cpgPosListMap
        List<Map.Entry<String, List<Integer>>> cpgPosListMapList = new ArrayList<Map.Entry<String, List<Integer>>>(cpgPosListMapRaw.entrySet());
        cpgPosListMapList.sort(new Comparator<Map.Entry<String, List<Integer>>>() {
            public int compare(Map.Entry<String, List<Integer>> o1, Map.Entry<String, List<Integer>> o2) {
                String chromNum1 = o1.getKey().substring(3, o1.getKey().length());
                String chromNum2 = o2.getKey().substring(3, o2.getKey().length());
                if (util.isNumeric(chromNum1) && util.isNumeric(chromNum2)) {
                    return Integer.valueOf(chromNum1) - Integer.valueOf(chromNum2);//o1减o2是升序，反之是降序
                } else {
                    return chromNum1.compareTo(chromNum2);
                }
            }
        });

        for (Map.Entry<String, List<Integer>> cpgPosListMap : cpgPosListMapList) {
            List<Integer> cpgPosList = cpgPosListMap.getValue();
            // get the whole region of this chrom
            Region region = new Region();
            region.setChrom(cpgPosListMap.getKey());
            region.setStart(cpgPosList.get(0));
            region.setEnd(cpgPosList.get(cpgPosList.size() - 1));

            Boolean calculateRegionResult = calculateRegion(cpgPosList, region, barcodeList);
            if (!calculateRegionResult) {
                log.error("Calculate " + region.toHeadString() + " fail!");
                return false;
            }
            log.info("Calculate " + cpgPosListMap.getKey() + " end!");
        }

        return true;
    }

    private boolean calculateRegion(List<Integer> cpgPosList, Region region, List<String> barcodeList) throws Exception {
        int[] nReadsList = new int[0]; // 该位点的总read个数
        int[] mReadList = new int[0]; // 该位点为甲基化的read个数
        if (args.getMetrics().contains("MM")) {
            nReadsList = new int[cpgPosList.size()]; // 该位点的总read个数
            mReadList = new int[cpgPosList.size()]; // 该位点为甲基化的read个数
        }
        // R2
        int[][] N00List = new int[0][0];
        int[][] N01List = new int[0][0];
        int[][] N10List = new int[0][0];
        int[][] N11List = new int[0][0];
        if (args.getMetrics().contains("R2")) {
            N00List = new int[4][cpgPosList.size()];
            N01List = new int[4][cpgPosList.size()];
            N10List = new int[4][cpgPosList.size()];
            N11List = new int[4][cpgPosList.size()];
        }

        TabixReader tabixReader = new TabixReader(args.getMhapPath());
        TabixReader.Iterator mhapIterator = tabixReader.query(region.getChrom(), region.getStart() - 1, region.getEnd());
        String mHapLine = "";
        Integer mHapLineCnt = 0;
        while((mHapLine = mhapIterator.next()) != null) {
            mHapLineCnt++;
            if (mHapLineCnt % 10000000 == 0) {
                log.info("Calculate complete " + region.getChrom() + " " + mHapLineCnt + " mhap lines.");
            }
            if (mHapLine.split("\t").length < 6) {
                continue;
            }
            if (args.getBcFile().equals("") && !barcodeList.contains(mHapLine.split("\t")[6])) {
                continue;
            }
            MHapInfo mHapInfo = new MHapInfo();
            mHapInfo.setChrom(mHapLine.split("\t")[0]);
            mHapInfo.setStart(Integer.valueOf(mHapLine.split("\t")[1]));
            mHapInfo.setEnd(Integer.valueOf(mHapLine.split("\t")[2]));
            mHapInfo.setCpg(mHapLine.split("\t")[3]);
            mHapInfo.setCnt(Integer.valueOf(mHapLine.split("\t")[4]));
            mHapInfo.setStrand(mHapLine.split("\t")[5]);
            mHapInfo.setBarcode(mHapLine.split("\t")[6]);

            Integer cpgPosIndex = util.indexOfList(cpgPosList, 0, cpgPosList.size() - 1, mHapInfo.getStart());
            String cpgStr = mHapInfo.getCpg();
            Integer cpgLen = cpgStr.length();
            Integer readCnt = mHapInfo.getCnt();

            if (args.getMetrics().contains("MM")) {
                for (int i = 0; i < cpgLen; i++) {
                    if (cpgStr.charAt(i) == '1') {
                        mReadList[cpgPosIndex + i] += readCnt;
                    }
                    nReadsList[cpgPosIndex + i] += readCnt;
                }
            }

            if (args.getMetrics().contains("R2")) {
                for (int i = 0; i < cpgLen; i++) {
                    for (int j = i - 2; j < i + 3; j++) {
                        if (j < 0 || j == i || j >= cpgLen) {
                            continue;
                        }
                        Integer index = j - i > 0 ? j - i + 1 : j - i + 2;
                        if (mHapInfo.getCpg().charAt(i) == '0' && mHapInfo.getCpg().charAt(j) == '0') {
                            N00List[index][cpgPosIndex + i] += readCnt;
                        } else if (mHapInfo.getCpg().charAt(i) == '0' && mHapInfo.getCpg().charAt(j) == '1') {
                            N01List[index][cpgPosIndex + i] += readCnt;
                        } else if (mHapInfo.getCpg().charAt(i) == '1' && mHapInfo.getCpg().charAt(j) == '0') {
                            N10List[index][cpgPosIndex + i] += readCnt;
                        } else if (mHapInfo.getCpg().charAt(i) == '1' && mHapInfo.getCpg().charAt(j) == '1') {
                            N11List[index][cpgPosIndex + i] += readCnt;
                        }
                    }
                }
            }
        }
        tabixReader.close();

        List<Integer> cpgPosListInRegion = util.getcpgPosListInRegion(cpgPosList, region);
        Integer start = util.indexOfList(cpgPosList, 0, cpgPosList.size(), cpgPosListInRegion.get(0));

        String[] metricsList = args.getMetrics().trim().split(" ");
        for (String metric : metricsList) {
            Integer cpgPosCnt = 0;
            for (Integer i = 0; i < cpgPosListInRegion.size(); i++) {
                cpgPosCnt++;
                if (cpgPosCnt % 100000 == 0) {
                    log.info("Calculate complete " + cpgPosCnt + " cpg positions.");
                }

                BedGraphInfo bedGraphInfo = new BedGraphInfo();
                bedGraphInfo.setChrom(region.getChrom());
                bedGraphInfo.setStart(cpgPosListInRegion.get(i) - 1);
                bedGraphInfo.setEnd(cpgPosListInRegion.get(i));
                if (metric.equals("MM")) {
                    Integer nReads = nReadsList[start + i];
                    Integer mRead = mReadList[start + i];
//                if (nReads < args.getCpgCov()) {
//                    continue;
//                }

                    Double MM = mRead.doubleValue() / nReads.doubleValue();
                    if (MM.isNaN() || MM.isInfinite()) {
                        continue;
                    }
                    bedGraphInfo.setMM(MM.floatValue());
                    bufferedWriterMM.write(bedGraphInfo.printMM());
                }

                if (metric.equals("R2")) {
                    Double r2Sum = 0.0;
                    Double r2Num = 0.0;
                    for (int j = i - 2; j < i + 3; j++) {
                        if (j < 0 || j == i || j >= cpgPosListInRegion.size()) {
                            continue;
                        }
                        Integer index = j - i > 0 ? j - i + 1 : j - i + 2;
                        Integer N00 = N00List[index][start + i];
                        Integer N01 = N01List[index][start + i];
                        Integer N10 = N10List[index][start + i];
                        Integer N11 = N11List[index][start + i];
//                    if ((N00 + N01 + N10 + N11) < args.getR2Cov()) {
//                        continue;
//                    }

                        // 计算r2
                        Double r2 = 0.0;
                        Double pvalue = 0.0;
                        Double N = N00 + N01 + N10 + N11 + 0.0;
                        if(N == 0) {
                            r2 = Double.NaN;
                            pvalue = Double.NaN;
                        }
                        Double PA = (N10 + N11) / N;
                        Double PB = (N01 + N11) / N;
                        Double D = N11 / N - PA * PB;
                        Double Num = D * D;
                        Double Den = PA * (1 - PA) * PB * (1 - PB);
                        if (Den == 0.0) {
                            r2 = Double.NaN;
                        } else {
                            r2 = Num / Den;
                            if (D < 0) {
                                r2 = -1 * r2;
                            }
                        }

                        if (!r2.isNaN()) {
                            r2Num++;
                            r2Sum += r2;
                        }
                    }

                    Double meanR2 = r2Sum / r2Num;
                    if (meanR2.isNaN() || meanR2.isInfinite()) {
                        continue;
                    }
                    bedGraphInfo.setR2(meanR2.floatValue());
                    bufferedWriterR2.write(bedGraphInfo.printR2());
                }
            }
        }

        return true;
    }

    private Double getMM(Integer[][] cpgHpMatInRegion, int pos) {
        Double mm = 0.0;
        Double cpgCnt = 0.0;
        Double unCpgCnt = 0.0;
        for (int j = 0; j < cpgHpMatInRegion.length; j++) {
            if (cpgHpMatInRegion[j][pos] != null) {
                if (cpgHpMatInRegion[j][pos] == 1) {
                    cpgCnt++;
                } else {
                    unCpgCnt++;
                }
            }
        }
        mm = cpgCnt / (cpgCnt + unCpgCnt);

        return mm;
    }
}
