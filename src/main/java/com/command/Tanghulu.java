package com.command;

import com.itextpdf.awt.DefaultFontMapper;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfTemplate;
import com.itextpdf.text.pdf.PdfWriter;
import htsjdk.tribble.readers.TabixReader;
import com.bean.MHapInfo;
import com.bean.Region;
import com.args.TanghuluArgs;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtils;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.*;
import org.jfree.chart.labels.ItemLabelAnchor;
import org.jfree.chart.labels.ItemLabelPosition;
import org.jfree.chart.labels.StandardXYItemLabelGenerator;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.chart.ui.TextAnchor;
import org.jfree.data.Range;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.rewrite.CustomXYLineAndShapeRenderer;

import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Rectangle2D;
import java.io.*;
import java.text.DecimalFormat;
import java.util.*;
import java.util.List;

public class Tanghulu {
    public static final Logger log = LoggerFactory.getLogger(Tanghulu.class);

    TanghuluArgs args = new TanghuluArgs();
    Region region = new Region();
    private final Integer SHIFT = 500;

    public void tanghulu(TanghuluArgs tanghuluArgs) throws Exception {
        log.info("command.Tanghulu start!");
        args = tanghuluArgs;

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
        if (region.getEnd() - region.getStart() > args.getOutcut()) {
            log.error("The region is larger than " + args.getOutcut()
                    + ", it's not recommanded to do tanghulu plotting and system will exit right now...");
            return;
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
        TabixReader.Iterator cpgIterator = cpgTabixReader.query(region.getChrom(), region.getStart() - SHIFT, region.getEnd() + SHIFT);
        String cpgLine = "";
        while((cpgLine = cpgIterator.next()) != null) {
            if (cpgLine.split("\t").length < 3) {
                continue;
            } else {
                cpgPosList.add(Integer.valueOf(cpgLine.split("\t")[1]));
            }
        }

        boolean tanghuluResult = tanghulu(mHapInfoListMap, cpgPosList);
        if (!tanghuluResult) {
            log.error("tanghulu fail, please check the command.");
            return;
        }


        log.info("command.Tanghulu end!");
    }

    private boolean checkArgs() {

        return true;
    }

    private boolean tanghulu(Map<String, List<MHapInfo>> mHapInfoListMap, List<Integer> cpgPosList) throws Exception {
        XYSeriesCollection dataset = new XYSeriesCollection();
        List<MHapInfo> mHapInfoListAll = new ArrayList<>();

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
        Integer cpgStart = cpgPosList.get(cpgStartPos);
        Integer cpgEnd = cpgPosList.get(cpgEndPos - 1);

        // 创建数据集
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

                mHapInfoListAll.add(mHapInfoList.get(i));
            }

            Integer strandCnt = plusFlag && minusFlag ? 2 : 1;// 链数

            for (int i = 0; i < mHapInfoList.size(); i++) {
                MHapInfo mHapInfo = mHapInfoList.get(i);
                String cpg = mHapInfo.getCpg();

                // 截断不在region内的位点
                Integer start = mHapInfo.getStart();
                if (mHapInfo.getStart() < cpgStart) { // mhap.start在region.start左边
                    start = cpgStart;
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

                XYSeries allSeries = new XYSeries(i + mHapInfo.indexByRead() + "_" + cpg +
                        "/" + mHapInfo.getBarcode()); // 将mhap中的cpg和cnt存入，合并时显示合并read数用
                XYSeries cpgSeries = new XYSeries("cpg" + i + mHapInfo.indexByRead());
                XYSeries unCpgSeries = new XYSeries("unCpg" + i + mHapInfo.indexByRead());
                if (plusFlag && minusFlag) {
                    if (mHapInfo.getStrand().equals("+")) {
                        for (int j = 0; j < cpg.length(); j++) {
                            Integer pos = cpgPosList.indexOf(start); // mhap某行起点在cpgPosList的位置
                            allSeries.add(cpgPosList.get(pos + j), Integer.valueOf(rowNum + 1));
                            if (cpg.charAt(j) == '1') {
                                cpgSeries.add(cpgPosList.get(pos + j), Integer.valueOf(rowNum + 1));
                            } else {
                                unCpgSeries.add(cpgPosList.get(pos + j), Integer.valueOf(rowNum + 1));
                            }
                        }
                    } else {
                        for (int j = 0; j < cpg.length(); j++) {
                            Integer pos = cpgPosList.indexOf(start); // mhap某行起点在cpgPosList的位置
                            allSeries.add(cpgPosList.get(pos + j), Integer.valueOf(rowNum + 2));
                            if (cpg.charAt(j) == '1') {
                                cpgSeries.add(cpgPosList.get(pos + j), Integer.valueOf(rowNum + 2));
                            } else {
                                unCpgSeries.add(cpgPosList.get(pos + j), Integer.valueOf(rowNum + 2));
                            }
                        }
                    }
                } else {
                    for (int j = 0; j < cpg.length(); j++) {
                        Integer pos = cpgPosList.indexOf(start); // mhap某行起点在cpgPosList的位置
                        allSeries.add(cpgPosList.get(pos + j), Integer.valueOf(rowNum + 1));
                        if (cpg.charAt(j) == '1') {
                            cpgSeries.add(cpgPosList.get(pos + j), Integer.valueOf(rowNum + 1));
                        } else {
                            unCpgSeries.add(cpgPosList.get(pos + j), Integer.valueOf(rowNum + 1));
                        }
                    }
                }

                // 全部节点和甲基化节点交替加入
                dataset.addSeries(allSeries);
                dataset.addSeries(cpgSeries);
                dataset.addSeries(unCpgSeries);
            }

            rowNum += strandCnt;
        }

        XYSeries alignSeries = new XYSeries("Align series");
        for (Integer i = cpgPosList.indexOf(cpgStart); i < cpgPosList.indexOf(cpgEnd) + 1; i++) {
            alignSeries.add(cpgPosList.get(i), Integer.valueOf(0));
        }
        dataset.addSeries(alignSeries);

        // 绘制XY图
        String head = args.getRegion() + "(" + args.getMhapPath() + ")";
        JFreeChart jfreechart = ChartFactory.createXYLineChart(head, // 标题
                "Genomic position", // categoryAxisLabel （category轴，横轴，X轴标签）
                "", // valueAxisLabel（value轴，纵轴，Y轴的标签）
                dataset, // dataset
                PlotOrientation.VERTICAL,
                true, // legend
                false, // tooltips
                false); // URLs

        XYPlot xyPlot = jfreechart.getXYPlot( );
        xyPlot.setBackgroundPaint(Color.WHITE); // 背景色
        xyPlot.setDomainGridlinesVisible(false); // 不显示X轴网格线
        xyPlot.setRangeGridlinesVisible(false); // 不显示Y轴网格线
        xyPlot.setOutlineVisible(false); // 不显示数据区的边界线条

        // 画布大小设置
        Integer width = (region.getEnd() - region.getStart()) * 15;
        width = width > 14400 ? 14400 : width;
        Integer height = dataset.getSeriesCount() * 15;
        height = height > 14400 ? 14400 : height;
        //width = width < height ? height : width;

        CustomXYLineAndShapeRenderer xyLineAndShapeRenderer = new CustomXYLineAndShapeRenderer();
        Double rate = 0.98;
        xyLineAndShapeRenderer.setWidth(width * rate);
        xyLineAndShapeRenderer.setDefaultItemLabelsVisible(true);

        // 普通糖葫芦格式设置
        Double tanghuluSize = 20.0;
        Shape tanghulu = new Ellipse2D.Double(-tanghuluSize / 2, -tanghuluSize / 2, tanghuluSize, tanghuluSize);
        for (int i = 0; i < dataset.getSeriesCount() - 1; i++) { // 糖葫芦样式设置
            if (i % 3 == 0) { // 全部节点画空心圆
                xyLineAndShapeRenderer.setSeriesShape(i, tanghulu);
                xyLineAndShapeRenderer.setSeriesShapesFilled(i, false);
                MHapInfo mHapInfo = mHapInfoListAll.get(i / 3);
                if (mHapInfo.getStrand().equals("+")) {
                    xyLineAndShapeRenderer.setSeriesPaint(i, Color.BLACK);
                } else {
                    xyLineAndShapeRenderer.setSeriesPaint(i, Color.BLUE);
                }
            } else if (i % 3 == 1) { // 甲基化节点画实心圆
                xyLineAndShapeRenderer.setSeriesShape(i, tanghulu);
                xyLineAndShapeRenderer.setSeriesShapesFilled(i, true);
                xyLineAndShapeRenderer.setSeriesLinesVisible(i, false);
                MHapInfo mHapInfo = mHapInfoListAll.get(i / 3);
                if (mHapInfo.getStrand().equals("+")) {
                    xyLineAndShapeRenderer.setSeriesPaint(i, Color.BLACK);
                } else {
                    xyLineAndShapeRenderer.setSeriesPaint(i, Color.BLUE);
                }
            } else { // 非甲基化节点以白色填充
                Shape tanghulu1 = new Ellipse2D.Double(-tanghuluSize / 2, -tanghuluSize / 2, tanghuluSize - 2, tanghuluSize - 2);
                xyLineAndShapeRenderer.setSeriesShape(i, tanghulu1);
                xyLineAndShapeRenderer.setSeriesShapesFilled(i, true);
                xyLineAndShapeRenderer.setSeriesLinesVisible(i, false);
                xyLineAndShapeRenderer.setSeriesPaint(i, Color.WHITE);
            }
        }
        // cpg位点刻度形状设置
        xyLineAndShapeRenderer.setSeriesShape(dataset.getSeriesCount() - 1, tanghulu);
        xyLineAndShapeRenderer.setSeriesShapesFilled(dataset.getSeriesCount() - 1, true);
        xyLineAndShapeRenderer.setSeriesLinesVisible(dataset.getSeriesCount() - 1, true);
        xyLineAndShapeRenderer.setSeriesPaint(dataset.getSeriesCount() - 1, Color.GRAY);
        xyLineAndShapeRenderer.setSeriesItemLabelsVisible(dataset.getSeriesCount() - 1, true);
        xyLineAndShapeRenderer.setDefaultLegendShape(tanghulu);

        xyPlot.setRenderer(xyLineAndShapeRenderer);

        // cpg位点刻度数字设置
        XYItemRenderer xyItemRenderer = xyPlot.getRenderer();
        DecimalFormat decimalformat = new DecimalFormat("############"); // 显示数据值的格式
        xyItemRenderer.setDefaultItemLabelGenerator(new StandardXYItemLabelGenerator("{1}",
                decimalformat, decimalformat)); // 显示X轴的值（cpg位点位置）
        ItemLabelPosition itemLabelPosition = new ItemLabelPosition(ItemLabelAnchor.OUTSIDE6,
                TextAnchor.TOP_CENTER, TextAnchor.CENTER, -0.5D); // 显示数据值的位置
        xyItemRenderer.setSeriesPositiveItemLabelPosition(dataset.getSeriesCount() - 1, itemLabelPosition);
        xyItemRenderer.setSeriesItemLabelPaint(dataset.getSeriesCount() - 1, Color.GRAY);
        Double fontSize = 20.0;
        xyItemRenderer.setSeriesItemLabelFont(dataset.getSeriesCount() - 1, new Font("", Font.PLAIN, fontSize.intValue()));
        xyPlot.setRenderer(xyItemRenderer);


        // X轴设置
        ValueAxis domainAxis = xyPlot.getDomainAxis();
        domainAxis.setVisible(false);
//        domainAxis.setLowerMargin(0.1);
        domainAxis.setUpperMargin((1 - rate) * 10);

        // Y轴设置
        NumberAxis rangeAxis = (NumberAxis) xyPlot.getRangeAxis();
        NumberTickUnit numberTickUnit = new NumberTickUnit(1);
        rangeAxis.setTickUnit(numberTickUnit);
        Range yRange = new Range(-1, rowNum + 1);
        rangeAxis.setRange(yRange);
        //rangeAxis.setRangeWithMargins(yRange);

        // 输出到文件
        saveAsFile(jfreechart, args.getOutputFile(), width, height);

        return true;
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

}
