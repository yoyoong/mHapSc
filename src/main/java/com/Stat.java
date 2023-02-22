package com;

import com.File.*;
import com.args.StatArgs;
import com.bean.MHapInfo;
import com.bean.Region;
import com.common.Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class Stat {
    public static final Logger log = LoggerFactory.getLogger(Stat.class);

    StatArgs args = new StatArgs();
    Util util = new Util();

    public void stat(StatArgs statArgs) throws Exception {
        log.info("Stat start!");
        args = statArgs;
        MHapFile mHapFile = new MHapFile(args.getMhapPath());
        CpgFile cpgFile = new CpgFile(args.getCpgPath());

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
        String headString = "chr" + "\t" + "start" + "\t" + "end";
        statOutputFile.writeHead(headString);

        // get the metric list
        for (Region region : regionList) {
            // parse the mhap file
            Map<String, List<MHapInfo>> mHapInfoListMap = mHapFile.parseByRegionAndBarcode(region, args.getStrand(), barcodeList);
            if (mHapInfoListMap.size() < 1) {
                log.info("MHap info list in " + region.toHeadString() + " is null!");
                continue;
            }

            // parse the cpg file
            List<Integer> cpgPosListInRegion = cpgFile.parseByRegion(region);
            if (cpgPosListInRegion.size() < 1) {
                log.info("Cpg pos list in " + region.toHeadString() + " is null!");
                continue;
            }

            boolean getStatResult = getStat(mHapInfoListMap, cpgPosListInRegion, region, metricsList, statOutputFile);
            if (!getStatResult) {
                log.error("getStat fail, please check the command.");
                return;
            }
            log.info("Region: " + region.toHeadString() + " calculate end!");
        }

        mHapFile.close();
        cpgFile.close();
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

    private boolean getStat(Map<String, List<MHapInfo>> mHapInfoListMap, List<Integer> cpgPosList, Region region,
                            List<String> metricsList, StatOutputFile statOutputFile) throws Exception {
        Integer nReads = 0; // 总read个数
        Integer mBase = 0; // 甲基化位点个数
        Integer cBase = 0; // 存在甲基化的read中的未甲基化位点个数
        Integer tBase = 0; // 总位点个数
        Integer K4plus = 0; // 长度大于等于K个位点的read个数
        Integer nDR = 0; // 长度大于等于K个位点且同时含有甲基化和未甲基化位点的read个数
        Integer nMR = 0; // 长度大于等于K个位点且含有甲基化位点的read个数
        Iterator<String> iterator = mHapInfoListMap.keySet().iterator();
        while (iterator.hasNext()) {
            List<MHapInfo> mHapInfoList = mHapInfoListMap.get(iterator.next());
            for (MHapInfo mHapInfo : mHapInfoList) {
                String cpg = mHapInfo.getCpg();
                Integer cnt = mHapInfo.getCnt();
                nReads += cnt;
                tBase += cpg.length() * cnt;
                for (int j = 0; j < cpg.length(); j++) {
                    if (cpg.charAt(j) == '1') {
                        mBase += cnt;
                    }
                }
                if (cpg.contains("1")) {
                    for (int j = 0; j < cpg.length(); j++) {
                        if (cpg.charAt(j) == '0') {
                            cBase += cnt;
                        }
                    }
                }
                if (cpg.length() >= args.getK()) {
                    K4plus += cnt;
                    if (cpg.contains("1")) {
                        nMR += cnt;
                        if (cpg.contains("0")) {
                            nDR += cnt;
                        }
                    }
                }
            }
        }

        String lineString = region.getChrom() + "\t" + region.getStart() + "\t" + region.getEnd();
        for (String metrics : metricsList) {
            if (metrics.equals("MM")) {
                Double mm = mBase.doubleValue() / tBase.doubleValue();
                lineString += "\t" + String.format("%.8f", mm);
            } else if (metrics.equals("CHALM")) {
                Double chalm = nMR.doubleValue() / K4plus.doubleValue();
                lineString += "\t" + String.format("%.8f", chalm);
            } else if (metrics.equals("PDR")) {
                Double pdr = nDR.doubleValue() / K4plus.doubleValue();
                lineString += "\t" + String.format("%.8f", pdr);
            } else if (metrics.equals("MHL")) {
                Double mhl = 0.0;
                lineString += "\t" + String.format("%.8f", mhl);
            } else if (metrics.equals("MBS")) {
                Double mbs = 0.0;
                lineString += "\t" + String.format("%.8f", mbs);
            } else if (metrics.equals("MCR")) {
                Double mcr = cBase.doubleValue() / tBase.doubleValue();
                lineString += "\t" + String.format("%.8f", mcr);
            } else if (metrics.equals("Entropy")) {
                Double entropy = 0.0;
                lineString += "\t" + String.format("%.8f", entropy);
            } else if (metrics.equals("R2")) {
                Double r2 = 0.0;
                lineString += "\t" + String.format("%.8f", r2);
            }
        }
        statOutputFile.writeLine(lineString);

        return true;
    }


}
