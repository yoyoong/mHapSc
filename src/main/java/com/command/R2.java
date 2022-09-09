package com.command;

import com.args.R2Args;
import com.bean.BedInfo;
import com.bean.MHapInfo;
import com.bean.R2Info;
import com.bean.Region;
import com.itextpdf.awt.DefaultFontMapper;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfTemplate;
import com.itextpdf.text.pdf.PdfWriter;
import com.rewrite.CustomXYBlockRenderer;
import com.rewrite.CustomXYBlockRenderer2;
import htsjdk.tribble.readers.TabixReader;
import org.apache.commons.math3.distribution.BinomialDistribution;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.AxisLocation;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.NumberTickUnit;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.Plot;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.LookupPaintScale;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.chart.renderer.category.StandardBarPainter;
import org.jfree.chart.renderer.xy.XYBlockRenderer;
import org.jfree.chart.title.PaintScaleLegend;
import org.jfree.chart.ui.RectangleEdge;
import org.jfree.data.Range;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.xy.DefaultXYZDataset;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.io.*;
import java.util.List;
import java.util.*;
import java.util.stream.Collectors;

public class R2 {
    public static final Logger log = LoggerFactory.getLogger(R2.class);

    R2Args args = new R2Args();
    Region region = new Region();
    private final Integer SHIFT = 500;

    public void R2(R2Args r2Args) throws Exception {
        log.info("command.R2 start!");
        args = r2Args;

        // 校验命令正确性
        boolean checkResult = checkArgs();
        if (!checkResult) {
            log.error("Checkargs fail, please check the command.");
            return;
        }

        // 解析region
        region.setChrom(args.getRegion().split(":")[0]);
        region.setStart(Integer.valueOf(args.getRegion().split(":")[1].split("-")[0]));
        region.setEnd(Integer.valueOf(args.getRegion().split(":")[1].split("-")[1]));

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

        // 解析mhap文件
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

        // 解析cpg文件
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

        boolean getR2Result = getR2(mHapInfoListMap, cpgPosList);
        if (!getR2Result) {
            log.error("getR2 fail, please check the command.");
            return;
        }

        if (args.isMhapView()) {
            boolean getMhapViewResult = getMhapView(mHapInfoListMap, cpgPosList);
            if (!getMhapViewResult) {
                log.error("getMhapView fail, please check the command.");
                return;
            }
        }

        log.info("command.R2 end!");
    }

    private boolean checkArgs() {

        return true;
    }

