package com.common;

import com.Convert;
import com.bean.*;
import com.itextpdf.awt.DefaultFontMapper;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfTemplate;
import com.itextpdf.text.pdf.PdfWriter;
import htsjdk.samtools.SRAFileReader;
import htsjdk.tribble.readers.TabixReader;
import org.apache.commons.math3.distribution.BinomialDistribution;
import org.jfree.chart.JFreeChart;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.io.*;
import java.util.*;
import java.util.List;
import java.util.zip.GZIPInputStream;

public class Util {
    public static final Logger log = LoggerFactory.getLogger(Util.class);

    public Region parseRegion(String regionStr) {
        Region region = new Region();
        region.setChrom(regionStr.split(":")[0]);
        region.setStart(Integer.valueOf(regionStr.split(":")[1].split("-")[0]));
        region.setEnd(Integer.valueOf(regionStr.split(":")[1].split("-")[1]));
        return region;
    }

    public List<Integer> parseCpgFile(String cpgPath, Region region) throws Exception {
        List<Integer> cpgPosList = new ArrayList<>();
        TabixReader tabixReader = new TabixReader(cpgPath);
        TabixReader.Iterator cpgIterator = tabixReader.query(region.getChrom(), region.getStart(), region.getEnd());
        String cpgLine = "";
        while((cpgLine = cpgIterator.next()) != null) {
            if (cpgLine.split("\t").length < 3) {
                continue;
            } else {
                cpgPosList.add(Integer.valueOf(cpgLine.split("\t")[1]));
            }
        }

        tabixReader.close();
        return cpgPosList;
    }

    public List<Integer> parseCpgFileWithShift(String cpgPath, Region region, Integer shift) throws Exception {
        List<Integer> cpgPosList = new ArrayList<>();
        TabixReader tabixReader = new TabixReader(cpgPath);
        TabixReader.Iterator cpgIterator = tabixReader.query(region.getChrom(), region.getStart() - shift, region.getEnd() + shift);
        String cpgLine = "";
        while((cpgLine = cpgIterator.next()) != null) {
            if (cpgLine.split("\t").length < 3) {
                continue;
            } else {
                cpgPosList.add(Integer.valueOf(cpgLine.split("\t")[1]));
            }
        }

        tabixReader.close();
        return cpgPosList;
    }

    public Map<String, List<Integer>> parseWholeCpgFile(String cpgPath) throws Exception {
        Map<String, List<Integer>> cpgPosListMap = new HashMap<>();

        List<Integer> cpgPosList = new ArrayList<>();
        TabixReader tabixReader = new TabixReader(cpgPath);
        String cpgLine = tabixReader.readLine();
        String lastChr = cpgLine.split("\t")[0];
        while(cpgLine != null && !cpgLine.equals("")) {
            if (cpgLine.split("\t").length < 3) {
                continue;
            } else {
                if (lastChr.equals(cpgLine.split("\t")[0])) {
                    cpgPosList.add(Integer.valueOf(cpgLine.split("\t")[1]));
                } else {
                    cpgPosListMap.put(lastChr, cpgPosList);
                    lastChr = cpgLine.split("\t")[0];
                    cpgPosList = new ArrayList<>();
                    cpgPosList.add(Integer.valueOf(cpgLine.split("\t")[1]));
                }
                cpgLine = tabixReader.readLine();
            }
        }
        cpgPosListMap.put(lastChr, cpgPosList);
        log.info("Read cpg file success.");

        tabixReader.close();
        return cpgPosListMap;
    }

    public List<String> parseBcFile(String bcFileName) throws Exception {
        List<String> barcodeList = new ArrayList<>();
        if (bcFileName != null && !bcFileName.equals("")) {
            File bcFile = new File(bcFileName);
            BufferedReader bufferedReader = new BufferedReader(new FileReader(bcFile));
            String bcLine = "";
            while ((bcLine = bufferedReader.readLine()) != null && !bcLine.equals("")) {
                barcodeList.add(bcLine.split("\t")[0]);
            }
        }
        return barcodeList;
    }

    public List<Region> parseBedFile(String bedFile) throws Exception {
        List<Region> regionList = new ArrayList<>();
        BufferedReader bufferedReader = new BufferedReader(new FileReader(new File(bedFile)));
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
        return regionList;
    }

