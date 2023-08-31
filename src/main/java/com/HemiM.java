package com;

import com.args.HemiMArgs;
import com.bean.HemiMInfo;
import com.bean.MHapInfo;
import com.bean.Region;
import com.common.Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.util.*;
import java.util.stream.Collectors;

public class HemiM {
    public static final Logger log = LoggerFactory.getLogger(HemiM.class);

    HemiMArgs args = new HemiMArgs();
    Util util = new Util();
    List<Region> regionList = new ArrayList<>();
    private final Integer SHIFT = 500;

    public void hemiM(HemiMArgs hemiMArgs) throws Exception {
        log.info("HemiM start!");
        args = hemiMArgs;

        // 校验命令正确性
        boolean checkResult = checkArgs();
        if (!checkResult) {
            log.error("Checkargs fail, please check the command.");
            return;
        }

        // get regionList, from region or bedfile
        if (args.getRegion() != null && !args.getRegion().equals("")) {
            Region region = util.parseRegion(args.getRegion());
            regionList.add(region);
        } else if (args.getBedFile() != null && !args.getBedFile().equals("")) {
            regionList = util.parseBedFile(args.getBedFile());
        } else {
            List<Region> wholeRegionList = new ArrayList<>();
            Map<String, List<Integer>> cpgPostListMap = util.parseWholeCpgFile(args.getCpgPath());
            Iterator<String> iterator = cpgPostListMap.keySet().iterator();
            while (iterator.hasNext()) {
                String chrom = iterator.next();
                List<Integer> cpgPostList = cpgPostListMap.get(chrom);
                Region region = new Region();
                region.setChrom(chrom);
                region.setStart(cpgPostList.get(0));
                region.setEnd(cpgPostList.get(cpgPostList.size() - 1));
                wholeRegionList.add(region);
            }

            for (Region region : wholeRegionList) {
                regionList.addAll(util.splitRegionToSmallRegion(region, 1000000, 0));
            }
            Collections.sort(regionList, new Comparator<Region>() {
                public int compare(Region o1, Region o2) {
                    return o1.getChrom().compareTo(o2.getChrom()) * 10 + o1.getStart().compareTo(o2.getStart());
                }
            });
        }

        // parse the barcodefile
        List<String> barcodeList = util.parseBcFile(args.getBcFile());

        // create the output directory and file
        BufferedWriter hemiMWriter = util.createOutputFile(args.getOutputDir(), args.getTag() + ".hemiM.txt");
        BufferedWriter statWriter = null;
        if (args.getStat()) {
            statWriter = util.createOutputFile(args.getOutputDir(), args.getTag() + ".hemiMStat.txt");
            String statHead = "chrom\tpos";
            for (String barcode : barcodeList) {
                statHead += "\t" + barcode;
            }
            statWriter.write(statHead + "\ttotal\n");
        }

        for (Region region : regionList) {
            // parse the mhap file
            Map<String, List<MHapInfo>> mHapListMap = util.parseMhapFile(args.getMhapPath(), barcodeList, args.getBcFile(), region);

            // parse the cpg file
            List<Integer> cpgPosList = util.parseCpgFileWithShift(args.getCpgPath(), region, SHIFT);
            if (cpgPosList.size() < 1) {
                log.info("Region " + region.toHeadString() + " has no cpg position.Skip...");
                continue;
            }

            // get cpg site list in region
            List<Integer> cpgPosListInRegion = util.getcpgPosListInRegion(cpgPosList, region);
            if (cpgPosListInRegion.size() < 1) {
                log.info("Region " + region.toHeadString() + " has no cpg position.Skip...");
                continue;
            }

            // hemiM list
            List<String> hemiMIndexList = new ArrayList<>();
            List<HemiMInfo> hemiMInfoList = new ArrayList<>(); //
            List<HemiMInfo> unHemiMInfoList = new ArrayList<>(); // has 2 strands but no hemiM

            Iterator<String> iterator = mHapListMap.keySet().iterator();
            while (iterator.hasNext()) {
                // get mHapInfoList and sorted
                List<MHapInfo> mHapInfoList = mHapListMap.get(iterator.next());
                Map<Integer, List<MHapInfo>> r2ListMap = mHapInfoList.stream().collect(Collectors.groupingBy(MHapInfo::getStart));
                List<Map.Entry<Integer, List<MHapInfo>>> r2ListMapSorted = new ArrayList<Map.Entry<Integer, List<MHapInfo>>>(r2ListMap.entrySet());
                Collections.sort(r2ListMapSorted, new Comparator<Map.Entry<Integer, List<MHapInfo>>>() { //升序排序
                    public int compare(Map.Entry<Integer, List<MHapInfo>> o1, Map.Entry<Integer, List<MHapInfo>> o2) {
                        return o1.getValue().get(0).getChrom().compareTo(o2.getValue().get(0).getChrom())
                                + o1.getValue().get(0).getStart().compareTo(o2.getValue().get(0).getStart());
                    }
                });

                // whether has 2 strands
                Boolean plusFlag = false;
                Boolean minusFlag = false;
                for (int i = 0; i < mHapInfoList.size(); i++) {
                    plusFlag = mHapInfoList.get(i).getStrand().equals("+") ? true : plusFlag;
                    minusFlag = mHapInfoList.get(i).getStrand().equals("-") ? true : minusFlag;
                }

                if (plusFlag && minusFlag) {
                    Integer[] plusList = new Integer[cpgPosListInRegion.size()];
                    Integer[] minusList = new Integer[cpgPosListInRegion.size()];

                    for (int i = 0; i < mHapInfoList.size(); i++) {
                        MHapInfo mHapInfo = mHapInfoList.get(i);
                        String cpg = mHapInfo.getCpg();

                        Integer pos = util.indexOfList(cpgPosList, 0, cpgPosList.size() - 1, cpgPosListInRegion.get(0)) -
                                util.indexOfList(cpgPosList, 0, cpgPosList.size() - 1, mHapInfo.getStart());
                        if (mHapInfo.getStrand().equals("+")) {
                            for (int j = 0; j < cpg.length(); j++) {
                                if (0 <= (j - pos) && (j - pos) < cpgPosListInRegion.size()) {
                                    plusList[j - pos] = Integer.valueOf(String.valueOf(cpg.charAt(j)));
                                }
                            }
                        } else {
                            for (int j = 0; j < cpg.length(); j++) {
                                if (0 <= (j - pos) && (j - pos) < cpgPosListInRegion.size()) {
                                    minusList[j - pos] = Integer.valueOf(String.valueOf(cpg.charAt(j)));
                                }
                            }
                        }
                    }

                    for (int i = 0; i < cpgPosListInRegion.size(); i++) {
                        HemiMInfo hemiMInfo = new HemiMInfo();
                        hemiMInfo.setChrom(region.getChrom());
                        hemiMInfo.setPos(cpgPosListInRegion.get(i));
                        hemiMInfo.setCpg(String.valueOf(plusList[i]));
                        hemiMInfo.setBarCode(mHapInfoList.get(0).getBarcode());
                        if (plusList[i] != null && minusList[i] != null && plusList[i] != minusList[i]) {
                            hemiMWriter.write(hemiMInfo.printPlusStrand());
                            hemiMInfo.setCpg(String.valueOf(minusList[i]));
                            hemiMWriter.write(hemiMInfo.printMinusStrand());
                            if (!hemiMIndexList.contains(hemiMInfo.index())) {
                                hemiMIndexList.add(hemiMInfo.index());
                            }
                            hemiMInfoList.add(hemiMInfo);
                        } else if (plusList[i] != null && minusList[i] != null && plusList[i] == minusList[i]) {
                            unHemiMInfoList.add(hemiMInfo);
                        }
                    }
                }
            }

            Collections.sort(hemiMIndexList, new Comparator<String>() {
                public int compare(String o1, String o2) {
                    String chrom1 = o1.split("\t")[0];
                    String chrom2 = o2.split("\t")[0];
                    Integer start1 = Integer.valueOf(o1.split("\t")[1]);
                    Integer start2 = Integer.valueOf(o2.split("\t")[1]);
                    return chrom1.compareTo(chrom2) * 10 + start1.compareTo(start2);
                }
            });

            if (args.getStat()) {
                // hemiM-barcode count array
                int[][] statArray = new int[hemiMIndexList.size()][barcodeList.size() + 1];
                for (HemiMInfo hemiMInfo : hemiMInfoList) {
                    Integer row = hemiMIndexList.indexOf(hemiMInfo.index());
                    Integer col = barcodeList.indexOf(hemiMInfo.getBarCode());
                    statArray[row][col] = 2;
                    statArray[row][barcodeList.size()] += 1;
                }
                for (HemiMInfo unHemiMInfo : unHemiMInfoList) {
                    Integer row = hemiMIndexList.indexOf(unHemiMInfo.index());
                    if (row >= 0) {
                        Integer col = barcodeList.indexOf(unHemiMInfo.getBarCode());
                        statArray[row][col] = 1;
                    } else {
                        continue;
                    }
                }

                for (int i = 0; i < hemiMIndexList.size(); i++) {
                    String statLine = hemiMIndexList.get(i);
                    for (int j = 0; j < barcodeList.size() + 1; j++) {
                        statLine += "\t" + statArray[i][j];
                    }
                    statWriter.write(statLine + "\n");
                }
            }
        }

        hemiMWriter.close();
        if (args.getStat()) {
            statWriter.close();
        }
        log.info("HemiM end!");
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
//        if (!args.getRegion().equals("") && !args.getBedFile().equals("")) {
//            log.error("Can not input region and bedPath at the same time.");
//            return false;
//        }
//        if (args.getRegion().equals("") && args.getBedFile().equals("")) {
//            log.error("Region and bedPath can not be null at the same time.");
//            return false;
//        }
        return true;
    }
}
