package com.File;

import com.bean.MHapInfo;
import com.bean.Region;
import htsjdk.tribble.readers.TabixReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.*;

public class MHapFile {
    public static final Logger log = LoggerFactory.getLogger(MHapFile.class);

    TabixReader tabixReader;

    public MHapFile(String mHapPath) throws IOException {
        tabixReader = new TabixReader(mHapPath);
    }

    public Map<String, List<MHapInfo>> parseByRegionIndexByBarCode(Region region, List barcodeList) throws IOException {
        TabixReader.Iterator mhapIterator = tabixReader.query(region.getChrom(), region.getStart() - 1, region.getEnd());
        TreeMap<String, List<MHapInfo>> mHapListMap = new TreeMap<>(); // mhap数据列表（通过barcode索引）
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

            if (!barcodeList.contains(mHapInfo.getBarcode())) {
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

    public Map<String, List<MHapInfo>> parseByRegionIndexByBarCodeAndStrand(Region region, String strand, List barcodeList) throws IOException {
        TabixReader.Iterator mhapIterator = tabixReader.query(region.getChrom(), region.getStart() - 1, region.getEnd());
        TreeMap<String, List<MHapInfo>> mHapInfoListMap = new TreeMap<>(); // mhap数据列表（通过barcode索引）
        String mHapLine = "";
        Integer lineCnt = 0;
        while((mHapLine = mhapIterator.next()) != null) {
            lineCnt++;
            if (lineCnt % 1000000 == 0) {
                log.info("Read " + region.getChrom() + " mhap " + lineCnt + " lines.");
            }

            MHapInfo mHapInfo = new MHapInfo();
            mHapInfo.setChrom(mHapLine.split("\t")[0]);
            mHapInfo.setStart(Integer.valueOf(mHapLine.split("\t")[1]));
            mHapInfo.setEnd(Integer.valueOf(mHapLine.split("\t")[2]));
            mHapInfo.setCpg(mHapLine.split("\t")[3]);
            mHapInfo.setCnt(Integer.valueOf(mHapLine.split("\t")[4]));
            mHapInfo.setStrand(mHapLine.split("\t")[5]);
            mHapInfo.setBarcode(mHapLine.split("\t")[6]);

            if (barcodeList.size() > 0 && !barcodeList.contains(mHapInfo.getBarcode())) {
                continue;
            } else {
                String key = mHapInfo.getBarcode() + mHapInfo.getStrand();
                List<MHapInfo> mHapListInMap = mHapInfoListMap.get(key);
                if (mHapListInMap != null && mHapListInMap.size() > 0) {
                    mHapListInMap.add(mHapInfo);
                } else {
                    mHapListInMap = new ArrayList<>();
                    mHapListInMap.add(mHapInfo);
                }
                mHapInfoListMap.put(key, mHapListInMap);
            }
        }
        return mHapInfoListMap;
    }

    /*
     A strand format: - mean null site, 1 mean methlation site, 0 mean unmethlation site, e.g. -----1111100000---111---
     */
    public ArrayList<String> matrixToStrangList(Integer[][] mHapMatrix) {
        ArrayList<String> strandList = new ArrayList<>();
        for (int i = 0; i < mHapMatrix.length; i++) {
            String strand = "";
            Integer[] row = mHapMatrix[i];
            for (int j = 0; j < row.length; j++) {
                if (mHapMatrix[i][j] == null) {
                    strand += "-";
                } else {
                    strand += mHapMatrix[i][j];
                }
            }
            strandList.add(strand);
        }
        return strandList;
    }

    public Integer[][] getMHapMatrix(Map<String, List<MHapInfo>> mHapInfoListMap, List<Integer> cpgPosList, List<Integer> cpgPosListInRegion) {
        Integer[][] mHapMatrix = new Integer[mHapInfoListMap.size()][cpgPosListInRegion.size()];

        for (int i = 0; i < cpgPosListInRegion.size(); i++) {
            int j = 0;
            Iterator<String> iterator = mHapInfoListMap.keySet().iterator();
            while (iterator.hasNext()) {
                List<MHapInfo> mHapInfoList = mHapInfoListMap.get(iterator.next());
                for (MHapInfo mHapInfo : mHapInfoList) {
                    if (cpgPosListInRegion.get(i) >= mHapInfo.getStart() && cpgPosListInRegion.get(i) <= mHapInfo.getEnd()) {
                        // 获取某个在区域内的位点在mhap的cpg中的相对位置
                        Integer pos = cpgPosList.indexOf(cpgPosListInRegion.get(i)) - cpgPosList.indexOf(mHapInfo.getStart());
                        for (int k = pos; k < mHapInfo.getCpg().length(); k++) {
                            if (i + k - pos < cpgPosListInRegion.size()) {
                                if (mHapInfo.getCpg().charAt(k) == '0') {
                                    mHapMatrix[j][i + k - pos] = 0;
                                } else {
                                    mHapMatrix[j][i + k - pos] = 1;
                                }
                            }
                        }
                    }
                }
                j++;
            }
        }
        return mHapMatrix;
    }

    public void close() {
        tabixReader.close();
    }
}
