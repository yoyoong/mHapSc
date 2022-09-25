package com;

import com.args.FlinkageArgs;
import com.bean.MHapInfo;
import com.bean.R2Info;
import com.bean.Region;
import com.common.Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

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
            File bedFile = new File(args.getBedFile());
            BufferedReader bufferedReader = new BufferedReader(new FileReader(bedFile));
            String bedLine = "";
            while ((bedLine = bufferedReader.readLine()) != null && !bedLine.equals("")) {
                Region region = new Region();
                if (bedLine.split("\t").length < 3) {
                    log.error("Interval not in correct format.");
                    break;
                }
                region.setChrom(bedLine.split("\t")[0]);
                region.setStart(Integer.valueOf(bedLine.split("\t")[1]) + 1);
                region.setEnd(Integer.valueOf(bedLine.split("\t")[2]));
                regionList.add(region);
            }
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
        String fileName = args.getOutputDir() + "/" + args.getTag() + ".longrange.txt";
        File file = new File(fileName);
        if (!file.exists()) {
            if (!file.createNewFile()) {
                log.error("create" + file.getAbsolutePath() + "fail");
                return;
            }
        } else {
            FileWriter fileWriter =new FileWriter(file.getAbsoluteFile());
            fileWriter.write("");  //写入空
            fileWriter.flush();
            fileWriter.close();
        }
        FileWriter fileWriter = new FileWriter(file.getAbsoluteFile(), true);
        BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);

        for (int i = 0; i < regionList.size(); i++) {
            for (int j = i + 1; j < regionList.size(); j++) {
                Region region1 = regionList.get(i);
                Region region2 = regionList.get(j);

                // parse the mhap file
                Map<String, List<MHapInfo>> mHapListMap1 = util.parseMhapFile(args.getMhapPath(), barcodeList, 
                        args.getBcFile(), region1);
                Map<String, List<MHapInfo>> mHapListMap2 = util.parseMhapFile(args.getMhapPath(), barcodeList,
                        args.getBcFile(), region2);

                // parse the cpg file
                List<Integer> cpgPosList1 = util.parseCpgFile(args.getCpgPath(), region1);
                List<Integer> cpgPosList2 = util.parseCpgFile(args.getCpgPath(), region2);

                boolean getFlinkageResult = getFlinkage(mHapListMap1, mHapListMap2, cpgPosList1, cpgPosList2, region1, region2, bufferedWriter);
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

    private boolean getFlinkage(Map<String, List<MHapInfo>> mHapListMap1, Map<String, List<MHapInfo>> mHapListMap2,
                                List<Integer> cpgPosList1, List<Integer> cpgPosList2, Region region1, Region region2,
                                BufferedWriter bufferedWriter) throws Exception {
        // 提取查询区域内的甲基化位点列表
        List<Integer> cpgPosListInRegion1 = util.getcpgPosListInRegion(cpgPosList1, region1);
        List<Integer> cpgPosListInRegion2 = util.getcpgPosListInRegion(cpgPosList2, region2);
        List<Integer> cpgPosListInRegion = new ArrayList<>();
        cpgPosListInRegion.addAll(cpgPosListInRegion1);
        cpgPosListInRegion.addAll(cpgPosListInRegion2);

        // filter the same barcode of mHapListMap1 and mHapListMap2, and merge the list of same barcode
        Iterator<String> iterator = mHapListMap1.keySet().iterator();
        while (iterator.hasNext()) {
            String key = iterator.next();
            if (mHapListMap2.containsKey(key)) { // exist, merge the list
                List<MHapInfo> mHapInfoList1 = mHapListMap1.get(key);
                List<MHapInfo> mHapInfoList2 = mHapListMap2.get(key);
                mHapInfoList1.addAll(mHapInfoList2);
                mHapListMap1.put(key, mHapInfoList1);
            } else { // not exist, remove
                iterator.remove();
            }

        }

        // 计算行数
        Integer rowNum = util.getMhapMapRowNum(mHapListMap1);

        // 甲基化状态矩阵 0-未甲基化 1-甲基化
        Integer[][] cpgHpMatInRegion = util.getCpgHpMat(rowNum, cpgPosListInRegion.size(), cpgPosListInRegion, mHapListMap1);
//        for (int i = 0; i < cpgHpMatInRegion.length; i++) {
//            for (int j = 0; j < cpgHpMatInRegion[i].length; j++) {
//                if (cpgHpMatInRegion[i][j] == null) {
//                    cpgHpMatInRegion[i][j] = -1;
//                }
//                System.out.print(cpgHpMatInRegion[i][j] + " ");
//            }
//            System.out.print("\n");
//        }

        // calculate the r2Info of erery position
        Integer totalR2Num = cpgPosListInRegion1.size() * cpgPosListInRegion2.size();
        Integer realR2Num = 0;
        for (int i = 0; i < cpgPosListInRegion1.size(); i++) {
            for (int j = cpgPosListInRegion1.size(); j < cpgPosListInRegion.size(); j++) {
                R2Info r2Info = util.getR2Info(cpgHpMatInRegion, i, j, rowNum);
                if (r2Info.getR2() > 0.5 && r2Info.getPvalue() < 0.05) {
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
