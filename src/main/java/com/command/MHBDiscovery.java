package com.command;

import com.args.MHBDiscoveryArgs;
import com.bean.MHBInfo;
import com.bean.MHapInfo;
import com.bean.R2Info;
import com.bean.Region;
import htsjdk.tribble.readers.TabixReader;
import org.apache.commons.math3.distribution.BinomialDistribution;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.*;

public class MHBDiscovery {
    public static final Logger log = LoggerFactory.getLogger(MHBDiscovery.class);

    MHBDiscoveryArgs args = new MHBDiscoveryArgs();
    List<Region> regionList = new ArrayList<>();
    private final Integer SHIFT = 500;

    public void MHBDiscovery(MHBDiscoveryArgs mhbDiscoveryArgs) throws Exception {
        log.info("command.MHBDiscovery start!");
        args = mhbDiscoveryArgs;

        // check the command
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

        // get bcFile
        List<String> barcodeList = new ArrayList<>();
        if (args.getBcFile() != null) {
            File bcFile = new File(args.getBcFile());
            BufferedReader bufferedReader = new BufferedReader(new FileReader(bcFile));
            String bcLine = "";
            while ((bcLine = bufferedReader.readLine()) != null && !bcLine.equals("")) {
                barcodeList.add(bcLine.split("\t")[0]);
            }
        }

        // create mhb file
        File mhbFile = new File(args.getOutFile());
        if (!mhbFile.exists()) {
            if (!mhbFile.createNewFile()){
                log.error("create" + mhbFile.getAbsolutePath() + "fail");
                return;
            }
        } else {
            FileWriter fileWriter =new FileWriter(mhbFile.getAbsoluteFile());
            fileWriter.write("");  //写入空
            fileWriter.flush();
            fileWriter.close();
        }
        FileWriter mhbFileWriter = new FileWriter(mhbFile.getAbsoluteFile(), true);
        BufferedWriter mhbBufferedWriter = new BufferedWriter(mhbFileWriter);

        for (Region region : regionList) {
            // get cpg file
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
            List<Integer> cpgPosListInRegion = cpgPosList.subList(cpgStartPos - 1, cpgEndPos + 1); /// end site add 1

            List<MHBInfo> mhbInfoList = new ArrayList<>();
            Integer startIndex = 0; // start mhb position index in cpgPosListInRegion
            Integer endIndex = 0; // end mhb position index in cpgPosListInRegion
            while (endIndex < cpgPosListInRegion.size() - 1) {
                MHBInfo mhbInfo = new MHBInfo();
                endIndex++;
                Boolean extendFlag = true;
                Integer index = 0;
                for (int i = 1; i < args.getWindow(); i++) {
                    index = endIndex - i; // cpg site index in cpgPosListInRegion for loop
                    if (index < 0) {
                        break;
                    }
                    // get mhap file
                    TabixReader mhapTabixReader = new TabixReader(args.getmHapPath());
                    TabixReader.Iterator mhapIterator = mhapTabixReader.query(region.getChrom(),
                            cpgPosListInRegion.get(index) - 1, cpgPosListInRegion.get(endIndex));
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

                    // get r2 and pvalue of startIndex
                    R2Info r2Info= getR2(mHapInfoListMap, cpgPosList, cpgPosListInRegion, index, endIndex);
                    System.out.println("startIndex: " + startIndex + " index: " + index + " endIndex: " + endIndex);
                    System.out.println(cpgPosListInRegion.get(index) + "\t" + cpgPosListInRegion.get(endIndex) + "\t"
                            + r2Info.getR2() + "\t" + r2Info.getPvalue());
                    if (r2Info == null || r2Info.getR2() < args.getrSquare() || r2Info.getPvalue() > args.getpValue()) {
                        extendFlag = false;
                        break;
                    }
                }

                if (!extendFlag) {
                    Integer mhbSize = endIndex - startIndex;
                    Integer mhbStart = startIndex;
                    Integer mhbEnd = endIndex - 1;
                    startIndex = endIndex;
                    if (mhbSize >= args.getWindow()) {
                        mhbInfo.setChrom(region.getChrom());
                        mhbInfo.setStart(cpgPosListInRegion.get(mhbStart));
                        mhbInfo.setEnd(cpgPosListInRegion.get(mhbEnd));
                        mhbInfoList.add(mhbInfo);

                        mhbBufferedWriter.write(mhbInfo.getChrom() + "\t" + mhbInfo.getStart() + "\t" + mhbInfo.getEnd() + "\n");
                    }
                }
            }

            mhbBufferedWriter.close();
        }



        log.info("command.MHBDiscovery end!");
    }

