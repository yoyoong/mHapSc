package com;

import com.File.CpgFile;
import com.args.ConvertArgs;
import com.bean.*;
import com.common.Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.file.Files;
import java.util.*;
import java.util.zip.GZIPInputStream;

public class Convert {
    public static final Logger log = LoggerFactory.getLogger(Convert.class);

    ConvertArgs args = new ConvertArgs();
    Util util = new Util();
    long totalLineCnt = 0l;
    long completeDLineCnt = 0l;
    CpgFile cpgFile;
    BufferedWriter bufferedWriter;

    public void convert(ConvertArgs convertArgs) throws Exception {
        log.info("Convert start!");
        args = convertArgs;
        cpgFile = new CpgFile(args.getCpgPath());

        // check the command
        boolean checkResult = checkArgs();
        if (!checkResult) {
            log.error("Checkargs fail, please check the command.");
            return;
        }

        // create the output directory and file
        bufferedWriter = util.createOutputFile(args.getOutputDir(), args.getTag() + ".mhap");

        if (args.isNanopolishFlag()) {
            boolean convertNanopolishResult = convertNanopolish();
            if (!convertNanopolishResult) {
                log.error("convertNanopolish fail, please check the command.");
                return;
            }
        } else if (args.isAllcFlag()) {
            boolean convertAllcResult = convertAllc();
            if (!convertAllcResult) {
                log.error("convertAllc fail, please check the command.");
                return;
            }
        } else{
            boolean convertScResult = convertSc();
            if (!convertScResult) {
                log.error("convertSc fail, please check the command.");
                return;
            }
        }

        cpgFile.close();
        bufferedWriter.close();
        log.info("Convert end!");
    }

    private boolean checkArgs() {
        if (args.getInputPath().equals("")) {
            log.error("The bed file cannot be empty!");
        }
        if (args.getCpgPath().equals("")) {
            log.error("The cpg file cannot be empty!");
        }
        return true;
    }

