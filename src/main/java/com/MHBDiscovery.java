package com;

import com.args.MHBDiscoveryArgs;
import com.bean.MHBInfo;
import com.bean.MHapInfo;
import com.bean.R2Info;
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

public class MHBDiscovery {
    public static final Logger log = LoggerFactory.getLogger(MHBDiscovery.class);

    Util util = new Util();
    MHBDiscoveryArgs args = new MHBDiscoveryArgs();
    List<Region> regionList = new ArrayList<>();

    public void MHBDiscovery(MHBDiscoveryArgs mhbDiscoveryArgs) throws Exception {
        log.info("MHBDiscovery start!");
        args = mhbDiscoveryArgs;

        // check the command
        boolean checkResult = checkArgs();
        if (!checkResult) {
            log.error("Checkargs fail, please check the command.");
            return;
        }

        if (args.isQcFlag()) {
            boolean doQCResult = doQC();
            if (!doQCResult) {
                log.error("do QC fail, please check the command.");
                return;
            }
        } else {
            boolean getMHBResult = getMHB();
            if (!getMHBResult) {
                log.error("get MHB fail, please check the command.");
                return;
            }
        }

        log.info("MHBDiscovery end!");
    }

    private boolean checkArgs() {
        if (args.getmHapPath().equals("")) {
            log.error("mhapPath can not be null.");
            return false;
        }
        if (args.getCpgPath().equals("")) {
            log.error("cpgPath can not be null.");
            return false;
        }
        if (!args.getRegion().equals("") && !args.getBedFile().equals("")) {
            log.error("Can not input region and bedPath at the same time.");
            return false;
        }

        return true;
    }

    private boolean doQC() throws Exception{
        BufferedWriter bufferedWriter = util.createOutputFile(args.getOutputDir(), args.getTag() + ".qcFlag.bed");

        List<Region> regionList = new ArrayList<>();
        if (args.getRegion() != null && !args.getRegion().equals("")) {
            Region region = util.parseRegion(args.getRegion());
            regionList.add(region);
        } else if (args.getBedFile() != null && !args.getBedFile().equals("")) {
            regionList = util.parseBedFile(args.getBedFile());
        }

        // get bcFile
        List<String> barcodeList = util.parseBcFile(args.getBcFile());

        for (Region region : regionList) {
            region.setStart(region.getStart() - 1);
            // parse the mhap file
            Map<String, List<MHapInfo>> mHapListMap = util.parseMhapFileIndexByBarCodeAndStrand(args.getmHapPath(), barcodeList, args.getBcFile(), region);
            if (mHapListMap.size() < 1) {
                continue;
            }

            // parse the cpg file
            List<Integer> cpgPosList = util.parseCpgFileWithShift(args.getCpgPath(), region, 2000);
            if (cpgPosList.size() < 1) {
                continue;
            }

            // get cpg site list in region
            List<Integer> cpgPosListInRegion = util.getcpgPosListInRegion(cpgPosList, region);
            if (cpgPosListInRegion.size() < 1) {
                continue;
            }

            // get mhap index list map to cpg positions
            Map<Integer, Map<String, List<MHapInfo>>> mHapIndexListMapToCpg = util.getMhapListMapToCpg(mHapListMap, cpgPosListInRegion);

            boolean isMHBFlag = true;
            Integer firstIndex = 0; // start mhb position index in cpgPosListInRegion
            Integer secondIndex = 0; // end mhb position index in cpgPosListInRegion
            while (secondIndex < cpgPosListInRegion.size() - 1) {
                secondIndex++;
                for (int i = 1; i < args.getWindow(); i++) {
                    firstIndex = secondIndex - i; // cpg site index in cpgPosListInRegion for loop
                    if (firstIndex < 0) {
                        break;
                    }

                    Integer cpgPos1 = cpgPosListInRegion.get(firstIndex);
                    Integer cpgPos2 = cpgPosListInRegion.get(secondIndex);
                    Map<String, List<MHapInfo>> mHapListMap1 = mHapIndexListMapToCpg.get(cpgPos1);
                    Map<String, List<MHapInfo>> mHapListMap2 = mHapIndexListMapToCpg.get(cpgPos2);

                    R2Info r2Info = null;
                    if (mHapListMap1 != null && mHapListMap1.size() >= 1 && mHapListMap2 != null && mHapListMap2.size() >= 1) {
                        // get r2 and pvalue of index and endIndex
                        r2Info = util.getR2FromMap(mHapListMap1, cpgPosList, cpgPos1, cpgPos2, 0);
                    }
                    if (r2Info == null || r2Info.getR2() < args.getR2() || r2Info.getPvalue() > args.getPvalue()) {
                        isMHBFlag = false;
                        if (r2Info == null || r2Info.getR2().isNaN()) {
                            bufferedWriter.write(region.getChrom() + "\t" + region.getStart() + "\t" + region.getEnd() + "\t" + "no" + "\t"
                                    + cpgPosListInRegion.get(firstIndex) + "-" + cpgPosListInRegion.get(secondIndex) + " r2 is null or nan!" + "\n");
                        } else {
                            bufferedWriter.write(region.getChrom() + "\t" + region.getStart() + "\t" + region.getEnd() + "\t" + "no" + "\t"
                                    + cpgPosListInRegion.get(firstIndex) + "-" + cpgPosListInRegion.get(secondIndex) + " " + r2Info.getR2() + " " + r2Info.getPvalue() + "\n");
                        }
                        break;
                    }
                }

                if (!isMHBFlag) {
                    break;
                }
            }

            if(isMHBFlag) {
                bufferedWriter.write(region.getChrom() + "\t" + region.getStart() + "\t" + region.getEnd() + "\t" + "yes" + "\n");
            }
        }
        bufferedWriter.close();

        return true;
    }