    public Map<String, List<BedInfo>> parseBedFileToMap(String bedFile) throws Exception {
        BufferedReader bufferedReader = new BufferedReader(new FileReader(new File(bedFile)));
        String bedLine = "";
        Map<String, List<BedInfo>> bedInfoListMap = new HashMap<>();
        while ((bedLine = bufferedReader.readLine()) != null) {
            BedInfo bedInfo = new BedInfo();
            if (bedLine.split("\t").length >= 4) {
                bedInfo.setChrom(bedLine.split("\t")[0]);
                bedInfo.setStart(Integer.valueOf(bedLine.split("\t")[1]));
                bedInfo.setEnd(Integer.valueOf(bedLine.split("\t")[2]));
                bedInfo.setBarCode(bedLine.split("\t")[3]);

                if (bedInfoListMap.containsKey(bedInfo.getBarCode())) {
                    List<BedInfo> bedInfoList = bedInfoListMap.get(bedInfo.getBarCode());
                    bedInfoList.add(bedInfo);
                } else {
                    List<BedInfo> bedInfoList = new ArrayList<>();
                    bedInfoList.add(bedInfo);
                    bedInfoListMap.put(bedInfo.getBarCode(), bedInfoList);
                }
            }
        }
        return bedInfoListMap;
    }

    public BufferedWriter createOutputFile(String directory, String fileName) throws IOException {
        // create the output directory
        File outputDir = new File(directory);
        if (!outputDir.exists()){
            if (!outputDir.mkdirs()){
                log.error("create" + outputDir.getAbsolutePath() + "fail");
                return null;
            }
        }

        // create the output file
        File file = new File(directory + "/" + fileName);
        if (!file.exists()) {
            if (!file.createNewFile()) {
                log.error("create" + file.getAbsolutePath() + "fail");
                return null;
            }
        } else {
            FileWriter fileWriter =new FileWriter(file.getAbsoluteFile());
            fileWriter.write("");  //写入空
            fileWriter.flush();
            fileWriter.close();
        }
        FileWriter fileWriter = new FileWriter(file.getAbsoluteFile(), true);
        BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);