    private boolean convertNanopolish() throws Exception {
        // parse whole cpg file
        Map<String, List<Integer>> cpgPostListMap = cpgFile.parseWholeGroupByChrom();
        if (cpgPostListMap.size() < 1) {
            log.error("Cpg file is null, please check!");
            return false;
        }

        FileInputStream fileInputStream = new FileInputStream(args.getInputPath());
        GZIPInputStream gzipInputStream = new GZIPInputStream(fileInputStream);
        InputStreamReader inputStreamReader = new InputStreamReader(gzipInputStream);
        BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
        String nanopolishLine = bufferedReader.readLine();
        List<Integer> cpgPostList = new ArrayList<>();
        String lastChrom = "";
        String thisChrom = "";
        String lastStrand = "";
        String thisStrand = "";
        String lastBarcode = "";
        String thisBarcode = "";
        List<NanopolishInfo> nanopolishList = new ArrayList<>(); // nanopolish line list from last extend end to this extend end
        String cpgStr = ""; // cpg string from last extend end to this extend end
        boolean extendFlag = true; // whether extend to this line
        while (nanopolishLine != null && !nanopolishLine.equals("")) {
            nanopolishLine = bufferedReader.readLine();
            if(nanopolishLine == null || nanopolishLine.equals("") || nanopolishLine.split("\t").length < 11) {
                continue;
            }
            NanopolishInfo nanopolishInfo = new NanopolishInfo();
            nanopolishInfo.setChrom(nanopolishLine.split("\t")[0]);
            nanopolishInfo.setStrand(nanopolishLine.split("\t")[1]);
            nanopolishInfo.setStart(Integer.valueOf(nanopolishLine.split("\t")[2]) + 1);
            nanopolishInfo.setEnd(Integer.valueOf(nanopolishLine.split("\t")[3]) + 1);
            String[] readName = nanopolishLine.split("\t")[4].split("-");
            nanopolishInfo.setReadName(readName[readName.length - 1]);
            nanopolishInfo.setLogLikMethylated(Double.valueOf(nanopolishLine.split("\t")[5]));
            nanopolishInfo.setNumMotifs(Integer.valueOf(nanopolishLine.split("\t")[9]));
            nanopolishInfo.setSequence(nanopolishLine.split("\t")[10]);

            // check this chrom whether equal with last chrom
            thisChrom = nanopolishInfo.getChrom();
            if (!lastChrom.equals("") && !thisChrom.equals(lastChrom)) {
                cpgPostList = cpgPostListMap.get(thisChrom);
                extendFlag = false;
            } else if (lastChrom.equals("")) {
                cpgPostList = cpgPostListMap.get(thisChrom);
            }
            if (cpgPostList == null || cpgPostList.size() < 1) {
                continue;
            }
            // filter the position do not exist in cpg posstion reference
            if (util.indexOfList(cpgPostList, 0, cpgPostList.size() - 1, nanopolishInfo.getStart()) < 0) {
                continue;
            }

            // check this readname whether equal with last readname
            thisBarcode = nanopolishInfo.getReadName();
            if (!lastBarcode.equals("") && !thisBarcode.equals(lastBarcode)) {
                extendFlag = false;
            }

            // check this strand whether equal with last strand
            thisStrand = nanopolishInfo.getStrand();
            if (!lastStrand.equals("") && !thisStrand.equals(lastStrand)) {
                extendFlag = false;
            }

            // check this cpg position whether follow with nanopolishList the last cpg position
            Integer thisStartCpgIndex = util.indexOfList(cpgPostList, 0, cpgPostList.size() - 1, nanopolishInfo.getStart());
            Integer lastEndCpgIndex = thisStartCpgIndex;
            if (nanopolishList.size() > 0) {
                lastEndCpgIndex = util.indexOfList(cpgPostList, 0, cpgPostList.size() - 1, nanopolishList.get(nanopolishList.size() - 1).getStart());
            }
            if (thisStartCpgIndex - lastEndCpgIndex > 1) {
                extendFlag = false;
            }

            if (!extendFlag) { // if not extend, print nanopolishList to mhap file
                if (nanopolishList.size() > 0) {
//                    log.info(lastChrom + "\t" + nanopolishList.get(0).getStart()+ "\t" + nanopolishList.get(nanopolishList.size() - 1).getStart() + "\t" +
//                            "\t" + cpgStr + "\t" + lastStrand + "\t" + "1" + "\t" + lastBarcode + "\n");
                    bufferedWriter.write(lastChrom + "\t" + nanopolishList.get(0).getStart()+ "\t" + nanopolishList.get(nanopolishList.size() - 1).getStart() +
                            "\t" + cpgStr + "\t" + "1" + "\t" + lastStrand + "\t" + lastBarcode + "\n");
                }
                nanopolishList = new ArrayList<>();
                cpgStr = "";
                extendFlag = true;
            }
            //log.info(nanopolishLine);

            // add this nanopolish line to next extend nanopolishList
            if (nanopolishInfo.getLogLikMethylated() >= 2 || nanopolishInfo.getLogLikMethylated() <= -2) {
                Integer firstCGIndex = 0;
                for (int i = 0; i < nanopolishInfo.getNumMotifs(); i++) {
                    Integer pos = nanopolishInfo.getSequence().indexOf("CG", firstCGIndex);
                    firstCGIndex = pos + 1;
                    NanopolishInfo newNanopolishInfo = new NanopolishInfo();
                    newNanopolishInfo.setChrom(nanopolishInfo.getChrom());
                    newNanopolishInfo.setStrand(nanopolishInfo.getStrand());
                    newNanopolishInfo.setStart(nanopolishInfo.getStart() + pos);
                    newNanopolishInfo.setEnd(nanopolishInfo.getEnd());
                    newNanopolishInfo.setReadName(nanopolishInfo.getReadName());
                    newNanopolishInfo.setLogLikMethylated(nanopolishInfo.getLogLikMethylated());
                    newNanopolishInfo.setNumMotifs(1);
                    newNanopolishInfo.setSequence(nanopolishInfo.getSequence());

                    if (nanopolishInfo.getLogLikMethylated() >= 2) {
                        cpgStr += "1";
                    } else if (nanopolishInfo.getLogLikMethylated() <= -2) {
                        cpgStr += "0";
                    }
                    nanopolishList.add(nanopolishInfo);
                }
            }

            lastChrom = thisChrom;
            lastBarcode = thisBarcode;
            lastStrand = thisStrand;
        }

        if (nanopolishList.size() > 0) { // print the last nanopolishList to mhap file
            bufferedWriter.write(lastChrom + "\t" + nanopolishList.get(0).getStart()+ "\t" + nanopolishList.get(nanopolishList.size() - 1).getStart() +
                    "\t" + cpgStr + "\t" + "1" + "\t" + lastStrand + "\t" + lastBarcode + "\n");
        }

        return true;
    }

