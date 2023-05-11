package com;

import com.File.*;
import com.args.ScStatArgs;
import com.bean.MHapInfo;
import com.bean.R2Info;
import com.bean.Region;
import com.common.Util;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class ScStat {
    public static final Logger log = LoggerFactory.getLogger(ScStat.class);

    ScStatArgs args = new ScStatArgs();
    Util util = new Util();
    MHapFile mHapFile;
    CpgFile cpgFile;
    ScStatOutputFile tBaseOutputFile;
    ScStatOutputFile mBaseOutputFile;

    public void scStat(ScStatArgs scStatArgs) throws Exception {
        log.info("ScStat start!");
        args = scStatArgs;
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

        // create the output file
        if (args.getMetrics().equals("MM")) {
            tBaseOutputFile = new ScStatOutputFile(args.getOutputDir(), args.getTag() + "_total.txt");
            tBaseOutputFile.setBarcodeList(barcodeList);
            String headString = "region";
            tBaseOutputFile.writeHead(headString);

            mBaseOutputFile = new ScStatOutputFile(args.getOutputDir(), args.getTag() + "_methylated.txt");
            mBaseOutputFile.setBarcodeList(barcodeList);
            mBaseOutputFile.writeHead(headString);
        }

        // get the metric list
        for (Region region : regionList) {
            // parse the mhap file
            Map<String, List<MHapInfo>> mHapInfoListMap = mHapFile.parseByRegionIndexByBarCode(region, barcodeList);
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

            boolean getScStatResult = getScStat(mHapInfoListMap, cpgPosList, cpgPosListInRegion, region, barcodeList);
            if (!getScStatResult) {
                log.error("getScStat fail, please check the command.");
                return;
            }

//            log.info("Region: " + region.toHeadString() + " calculate end!");
        }

        mHapFile.close();
        cpgFile.close();
        tBaseOutputFile.close();
        mBaseOutputFile.close();
        log.info("ScStat end!");
    }

    private boolean checkArgs() {
        if (args.getMhapPath().equals("")) {
            log.error("The mhap file cannot be empty!");
        }
        if (args.getCpgPath().equals("")) {
            log.error("The cpg file cannot be empty!");
        }
        if (!args.getRegion().equals("") && !args.getBedPath().equals("")) {
            log.error("Can not input region and bedPath at the same time.");
            return false;
        }
        if (args.getBcFile() == null || args.getBcFile().equals("")) {
            log.error("The barcode file cannot be empty!");
            return false;
        }
        if (args.getMetrics() == null || args.getMetrics().equals("")) {
            log.error("The metrics cannot be empty!");
            return false;
        } else {
            if (!args.getMetrics().equals("MM")) {
                log.error("The metrics should be MM!");
                return false;
            }
        }
        return true;
    }

    private boolean getScStat(Map<String, List<MHapInfo>> mHapInfoListMap, List<Integer> cpgPosList,
                              List<Integer> cpgPosListInRegion, Region region, List<String> barcodeList) throws Exception {
        int[] tBaseArray = new int[barcodeList.size()];
        int[] mBaseArray = new int[barcodeList.size()];
        Iterator<String> iterator = mHapInfoListMap.keySet().iterator();
        while (iterator.hasNext()) {
            Integer tBase = 0; // 总位点个数
            Integer mBase = 0; // 甲基化位点个数
            String key = iterator.next();
            List<MHapInfo> mHapInfoList = mHapInfoListMap.get(key);
            for (MHapInfo mHapInfo : mHapInfoList) {
                String cpg = util.cutReads(mHapInfo, cpgPosList, cpgPosListInRegion);;
                tBase += cpg.length();
                for (int j = 0; j < cpg.length(); j++) {
                    if (cpg.charAt(j) == '1') {
                        mBase++;
                    }
                }
            }

            Integer barcodeIndex = barcodeList.indexOf(key);
            if (barcodeIndex < 0) {
                continue;
            } else {
                tBaseArray[barcodeIndex] = tBase;
                mBaseArray[barcodeIndex] = mBase;
            }
        }

        String tBaseLineString = region.toHeadString() + "\t";
        String mBaseLineString = region.toHeadString() + "\t";
        String tBaseArrayString = ArrayUtils.toString(tBaseArray).replaceAll(",", "\t");
        String mBaseArrayString = ArrayUtils.toString(mBaseArray).replaceAll(",", "\t");
        tBaseLineString += tBaseArrayString.substring(1, tBaseArrayString.length() - 1) + "\n";
        mBaseLineString += mBaseArrayString.substring(1, mBaseArrayString.length() - 1) + "\n";
        tBaseOutputFile.writeLine(tBaseLineString);
        mBaseOutputFile.writeLine(mBaseLineString);

        return true;
    }
}