    private boolean getMHB() throws Exception {
        // get regionList, from region or bedfile
        List<Region> regionList = new ArrayList<>();
        if (args.getRegion() != null && !args.getRegion().equals("")) {
            Region region = util.parseRegion(args.getRegion());
            regionList.addAll(util.splitRegionToSmallRegion(region, 100000, 1000));
        } else if (args.getBedFile() != null && !args.getBedFile().equals("")) {
            List<Region> regionListInBed = util.parseBedFile(args.getBedFile());
            // merge adjacent regions
            List<Region> regionListMerged = new ArrayList<>();
            Map<String, List<Integer>> cpgPosListMap = util.parseWholeCpgFile(args.getCpgPath());
            Iterator<String> iterator = cpgPosListMap.keySet().iterator();
            while (iterator.hasNext()) {
                String chrom = iterator.next();
                List<Region> regionListInChrom = regionListInBed.stream().filter(region -> region.getChrom().equals(chrom)).collect(Collectors.toList());
                if (regionListInChrom.size() > 1) {
                    for (Integer i = 0; i < regionListInChrom.size() - 1;) {
                        Region thisRegion = regionListInChrom.get(i);
                        Integer start = thisRegion.getStart();
                        Region nextRegion = regionListInChrom.get(i + 1);
                        Integer end = nextRegion.getEnd();
                        List<Integer> cpgPosList = cpgPosListMap.get(thisRegion.getChrom());
                        List<Integer> cpgPosListInThisRegion = util.getcpgPosListInRegion(cpgPosList, thisRegion);
                        List<Integer> cpgPosListInNextRegion = util.getcpgPosListInRegion(cpgPosList, nextRegion);
                        Integer thisRegionEndCpgIndex = util.indexOfList(cpgPosList, 0, cpgPosList.size() - 1,
                                cpgPosListInThisRegion.get(cpgPosListInThisRegion.size() - 1));
                        Integer nextRegionStartCpgIndex = util.indexOfList(cpgPosList, 0, cpgPosList.size() - 1,
                                cpgPosListInNextRegion.get(0));
                        if (nextRegionStartCpgIndex <= thisRegionEndCpgIndex + 1) {
                            int nextNum = 2;
                            while (i + nextNum < regionListInChrom.size() && nextRegionStartCpgIndex <= thisRegionEndCpgIndex + 1) {
                                thisRegion = regionListInChrom.get(i + nextNum - 1);
                                cpgPosListInThisRegion = util.getcpgPosListInRegion(cpgPosList, thisRegion);
                                thisRegionEndCpgIndex = util.indexOfList(cpgPosList, 0, cpgPosList.size() - 1,
                                        cpgPosListInThisRegion.get(cpgPosListInThisRegion.size() - 1));
                                nextRegion = regionListInChrom.get(i + nextNum);
                                cpgPosListInNextRegion = util.getcpgPosListInRegion(cpgPosList, nextRegion);
                                nextRegionStartCpgIndex = util.indexOfList(cpgPosList, 0, cpgPosList.size() - 1,
                                        cpgPosListInNextRegion.get(0));
                                nextNum++;
                            }
                            if (i + nextNum == regionListInChrom.size()) {
                                end = nextRegion.getEnd();
                            } else {
                                end = thisRegion.getEnd();
                            }

                            Region mergeRegion = new Region();
                            mergeRegion.setChrom(chrom);
                            mergeRegion.setStart(start);
                            mergeRegion.setEnd(end);
                            regionListMerged.add(mergeRegion);
                            i += (nextNum - 1);
                        } else {
                            i++;
                            regionListMerged.add(thisRegion);
                        }
                    }
                } else {
                    regionListMerged.add(regionListInChrom.get(0));
                }
            }
            for (Region region : regionListMerged) {
                regionList.addAll(util.splitRegionToSmallRegion(region, 100000, 1000));
            }
        } else {
//            List<Region> wholeRegionList = util.getWholeRegionFromMHapFile(args.getmHapPath());
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
                regionList.addAll(util.splitRegionToSmallRegion(region, 100000, 1000));
            }
        }

        // get bcFile
        List<String> barcodeList = util.parseBcFile(args.getBcFile());

        // create the output directory and file
        BufferedWriter bufferedWriter = util.createOutputFile(args.getOutputDir(), args.getTag() + ".txt");

        Map<String, String> mhbInfoListMap = new HashMap<>();