    private boolean convertAllc() throws Exception {
        Map<String, List<Integer>> cpgPostListMap = cpgFile.parseWholeGroupByChrom();
        if (cpgPostListMap.size() < 1) {
            log.error("Cpg file is null, please check!");
            return false;
        }

        FileInputStream fileInputStream = new FileInputStream(args.getInputPath());
        GZIPInputStream gzipInputStream = new GZIPInputStream(fileInputStream);
        InputStreamReader inputStreamReader = new InputStreamReader(gzipInputStream);
        BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
        String allcLine = bufferedReader.readLine();
        List<Integer> cpgPostList = new ArrayList<>();
        String barCode = args.getInputPath().substring(0, args.getInputPath().indexOf("_"));
        String lastChrom = "";
        String thisChrom = "";
        String lastStrand = "";
        String thisStrand = "";
        String cpgStr = "";
        List<AllcInfo> allcInfoList = new ArrayList<>(); // llcInfo line list from last extend end to this extend end
        while (allcLine != null && !allcLine.equals("")) {
            allcLine = bufferedReader.readLine();
            if (allcLine == null || allcLine.equals("") || allcLine.split("\t").length < 6) {
                continue;
            }
            AllcInfo allcInfo = new AllcInfo();
            allcInfo.setChromosome(allcLine.split("\t")[0]);
            allcInfo.setStrand(allcLine.split("\t")[2]);
            if (allcInfo.getStrand().equals("+")) {
                allcInfo.setPosition(Integer.valueOf(allcLine.split("\t")[1]));
            } else {
                allcInfo.setPosition(Integer.valueOf(allcLine.split("\t")[1]) - 1);
            }
            allcInfo.setSequenceContext(allcLine.split("\t")[3]);
            allcInfo.setMc(Integer.valueOf(allcLine.split("\t")[4]));
            allcInfo.setCov(Integer.valueOf(allcLine.split("\t")[5]));
            allcInfo.setMethylated(Integer.valueOf(allcLine.split("\t")[6]));

            // check this chrom whether equal with last chrom
            thisChrom = allcInfo.getChromosome();
            if (!lastChrom.equals("") && !thisChrom.equals(lastChrom)) {
                cpgPostList = cpgPostListMap.get(thisChrom);
                if (allcInfoList.size() > 0) {
                    bufferedWriter.write(writeMHap(allcInfoList, lastChrom, cpgStr, lastStrand, barCode).print());
                }
                allcInfoList = new ArrayList<>();
                cpgStr = "";
            } else if (lastChrom.equals("")) {
                cpgPostList = cpgPostListMap.get(thisChrom);
            }
            lastChrom = thisChrom;
            if (cpgPostList == null || cpgPostList.size() < 1) {
                continue;
            }

            // check this strand whether equal with last strand
            thisStrand = allcInfo.getStrand();
            if (!lastStrand.equals("") && !thisStrand.equals(lastStrand)) {
                if (allcInfoList.size() > 0) {
                    bufferedWriter.write(writeMHap(allcInfoList, lastChrom, cpgStr, lastStrand, barCode).print());
                }
                allcInfoList = new ArrayList<>();
                cpgStr = "";
            }
            lastStrand = thisStrand;

            // check this cpg position whether follow with nanopolishList the last cpg position
            Integer thisStartCpgIndex = util.indexOfList(cpgPostList, 0, cpgPostList.size() - 1, allcInfo.getPosition());
            if (thisStartCpgIndex < 0) {
                continue;
            }
            Integer lastEndCpgIndex = thisStartCpgIndex;
            if (allcInfoList.size() > 0) {
                lastEndCpgIndex = util.indexOfList(cpgPostList, 0, cpgPostList.size() - 1, allcInfoList.get(allcInfoList.size() - 1).getPosition());
            }
            if (lastEndCpgIndex > 0 && thisStartCpgIndex - lastEndCpgIndex > 1) {
                if (allcInfoList.size() > 0) {
                    bufferedWriter.write(writeMHap(allcInfoList, lastChrom, cpgStr, lastStrand, barCode).print());
                }
                allcInfoList = new ArrayList<>();
                cpgStr = "";
            }

            if (((double) allcInfo.getMc()) / ((double) allcInfo.getCov()) <= 0.1) {
                cpgStr += "0";
            } else if (((double) allcInfo.getMc()) / ((double) allcInfo.getCov()) >= 0.9) {
                cpgStr += "1";
            } else {
                continue;
            }
            allcInfoList.add(allcInfo);
        }

        if (allcInfoList.size() > 0) { // print the last allcInfoList to mhap file
            bufferedWriter.write(writeMHap(allcInfoList, lastChrom, cpgStr, lastStrand, barCode).print());
        }

        return true;
    }

