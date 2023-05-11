package com;

import Jama.Matrix;
import com.File.*;
import com.args.CSNDiscoveryArgs;
import com.bean.MHapInfo;
import com.bean.Region;
import com.common.Util;
import org.apache.commons.math3.distribution.BinomialDistribution;
import org.apache.commons.math3.distribution.NormalDistribution;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class CSNDiscovery {
    public static final Logger log = LoggerFactory.getLogger(CSNDiscovery.class);

    Util util = new Util();
    CSNDiscoveryArgs args = new CSNDiscoveryArgs();
    List<Region> regionList = new ArrayList<>();
    MHapFile mHapFile;
    CpgFile cpgFile;
    BedFile bedFile;
    BarcodeFile barcodeFile;

    public void csnDiscovery(CSNDiscoveryArgs csnDiscoveryArgs) throws Exception {
        log.info("CSNDiscovery start!");
        args = csnDiscoveryArgs;
        mHapFile = new MHapFile(args.getmHapPath());
        cpgFile = new CpgFile(args.getCpgPath());
        bedFile = new BedFile(args.getBedPath());
        barcodeFile = new BarcodeFile(args.getBcFile());

        // check the command
        boolean checkResult = checkArgs();
        if (!checkResult) {
            log.error("Checkargs fail, please check the command.");
            return;
        }

        // get region list from bed file
        List<Region> regionList = bedFile.parseWholeFile();
        if (regionList.size() < 1) {
            log.info("The bed file is empty.");
            return;
        }

        // get bcFile
        List<String> barcodeList = barcodeFile.parseBcFile();
        if (barcodeList.size() < 1) {
            log.info("The barcode file is empty.");
            return;
        }

        // get the average MM matrix, x axis: cell, y axis: region
        double[][] mmMatrix = new double[regionList.size()][barcodeList.size()];
        for (int i = 0; i < regionList.size(); i++) {
            Region region = regionList.get(i);

            // parse the mhap file
            Map<String, List<MHapInfo>> mHapInfoListMap = mHapFile.parseByRegionIndexByBarCode(region, barcodeList);
            if (mHapInfoListMap.size() < 1) {
                log.info("MHap info list in " + region.toHeadString() + " is null!");
                continue;
            }

            // parse the cpg file
            List<Integer> cpgPosList = cpgFile.parseByRegionWithShift(region, 2000);
            if (cpgPosList.size() < 1) {
                log.info("Cpg pos list in " + region.toHeadString() + " is null!");
                continue;
            }
            List<Integer> cpgPosListInRegion = cpgFile.parseByRegion(region);
            if (cpgPosListInRegion.size() < 1) {
                log.info("Cpg pos list in " + region.toHeadString() + " is null!");
                continue;
            }

            // get mean methylation
            for (int j = 0; j < barcodeList.size(); j++) {
                String barCode = barcodeList.get(j);
                List<MHapInfo> mHapInfoList = mHapInfoListMap.get(barCode);
                if (mHapInfoList == null || mHapInfoList.size() < 1) {
                    continue;
                }
                double mm = getMM(mHapInfoList, cpgPosList, cpgPosListInRegion);
                mmMatrix[i][j] = mm;
            }
        }

//        double[][] rawData = new double[51][1018];
//        BufferedReader bufferedReader = new BufferedReader(new FileReader("logChumarker.txt"));
//        String line = bufferedReader.readLine();
//        Integer lineNum = 0;
//        while((line = bufferedReader.readLine()) != null){
//            String[] lineArray = Arrays.copyOfRange(line.split(" "), 1, line.split(" ").length);
//            rawData[lineNum] = Arrays.stream(lineArray).mapToDouble(Double::parseDouble).toArray();
//            lineNum++;
//        }
//        bufferedReader.close();

        Map<String, double[][]> upperlower = getUpperlower(mmMatrix);
        double[][] upper = upperlower.get("upper");
        double[][] lower = upperlower.get("lower");

        int[][] ndm = new int[regionList.size()][barcodeList.size()];
        for (int i = 0; i < barcodeList.size(); i++) {
            String barcode = barcodeList.get(i);
            Integer index = barcodeList.indexOf(barcode);
            int[][] csn = getCSN(mmMatrix, upper, lower, index);

            if (args.isNdmFlag()) {
                int[] ndmColumn = Arrays.stream(csn).mapToInt(item -> IntStream.of(item).sum()).toArray();
                for (int j = 0; j < ndmColumn.length; j++) {
                    ndm[j][i] = ndmColumn[j];
                }
            }

            log.info("Get csn of barcode:" + barcode + " end.");
        }

        log.info("CSNDiscovery end!");
    }

    private boolean checkArgs() {
        if (args.getmHapPath().equals("")) {
            log.error("mhapPath can not be null.");
            return false;
        }
        if (args.getCpgPath().equals("")) {
            log.error("cpgPath can not be null.");
            return false;
        }
        if (args.getBedPath().equals("")) {
            log.error("bedPath can not be null.");
            return false;
        }
        if (args.getBcFile().equals("")) {
            log.error("bcFile can not be null.");
            return false;
        }

        return true;
    }

    private double getMM(List<MHapInfo> mHapInfoList, List<Integer> cpgPosList, List<Integer> cpgPosListInRegion) {
        Integer mBase = 0; // 甲基化位点个数
        Integer tBase = 0; // 总位点个数
        for (MHapInfo mHapInfo : mHapInfoList) {
            String cpg = util.cutReads(mHapInfo, cpgPosList, cpgPosListInRegion);;
            tBase += cpg.length();
            for (int j = 0; j < cpg.length(); j++) {
                if (cpg.charAt(j) == '1') {
                    mBase++;
                }
            }
        }
        Double mm = mBase.doubleValue() / tBase.doubleValue();
        return mm;
    }

    private Map<String, double[][]> getUpperlower(double[][] rawData) {
        Map<String, double[][]> upperlower = new HashMap<>();
        Integer n1 = rawData.length;
        Integer n2 = rawData[0].length;
        double[][] upper = new double[n1][n2];
        double[][] lower = new double[n1][n2];

        for (Integer i = 0; i < n1; i++) {
            double[] rawDataLine = rawData[i].clone();
            int[] s2 = util.sortArray(rawDataLine);
            double[] s1 = rawDataLine; // rawDataClone is sorted
            Integer h = Math.toIntExact(Math.round(args.getBoxSize() / 2 * n2));
            Integer k = 0;
            while (k < n2) {
                Integer s = 0;
                while (k + s + 1 < n2) {
                    if (s1[k + s + 1] == s1[k]) {
                        s++;
                    } else {
                        break;
                    }
                }

                for (Integer index = k; index < k + s + 1; index++) {
                    if (s >= h) {
                        upper[i][s2[index]] = rawData[i][s2[k]];
                        lower[i][s2[index]] = rawData[i][s2[k]];
                    } else {
                        upper[i][s2[index]] = rawData[i][s2[n2 - 1 > k + s + h ? k + s + h : n2 - 1]];
                        lower[i][s2[index]] = rawData[i][s2[k - h > 0 ? k - h : 0]];
                    }
                }
                k = k + s + 1;
            }
        }

        upperlower.put("upper", upper);
        upperlower.put("lower", lower);
        return upperlower;
    }

    private int[][] getCSN(double[][] rawData, double[][] upper, double[][] lower, Integer index) {
        Integer n1 = rawData.length;
        Integer n2 = rawData[0].length;

        double[][] B = new double[n1][n2];
        double[] a = new double[n1];
        for (Integer j = 0; j < n2; j++) {
            for (Integer k = 0; k < n1; k++) {
                if (rawData[k][j] <= upper[k][index] && rawData[k][j] >= lower[k][index] && rawData[k][j] > 0) {
                    B[k][j] = 1;
                    a[k] += 1;
                }
            }
        }

        double[][] A = new double[1][n1];
        double[][] n2_A = new double[1][n1];
        double[] n2_a = new double[n1];
        for (Integer j = 0; j < n1; j++) {
            n2_a[j] = n2 - a[j];
        }
        A[0] = a;
        n2_A[0] = n2_a;

        Matrix matrixB = new Matrix(B);
        Matrix matrixB_T = matrixB.transpose();
        Matrix matrixA = new Matrix(A);
        Matrix matrixA_T = matrixA.transpose();
        Matrix matrixN2_a = new Matrix(n2_A);
        Matrix matrixN2_a_T = matrixN2_a.transpose();

        Matrix tempMatrix1 = matrixB.times(matrixB_T).times(n2).minus(matrixA_T.times(matrixA));
        Matrix tempMatrix2 = matrixA_T.times(matrixA).arrayTimes(matrixN2_a_T.times(matrixN2_a)).times(Double.valueOf(1) / Double.valueOf(n2 - 1));
        double[][] temp2Array = tempMatrix2.getArray();
        for (Integer row = 0; row < tempMatrix2.getRowDimension(); row++) {
            for (Integer col = 0; col < tempMatrix2.getColumnDimension(); col++) {
                if (tempMatrix2.get(row, col) == 0) {
                    temp2Array[row][col] = Double.MIN_NORMAL;
                } else {
                    temp2Array[row][col] = Math.sqrt(tempMatrix2.get(row, col));
                }
            }
        }

        Matrix tempMatrix = tempMatrix1.arrayRightDivide(tempMatrix2);
        double[][] tempArray = tempMatrix.getArray();
        int[][] csn = new int[n1][n1];
        NormalDistribution normalDistribution = new NormalDistribution();
        double level = normalDistribution.inverseCumulativeProbability(1 - args.getAlpha());
        for (Integer row = 0; row < tempMatrix.getRowDimension(); row++) {
            for (Integer col = 0; col < tempMatrix.getColumnDimension(); col++) {
                if (row == col) {
                    tempArray[row][col] = 0;
                }
                if (tempArray[row][col] > level) {
                    csn[row][col] = 1;
                }
            }
        }

        return csn;
    }
}
