package com;

import com.args.ConvertArgs;
import com.bean.Region;
import com.bean.ScBedInfo;
import com.common.Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.nio.file.Files;
import java.util.*;

public class Convert {
    public static final Logger log = LoggerFactory.getLogger(Convert.class);

    ConvertArgs args = new ConvertArgs();
    Util util = new Util();

    public void convert(ConvertArgs convertArgs) throws Exception {
        log.info("Convert start!");
        args = convertArgs;

        // check the command
        boolean checkResult = checkArgs();
        if (!checkResult) {
            log.error("Checkargs fail, please check the command.");
            return;
        }

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

        // create the output directory and file
        BufferedWriter bufferedWriter = util.createOutputFile(args.getOutputDir(), args.getTag() + ".mhap");

        for (Map.Entry<String, List<ScBedInfo>> scBedList : sortedScBedListMap) {
            // parse the cpg file
            Region cpgRegion = new Region();
            cpgRegion.setChrom(scBedList.getValue().get(0).getChrom());
            cpgRegion.setStart(scBedList.getValue().get(0).getPos());
            cpgRegion.setEnd(scBedList.getValue().get(scBedList.getValue().size() - 1).getPos());
            List<Integer> cpgPosList = util.parseCpgFile(args.getCpgPath(), cpgRegion);

            boolean convertResult = convert(scBedList.getValue(), cpgPosList, bufferedWriter);
            if (!convertResult) {
                log.error("convert fail, please check the command.");
                return;
            }

            log.info("Convert " + scBedList.getKey() + " end.");
        }
        bufferedWriter.close();

        log.info("Convert end!");
    }

    private boolean checkArgs() {
        if (args.getBedPath().equals("")) {
            log.error("The bed file cannot be empty!");
        }
        if (args.getCpgPath().equals("")) {
            log.error("The cpg file cannot be empty!");
        }
        return true;
    }

    private Map<String, List<ScBedInfo>> getScBedList() throws Exception {
        File scBedFile = new File(args.getBedPath());
        BufferedReader bufferedReader = new BufferedReader(new FileReader(scBedFile));
        String scBedLine = "";
        Map<String, List<ScBedInfo>> scBedListMap = new HashMap<>();
        // get the total lines of file
        long totalLineCnt = Files.lines(scBedFile.toPath()).count();
        long lineCnt = 0l;
        long scBedInfoCnt = 0l;
        while ((scBedLine = bufferedReader.readLine()) != null) {
            lineCnt++;
            if (lineCnt % (totalLineCnt / 10) == 0) {
                int percent = (int) Math.round(Double.valueOf(lineCnt) * 100 / totalLineCnt);
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

    private boolean convert(List<ScBedInfo> scBedList, List<Integer> cpgPosList, BufferedWriter bufferedWriter) throws Exception {
        String barCode = args.getBedPath().split("\\.")[0];

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
                bufferedWriter.write(scBedList.get(0).getChrom() + "\t" + scBedList.get(i).getPos() + "\t" +scBedList.get(i + expandLength - 1).getPos() + "\t" +
                        "\t" + cpg + "\t" + "+" + "\t" + "1" + "\t" + barCode + "\n");
            } else {
//                System.out.println(scBedList.get(0).getChrom() + "\t" + scBedList.get(i).getPos() + "    " +scBedList.get(i + expandLength - 1).getPos() + "\t" +
//                        "\t" + cpg + "\t" + "+" + "\t" + "1" + "\t" + barCode);
                bufferedWriter.write(scBedList.get(0).getChrom() + "\t" + scBedList.get(i).getPos()+ "\t" + scBedList.get(i + expandLength - 1).getPos() + "\t" +
                        "\t" + cpg + "\t" + "-" + "\t" + "1" + "\t" + barCode + "\n");
            }

            i += expandLength - 1;
            startIndex += expandLength - 1;
        }

        return true;
    }

}
