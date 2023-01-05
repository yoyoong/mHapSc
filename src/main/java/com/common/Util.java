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
        for (int i = 0; i < cpgPosList.size() - 2; i++) {
            if (cpgPosList.get(i) < region.getStart() && cpgPosList.get(i + 1) >= region.getStart()) {
                cpgStartPos = i + 1;
                break;
            }
        }
        for (int i = cpgStartPos; i < cpgPosList.size() - 2; i++) {
            if (cpgPosList.get(i) > region.getEnd()) {
                cpgEndPos = i;
                break;
            } else if (cpgPosList.get(i).equals(region.getEnd())) {
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

            if (!bcFile.equals("") && !barcodeList.contains(mHapInfo.getBarcode())) {
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

    public String cutReads(MHapInfo mHapInfo, List<Integer> cpgPosList, List<Integer> cpgPosListInRegion) {
        String cpg = mHapInfo.getCpg();
        Integer cpgStart = cpgPosListInRegion.get(0);
        Integer cpgEnd = cpgPosListInRegion.get(cpgPosListInRegion.size() - 1);

        if (mHapInfo.getStart() < cpgStart) { // mhap.start在region.start左边
            if (mHapInfo.getEnd() < cpgEnd) { // mhap.end在region.end左边
                int pos = 0;
                for (int j = cpgPosList.indexOf(mHapInfo.getStart()); j < cpgPosList.indexOf(cpgStart); j++) {
                    pos++;
                }
                cpg = cpg.substring(pos);
            } else { // mhap.end在region.end右边
                int pos = cpgPosList.indexOf(mHapInfo.getStart());
                int pos1 = cpgPosList.indexOf(cpgStart);
                int pos2 = cpgPosList.indexOf(cpgEnd);
                cpg = cpg.substring(pos1 - pos, pos2 - pos);
            }
        } else { // mhap.start在region.start右边
            if (mHapInfo.getEnd() > cpgEnd) { // mhap.end在region.end右边
                int pos = 0;
                for (int j = cpgPosList.indexOf(mHapInfo.getStart()); j <= cpgPosList.indexOf(cpgEnd); j++) {
                    pos++;
                }
                cpg = cpg.substring(0, pos);
            }
        }

        return cpg;
    }

    public Map<Integer, Map<String, List<MHapInfo>>> getMhapListMapToCpg(Map<String, List<MHapInfo>> mHapListMap, List<Integer> cpgPosListInRegion) throws Exception {
        TreeMap<Integer, Map<String, List<MHapInfo>>> mHapIndexMapToCpg = new TreeMap<>();

        Iterator<String> iterator = mHapListMap.keySet().iterator();
        while (iterator.hasNext()) {
            String key = iterator.next();
            List<MHapInfo> mHapInfoList = mHapListMap.get(key);

            Integer cpgStartIndex = 0;
            Integer cpgEndIndex = 0;
            for (MHapInfo mHapInfo : mHapInfoList) {
                // get the cpg postions in mhap line
                while (cpgStartIndex < cpgPosListInRegion.size() - 1 && mHapInfo.getStart() > cpgPosListInRegion.get(cpgStartIndex)) {
                    cpgStartIndex++;
                }
                cpgEndIndex = cpgStartIndex;
                while (cpgEndIndex < cpgPosListInRegion.size() - 1 && cpgPosListInRegion.get(cpgEndIndex) < mHapInfo.getEnd()) {
                    cpgEndIndex++;
                }
                if (cpgPosListInRegion.get(cpgEndIndex) > mHapInfo.getEnd()) {
                    cpgEndIndex--;
                }

                for (int j = cpgStartIndex; j <= cpgEndIndex; j++) {
                    Integer cpgPos = cpgPosListInRegion.get(j);
                    Map<String, List<MHapInfo>> mHapListInMap = mHapIndexMapToCpg.get(cpgPos);
                    if (mHapListInMap != null && mHapListInMap.size() > 0) {
                        mHapListInMap.put(key, mHapInfoList);
                    } else {
                        mHapListInMap = new HashMap<>();
                        mHapListInMap.put(key, mHapInfoList);
                    }
                    mHapIndexMapToCpg.put(cpgPos, mHapListInMap);
                }
            }
        }

        return mHapIndexMapToCpg;
    }

    public R2Info getR2FromMap(Map<String, List<MHapInfo>> mHapListMap, List<Integer> cpgPosList,
                               Integer cpgPos1, Integer cpgPos2, Integer r2Cov) {
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
            try {
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
            } catch (Exception e) {
                log.info("Error in " + cpgPos1 + "-" + cpgPos2);
            }

        }

        if ((N00 + N01 + N10 + N11) < r2Cov) {
            return null;
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

            if (bcFile != null && !bcFile.equals("") && !barcodeList.contains(mHapInfo.getBarcode())) {
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