    private boolean getR2(Map<String, List<MHapInfo>> mHapInfoListMap, List<Integer> cpgPosList) throws Exception {
        // 创建文件夹
        File outputDir = new File(args.getOutputDir());
        if (!outputDir.exists()){
            if (!outputDir.mkdirs()){
                log.error("create" + outputDir.getAbsolutePath() + "fail");
                return false;
            }
        }

        // 创建文件
        String r2FileName = args.getOutputDir() + "/" + args.getTag() + "_" + region.toFileString() + ".cpg_sites_rsquare.txt";
        File r2File = new File(r2FileName);
        if (!r2File.exists()) {
            if (!r2File.createNewFile()){
                log.error("create" + r2File.getAbsolutePath() + "fail");
                return false;
            }
        } else {
            FileWriter fileWriter =new FileWriter(r2File.getAbsoluteFile());
            fileWriter.write("");  //写入空
            fileWriter.flush();
            fileWriter.close();
        }
        FileWriter r2FileWriter = new FileWriter(r2File.getAbsoluteFile(), true);
        BufferedWriter r2BufferedWriter = new BufferedWriter(r2FileWriter);
        // 写入文件头部
        String head = "chr" + "\t" + "posi" + "\t" + "posj" + "\t" + "N00" + "\t" + "N01" + "\t"
                + "N10" + "\t" + "N11" + "\t" + "r2" + "\t" + "pvalue" + "\n";
        r2BufferedWriter.write(head);

        BufferedWriter longrangeBufferedWriter = new BufferedWriter(r2FileWriter);
        if (args.isLongrange()) {
            String longrangeFileName = args.getOutputDir() + "/" + args.getTag() + "_" + region.toFileString() + ".longrange.txt";
            File longrangeFile = new File(longrangeFileName);
            if (!longrangeFile.exists()) {
                if (!longrangeFile.createNewFile()){
                    log.error("create" + longrangeFile.getAbsolutePath() + "fail");
                    return false;
                }
            } else {
                FileWriter fileWriter =new FileWriter(longrangeFile.getAbsoluteFile());
                fileWriter.write("");  //写入空
                fileWriter.flush();
                fileWriter.close();
            }
            FileWriter longrangeWriter = new FileWriter(longrangeFile.getAbsoluteFile(), true);
            longrangeBufferedWriter = new BufferedWriter(longrangeWriter);
        }

        // 提取查询区域内的甲基化位点列表
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
        Integer cpgStart = cpgPosList.get(cpgStartPos);
        Integer cpgEnd = cpgPosList.get(cpgEndPos - 1);
        List<Integer> cpgPosListInRegion = cpgPosList.subList(cpgStartPos, cpgEndPos);

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

        for (int i = 0; i < cpgPosListInRegion.size(); i++) {
            for (int j = i + 1; j < cpgPosListInRegion.size(); j++) {
                Integer N00 = 0;
                Integer N01 = 0;
                Integer N10 = 0;
                Integer N11 = 0;

                for (int k = 0; k < rowNum; k++) {
                    if (cpgHpMatInRegion[k][i] != null && cpgHpMatInRegion[k][j] != null) {
                        if (cpgHpMatInRegion[k][i] == 0 && cpgHpMatInRegion[k][j] == 0) {
                            N00++;
                        } else if (cpgHpMatInRegion[k][i] == 0 && cpgHpMatInRegion[k][j] == 1) {
                            N01++;
                        } else if (cpgHpMatInRegion[k][i] == 1 && cpgHpMatInRegion[k][j] == 0) {
                            N10++;
                        } else if (cpgHpMatInRegion[k][i] == 1 && cpgHpMatInRegion[k][j] == 1) {
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

                r2BufferedWriter.write(region.getChrom() + "\t" + cpgPosListInRegion.get(i) +
                        "\t" + cpgPosListInRegion.get(j) + "\t" + N00 + "\t" + N01 + "\t" + N10 + "\t"  + N11 +
                        "\t" +String.format("%1.8f" , r2) + "\t" + pvalue + "\n");
                if (args.isLongrange()) {
                    longrangeBufferedWriter.write(region.getChrom() + "\t" + cpgPosListInRegion.get(i) + "\t"
                            + (cpgPosListInRegion.get(i) + 1) + "\t" + region.getChrom() + ":" + cpgPosListInRegion.get(j)
                            + "-" + (cpgPosListInRegion.get(j) + 1) + "," + String.format("%1.8f" , r2) + "\n");
                }
            }
        }


        r2BufferedWriter.close();
        if (args.isLongrange()) {
            longrangeBufferedWriter.close();
        }

        return true;
    }

    private boolean getMhapView(Map<String, List<MHapInfo>> mHapInfoListMap, List<Integer> cpgPosList) throws Exception {

        // 提取查询区域内的甲基化位点列表
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

        // 按甲基化比率递减排序
        Arrays.sort(cpgHpMatInRegion, new Comparator<Integer[]>() {
            public int compare(Integer[] a, Integer[] b){
                Integer cpgNumA = 0;
                Integer unCpgNumA = 0;
                Integer emptyNumA = 0;
                for (int i = 0; i < a.length; i++) {
                    if (a[i] != null) {
                        if (a[i] == 1) {
                            cpgNumA++;
                        } else {
                            unCpgNumA++;
                        }
                    } else {
                        emptyNumA++;
                    }
                }
                Integer cpgNumB = 0;
                Integer unCpgNumB = 0;
                Integer emptyNumB = 0;
                for (int i = 0; i < b.length; i++) {
                    if (b[i] != null) {
                        if (b[i] == 1) {
                            cpgNumB++;
                        } else {
                            unCpgNumB++;
                        }
                    } else {
                        emptyNumB++;
                    }
                }
//                Double cpgRateA = (double) cpgNumA / (cpgNumA + unCpgNumA);
//                Double cpgRateB = (double) cpgNumB / (cpgNumB + unCpgNumB);
//                return (int) ((cpgRateB - cpgRateA) * 100);
                return cpgNumB - cpgNumA;
            }
        });

        CategoryPlot cellCntPlot = createCellCntPlot(mHapInfoListMap, cpgPosListInRegion);
        CategoryPlot mmPlot = createMMPlot(cpgPosListInRegion, cpgHpMatInRegion);
        XYPlot whiteBlackPlot = createWhiteBlackPlot(cpgHpMatInRegion);
        XYPlot bedRegionPlot = createBedRegionPlot(cpgPosListInRegion);
        XYPlot R2HeatMapPlot = createR2HeatMapPlot(cpgHpMatInRegion);

        // 画布大小设置
        Integer width = cpgHpMatInRegion[0].length * 50;
        List<Plot> plotList = new ArrayList<>();
        List<Integer> heightList = new ArrayList<>();
        plotList.add(cellCntPlot);
        heightList.add(cpgHpMatInRegion[0].length * 7);
        plotList.add(mmPlot);
        heightList.add(cpgHpMatInRegion[0].length * 5);
        plotList.add(whiteBlackPlot);
        heightList.add(cpgHpMatInRegion[0].length * 20);
        plotList.add(bedRegionPlot);
        heightList.add(cpgHpMatInRegion[0].length * 3);
        plotList.add(R2HeatMapPlot);
        heightList.add(cpgHpMatInRegion[0].length * 15);

        // 输出到文件
        String outputPath = args.getOutputDir() + "/" + args.getTag() + "_" + region.toFileString() + ".cpg_sites_rsquare.pdf";
        saveAsFile(plotList, outputPath, width, heightList);

        return true;
    }

    private CategoryPlot createCellCntPlot(Map<String, List<MHapInfo>> mHapInfoListMap, List<Integer> cpgPosListInRegion) {
        // get cell count of every cpg site
        List<Integer> cellCntList = new ArrayList<>();
        for (int i = 0; i < cpgPosListInRegion.size(); i++) {
            Integer cellCnt = 0;
            Integer aa = 0;
            Iterator<String> iterator = mHapInfoListMap.keySet().iterator();
            while (iterator.hasNext()) {
                aa++;
                List<MHapInfo> mHapInfoList = mHapInfoListMap.get(iterator.next());

                for (int j = 0; j < mHapInfoList.size(); j++) {
                    MHapInfo mHapInfo = mHapInfoList.get(j);
                    if (mHapInfo.getStart() <= cpgPosListInRegion.get(i) && cpgPosListInRegion.get(i) <= mHapInfo.getEnd()) {
                        cellCnt += 1;
                        break;
                    }
                }
            }
            cellCntList.add(cellCnt);
        }

        // create the barchart dataset
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        Integer maxCellCnt = 0;
        for (int i = 0; i < cellCntList.size(); i++) {
            dataset.addValue(cellCntList.get(i), cpgPosListInRegion.get(i), "");
            maxCellCnt = cellCntList.get(i) > maxCellCnt ? cellCntList.get(i) : maxCellCnt;
        }

        // X axis
        CategoryAxis categoryAxis = new CategoryAxis();
        categoryAxis.setUpperMargin(0);
        categoryAxis.setLowerMargin(0);

        // Y axis
        NumberAxis valueAxis = new NumberAxis();
        valueAxis.setRange(new Range(1, maxCellCnt * 1.1));
        valueAxis.setVisible(true);
        valueAxis.setTickUnit(new NumberTickUnit(50));
        valueAxis.setTickLabelFont(new Font("", Font.PLAIN, cpgPosListInRegion.size() / 3));
        valueAxis.setLabel("cell count");
        valueAxis.setLabelFont(new Font("", Font.PLAIN, cpgPosListInRegion.size() / 2));

        // renderer
        BarRenderer barRenderer = new BarRenderer();
        barRenderer.setBarPainter(new StandardBarPainter()); // 设置柱子为平面图不是立体的
        barRenderer.setShadowVisible(false);
        barRenderer.setDrawBarOutline(false);
        barRenderer.setMaximumBarWidth(0.008);
        barRenderer.setDefaultItemLabelsVisible(true);
        for (int i = 0; i < dataset.getRowCount(); i++) {
            barRenderer.setSeriesPaint(i, new Color(70, 130, 180));
            barRenderer.setSeriesItemLabelsVisible(i, true);
        }

        // paint bar plot
        CategoryPlot categoryPlot = new CategoryPlot(dataset, categoryAxis, valueAxis, barRenderer);
        categoryPlot.setDomainGridlinesVisible(false);
        categoryPlot.setRangeGridlinesVisible(false);

        return categoryPlot;
    }

    private CategoryPlot createMMPlot(List<Integer> cpgPosListInRegion, Integer[][] cpgHpMatInRegion) {
        // get mean methylation of every cpg site
        List<Double> mmList = new ArrayList<>();
        for (int i = 0; i < cpgPosListInRegion.size(); i++) {
            Double mm = 0.0;
            Double cpgCnt = 0.0;
            Double unCpgCnt = 0.0;
            for (int j = 0; j < cpgHpMatInRegion.length; j++) {
                if (cpgHpMatInRegion[j][i] != null) {
                    if (cpgHpMatInRegion[j][i] == 1) {
                        cpgCnt++;
                    } else {
                        unCpgCnt++;
                    }
                }
            }
            mm = cpgCnt / (cpgCnt + unCpgCnt);
            mmList.add(mm);
        }

        // create the barchart dataset
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        for (int i = 0; i < mmList.size(); i++) {
            dataset.addValue(mmList.get(i), cpgPosListInRegion.get(i), "");
        }

        // X axis
        CategoryAxis categoryAxis = new CategoryAxis();
        categoryAxis.setUpperMargin(0);
        categoryAxis.setLowerMargin(0);

        // Y axis
        NumberAxis valueAxis = new NumberAxis();
        valueAxis.setRange(new Range(0, 1.1));
        valueAxis.setVisible(true);
        valueAxis.setTickUnit(new NumberTickUnit(0.5));
        valueAxis.setTickLabelFont(new Font("", Font.PLAIN, cpgPosListInRegion.size() / 3));
        valueAxis.setLabel("mean methylation");
        valueAxis.setLabelFont(new Font("", Font.PLAIN, cpgPosListInRegion.size() / 2));

        // renderer
        BarRenderer barRenderer = new BarRenderer();
        barRenderer.setBarPainter(new StandardBarPainter()); // 设置柱子为平面图不是立体的
        barRenderer.setShadowVisible(false);
        barRenderer.setDrawBarOutline(false);
        barRenderer.setMaximumBarWidth(0.008);
        barRenderer.setDefaultItemLabelsVisible(true);
        for (int i = 0; i < dataset.getRowCount(); i++) {
            barRenderer.setSeriesPaint(i, new Color(70, 130, 180));
            barRenderer.setSeriesItemLabelsVisible(i, true);
        }

        // paint bar plot
        CategoryPlot categoryPlot = new CategoryPlot(dataset, categoryAxis, valueAxis, barRenderer);
        categoryPlot.setDomainGridlinesVisible(false);
        categoryPlot.setRangeGridlinesVisible(false);

        return categoryPlot;
    }

    private XYPlot createWhiteBlackPlot(Integer[][] cpgHpMatInRegion) {

        // 创建数据集
        DefaultXYZDataset dataset = new DefaultXYZDataset();
        double x[] = new double[cpgHpMatInRegion.length * cpgHpMatInRegion[0].length];
        double y[] = new double[cpgHpMatInRegion.length * cpgHpMatInRegion[0].length];
        double z[] = new double[cpgHpMatInRegion.length * cpgHpMatInRegion[0].length];
        for (int i = 0; i < cpgHpMatInRegion.length; i++) {
            for (int j = 0; j < cpgHpMatInRegion[0].length; j++) {
                x[cpgHpMatInRegion[0].length * i + j] = j;
                y[cpgHpMatInRegion[0].length * i + j] = i;
                if (cpgHpMatInRegion[i][j] != null) {
                    if (cpgHpMatInRegion[i][j] == 0) {
                        z[cpgHpMatInRegion[0].length * i + j] = -1;
                    } else {
                        z[cpgHpMatInRegion[0].length * i + j] = 1;
                    }
                } else {
                    z[cpgHpMatInRegion[0].length * i + j] = 0;
                }
            }
        }
        double pos[][] = {x, y, z};
        dataset.addSeries( "Series" , pos);

        // xy轴
        NumberAxis xAxis = new NumberAxis();
        xAxis.setUpperMargin(0);
        xAxis.setLowerMargin(0);
        xAxis.setVisible(false);

        NumberAxis yAxis = new NumberAxis();
        yAxis.setTickUnit(new NumberTickUnit(cpgHpMatInRegion.length * 2)); // 不让它显示y轴
        yAxis.setRange(new Range(1, cpgHpMatInRegion.length));
        yAxis.setVisible(true);
        yAxis.setLabel("cpg");
        yAxis.setLabelFont(new Font("", Font.PLAIN, cpgHpMatInRegion[0].length / 2));

        LookupPaintScale paintScale = new LookupPaintScale(-1, 2, Color.black);
        paintScale.add(-1, Color.white);
        paintScale.add(0, new Color(220, 220, 220));
        paintScale.add(1, Color.black);

        // 绘制色块图
        XYPlot xyPlot = new XYPlot(dataset, xAxis, yAxis, new XYBlockRenderer());
        XYBlockRenderer xyBlockRenderer = new XYBlockRenderer();
        xyBlockRenderer.setPaintScale(paintScale);
        xyBlockRenderer.setBlockHeight(1.0f);
        xyBlockRenderer.setBlockWidth(1.0f);
        xyPlot.setRenderer(xyBlockRenderer);
        xyPlot.setDomainGridlinesVisible(false); // 不显示X轴网格线
        xyPlot.setRangeGridlinesVisible(false); // 不显示Y轴网格线
        xyPlot.setOutlineVisible(false);

        return xyPlot;
    }

    private XYPlot createBedRegionPlot(List<Integer> cpgPosListInRegion) throws IOException {
        File bedFile = new File(args.getBedFile());
        BufferedReader bufferedReader = new BufferedReader(new FileReader(bedFile));
        String bedLine = "";
        Map<String, List<BedInfo>> bedInfoListMap = new HashMap<>(); // mhap数据列表（通过barcode索引）
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

        // 创建数据集
        DefaultXYZDataset dataset = new DefaultXYZDataset();
        double x[] = new double[cpgPosListInRegion.size() * (bedInfoListMap.size() * 2 + 1)];
        double y[] = new double[cpgPosListInRegion.size() * (bedInfoListMap.size() * 2 + 1)];
        double z[] = new double[cpgPosListInRegion.size() * (bedInfoListMap.size() * 2 + 1)];
        List<String> labelList = new ArrayList<>();
        for (int i = 0; i < cpgPosListInRegion.size(); i++) {
            Iterator<String> iterator = bedInfoListMap.keySet().iterator();
            Integer cnt = 2;
            while (iterator.hasNext()) {
                List<BedInfo> bedInfoList = bedInfoListMap.get(iterator.next());
                for (int j = 0; j < bedInfoList.size(); j++) {
                    BedInfo bedInfo = bedInfoList.get(j);
                    if (bedInfo.getStart() <= cpgPosListInRegion.get(i) && cpgPosListInRegion.get(i) <= bedInfo.getEnd()) {
                        x[cpgPosListInRegion.size() * cnt + i] = i;
                        y[cpgPosListInRegion.size() * cnt + i] = cnt;
                        z[cpgPosListInRegion.size() * cnt + i] = 1;
                        labelList.add(bedInfo.getBarCode());
                    }
                }
                cnt += 2;
            }
        }

        double pos[][] = {x, y, z};
        dataset.addSeries( "Series" , pos);

        // xy轴
        NumberAxis xAxis = new NumberAxis();
        xAxis.setUpperMargin(0);
        xAxis.setLowerMargin(0);
        xAxis.setRange(new Range(0, cpgPosListInRegion.size()));
        xAxis.setVisible(false);

        NumberAxis yAxis = new NumberAxis();
        yAxis.setTickUnit(new NumberTickUnit(bedInfoListMap.size() * 3));
        yAxis.setRange(new Range(1, bedInfoListMap.size() * 2 + 1));
        yAxis.setVisible(true);
        yAxis.setLabel("bed file");
        yAxis.setLabelFont(new Font("", Font.PLAIN, cpgPosListInRegion.size() / 2));

        LookupPaintScale paintScale = new LookupPaintScale(0, 2, Color.WHITE);
        paintScale.add(0, Color.WHITE);
        paintScale.add(1, new Color(70, 130, 180));

        // 绘制色块图
        XYPlot xyPlot = new XYPlot(dataset, xAxis, yAxis, new XYBlockRenderer());
        CustomXYBlockRenderer2 xyBlockRenderer = new CustomXYBlockRenderer2();
        xyBlockRenderer.setPaintScale(paintScale);
        xyBlockRenderer.setBlockHeight(0.5f);
        xyBlockRenderer.setBlockWidth(1.0f);
        xyBlockRenderer.setxBlockNum(cpgPosListInRegion.size());
        xyBlockRenderer.setyBlockNum(bedInfoListMap.size() * 2 + 1);
        xyBlockRenderer.setSeriesItemLabelsVisible(0, true);
        xyBlockRenderer.setSeriesItemLabelFont(0, new Font("", Font.PLAIN,
                cpgPosListInRegion.size() * 3 / (bedInfoListMap.size() * 2 + 1)));
        xyBlockRenderer.setSeriesItemLabelPaint(0, Color.BLACK);
        xyBlockRenderer.setLabelList(labelList);
        xyPlot.setRenderer(xyBlockRenderer);
        xyPlot.setDomainGridlinesVisible(false); // 不显示X轴网格线
        xyPlot.setRangeGridlinesVisible(false); // 不显示Y轴网格线
        xyPlot.setOutlineVisible(false);

        return  xyPlot;
    }

    private XYPlot createR2HeatMapPlot(Integer[][] cpgHpMatInRegion) throws IOException {
        String r2FileName = args.getOutputDir() + "/" + args.getTag() + "_" + region.toFileString() + ".cpg_sites_rsquare.txt";
        File r2File = new File(r2FileName);
        BufferedReader bufferedReader = new BufferedReader(new FileReader(r2File));
        String r2Line = bufferedReader.readLine();
        List<R2Info> r2List = new ArrayList<>(); // R2 data List
        while ((r2Line = bufferedReader.readLine()) != null) {
            R2Info r2Info = new R2Info();
            if (!String.valueOf(r2Line.split("\t")[7]).equals("NaN")) {
                r2Info.setChrom(r2Line.split("\t")[0]);
                r2Info.setStart(Integer.valueOf(r2Line.split("\t")[1]));
                r2Info.setEnd(Integer.valueOf(r2Line.split("\t")[2]));
                r2Info.setN00(Integer.valueOf(r2Line.split("\t")[3]));
                r2Info.setN01(Integer.valueOf(r2Line.split("\t")[4]));
                r2Info.setN10(Integer.valueOf(r2Line.split("\t")[5]));
                r2Info.setN11(Integer.valueOf(r2Line.split("\t")[6]));
                r2Info.setR2(Double.valueOf(r2Line.split("\t")[7]));
                r2Info.setPvalue(Double.valueOf(r2Line.split("\t")[8]));
                r2List.add(r2Info);
            }
        }

        // R2 data List group by start and sorted
        Map<Integer, List<R2Info>> r2ListMap = r2List.stream().collect(Collectors.groupingBy(R2Info::getStart));
        List<Map.Entry<Integer, List<R2Info>>> r2ListMapSorted = new ArrayList<Map.Entry<Integer, List<R2Info>>>(r2ListMap.entrySet());
        Collections.sort(r2ListMapSorted, new Comparator<Map.Entry<Integer, List<R2Info>>>() { //升序排序
            public int compare(Map.Entry<Integer, List<R2Info>> o1, Map.Entry<Integer, List<R2Info>> o2) {
                return o1.getKey().compareTo(o2.getKey());
            }
        });

        // 创建数据集
        DefaultXYZDataset dataset = new DefaultXYZDataset();
        double x[] = new double[r2List.size()];
        double y[] = new double[r2List.size()];
        double z[] = new double[r2List.size()];
        int next = 0;
        for (int i = 0; i < r2ListMapSorted.size(); i++) {
            List<R2Info> r2InfoList = r2ListMapSorted.get(i).getValue();
            for (int j = 0; j < r2InfoList.size(); j++) {
                R2Info r2Info = r2InfoList.get(j);
                x[next + j] = i;
                y[next + j] = j;
                z[next + j] = 255 + r2Info.getR2() * 255;
            }
            next += r2InfoList.size();
        }
        double pos[][] = {x , y , z};
        dataset.addSeries( "Series" , pos);

        // xy轴
        NumberAxis xAxis = new NumberAxis();
        xAxis.setVisible(false);
        NumberAxis yAxis = new NumberAxis();
        yAxis.setTickUnit(new NumberTickUnit(cpgHpMatInRegion.length * 2)); // 不让它显示y轴
        yAxis.setRange(new Range(1, cpgHpMatInRegion.length));
        yAxis.setVisible(true);
        yAxis.setLabel("R2 HeatMap");
        yAxis.setLabelFont(new Font("", Font.PLAIN, cpgHpMatInRegion[0].length / 2));

        // 颜色定义
        LookupPaintScale paintScale = new LookupPaintScale(0, 510, Color.black);
        for (int i = 0; i < 255; i++) {
            paintScale.add(i, new Color(i, i, 255));
        }
        for (int i = 255; i < 510; i++) {
            paintScale.add(i, new Color(255, 510 - i, 510 - i));
        }

        XYPlot xyPlot = new XYPlot(dataset, xAxis, yAxis, new CustomXYBlockRenderer());
        CustomXYBlockRenderer xyBlockRenderer = new CustomXYBlockRenderer();
        xyBlockRenderer.setPaintScale(paintScale);
        xyBlockRenderer.setBlockHeight(1.0f);
        xyBlockRenderer.setBlockWidth(1.0f);
        xyBlockRenderer.setBlockNum(r2ListMapSorted.size());
        xyPlot.setRenderer(xyBlockRenderer);
        xyPlot.setDomainGridlinesVisible(false); // 不显示X轴网格线
        xyPlot.setRangeGridlinesVisible(false); // 不显示Y轴网格线
        xyPlot.setOutlineVisible(false);

        return  xyPlot;
    }

    // 保存为文件
    public void saveAsFile(List<Plot> plotList, String outputPath, Integer width, List<Integer> heightList)
            throws FileNotFoundException, DocumentException {
        width = width > 14400 ? 14400 : width;
        Integer sumHeight = 0;
        for (int i = 0; i < heightList.size(); i++) {
            sumHeight += heightList.get(i);
        }
        if (sumHeight > 14400) {
            width = width / sumHeight * 14400;
            sumHeight = 14400;
        }

        OutputStream outputStream = new BufferedOutputStream(new FileOutputStream(outputPath));
        // 设置文档大小
        com.itextpdf.text.Rectangle pagesize = new com.itextpdf.text.Rectangle(width, sumHeight);
        // 创建一个文档
        Document document = new Document(pagesize, 50, 50, 50, 50);
        // 创建writer，通过writer将文档写入磁盘
        PdfWriter pdfWriter = PdfWriter.getInstance(document, outputStream);
        // 打开文档，只有打开后才能往里面加东西
        document.open();
        // 加入统计图
        PdfContentByte pdfContentByte = pdfWriter.getDirectContent();
        PdfTemplate pdfTemplate = pdfContentByte.createTemplate(width, sumHeight);
        Graphics2D graphics2D = pdfTemplate.createGraphics(width, sumHeight, new DefaultFontMapper());

        Integer nextHeight = 0;
        for (int i = 0; i < plotList.size(); i++) {
            JFreeChart jFreeChart = new JFreeChart("", null, plotList.get(i), false);
            if (i == 0) {
                jFreeChart = new JFreeChart(region.toHeadString(), new Font("", Font.PLAIN, sumHeight / 30), plotList.get(i), false);
            } else if (i == plotList.size() - 1) {
                // 颜色定义
                LookupPaintScale paintScale = new LookupPaintScale(-1, 1, Color.black);
                for (double j = -1; j < 0; j += 0.01) {
                    paintScale.add(j, new Color((int) (255 + j * 255), (int) (255 + j * 255), 255));
                }
                for (double j = 0; j < 1; j += 0.01) {
                    paintScale.add(j, new Color(255, (int) (255 - j * 255), (int) (255 - j * 255)));
                }

                // 颜色示意图
                PaintScaleLegend paintScaleLegend = new PaintScaleLegend(paintScale, new NumberAxis());
                paintScaleLegend.setStripWidth(30);
                paintScaleLegend.setPosition(RectangleEdge.RIGHT);
                paintScaleLegend.setAxisLocation(AxisLocation.BOTTOM_OR_RIGHT);
                paintScaleLegend.setMargin(heightList.get(i) * 1 / 3, 0, heightList.get(i) * 1 / 3, 0);
                jFreeChart.addSubtitle(paintScaleLegend);
            }
            jFreeChart.setBackgroundPaint(Color.WHITE);

            Rectangle2D rectangle2D0 = new Rectangle2D.Double(0, nextHeight, width, heightList.get(i));
            jFreeChart.draw(graphics2D, rectangle2D0);
            pdfContentByte.addTemplate(pdfTemplate, 0, 0);
            nextHeight += heightList.get(i);
        }
        graphics2D.dispose();

        // 关闭文档，才能输出
        document.close();
        pdfWriter.close();
    }
}
