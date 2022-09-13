package com.common;

import com.bean.MHapInfo;
import com.bean.R2Info;
import com.bean.Region;
import com.bean.ScBedInfo;
import htsjdk.samtools.SRAFileReader;
import htsjdk.tribble.readers.TabixReader;
import org.apache.commons.math3.distribution.BinomialDistribution;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

public class Util {
    public Region parseRegion(String regionStr) {
        Region region = new Region();
        region.setChrom(regionStr.split(":")[0]);
        region.setStart(Integer.valueOf(regionStr.split(":")[1].split("-")[0]));
        region.setEnd(Integer.valueOf(regionStr.split(":")[1].split("-")[1]));
        return region;
    }

    public List<Integer> parseCpgFile(String cpgPath, Region region) throws Exception {
        List<Integer> cpgPosList = new ArrayList<>();
        TabixReader cpgTabixReader = new TabixReader(cpgPath);
        TabixReader.Iterator cpgIterator = cpgTabixReader.query(region.getChrom(), region.getStart(), region.getEnd());
        String cpgLine = "";
        while((cpgLine = cpgIterator.next()) != null) {
            if (cpgLine.split("\t").length < 3) {
                continue;
            } else {
                cpgPosList.add(Integer.valueOf(cpgLine.split("\t")[1]));
            }
        }

        return cpgPosList;
    }

    public List<Integer> parseCpgFileWithShift(String cpgPath, Region region, Integer shift) throws Exception {
        List<Integer> cpgPosList = new ArrayList<>();
        TabixReader cpgTabixReader = new TabixReader(cpgPath);
        TabixReader.Iterator cpgIterator = cpgTabixReader.query(region.getChrom(), region.getStart() - shift, region.getEnd() + shift);
        String cpgLine = "";
        while((cpgLine = cpgIterator.next()) != null) {
            if (cpgLine.split("\t").length < 3) {
                continue;
            } else {
                cpgPosList.add(Integer.valueOf(cpgLine.split("\t")[1]));
            }
        }

        return cpgPosList;
    }

    public List<Integer> getcpgPosListInRegion(List<Integer> cpgPosList, Region region) throws Exception {
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
        List<Integer> cpgPosListInRegion = cpgPosList.subList(cpgStartPos - 1, cpgEndPos);

        return cpgPosListInRegion;
    }

    public Map<String, List<MHapInfo>> parseMhapFile(String mhapPath, List<String> barcodeList, String bcFile, Region region) throws IOException {
        TabixReader mhapTabixReader = new TabixReader(mhapPath);
        TabixReader.Iterator mhapIterator = mhapTabixReader.query(region.getChrom(), region.getStart() - 1, region.getEnd());
        Map<String, List<MHapInfo>> mHapListMap = new HashMap<>(); // mhap数据列表（通过barcode索引）
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

            if (bcFile != null && !barcodeList.contains(mHapInfo.getBarcode())) {
                continue;
            } else {
                if (mHapListMap.containsKey(mHapInfo.getBarcode())) {
                    List<MHapInfo> mHapInfoList = mHapListMap.get(mHapInfo.getBarcode());
                    mHapInfoList.add(mHapInfo);
                } else {
                    List<MHapInfo> mHapInfoList = new ArrayList<>();
                    mHapInfoList.add(mHapInfo);
                    mHapListMap.put(mHapInfo.getBarcode(), mHapInfoList);
                }
            }
        }

        return mHapListMap;
    }
    
    public Integer getMhapMapRowNum(Map<String, List<MHapInfo>> mHapListMap) {
        Iterator<String> iterator = mHapListMap.keySet().iterator();
        Integer rowNum = 0;
        while (iterator.hasNext()) {
            List<MHapInfo> mHapInfoList = mHapListMap.get(iterator.next());
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

        return rowNum;
    }

    public Integer[][] getCpgHpMat(Integer rowNum, Integer colNum, List<Integer> cpgPosList, Map<String, List<MHapInfo>> mHapListMap) {
        Integer[][] cpgHpMatInRegion = new Integer[rowNum][colNum];
        for (int i = 0; i < cpgPosList.size(); i++) {
            Iterator<String> iterator1 = mHapListMap.keySet().iterator();
            Integer row = 0;
            while (iterator1.hasNext()) {
                List<MHapInfo> mHapInfoList = mHapListMap.get(iterator1.next());

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
                    if (cpgPosList.get(i) >= mHapInfo.getStart() && cpgPosList.get(i) <= mHapInfo.getEnd()) {
                        // 获取某个在区域内的位点在mhap的cpg中的相对位置
                        Integer pos = cpgPosList.indexOf(cpgPosList.get(i)) - cpgPosList.indexOf(mHapInfo.getStart());

                        if (plusFlag && minusFlag) {
                            if (mHapInfo.getStrand().equals("+")) {
                                for (int k = pos; k < mHapInfo.getCpg().length(); k++) {
                                    if (i + k - pos < cpgPosList.size()) {
                                        if (mHapInfo.getCpg().charAt(k) == '0') {
                                            cpgHpMatInRegion[row][i + k - pos] = 0;
                                        } else {
                                            cpgHpMatInRegion[row][i + k - pos] = 1;
                                        }
                                    }
                                }
                            } else {
                                for (int k = pos; k < mHapInfo.getCpg().length(); k++) {
                                    if (i + k - pos < cpgPosList.size()) {
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
                                if (i + k - pos < cpgPosList.size()) {
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

    public R2Info getR2Info(Integer[][] cpgHpMat, Integer col1, Integer col2, Integer rowNum) {
        R2Info r2Info = new R2Info();

        Integer N00 = 0;
        Integer N01 = 0;
        Integer N10 = 0;
        Integer N11 = 0;

        for (int i = 0; i < rowNum; i++) {
            if (cpgHpMat[i][col1] != null && cpgHpMat[i][col2] != null) {
                if (cpgHpMat[i][col1] == 0 && cpgHpMat[i][col2] == 0) {
                    N00++;
                } else if (cpgHpMat[i][col1] == 0 && cpgHpMat[i][col2] == 1) {
                    N01++;
                } else if (cpgHpMat[i][col1] == 1 && cpgHpMat[i][col2] == 0) {
                    N10++;
                } else if (cpgHpMat[i][col1] == 1 && cpgHpMat[i][col2] == 1) {
                    N11++;
                }
            }
        }

        /// 计算r2
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
            if (D < 0) {
                r2 = -1 * r2;
            }
        }

        // 计算pvalue
        BinomialDistribution binomialDistribution = new BinomialDistribution(N.intValue(), PA * PB);
        Double pGreater = 1 - binomialDistribution.cumulativeProbability(N11);
        Double pEqual = binomialDistribution.probability(N11);
        pvalue = pGreater + pEqual;

        r2Info.setN00(N00);
        r2Info.setN01(N01);
        r2Info.setN10(N01);
        r2Info.setN11(N11);
        r2Info.setR2(r2);
        r2Info.setPvalue(pvalue);

        return r2Info;
    }

}
