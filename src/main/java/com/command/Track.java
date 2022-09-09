package com.command;

import com.args.TrackArgs;
import com.bean.MHapInfo;
import com.bean.Region;
import htsjdk.tribble.readers.TabixReader;
import org.apache.commons.math3.distribution.BinomialDistribution;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.*;
import java.util.stream.Collectors;

public class Track {
    public static final Logger log = LoggerFactory.getLogger(Track.class);

    TrackArgs args = new TrackArgs();
    List<Region> regionList = new ArrayList<>();
    private final Integer SHIFT = 500;

    public void track(TrackArgs trackArgs) throws Exception {
        log.info("command.Track start!");
        args = trackArgs;

        // 校验命令正确性
        boolean checkResult = checkArgs();
        if (!checkResult) {
            log.error("Checkargs fail, please check the command.");
            return;
        }

        // get regionList, from region or bedfile
        if (args.getRegion() != null && !args.getRegion().equals("")) {
            Region region = new Region();
            region.setChrom(args.getRegion().split(":")[0]);
            region.setStart(Integer.valueOf(args.getRegion().split(":")[1].split("-")[0]));
            region.setEnd(Integer.valueOf(args.getRegion().split(":")[1].split("-")[1]));
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

        // 解析bcFile
        List<String> barcodeList = new ArrayList<>();
        if (args.getBcFile() != null) {
            File bcFile = new File(args.getBcFile());
            BufferedReader bufferedReader = new BufferedReader(new FileReader(bcFile));
            String bcLine = "";
            while ((bcLine = bufferedReader.readLine()) != null && !bcLine.equals("")) {
                barcodeList.add(bcLine.split("\t")[0]);
            }
        }

        // get the metric list
        String[] metricList = args.getMetric().split(" ");
        for (String metric : metricList) {
            // create the directory
            File outputDir = new File(args.getOutputDir());
            if (!outputDir.exists()){
                if (!outputDir.mkdirs()){
                    log.error("create" + outputDir.getAbsolutePath() + "fail");
                    return;
                }
            }

            // create the file
            String fileName = args.getOutputDir() + "/" + args.getTag() + "." + metric + ".bedGraph";
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

            for (Region region : regionList) {
                // parse the mhap file
                TabixReader mhapTabixReader = new TabixReader(args.getMhapPath());
                TabixReader.Iterator mhapIterator = mhapTabixReader.query(region.getChrom(), region.getStart() - 1, region.getEnd());
                Map<String, List<MHapInfo>> mHapInfoListMap = new HashMap<>(); // mhap数据列表（通过barcode索引）
                String mHapLine = "";
                while((mHapLine = mhapIterator.next()) != null) {
                    MHapInfo mHapInfo = new MHapInfo();
                    mHapInfo.setChrom(mHapLine.split("\t")[0]);
                    mHapInfo.setStart(Integer.valueOf(mHapLine.split("\t")[1]));
                    mHapInfo.setEnd(Integer.valueOf(mHapLine.split("\t")[2]));
                    mHapInfo.setCpg(mHapLine.split("\t")[3]);
                    mHapInfo.setCnt(Integer.valueOf(mHapLine.split("\t")[4]));
                    mHapInfo.setStrand(mHapLine.split("\t")[5]);
                    mHapInfo.setBarcode(mHapLine.split("\t")[6]);

                    if (args.getBcFile() != null && !barcodeList.contains(mHapInfo.getBarcode())) {
                        continue;
                    } else {
                        if (mHapInfoListMap.containsKey(mHapInfo.getBarcode())) {
                            List<MHapInfo> mHapInfoList = mHapInfoListMap.get(mHapInfo.getBarcode());
                            mHapInfoList.add(mHapInfo);
                        } else {
                            List<MHapInfo> mHapInfoList = new ArrayList<>();
                            mHapInfoList.add(mHapInfo);
                            mHapInfoListMap.put(mHapInfo.getBarcode(), mHapInfoList);
                        }
                    }
                }

                // parse cpg file
                List<Integer> cpgPosList = new ArrayList<>();
                TabixReader cpgTabixReader = new TabixReader(args.getCpgPath());
                TabixReader.Iterator cpgIterator = cpgTabixReader.query(region.getChrom(),
                        region.getStart() - SHIFT, region.getEnd() + SHIFT); // 查询范围扩大500
                String cpgLine = "";
                while((cpgLine = cpgIterator.next()) != null) {
                    if (cpgLine.split("\t").length < 3) {
                        continue;
                    } else {
                        cpgPosList.add(Integer.valueOf(cpgLine.split("\t")[1]));
                    }
                }

                // get cpg site list in region
                Integer cpgStartPos = region.getStart() > cpgPosList.get(0) ? region.getStart() : cpgPosList.get(0);
                Integer cpgEndPos = region.getEnd() > cpgPosList.get(cpgPosList.size() - 1) ? cpgPosList.get(cpgPosList.size() - 1) : region.getEnd();
                for (int i = 0; i < cpgPosList.size(); i++) {
                    if (cpgPosList.get(i) <= cpgStartPos && cpgPosList.get(i + 1) >= cpgStartPos) {
                        cpgStartPos = i + 1;
                        break;
                    }
                }
                for (int i = 0; i < cpgPosList.size(); i++) {
                    if (cpgPosList.get(i) > cpgEndPos) {
                        cpgEndPos = i;
                        break;
                    } else if (cpgPosList.get(i).equals(cpgEndPos)) {
                        cpgEndPos = i + 1;
                        break;
                    }
                }
                List<Integer> cpgPosListInRegion = cpgPosList.subList(cpgStartPos, cpgEndPos);

                if (metric.equals("mm")) {
                    // get total strand count
                    Integer strandCnt = getStrandCnt(mHapInfoListMap);

                    // get cpg ststus matrix in region
                    Integer[][] cpgHpMatInRegion = getCpgHpMat(mHapInfoListMap, cpgPosList, cpgPosListInRegion, strandCnt);

                    // get the mm of every cpg site
                    for (int i = 0; i < cpgPosListInRegion.size(); i++) {
                        Double mm = getMM(cpgHpMatInRegion, i);
                        bufferedWriter.write(region.getChrom() + "\t" + cpgPosListInRegion.get(i) + "\t" +
                                cpgPosListInRegion.get(i) + "\t" + mm + "\n");
                    }
                } else if (metric.equals("r2")) {
                    // get total strand count
                    Integer strandCnt = getStrandCnt(mHapInfoListMap);

                    // get cpg ststus matrix in region
                    Integer[][] cpgHpMatInRegion = getCpgHpMat(mHapInfoListMap, cpgPosList, cpgPosListInRegion, strandCnt);

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

        log.info("command.Track end!");
    }

    private boolean checkArgs() {

        return true;
    }

    private Integer getStrandCnt(Map<String, List<MHapInfo>> mHapInfoListMap) {
        // 计算总链数
        Iterator<String> iterator = mHapInfoListMap.keySet().iterator();
        Integer totalStrandCnt = 0;
        while (iterator.hasNext()) {
            List<MHapInfo> mHapInfoList = mHapInfoListMap.get(iterator.next());
            // 是否存在正负两条链
            Boolean plusFlag = false;
            Boolean minusFlag = false;
            for (int i = 0; i < mHapInfoList.size(); i++) {
                plusFlag = mHapInfoList.get(i).getStrand().equals("+") ? true : plusFlag;
                minusFlag = mHapInfoList.get(i).getStrand().equals("-") ? true : minusFlag;
            }

            Integer strandCnt = plusFlag && minusFlag ? 2 : 1;// 链数
            totalStrandCnt += strandCnt;
        }

        return totalStrandCnt;
    }

    private Integer[][] getCpgHpMat(Map<String, List<MHapInfo>> mHapInfoListMap, List<Integer> cpgPosList,
                                    List<Integer> cpgPosListInRegion, Integer totalStrandCnt) {

        // 甲基化状态矩阵 0-未甲基化 1-甲基化
        Integer[][] cpgHpMatInRegion = new Integer[totalStrandCnt][cpgPosListInRegion.size()];
        for (int i = 0; i < cpgPosListInRegion.size(); i++) {
            Iterator<String> iterator1 = mHapInfoListMap.keySet().iterator();
            Integer row = 0;
            while (iterator1.hasNext()) {
                List<MHapInfo> mHapInfoList = mHapInfoListMap.get(iterator1.next());

                // 是否存在正负两条链
                Boolean plusFlag = false;
                Boolean minusFlag = false;
                for (int j = 0; j < mHapInfoList.size(); j++) {
                    plusFlag = mHapInfoList.get(j).getStrand().equals("+") ? true : plusFlag;
                    minusFlag = mHapInfoList.get(j).getStrand().equals("-") ? true : minusFlag;
                }
                Integer strandCnt = plusFlag && minusFlag ? 2 : 1;// 链数

                for (int j = 0; j < mHapInfoList.size(); j++) {
                    MHapInfo mHapInfo = mHapInfoList.get(j);
                    if (cpgPosListInRegion.get(i) >= mHapInfo.getStart() && cpgPosListInRegion.get(i) <= mHapInfo.getEnd()) {
                        // 获取某个在区域内的位点在mhap的cpg中的相对位置
                        Integer pos = cpgPosList.indexOf(cpgPosListInRegion.get(i)) - cpgPosList.indexOf(mHapInfo.getStart());

                        if (plusFlag && minusFlag) {
                            if (mHapInfo.getStrand().equals("+")) {
                                for (int k = pos; k < mHapInfo.getCpg().length(); k++) {
                                    if (i + k - pos < cpgPosListInRegion.size()) {
                                        if (mHapInfo.getCpg().charAt(k) == '0') {
                                            cpgHpMatInRegion[row][i + k - pos] = 0;
                                        } else {
                                            cpgHpMatInRegion[row][i + k - pos] = 1;
                                        }
                                    }
                                }
                            } else {
                                for (int k = pos; k < mHapInfo.getCpg().length(); k++) {
                                    if (i + k - pos < cpgPosListInRegion.size()) {
                                        if (mHapInfo.getCpg().charAt(k) == '0') {
                                            cpgHpMatInRegion[row + 1][i + k - pos] = 0;
                                        } else {
                                            cpgHpMatInRegion[row + 1][i + k - pos] = 1;
                                        }
                                    }
                                }
                            }
                        } else {
                            for (int k = pos; k < mHapInfo.getCpg().length(); k++) {
                                if (i + k - pos < cpgPosListInRegion.size()) {
                                    if (mHapInfo.getCpg().charAt(k) == '0') {
                                        cpgHpMatInRegion[row][i + k - pos] = 0;
                                    } else {
                                        cpgHpMatInRegion[row][i + k - pos] = 1;
                                    }
                                }
                            }
                        }
                    }
                }
                row += strandCnt;
            }
        }

        return cpgHpMatInRegion;
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
            if (j < 0 || j >= cpgPosListInRegion.size()) {
                continue;
            }
            Integer N00 = 0;
            Integer N01 = 0;
            Integer N10 = 0;
            Integer N11 = 0;

            for (int k = 0; k < totalStrandCnt; k++) {
                if (j == 59) {
                    j = 59;
                }
                if (cpgHpMatInRegion[k][pos] != null && cpgHpMatInRegion[k][j] != null) {
                    if (cpgHpMatInRegion[k][pos] == 0 && cpgHpMatInRegion[k][j] == 0) {
                        N00++;
                    } else if (cpgHpMatInRegion[k][pos] == 0 && cpgHpMatInRegion[k][j] == 1) {
                        N01++;
                    } else if (cpgHpMatInRegion[k][pos] == 1 && cpgHpMatInRegion[k][j] == 0) {
                        N10++;
                    } else if (cpgHpMatInRegion[k][pos] == 1 && cpgHpMatInRegion[k][j] == 1) {
                        N11++;
                    }
                }
            }

            Double r2 = 0.0;
            Double N = N00 + N01 + N10 + N11 + 0.0;
            if(N == 0) {
                r2 = Double.NaN;
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

            r2List.add(r2);
        }

        for (int i = 0; i < r2List.size(); i++) {
            r2Sum += r2List.get(i);
        }

        return r2Sum / r2List.size();
    }
}