    private MHapInfo writeMHap(List<AllcInfo> allcInfoList, String chrom, String cpg, String strand, String badCode) {
        MHapInfo mHapLine = new MHapInfo();
        mHapLine.setChrom(chrom);
        mHapLine.setStart(allcInfoList.get(0).getPosition());
        mHapLine.setEnd(allcInfoList.get(allcInfoList.size() - 1).getPosition());
        mHapLine.setCpg(cpg);
        mHapLine.setCnt(1);
        mHapLine.setStrand(strand);
        mHapLine.setBarcode(badCode);
        log.info(mHapLine.print());
        return mHapLine;
    }

    private boolean convertSc() throws Exception {
        // parse the single cell bed file, get the scBedListMap, group by chr and sorted
        Map<String, List<ScBedInfo>> scBedListMap = getScBedList();
        List<Map.Entry<String, List<ScBedInfo>>> sortedScBedListMap = new ArrayList<Map.Entry<String, List<ScBedInfo>>>(scBedListMap.entrySet());
        Collections.sort(sortedScBedListMap, new Comparator<Map.Entry<String, List<ScBedInfo>>>() { //升序排序
            public int compare(Map.Entry<String, List<ScBedInfo>> o1, Map.Entry<String, List<ScBedInfo>> o2) {
                if (util.isNumeric(o1.getValue().get(0).getChrNum()) && util.isNumeric(o2.getValue().get(0).getChrNum())) {
                    return Integer.valueOf(o1.getValue().get(0).getChrNum()).compareTo(Integer.valueOf(o2.getValue().get(0).getChrNum()));
                } else {
                    return o1.getValue().get(0).getChrNum().compareTo(o2.getValue().get(0).getChrNum());
                }
            }
        });

        for (Map.Entry<String, List<ScBedInfo>> scBedListInMap : sortedScBedListMap) {
            List<ScBedInfo> scBedList = scBedListInMap.getValue();
            // parse the cpg file
            Region cpgRegion = new Region();
            cpgRegion.setChrom(scBedList.get(0).getChrom());
            cpgRegion.setStart(scBedList.get(0).getPos());
            cpgRegion.setEnd(scBedList.get(scBedList.size() - 1).getPos());
            List<Integer> cpgPosList = util.parseCpgFile(args.getCpgPath(), cpgRegion);

            String barCode = args.getInputPath().split("\\.")[0];

            Integer startIndex = 0;
            for (int i = 0; i < scBedList.size(); i++) {
                String cpg = "";
                Integer mapPos = scBedList.get(i).getPos();
                if (scBedList.get(i).getNuc().equals("G")) {
                    mapPos = scBedList.get(i).getPos() - 1;
                }

                while (startIndex < cpgPosList.size() - 1 && cpgPosList.get(startIndex) < mapPos) {
                    startIndex++;
                }

                if (!cpgPosList.get(startIndex).equals(mapPos)) {
                    continue;
                }

                String lastNuc = scBedList.get(i).getNuc();
                Integer expandLength = 0;

                while (i + expandLength < scBedList.size() && cpgPosList.get(startIndex + expandLength).equals(mapPos)
                        && lastNuc.equals(scBedList.get(i + expandLength).getNuc())) {
                    cpg += Math.round(scBedList.get(i + expandLength).getMeth());
                    lastNuc = scBedList.get(i + expandLength).getNuc();
                    expandLength++;

                    if (i + expandLength > scBedList.size() - 1) {
                        break;
                    }

                    mapPos = scBedList.get(i).getPos();
                    if (scBedList.get(i + expandLength).getNuc().equals("G")) {
                        mapPos = scBedList.get(i + expandLength).getPos() - 1;
                    }
                }

                if (lastNuc.equals("C")) {
//                System.out.println(scBedList.get(0).getChrom() + "\t" + scBedList.get(i).getPos() + "    " +scBedList.get(i + expandLength - 1).getPos() + "\t" +
//                        "\t" + cpg + "\t" + "+" + "\t" + "1" + "\t" + barCode);
                    bufferedWriter.write(scBedList.get(0).getChrom() + "\t" + scBedList.get(i).getPos() + "\t" +scBedList.get(i + expandLength - 1).getPos() +
                            "\t" + cpg + "\t" + "\t" + "1" + "+" + "\t" + barCode + "\n");
                } else {
//                System.out.println(scBedList.get(0).getChrom() + "\t" + scBedList.get(i).getPos() + "    " +scBedList.get(i + expandLength - 1).getPos() + "\t" +
//                        "\t" + cpg + "\t" + "+" + "\t" + "1" + "\t" + barCode);
                    bufferedWriter.write(scBedList.get(0).getChrom() + "\t" + scBedList.get(i).getPos()+ "\t" + scBedList.get(i + expandLength - 1).getPos() +
                            "\t" + cpg + "\t" + "1" + "\t" + "-" + "\t" + barCode + "\n");
                }

                i += expandLength - 1;
                startIndex += expandLength - 1;
            }

            log.info("Convert " + scBedListInMap.getKey() + " end.");
        }

        return true;
    }

