package com.File;

import com.bean.MHapInfo;
import com.bean.Region;
import htsjdk.tribble.readers.TabixReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class MHapFile {
    public static final Logger log = LoggerFactory.getLogger(MHapFile.class);

    TabixReader tabixReader;

    public MHapFile(String mHapPath) throws IOException {
        tabixReader = new TabixReader(mHapPath);
    }

    public Map<String, List<MHapInfo>> parseByRegionAndBarcode(Region region, String strang, List barcodeList) throws IOException {
        TabixReader.Iterator mhapIterator = tabixReader.query(region.getChrom(), region.getStart() - 1, region.getEnd());
        TreeMap<String, List<MHapInfo>> mHapListMap = new TreeMap<>(); // mhap数据列表（通过barcode索引）
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

    public void close() {
        tabixReader.close();
    }
}
