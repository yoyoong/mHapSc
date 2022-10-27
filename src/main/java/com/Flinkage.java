package com;

import com.args.FlinkageArgs;
import com.bean.MHapInfo;
import com.bean.R2Info;
import com.bean.Region;
import com.common.Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.*;

public class Flinkage {
    public static final Logger log = LoggerFactory.getLogger(Flinkage.class);

    FlinkageArgs args = new FlinkageArgs();
    Util util = new Util();
    List<Region> regionList = new ArrayList<>();
    

    public void flinkage(FlinkageArgs flinkageArgs) throws Exception {
        log.info("Flinkage start!");
        args = flinkageArgs;

        // 校验命令正确性
        boolean checkResult = checkArgs();
        if (!checkResult) {
            log.error("Checkargs fail, please check the command.");
            return;
        }

        // get regionList, from region or bedfile
        if (args.getRegion1() != null && !args.getRegion1().equals("") && args.getRegion2() != null && !args.getRegion2().equals("")) {
            Region region1 = util.parseRegion(args.getRegion1());    
            regionList.add(region1);
            Region region2 = util.parseRegion(args.getRegion2());    
            regionList.add(region2);
        } else {
            regionList = util.parseBedFile(args.getBedFile());
        }

        // parse the barcodefile
        List<String> barcodeList = util.parseBcFile(args.getBcFile());

        // create the output directory
        File outputDir = new File(args.getOutputDir());
        if (!outputDir.exists()){
            if (!outputDir.mkdirs()){
                log.error("create" + outputDir.getAbsolutePath() + "fail");
                return;
            }
        }

        // create the output file
        BufferedWriter bufferedWriter = util.createOutputFile(args.getOutputDir(), args.getTag() + ".longrange.txt");

        for (int i = 0; i < regionList.size(); i++) {
            for (int j = i + 1; j < regionList.size(); j++) {
                Region region1 = regionList.get(i);
                Region region2 = regionList.get(j);

                TreeMap<String, List<MHapInfo>> mHapListMap1 = util.parseMhapFileIndexByBarCodeAndStrand(args.getMhapPath(), barcodeList,
                        args.getBcFile(), region1);
                TreeMap<String, List<MHapInfo>> mHapListMap2 = util.parseMhapFileIndexByBarCodeAndStrand(args.getMhapPath(), barcodeList,
                        args.getBcFile(), region2);

                // filter the same barcode&strand of mHapListMap1 and mHapListMap2, and merge the list of same barcode
                TreeMap<String, List<MHapInfo>> mHapListMapMerged = new TreeMap<>();
                Iterator<String> iterator = mHapListMap1.keySet().iterator();
                while (iterator.hasNext()) {
                    String key = iterator.next();
                    if (mHapListMap2.containsKey(key)) { // exist, merge the list
                        List<MHapInfo> mHapInfoList1 = mHapListMap1.get(key);
                        List<MHapInfo> mHapInfoList2 = mHapListMap2.get(key);
                        mHapInfoList1.addAll(mHapInfoList2);
                        mHapListMapMerged.put(key, mHapInfoList1);
                    }
                }

                // parse the cpg file
;               List<Integer> cpgPosList1 = util.parseCpgFile(args.getCpgPath(), region1);
                List<Integer> cpgPosList2 = util.parseCpgFile(args.getCpgPath(), region2);

                boolean getFlinkageResult = getFlinkage(mHapListMapMerged, cpgPosList1, cpgPosList2, region1, region2, bufferedWriter);
                if (!getFlinkageResult) {
                    log.error("getFlinkage fail, please check the command.");
                    return;
                }
            }
        }
        bufferedWriter.close();

        log.info("Flinkage end!");
    }

    private boolean checkArgs() {

        return true;
    }

    private boolean getFlinkage(TreeMap<String, List<MHapInfo>> mHapListMapMerged,
                                List<Integer> cpgPosList1, List<Integer> cpgPosList2, Region region1, Region region2,
                                BufferedWriter bufferedWriter) throws Exception {
        // 提取查询区域内的甲基化位点列表
        List<Integer> cpgPosListInRegion1 = util.getcpgPosListInRegion(cpgPosList1, region1);
        List<Integer> cpgPosListInRegion2 = util.getcpgPosListInRegion(cpgPosList2, region2);
        List<Integer> cpgPosListInRegion = new ArrayList<>();
        cpgPosListInRegion.addAll(cpgPosListInRegion1);
        cpgPosListInRegion.addAll(cpgPosListInRegion2);

        // 甲基化状态矩阵 0-未甲基化 1-甲基化
        Integer[][] cpgMatrix = util.getCpgMatrix(mHapListMapMerged, cpgPosListInRegion);

        // calculate the r2Info of erery position
        Integer totalR2Num = cpgPosListInRegion1.size() * cpgPosListInRegion2.size();
        Integer realR2Num = 0;
        for (int i = 0; i < cpgPosListInRegion1.size(); i++) {
            for (int j = cpgPosListInRegion1.size(); j < cpgPosListInRegion.size(); j++) {
                R2Info r2Info = util.getR2Info(cpgMatrix, i, j, cpgMatrix.length);
                if (r2Info != null && r2Info.getR2() != Double.NaN && r2Info.getR2() > 0.5 && r2Info.getPvalue() < 0.05) {
                    realR2Num++;
                }
//                bufferedWriter.write(region1.getChrom() + "\t" + cpgPosListInRegion.get(i) + "\t" + cpgPosListInRegion.get(j) + "\t"
//                        + r2Info.getN00() + "\t" + r2Info.getN01() + "\t" + r2Info.getN10() + "\t"  + r2Info.getN11() + "\t"
//                        + String.format("%1.8f" , r2Info.getR2()) + "\t" + r2Info.getPvalue() + "\n");
            }
        }
        bufferedWriter.write(region1.getChrom() + "\t" + region1.getStart() + "\t" + region1.getEnd() + "\t"
                + region2.getChrom() + ":" + region2.getStart() + "-" + region2.getEnd() + "\t" + cpgPosListInRegion1.size() + "\t"
                + cpgPosListInRegion2.size() + "\t" + totalR2Num + "\t" + realR2Num + "\n");

        return true;
    }
}
