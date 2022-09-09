package com.command;

import com.args.HemiMArgs;
import com.bean.MHapInfo;
import com.bean.MHapInfo;
import com.bean.Region;
import htsjdk.tribble.readers.TabixReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.*;
import java.util.stream.Collectors;

public class HemiM {
    public static final Logger log = LoggerFactory.getLogger(HemiM.class);

    HemiMArgs args = new HemiMArgs();
    List<Region> regionList = new ArrayList<>();
    private final Integer SHIFT = 500;

    public void hemiM(HemiMArgs hemiMArgs) throws Exception {
        log.info("command.HemiM start!");
        args = hemiMArgs;

        // 校验命令正确性
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

        // 创建文件
        String fileName = args.getTag() + ".hemi-methylation.txt";
        File file = new File(fileName);
        if (!file.exists()) {
            if (!file.createNewFile()) {
                log.error("create" + file.getAbsolutePath() + "fail");
                return;
            }
        } else {
            FileWriter fileWriter =new FileWriter(file.getAbsoluteFile());
            fileWriter.write("");  //写入空
            fileWriter.flush();
            fileWriter.close();
        }
        FileWriter fileWriter = new FileWriter(file.getAbsoluteFile(), true);
        BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);

        for (Region region : regionList) {
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
            List<Integer> cpgPosListInRegion = cpgPosList.subList(cpgStartPos, cpgEndPos);

            Iterator<String> iterator = mHapInfoListMap.keySet().iterator();
            while (iterator.hasNext()) {
                // get mHapInfoList and sorted
                List<MHapInfo> mHapInfoList = mHapInfoListMap.get(iterator.next());
                Map<Integer, List<MHapInfo>> r2ListMap = mHapInfoList.stream().collect(Collectors.groupingBy(MHapInfo::getStart));
                List<Map.Entry<Integer, List<MHapInfo>>> r2ListMapSorted = new ArrayList<Map.Entry<Integer, List<MHapInfo>>>(r2ListMap.entrySet());
                Collections.sort(r2ListMapSorted, new Comparator<Map.Entry<Integer, List<MHapInfo>>>() { //升序排序
                    public int compare(Map.Entry<Integer, List<MHapInfo>> o1, Map.Entry<Integer, List<MHapInfo>> o2) {
                        return o1.getValue().get(0).getChrom().compareTo(o2.getValue().get(0).getChrom())
                                + o1.getValue().get(0).getStart().compareTo(o2.getValue().get(0).getStart());
                    }
                });

                // whether has 2 strands
                Boolean plusFlag = false;
                Boolean minusFlag = false;
                for (int i = 0; i < mHapInfoList.size(); i++) {
                    plusFlag = mHapInfoList.get(i).getStrand().equals("+") ? true : plusFlag;
                    minusFlag = mHapInfoList.get(i).getStrand().equals("-") ? true : minusFlag;
                }

                if (plusFlag && minusFlag) {
                    Integer[] plusList = new Integer[cpgPosListInRegion.size()];
                    Integer[] minusList = new Integer[cpgPosListInRegion.size()];

                    for (int i = 0; i < mHapInfoList.size(); i++) {
                        MHapInfo mHapInfo = mHapInfoList.get(i);
                        String cpg = mHapInfo.getCpg();

                        Integer pos = cpgPosList.indexOf(cpgPosListInRegion.get(0)) - cpgPosList.indexOf(mHapInfo.getStart());
                        if (mHapInfo.getStrand().equals("+")) {
                            for (int j = 0; j < cpg.length(); j++) {
                                if (0 <= (j - pos) && (j - pos) < cpgPosListInRegion.size()) {
                                    plusList[j - pos] = Integer.valueOf(String.valueOf(cpg.charAt(j)));
                                }
                            }
                        } else {
                            for (int j = 0; j < cpg.length(); j++) {
                                if (0 <= (j - pos) && (j - pos) < cpgPosListInRegion.size()) {
                                    minusList[j - pos] = Integer.valueOf(String.valueOf(cpg.charAt(j)));
                                }
                            }
                        }
                    }

                    for (int i = 0; i < cpgPosListInRegion.size(); i++) {
                        if (plusList[i] != null && minusList[i] != null && plusList[i] != minusList[i]) {
                            String barCode = mHapInfoList.get(0).getBarcode();
                            if (plusList[i] == 0 && minusList[i] == 1) {
                                bufferedWriter.write(region.getChrom() + "\t" + cpgPosListInRegion.get(i) + "\t" +  "0" +
                                        "\t" + "+" + "\t" + barCode + "\n");
                                bufferedWriter.write(region.getChrom() + "\t" + cpgPosListInRegion.get(i) + "\t" +  "1" +
                                        "\t" + "-" + "\t" + barCode + "\n");
                            } else if (plusList[i] == 1 && minusList[i] == 0) {
                                bufferedWriter.write(region.getChrom() + "\t" + cpgPosListInRegion.get(i) + "\t" +  "1" +
                                        "\t" + "+" + "\t" + barCode + "\n");
                                bufferedWriter.write(region.getChrom() + "\t" + cpgPosListInRegion.get(i) + "\t" +  "0" +
                                        "\t" + "-" + "\t" + barCode + "\n");
                            }
                        }
                    }
                }
            }
        }

        bufferedWriter.close();

        log.info("command.HemiM end!");
    }

    private boolean checkArgs() {

        return true;
    }
}
