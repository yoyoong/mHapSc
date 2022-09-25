package com;

import com.args.TrackArgs;
import com.bean.MHapInfo;
import com.bean.R2Info;
import com.bean.Region;
import com.common.Util;
import htsjdk.tribble.readers.TabixReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.*;

public class Track {
    public static final Logger log = LoggerFactory.getLogger(Track.class);

    Util util = new Util();
    TrackArgs args = new TrackArgs();
    List<Region> regionList = new ArrayList<>();

    public void track(TrackArgs trackArgs) throws Exception {
        log.info("Track start!");
        args = trackArgs;

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

        // get the metric list
        String[] metricList = args.getMetric().split(" ");
        for (String metric : metricList) {
            // create the output directory and file
            BufferedWriter bufferedWriter = util.createOutputFile(args.getOutputDir(),
                    args.getTag() + "." + metric + ".bedGraph");

            for (Region region : regionList) {
                // parse the mhap file
                Map<String, List<MHapInfo>> mHapListMap = util.parseMhapFile(args.getMhapPath(), barcodeList, args.getBcFile(), region);

                // parse the cpg file
                List<Integer> cpgPosList = util.parseCpgFile(args.getCpgPath(), region);

                // get cpg site list in region
                List<Integer> cpgPosListInRegion = util.getcpgPosListInRegion(cpgPosList, region);

                if (metric.equals("mm")) {
                    // calculate the row number
                    Integer strandCnt = util.getMhapMapRowNum(mHapListMap);

                    // get cpg ststus matrix in region
                    Integer[][] cpgHpMatInRegion = util.getCpgHpMat(strandCnt, cpgPosListInRegion.size(), cpgPosListInRegion, mHapListMap);

                    // get the mm of every cpg site
                    for (int i = 0; i < cpgPosListInRegion.size(); i++) {
                        Double mm = getMM(cpgHpMatInRegion, i);
                        bufferedWriter.write(region.getChrom() + "\t" + cpgPosListInRegion.get(i) + "\t" +
                                cpgPosListInRegion.get(i) + "\t" + mm + "\n");
                    }
                } else if (metric.equals("r2")) {
                    // calculate the row number
                    Integer strandCnt = util.getMhapMapRowNum(mHapListMap);

                    // get cpg ststus matrix in region
                    Integer[][] cpgHpMatInRegion = util.getCpgHpMat(strandCnt, cpgPosListInRegion.size(), cpgPosListInRegion, mHapListMap);

                    // get the mean r2 of every cpg site
                    for (int i = 0; i < cpgPosListInRegion.size(); i++) {
                        Double meanR2 = getMeanR2(cpgHpMatInRegion, cpgPosListInRegion, strandCnt, i);
                        bufferedWriter.write(region.getChrom() + "\t" + cpgPosListInRegion.get(i) + "\t" +
                                cpgPosListInRegion.get(i) + "\t" + meanR2 + "\n");
                    }
                }

            }

            bufferedWriter.close();
        }

        log.info("Track end!");
    }

    private boolean checkArgs() {

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

    private Double getMeanR2(Integer[][] cpgHpMatInRegion, List<Integer> cpgPosListInRegion, Integer totalStrandCnt, int pos) {
        Double r2Sum = 0.0;
        List<Double> r2List = new ArrayList<>();

        for (int j = pos - 2; j < pos + 3; j++) {
            if (j < 0 || j == pos ||  j >= cpgPosListInRegion.size()) {
                continue;
            }
            R2Info r2Info = util.getR2Info(cpgHpMatInRegion, pos, j, totalStrandCnt);
            if (r2Info != null && r2Info.getR2() != Double.NaN) {
                r2List.add(r2Info.getR2());
            }
        }

        for (int i = 0; i < r2List.size(); i++) {
            r2Sum += r2List.get(i);
        }

        return r2Sum / r2List.size();
    }
}