        return bufferedWriter;
    }

    public List<Integer> getcpgPosListInRegion(List<Integer> cpgPosList, Region region) throws Exception {
        Integer cpgStartPos = 0;
        Integer cpgEndPos = cpgPosList.size() - 1;
        for (int i = 0; i < cpgPosList.size(); i++) {
            if (cpgPosList.get(i) < region.getStart() && cpgPosList.get(i + 1) >= region.getStart()) {
                cpgStartPos = i + 1;
                break;
            }
        }
        for (int i = cpgStartPos; i < cpgPosList.size(); i++) {
            if (cpgPosList.get(i) > region.getEnd()) {
                cpgEndPos = i;
                break;
            } else if (cpgPosList.get(i) == region.getEnd()) {
                cpgEndPos = i + 1;
                break;
            }
        }
        List<Integer> cpgPosListInRegion = cpgPosList.subList(cpgStartPos, cpgEndPos);

        return cpgPosListInRegion;
    }

    public List<Region> splitRegionToSmallRegion(Region region, Integer splitSize, Integer shift) {
        List<Region> regionList = new ArrayList<>();
        if (region.getEnd() - region.getStart() > splitSize) {
            Integer regionNum = (region.getEnd() - region.getStart()) / splitSize + 1;
            for (int i = 0; i < regionNum; i++) {
                Region newRegion = new Region();
                newRegion.setChrom(region.getChrom());
                newRegion.setStart(region.getStart());
                if (region.getStart() + splitSize + shift - 1 <= region.getEnd()) {
                    newRegion.setEnd(region.getStart() + splitSize + shift - 1);
                } else {
                    newRegion.setEnd(region.getEnd());
                }
                regionList.add(newRegion);
                if (newRegion.getEnd() - shift + 1 < 1) {
                    region.setStart(newRegion.getEnd() + 1);
                } else {
                    region.setStart(newRegion.getEnd() - shift + 1);
                }
            }
        } else {
            regionList.add(region);
        }
        return regionList;
    }

    public Map<String, List<MHapInfo>> parseMhapFile(String mhapPath, List<String> barcodeList, String bcFile, Region region) throws IOException {
        TabixReader tabixReader = new TabixReader(mhapPath);
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

        tabixReader.close();
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
                        Integer pos = indexOfList(cpgPosList, 0, cpgPosList.size() - 1, cpgPosList.get(i)) -
                                indexOfList(cpgPosList, 0, cpgPosList.size() - 1, mHapInfo.getStart());

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

    public String cutReads(MHapInfo mHapInfo, List<Integer> cpgPosList, List<Integer> cpgPosListInRegion) {
        String cpg = mHapInfo.getCpg();
        Integer cpgStart = cpgPosListInRegion.get(0);
        Integer cpgEnd = cpgPosListInRegion.get(cpgPosListInRegion.size() - 1);
        int cpgStartPos = indexOfList(cpgPosList, 0, cpgPosList.size() - 1, cpgStart);
        int cpgEndPos = indexOfList(cpgPosList, 0, cpgPosList.size() - 1, cpgEnd);

        if (mHapInfo.getStart() <= cpgStart) {
            int startPos = cpgStartPos - indexOfList(cpgPosList, 0, cpgPosList.size() - 1, mHapInfo.getStart());
            if (mHapInfo.getEnd() <= cpgEnd) {
                cpg = cpg.substring(startPos);
            } else {
                int endPos = indexOfList(cpgPosList, 0, cpgPosList.size() - 1, mHapInfo.getEnd()) - cpgEndPos;
                cpg = cpg.substring(startPos, endPos + 1);
            }
        } else {
            if (mHapInfo.getEnd() > cpgEnd) {
                int endPos = cpgEndPos - indexOfList(cpgPosList, 0, cpgPosList.size() - 1, mHapInfo.getStart());
                cpg = cpg.substring(0, endPos + 1);
            }
        }

        return cpg;
    }

    public R2Info getR2FromMap(Map<String, List<MHapInfo>> mHapListMap, List<Integer> cpgPosList, Integer cpgPos1, Integer cpgPos2) {
        R2Info r2Info = new R2Info();
        Integer N00 = 0;
        Integer N01 = 0;
        Integer N10 = 0;
        Integer N11 = 0;
        if (cpgPos2 < cpgPos1) {
            Integer temp = cpgPos2;
            cpgPos2 = cpgPos1;
            cpgPos1 = temp;
        }

        if (mHapListMap == null || mHapListMap.size() < 1) {
            return null;
        }

        // filter the mhap line include 2 cpg position
        Iterator<String> iterator = mHapListMap.keySet().iterator();
        while (iterator.hasNext()) {
            String key = iterator.next();
            List<MHapInfo> mHapInfoList = mHapListMap.get(key);
            boolean cpgPos1Flag = false;
            Integer cpgPos1ExistIndex = null;
            boolean cpgPos2Flag = false;
            Integer cpgPos2ExistIndex = null;
            for (int i = 0; i < mHapInfoList.size(); i++) {
                MHapInfo mHapInfo = mHapInfoList.get(i);
                if (mHapInfo.getStart() <= cpgPos1 && mHapInfo.getEnd() >= cpgPos1) {
                    cpgPos1Flag = true;
                    cpgPos1ExistIndex = i;
                }
                if (mHapInfo.getStart() <= cpgPos2 && mHapInfo.getEnd() >= cpgPos2) {
                    cpgPos2Flag = true;
                    cpgPos2ExistIndex = i;
                }
            }
            if (cpgPos1Flag && cpgPos2Flag) {
                MHapInfo mHapInfoInCpgPos1 = mHapInfoList.get(cpgPos1ExistIndex);
                MHapInfo mHapInfoInCpgPos2 = mHapInfoList.get(cpgPos2ExistIndex);
                Integer pos1 = indexOfList(cpgPosList, 0, cpgPosList.size() - 1, cpgPos1)
                        - indexOfList(cpgPosList, 0, cpgPosList.size() - 1, mHapInfoInCpgPos1.getStart());
                Integer pos2 = indexOfList(cpgPosList, 0, cpgPosList.size() - 1, cpgPos2)
                        - indexOfList(cpgPosList, 0, cpgPosList.size() - 1, mHapInfoInCpgPos2.getStart());
                if (mHapInfoInCpgPos1.getCpg().charAt(pos1) == '0' && mHapInfoInCpgPos2.getCpg().charAt(pos2) == '0') {
                    N00 += 1;
                } else if (mHapInfoInCpgPos1.getCpg().charAt(pos1) == '0' && mHapInfoInCpgPos2.getCpg().charAt(pos2) == '1') {
                    N01 += 1;
                } else if (mHapInfoInCpgPos1.getCpg().charAt(pos1) == '1' && mHapInfoInCpgPos2.getCpg().charAt(pos2) == '0') {
                    N10 += 1;
                } else if (mHapInfoInCpgPos1.getCpg().charAt(pos1) == '1' && mHapInfoInCpgPos2.getCpg().charAt(pos2) == '1') {
                    N11 += 1;
                }
            }
        }

//        if ((N00 + N01 + N10 + N11) < r2Cov) {
//            return null;
//        }

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
        r2Info.setN10(N10);
        r2Info.setN11(N11);
        r2Info.setR2(r2);
        r2Info.setPvalue(pvalue);

        return r2Info;
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
        r2Info.setN10(N10);
        r2Info.setN11(N11);
        r2Info.setR2(r2);
        r2Info.setPvalue(pvalue);

        return r2Info;
    }

    public TreeMap<String, List<MHapInfo>> parseMhapFileIndexByBarCodeAndStrand(String mhapPath, List<String> barcodeList,
                                                                                String bcFile, Region region) throws IOException {
        TreeMap<String, List<MHapInfo>> mHapListIndexByBarCodeAndStrand = new TreeMap<>();
        TabixReader tabixReader = new TabixReader(mhapPath);
        TabixReader.Iterator mhapIterator = tabixReader.query(region.getChrom(), region.getStart() - 1, region.getEnd());
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

            if (bcFile != null && !barcodeList.contains(mHapInfo.getBarcode())) {
                continue;
            } else {
                String key = mHapInfo.getBarcode() + mHapInfo.getStrand();
                List<MHapInfo> mHapListInMap = mHapListIndexByBarCodeAndStrand.get(key);
                if (mHapListInMap != null && mHapListInMap.size() > 0) {
                    mHapListInMap.add(mHapInfo);
                } else {
                    mHapListInMap = new ArrayList<>();
                    mHapListInMap.add(mHapInfo);
                }
                mHapListIndexByBarCodeAndStrand.put(key, mHapListInMap);
            }
        }

        tabixReader.close();
        return mHapListIndexByBarCodeAndStrand;
    }

    // 保存为文件
    public void saveAsFile(JFreeChart chart, String outputPath, int width, int height) throws DocumentException, IOException {
        OutputStream outputStream = new BufferedOutputStream(new FileOutputStream(outputPath));
        // 设置文档大小
        Rectangle pagesize = new Rectangle(width, height);
        // 创建一个文档
        Document document = new Document(pagesize, 50, 50, 50, 50);
        // document.setPageSize(PageSize.A4); // 设置大小
        // document.setMargins(50, 50, 50, 50); // 设置边距
        // 创建writer，通过writer将文档写入磁盘
        PdfWriter pdfWriter = PdfWriter.getInstance(document, outputStream);
        // 打开文档，只有打开后才能往里面加东西
        document.open();
        // 加入统计图
        PdfContentByte pdfContentByte = pdfWriter.getDirectContent();
        PdfTemplate pdfTemplate = pdfContentByte.createTemplate(width, height);
        Graphics2D graphics2D = pdfTemplate.createGraphics(width, height, new DefaultFontMapper());
        Rectangle2D rectangle2D = new Rectangle2D.Double(0, 0, width, height);
        chart.draw(graphics2D, rectangle2D);
        graphics2D.dispose();
        pdfContentByte.addTemplate(pdfTemplate, 0, 0);
        // 关闭文档，才能输出
        document.close();
        pdfWriter.close();
    }

    public static boolean isNumeric(String str) {
        for (int i = str.length(); --i >= 0; ) {
            if (!Character.isDigit(str.charAt(i))) {
                return false;
            }
        }
        return true;
    }

    public Integer indexOfList(List<Integer> list, Integer start, Integer end, Integer findValue) {
        if(start <= end){
            Integer middle = (start + end) / 2;
            Integer middleValue = list.get(middle);//中间值
            if (findValue.equals(middleValue)) {
                //查找值等于中间值直接返回
                return  middle;
            } else if (findValue < middleValue) {
                //小于中间值，在中间值之前的数据中查找
                return indexOfList(list, start, middle - 1, findValue);
            } else {
                //大于中间值，在中间值之后的数据中查找
                return indexOfList(list, middle + 1, end, findValue);
            }
        }
        return -1;
    }
}