    private Map<String, List<ScBedInfo>> getScBedList() throws Exception {
        File scBedFile = new File(args.getInputPath());
        BufferedReader bufferedReader = new BufferedReader(new FileReader(scBedFile));
        String scBedLine = "";
        Map<String, List<ScBedInfo>> scBedListMap = new HashMap<>();
        totalLineCnt = Files.lines(scBedFile.toPath()).count();
        long scBedInfoCnt = 0l;
        while ((scBedLine = bufferedReader.readLine()) != null) {
            completeDLineCnt++;
            if (completeDLineCnt % (totalLineCnt / 10) == 0) {
                int percent = (int) Math.round(Double.valueOf(completeDLineCnt) * 100 / totalLineCnt);
                log.info("Read bed file complete " + percent + "%.");
            }
            ScBedInfo scBedInfo = new ScBedInfo();
            if (scBedLine.split("\t").length < 8 || !scBedLine.split("\t")[4].equals("CG")) {
                continue;
            }
            if (Float.valueOf(scBedLine.split("\t")[5]) > 0.1 && Float.valueOf(scBedLine.split("\t")[5]) < 0.9) {
                continue;
            }
            scBedInfoCnt++;
            scBedInfo.setChrom(scBedLine.split("\t")[0]);
            scBedInfo.setNuc(scBedLine.split("\t")[1]);
            scBedInfo.setPos(Integer.valueOf(scBedLine.split("\t")[2]));
            scBedInfo.setCont(scBedLine.split("\t")[3]);
            scBedInfo.setDinuc(scBedLine.split("\t")[4]);
            scBedInfo.setMeth(Float.valueOf(scBedLine.split("\t")[5]));
            scBedInfo.setMc(Integer.valueOf(scBedLine.split("\t")[6]));
            scBedInfo.setNc(Integer.valueOf(scBedLine.split("\t")[7]));

            if (scBedListMap.containsKey(scBedInfo.getChrom())) {
                List<ScBedInfo> scBedList = scBedListMap.get(scBedInfo.getChrom());
                scBedList.add(scBedInfo);
            } else {
                List<ScBedInfo> scBedList =new ArrayList<>();
                scBedList.add(scBedInfo);
                scBedListMap.put(scBedInfo.getChrom(), scBedList);
            }
        }
        log.info("Read bed file end! And get " + scBedInfoCnt + " lines can be convert.");

        return scBedListMap;
    }

}