    private boolean checkArgs() {

        return true;
    }

    private R2Info getR2(Map<String, List<MHapInfo>> mHapInfoListMap, List<Integer> cpgPosList, List<Integer> cpgPosListInRegion,
                         Integer firstIndex, Integer secondIndex) throws Exception {
        // 计算行数
        Iterator<String> iterator = mHapInfoListMap.keySet().iterator();
        Integer rowNum = 0;
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
            rowNum += strandCnt;
        }

        // 甲基化状态矩阵 0-未甲基化 1-甲基化
        Integer[][] cpgHpMatInRegion = new Integer[rowNum][cpgPosListInRegion.size()];
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

        R2Info r2Info = new R2Info();

        Integer N00 = 0;
        Integer N01 = 0;
        Integer N10 = 0;
        Integer N11 = 0;

        for (int k = 0; k < rowNum; k++) {
            if (cpgHpMatInRegion[k][firstIndex] != null && cpgHpMatInRegion[k][secondIndex] != null) {
                if (cpgHpMatInRegion[k][firstIndex] == 0 && cpgHpMatInRegion[k][secondIndex] == 0) {
                    N00++;
                } else if (cpgHpMatInRegion[k][firstIndex] == 0 && cpgHpMatInRegion[k][secondIndex] == 1) {
                    N01++;
                } else if (cpgHpMatInRegion[k][firstIndex] == 1 && cpgHpMatInRegion[k][secondIndex] == 0) {
                    N10++;
                } else if (cpgHpMatInRegion[k][firstIndex] == 1 && cpgHpMatInRegion[k][secondIndex] == 1) {
                    N11++;
                }
            }
        }

        // 计算r2
        Double r2 = 0.0;
        Double pvalue = 0.0;
        Double N = N00 + N01 + N10 + N11 + 0.0;
        if(N == 0) {
            r2 = Double.NaN;
            pvalue = Double.NaN;
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
        }
        if (D < 0) {
            r2 = -1 * r2;
        }

        // 计算pvalue
        BinomialDistribution binomialDistribution = new BinomialDistribution(N.intValue(), PA * PB);
        Double pGreater = 1 - binomialDistribution.cumulativeProbability(N11);
        Double pEqual = binomialDistribution.probability(N11);
        pvalue = pGreater + pEqual;

        r2Info.setR2(r2);
        r2Info.setPvalue(pvalue);

        return r2Info;
    }

    private Integer[][] getMC(List<MHapInfo> mHapInfoList, List<Integer> cpgPosListInRegion) {
        Integer[][] MC = new Integer[mHapInfoList.size()][cpgPosListInRegion.size()];

        return MC;
    }

    private Integer[][] getM1(List<MHapInfo> mHapInfoList, List<Integer> cpgPosListInRegion) {
        Integer[][] M1 = new Integer[mHapInfoList.size()][cpgPosListInRegion.size()];

        return M1;
    }

    private Integer[][] getM0(List<MHapInfo> mHapInfoList, List<Integer> cpgPosListInRegion) {
        Integer[][] M0 = new Integer[mHapInfoList.size()][cpgPosListInRegion.size()];

        return M0;
    }
}
