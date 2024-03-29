package com;

import com.args.TanghuluArgs;
import com.bean.MHapInfo;
import com.bean.Region;
import com.common.Util;
import com.rewrite.CustomXYLineAndShapeRenderer;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.NumberTickUnit;
import org.jfree.chart.axis.ValueAxis;
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

import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class Tanghulu {
    public static final Logger log = LoggerFactory.getLogger(Tanghulu.class);

    TanghuluArgs args = new TanghuluArgs();
    Util util = new Util();
    Region region = new Region();

    public void tanghulu(TanghuluArgs tanghuluArgs) throws Exception {
        log.info("Tanghulu start!");
        args = tanghuluArgs;

        // check the command
        boolean checkResult = checkArgs();
        if (!checkResult) {
            log.error("Checkargs fail, please check the command.");
            return;
        }

        // parse the region
        region = util.parseRegion(args.getRegion());
        if (region.getEnd() - region.getStart() > args.getOutcut()) {
            log.error("The region is larger than " + args.getOutcut()
                    + ", it's not recommanded to do tanghulu plotting and system will exit right now...");
            return;
        }

        // parse the barcodefile
        List<String> barcodeList = util.parseBcFile(args.getBcFile());

        // parse the mhap file
//        Map<String, List<MHapInfo>> mHapListMap = util.parseMhapFile(args.getMhapPath(), barcodeList,
//                args.getBcFile(), region);
        Map<String, List<MHapInfo>> mHapListMap = util.parseMhapFileIndexByBarCodeAndStrand(args.getMhapPath(), barcodeList, args.getBcFile(), region);
        if (mHapListMap.size() < 1) {
            return;
        }

        // parse the cpg file
        List<Integer> cpgPosList = util.parseCpgFileWithShift(args.getCpgPath(), region, 2000);

        boolean tanghuluResult = paintTanghulu(mHapListMap, cpgPosList, region);
        if (!tanghuluResult) {
            log.error("tanghulu fail, please check the command.");
            return;
        }

        log.info("Tanghulu end!");
    }

    private boolean checkArgs() {
        if (args.getMhapPath().equals("")) {
            log.error("mhapPath can not be null.");
            return false;
        }
        if (args.getCpgPath().equals("")) {
            log.error("cpgPath can not be null.");
            return false;
        }
        if (args.getRegion().equals("")) {
            log.error("region can not be null.");
            return false;
        }
        if (args.getOutcut() > 2000) {
            log.error("The region is larger than 2000, it's not recommand to do tanghulu plotting, please re-enter the outcut.");
            return false;
        }

        return true;
    }

    public boolean paintTanghulu(Map<String, List<MHapInfo>> mHapListMap, List<Integer> cpgPosList, Region region) throws Exception {
        XYSeriesCollection dataset = new XYSeriesCollection();
        List<MHapInfo> mHapInfoListAll = new ArrayList<>();

        // get cpg site list in region
        List<Integer> cpgPosListInRegion = util.getcpgPosListInRegion(cpgPosList, region);
        Integer cpgStart = cpgPosListInRegion.get(0);
        Integer cpgEnd = cpgPosListInRegion.get(cpgPosListInRegion.size() - 1);

        // 创建数据集
        Iterator<String> iterator = mHapListMap.keySet().iterator();
        Integer rowNum = 0;
        while (iterator.hasNext()) {
            String key = iterator.next();
            List<MHapInfo> mHapInfoList = mHapListMap.get(key);

            for (int i = 0; i < mHapInfoList.size(); i++) {
                MHapInfo mHapInfo = mHapInfoList.get(i);
                mHapInfoListAll.add(mHapInfo);

                String cpg = util.cutReads(mHapInfo, cpgPosList, cpgPosListInRegion);
                Integer start = mHapInfo.getStart() < cpgStart ? cpgStart : mHapInfo.getStart();

                XYSeries allSeries = new XYSeries(i + mHapInfo.indexByRead() + "_" + cpg +
                        "/" + mHapInfo.getBarcode()); // 将mhap中的cpg和cnt存入，合并时显示合并read数用
                XYSeries cpgSeries = new XYSeries("cpg" + i + mHapInfo.indexByRead());
                XYSeries unCpgSeries = new XYSeries("unCpg" + i + mHapInfo.indexByRead());
                for (int j = 0; j < cpg.length(); j++) {
                    Integer pos = cpgPosListInRegion.indexOf(start); // mhap某行起点在cpgPosList的位置
                    allSeries.add(cpgPosListInRegion.get(pos + j), Integer.valueOf(rowNum + 1));
                    if (cpg.charAt(j) == '1') {
                        cpgSeries.add(cpgPosListInRegion.get(pos + j), Integer.valueOf(rowNum + 1));
                    } else {
                        unCpgSeries.add(cpgPosListInRegion.get(pos + j), Integer.valueOf(rowNum + 1));
                    }
                }

                // 全部节点和甲基化节点交替加入
                dataset.addSeries(allSeries);
                dataset.addSeries(cpgSeries);
                dataset.addSeries(unCpgSeries);
            }
            rowNum ++;
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

        // output to file
        String outputFile = args.getTag() + ".tanghulu.pdf";
        util.saveAsFile(jfreechart, outputFile, width, height);

        return true;
    }
}
