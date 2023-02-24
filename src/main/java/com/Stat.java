package com;

import com.File.*;
import com.args.StatArgs;
import com.bean.MHapInfo;
import com.bean.R2Info;
import com.bean.Region;
import com.common.Util;
import com.sun.org.apache.xpath.internal.operations.Bool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class Stat {
    public static final Logger log = LoggerFactory.getLogger(Stat.class);

    StatArgs args = new StatArgs();
    Util util = new Util();
    MHapFile mHapFile;
    CpgFile cpgFile;

    public void stat(StatArgs statArgs) throws Exception {
        log.info("Stat start!");
        args = statArgs;
        mHapFile = new MHapFile(args.getMhapPath());
        cpgFile = new CpgFile(args.getCpgPath());

        // check the command
        boolean checkResult = checkArgs();
        if (!checkResult) {
            log.error("Checkargs fail, please check the command.");
            return;
        }

        // get regionList, from region or bedfile
        List<Region> regionList = new ArrayList<>();
        if (args.getRegion() != null && !args.getRegion().equals("")) {
            Region region = util.parseRegion(args.getRegion());
            regionList.add(region);
        } else {
            BedFile bedFile = new BedFile(args.getBedPath());
            regionList = bedFile.parseWholeFile();
        }

        // parse the barcodefile
        List<String> barcodeList = new ArrayList<>();
        if (args.getBcFile() != null && !args.getBcFile().equals("")) {
            BarcodeFile barcodeFile = new BarcodeFile(args.getBcFile());
            barcodeList = barcodeFile.parseBcFile();
        }

        String[] metrics = args.getMetrics().split(" ");
        List<String> metricsList = new ArrayList<>();
        for (String metric : metrics) {
            metricsList.add(metric);
        }
        StatOutputFile statOutputFile = new StatOutputFile("", args.getOutputFile());
        statOutputFile.setMetricsList(metricsList);
        String headString = "chr" + "\t" + "start" + "\t" + "end" + "\t" + "nStrands" + "\t" + "mBase" + "\t" + "cBase" + "\t"
                + "tBase" + "\t" + "K4plus" + "\t" + "nDS" + "\t" + "nMS";
        statOutputFile.writeHead(headString);

        // get the metric list
        for (Region region : regionList) {
            // parse the mhap file
            Map<String, List<MHapInfo>> mHapInfoListMap = mHapFile.parseByRegionIndexByBarCodeAndStrand(region, args.getStrand(), barcodeList);
            if (mHapInfoListMap.size() < 1) {
                log.info("MHap info list in " + region.toHeadString() + " is null!");
                continue;
            }

            // parse the cpg file
            List<Integer> cpgPosList = cpgFile.parseByRegionWithShift(region, 2000);
            if (cpgPosList.size() < 1) {
                log.info("Cpg pos list in " + region.toHeadString() + " is null!");
                continue;
            }
            List<Integer> cpgPosListInRegion = cpgFile.parseByRegion(region);
            if (cpgPosListInRegion.size() < 1) {
                log.info("Cpg pos list in " + region.toHeadString() + " is null!");
                continue;
            }

            boolean getStatResult = getStat(mHapInfoListMap, cpgPosList, cpgPosListInRegion, region, metricsList, statOutputFile);
            if (!getStatResult) {
                log.error("getStat fail, please check the command.");
                return;
            }
            log.info("Region: " + region.toHeadString() + " calculate end!");
        }

        mHapFile.close();
        cpgFile.close();
        statOutputFile.close();
        log.info("Stat end!");
    }

    private boolean checkArgs() {
        if (!args.getRegion().equals("") && !args.getBedPath().equals("")) {
            log.error("Can not input region and bedPath at the same time.");
            return false;
        }
        if (!args.getStrand().equals("plus") && !args.getStrand().equals("minus") && !args.getStrand().equals("both")) {
            log.error("The strand must be one of plus, minus or both");
            return false;
        }
        return true;
    }

    private boolean getStat(Map<String, List<MHapInfo>> mHapInfoListMap, List<Integer> cpgPosList, List<Integer> cpgPosListInRegion, Region region,
                            List<String> metricsList, StatOutputFile statOutputFile) throws Exception {
        Integer nStrands = mHapInfoListMap.size(); // 总链数（tanghulu上一行为一个链，可能会含有多条read）
        Integer mBase = 0; // 甲基化位点个数
        Integer cBase = 0; // 存在甲基化的strand中的未甲基化位点个数
        Integer tBase = 0; // 总位点个数
        Integer K4plus = 0; // 长度大于等于K个位点的strand个数
        Integer nDS = 0; // 长度大于等于K个位点且同时含有甲基化和未甲基化位点的strand个数
        Integer nMS = 0; // 长度大于等于K个位点且含有甲基化位点的strand个数
        Iterator<String> iterator = mHapInfoListMap.keySet().iterator();
        while (iterator.hasNext()) {
            String key = iterator.next();
            List<MHapInfo> mHapInfoList = mHapInfoListMap.get(key);
            Boolean hasMethFlag = false;
            Boolean hasBothFlag = false;
            Integer sumNotNullSite = 0;
            for (MHapInfo mHapInfo : mHapInfoList) {
                String cpg = util.cutReads(mHapInfo, cpgPosList, cpgPosListInRegion);;
                tBase += cpg.length();
                for (int j = 0; j < cpg.length(); j++) {
                    if (cpg.charAt(j) == '1') {
                        mBase++;
                    }
                }
                if (cpg.contains("1")) {
                    for (int j = 0; j < cpg.length(); j++) {
                        if (cpg.charAt(j) == '0') {
                            cBase++;
                        }
                    }
                }
                sumNotNullSite += cpg.length();
                if (cpg.contains("1")) {
                    hasMethFlag = true;
                }
                if (hasMethFlag) {
                    if (cpg.contains("0")) {
                        hasBothFlag = true;
                    }
                }
            }
            if (sumNotNullSite >= args.getK()) {
                K4plus++;
                if (hasMethFlag) {
                    nMS++;
                }
                if (hasBothFlag) {
                    nDS++;
                }
            }
        }

        String lineString = region.getChrom() + "\t" + region.getStart() + "\t" + region.getEnd() + "\t" + nStrands +
                "\t" + mBase + "\t" + cBase + "\t" + tBase + "\t" + K4plus + "\t" + nDS + "\t" + nMS;
        for (String metrics : metricsList) {
            if (metrics.equals("MM")) {
                Double mm = mBase.doubleValue() / tBase.doubleValue();
                lineString += "\t" + String.format("%.8f", mm);
            } else if (metrics.equals("CHALM")) {
                Double chalm = nMS.doubleValue() / K4plus.doubleValue();
                lineString += "\t" + String.format("%.8f", chalm);
            } else if (metrics.equals("PDR")) {
                Double pdr = nDS.doubleValue() / K4plus.doubleValue();
                lineString += "\t" + String.format("%.8f", pdr);
            } else if (metrics.equals("MHL")) {
                Double mhl = calculateMHL(mHapInfoListMap, args.getMinK(), args.getMaxK());
                lineString += "\t" + String.format("%.8f", mhl);
            } else if (metrics.equals("MBS")) {
                Double mbs = calculateMBS(mHapInfoListMap, args.getK());
                lineString += "\t" + String.format("%.8f", mbs);
            } else if (metrics.equals("MCR")) {
                Double mcr = cBase.doubleValue() / tBase.doubleValue();
                lineString += "\t" + String.format("%.8f", mcr);
            } else if (metrics.equals("Entropy")) {
                Double entropy = calculateEntropy(mHapInfoListMap, args.getK());
                lineString += "\t" + String.format("%.8f", entropy);
            } else if (metrics.equals("R2")) {
                Double r2 = calculateR2(mHapInfoListMap, cpgPosList, cpgPosListInRegion);
                lineString += "\t" + String.format("%.8f", r2);
            }
        }
        lineString += "\n";
        statOutputFile.writeLine(lineString);

        return true;
    }

    public Double calculateMHL(Map<String, List<MHapInfo>> mHapInfoListMap, Integer minK, Integer maxK) {
        Double MHL = 0.0;
        Integer maxCpgLength = 0;
        Iterator<String> iterator = mHapInfoListMap.keySet().iterator();
        while (iterator.hasNext()) {
            List<MHapInfo> mHapInfoList = mHapInfoListMap.get(iterator.next());
            for (MHapInfo mHapInfo : mHapInfoList) {
                if (minK > mHapInfo.getCpg().length()) {
                    log.error("calculate MHL Error: minK is too large.");
                    return 0.0;
                }
                if (maxCpgLength < mHapInfo.getCpg().length()) {
                    maxCpgLength = mHapInfo.getCpg().length();
                }
            }
        }
        if (maxK > maxCpgLength) {
            maxK = maxCpgLength;
        }

        Double temp = 0.0;
        Integer w = 0;
        String fullMethStr = "";
        for (int i = 0; i < minK; i++) {
            fullMethStr += "1";
        }
        for (Integer kmer = minK; kmer < maxK + 1; kmer++) {
            Map<String, Integer> kmerMap = new HashMap<>();
            Integer kmerNum = 0;
            Integer mKmerNum = 0;
            w += kmer;
            iterator = mHapInfoListMap.keySet().iterator();
            while (iterator.hasNext()) {
                List<MHapInfo> mHapInfoList = mHapInfoListMap.get(iterator.next());
                for (MHapInfo mHapInfo : mHapInfoList) {
                    if (mHapInfo.getCpg().length() >= kmer) {
                        for (int k = 0; k < mHapInfo.getCpg().length() - kmer + 1; k++) {
                            String kmerStr = mHapInfo.getCpg().substring(k, k + kmer);
                            if (kmerMap.containsKey(kmerStr)) {
                                kmerMap.put(kmerStr, kmerMap.get(kmerStr) + mHapInfo.getCnt());
                            } else {
                                kmerMap.put(kmerStr, mHapInfo.getCnt());
                            }
                        }
                    }
                }
            }

            iterator = kmerMap.keySet().iterator();
            while (iterator.hasNext()) {
                String key = iterator.next();
                kmerNum += kmerMap.get(key);
                if (key.substring(0, kmer).equals(fullMethStr)) {
                    mKmerNum += kmerMap.get(key);
                }
            }

            fullMethStr += "1";
            temp += kmer.doubleValue() * mKmerNum.doubleValue() / kmerNum.doubleValue();
        }
        MHL = temp / w;

        return MHL;
    }

    public Double calculateMBS(Map<String, List<MHapInfo>> mHapInfoListMap, Integer K) {
        Double MBS = 0.0;
        Integer kmerNum = 0;
        Double temp1 = 0.0;
        Iterator<String> iterator = mHapInfoListMap.keySet().iterator();
        while (iterator.hasNext()) {
            List<MHapInfo> mHapInfoList = mHapInfoListMap.get(iterator.next());
            for (MHapInfo mHapInfo : mHapInfoList) {
                if (mHapInfo.getCpg().length() >= K) {
                    String[] cpgStrList = mHapInfo.getCpg().split("0");
                    Double temp2 = 0.0;
                    for (String cpg : cpgStrList) {
                        temp2 += Math.pow(cpg.length(), 2);
                    }
                    temp1 += temp2 / Math.pow(mHapInfo.getCpg().length(), 2) * mHapInfo.getCnt();
                    kmerNum += mHapInfo.getCnt();
                }
            }
        }
        MBS = temp1 / kmerNum.doubleValue();

        return MBS;
    }

    public Double calculateEntropy(Map<String, List<MHapInfo>> mHapInfoListMap, Integer K) {
        Double Entropy = 0.0;
        Map<String, Integer> kmerMap = new HashMap<>();
        Integer kmerAll = 0;
        Iterator<String> iterator = mHapInfoListMap.keySet().iterator();
        while (iterator.hasNext()) {
            List<MHapInfo> mHapInfoList = mHapInfoListMap.get(iterator.next());
            for (MHapInfo mHapInfo : mHapInfoList) {
                if (mHapInfo.getCpg().length() >= K) {
                    for (int j = 0; j < mHapInfo.getCpg().length() - K + 1; j++) {
                        kmerAll += mHapInfo.getCnt();
                        String kmerStr = mHapInfo.getCpg().substring(j, j + K);
                        if (kmerMap.containsKey(kmerStr)) {
                            kmerMap.put(kmerStr, kmerMap.get(kmerStr) + mHapInfo.getCnt());
                        } else {
                            kmerMap.put(kmerStr, mHapInfo.getCnt());
                        }
                    }
                }
            }
        }

        iterator = kmerMap.keySet().iterator();
        Double temp = 0.0;
        while (iterator.hasNext()) {
            Integer cnt = kmerMap.get(iterator.next());
            temp += cnt.doubleValue() / kmerAll.doubleValue() * Math.log(cnt.doubleValue() / kmerAll.doubleValue()) / Math.log(2);
        }
        Entropy = - 1 / K.doubleValue() * temp;

        return Entropy;
    }

    public Double calculateR2(Map<String, List<MHapInfo>> mHapInfoListMap, List<Integer> cpgPosList,
                              List<Integer> cpgPosListInRegion) {
        Double r2Sum = 0.0;
        Integer r2Num = 0;
        Integer[][] mHapMatrix = mHapFile.getMHapMatrix(mHapInfoListMap, cpgPosList, cpgPosListInRegion);
        for (int i = 0; i < cpgPosListInRegion.size(); i++) {
            for (int j = i + 1; j < cpgPosListInRegion.size(); j++) {
                R2Info r2Info = util.getR2InfoFromMatrix(mHapMatrix, i, j, args.getR2Cov());
                if (r2Info != null && r2Info.getR2() != null && !r2Info.getR2().isNaN()) {
                    r2Sum += r2Info.getR2();
                }
                r2Num++;
            }
        }
        return r2Sum/r2Num;
    }
}