        // 打印处理进度
        long totalCnt = regionList.size();
        long completeCnt= 0l;
        for (Region region : regionList) {
            completeCnt++;
            if (completeCnt % (totalCnt / 100) == 0) {
                int percent = (int) Math.round(Double.valueOf(completeCnt) * 100 / totalCnt );
                log.info("Process complete " + percent + "%.");
            }
            // parse the mhap file
            //List<MHapInfo> mHapInfoList = util.parseMhapFile(args.getmHapPath(), region, "both", true);
            Map<String, List<MHapInfo>> mHapListMap = util.parseMhapFileIndexByBarCodeAndStrand(args.getmHapPath(), barcodeList, args.getBcFile(), region);
            if (mHapListMap.size() < 1) {
                //log.info("mHap is null in region:" + region.toHeadString());
                continue;
            }

            // parse the cpg file
            List<Integer> cpgPosList = util.parseCpgFileWithShift(args.getCpgPath(), region, 2000);
            if (cpgPosList.size() < 1) {
                //log.info("cpg pos is null in region:" + region.toHeadString());
                continue;
            }

            // get cpg site list in region
            List<Integer> cpgPosListInRegion = util.getcpgPosListInRegion(cpgPosList, region);
            if (cpgPosListInRegion.size() < 1) {
                //log.info("cpg pos is null in region:" + region.toHeadString());
                continue;
            }

            // get mhap index list map to cpg positions
            Map<Integer, Map<String, List<MHapInfo>>> mHapIndexListMapToCpg = util.getMhapListMapToCpg(mHapListMap, cpgPosListInRegion);

            Integer startIndex = 0; // start mhb position index in cpgPosListInRegion
            Integer endIndex = 0; // end mhb position index in cpgPosListInRegion
            Integer index = 0;
            while (endIndex < cpgPosListInRegion.size() - 1) {
                endIndex++;
                Boolean extendFlag = true;
                for (int i = 1; i < args.getWindow(); i++) {
                    index = endIndex - i; // cpg site index in cpgPosListInRegion for loop
                    if (index < 0) {
                        break;
                    }

                    Integer cpgPos1 = cpgPosListInRegion.get(index);
                    Integer cpgPos2 = cpgPosListInRegion.get(endIndex);
                    Map<String, List<MHapInfo>> mHapListMap1 = mHapIndexListMapToCpg.get(cpgPos1);
                    Map<String, List<MHapInfo>> mHapListMap2 = mHapIndexListMapToCpg.get(cpgPos2);

                    R2Info r2Info = null;
                    if (mHapListMap1 != null && mHapListMap1.size() >= 1 &&
                            mHapListMap2 != null && mHapListMap2.size() >= 1) {
                        // get r2 and pvalue of index and endIndex
                        r2Info = util.getR2FromMap(mHapListMap1, cpgPosList, cpgPos1, cpgPos2, 0);
                    }
//                    System.out.println(cpgPosListInRegion.get(index) + "\t" + cpgPosListInRegion.get(endIndex) + "\t"
//                            + r2Info.getR2() + "\t" + r2Info.getPvalue());
                    if (r2Info == null || r2Info.getR2() < args.getR2() || r2Info.getPvalue() > args.getPvalue()) {
                        extendFlag = false;
                        break;
                    }
                }

                if (!extendFlag) {
                    MHBInfo mhbInfo = new MHBInfo();
                    Integer mhbSize = endIndex - startIndex;
                    mhbInfo.setChrom(region.getChrom());
                    mhbInfo.setStart(cpgPosListInRegion.get(startIndex));
                    mhbInfo.setEnd(cpgPosListInRegion.get(endIndex - 1));
                    startIndex = index + 1 > startIndex ? index + 1 : startIndex;
                    if (mhbSize >= args.getWindow() && !mhbInfoListMap.containsKey(mhbInfo.toString())) {
                        mhbInfoListMap.put(mhbInfo.toString(), mhbInfo.toString());
                        //log.info("discovery a mhb in : " + mhbInfo.getChrom() + ":" + mhbInfo.getStart() + "-" + mhbInfo.getEnd());
                        bufferedWriter.write(mhbInfo.getChrom() + "\t" + mhbInfo.getStart() + "\t" + mhbInfo.getEnd() + "\n");
                    }
                }
            }

            if (endIndex - startIndex >= args.getWindow() - 1) {
                MHBInfo mhbInfo = new MHBInfo();
                mhbInfo.setChrom(region.getChrom());
                mhbInfo.setStart(cpgPosListInRegion.get(startIndex));
                mhbInfo.setEnd(cpgPosListInRegion.get(endIndex - 1));
                if (!mhbInfoListMap.containsKey(mhbInfo.toString())) {
                    mhbInfoListMap.put(mhbInfo.toString(), mhbInfo.toString());
                    bufferedWriter.write(mhbInfo.getChrom() + "\t" + mhbInfo.getStart() + "\t" + mhbInfo.getEnd() + "\n");
                }
            }
            // log.info("Get MHB from region: " + region.toHeadString() + " end!");
        }

        bufferedWriter.close();
        return true;
    }
}
