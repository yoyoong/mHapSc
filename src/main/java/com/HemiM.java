package com;

import com.args.HemiMArgs;
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
        } else {
            regionList = util.parseBedFile(args.getBedFile());
        }

        // parse the barcodefile
        List<String> barcodeList = util.parseBcFile(args.getBcFile());

        // create the output directory and file
        BufferedWriter bufferedWriter = util.createOutputFile(args.getOutputDir(), args.getTag() + ".hemi-methylation.txt");

        for (Region region : regionList) {
            // parse the mhap file
            Map<String, List<MHapInfo>> mHapListMap = util.parseMhapFile(args.getMhapPath(), barcodeList, args.getBcFile(), region);

            // parse the cpg file
            List<Integer> cpgPosList = util.parseCpgFileWithShift(args.getCpgPath(), region, SHIFT);

            // get cpg site list in region
            List<Integer> cpgPosListInRegion = util.getcpgPosListInRegion(cpgPosList, region);

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
                        if (plusList[i] != null && minusList[i] != null && plusList[i] != minusList[i]) {
                            String barCode = mHapInfoList.get(0).getBarcode();
                            bufferedWriter.write(region.getChrom() + "\t" + cpgPosListInRegion.get(i) + "\t" +  String.valueOf(plusList[i]) +
                                    "\t" + "+" + "\t" + barCode + "\n");
                            bufferedWriter.write(region.getChrom() + "\t" + cpgPosListInRegion.get(i) + "\t" +  String.valueOf(minusList[i]) +
                                    "\t" + "-" + "\t" + barCode + "\n");
                        }
                    }
                }
            }
        }

        bufferedWriter.close();

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
        if (!args.getRegion().equals("") && !args.getBedFile().equals("")) {
            log.error("Can not input region and bedPath at the same time.");
            return false;
        }
        if (args.getRegion().equals("") && args.getBedFile().equals("")) {
            log.error("Region and bedPath can not be null at the same time.");
            return false;
        }
        return true;
    }
}
