package com;

import com.args.ConvertArgs;
import com.bean.Region;
import com.bean.ScBedInfo;
import com.common.Util;
import htsjdk.tribble.readers.TabixReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.List;
import java.util.*;

public class Convert {
    public static final Logger log = LoggerFactory.getLogger(Convert.class);

    ConvertArgs args = new ConvertArgs();
    Util util = new Util();
    Region region = new Region();
    private final Integer SHIFT = 500;

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
                if (isNumeric(o1.getValue().get(0).getChrNum()) && isNumeric(o2.getValue().get(0).getChrNum())) {
                    return Integer.valueOf(o1.getValue().get(0).getChrNum()).compareTo(Integer.valueOf(o2.getValue().get(0).getChrNum()));
                } else {
                    return o1.getValue().get(0).getChrNum().compareTo(o2.getValue().get(0).getChrNum());
                }
            }
        });

        // create the output file
        String fileName = args.getTag() + ".mhap";
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

        for (Map.Entry<String, List<ScBedInfo>> scBedList : sortedScBedListMap) {
            System.out.println(scBedList.getKey() + " convert start.");
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

            System.out.println(scBedList.getKey() + " convert end.");
        }
        bufferedWriter.close();

        log.info("Convert end!");
    }

    private boolean checkArgs() {

        return true;
    }

    private Map<String, List<ScBedInfo>> getScBedList() throws Exception {
        File scBedFile = new File(args.getBedPath());
        BufferedReader bufferedReader = new BufferedReader(new FileReader(scBedFile));
        String scBedLine = "";
        Map<String, List<ScBedInfo>> scBedListMap = new HashMap<>();
        while ((scBedLine = bufferedReader.readLine()) != null) {
            ScBedInfo scBedInfo = new ScBedInfo();
            if (scBedLine.split("\t").length < 8 || !scBedLine.split("\t")[4].equals("CG")) {
                continue;
            }
            if (Float.valueOf(scBedLine.split("\t")[5]) > 0.1 && Float.valueOf(scBedLine.split("\t")[5]) < 0.9) {
                continue;
            }
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

        return scBedListMap;
    }



    private boolean convert(List<ScBedInfo> scBedList, List<Integer> cpgPosList, BufferedWriter bufferedWriter) throws Exception {

        String barCode = args.getBedPath().split("\\.")[0];

        Integer cnt = 0;
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

//            if (scBedList.get(i).getPos().equals(248906288)) {
//                int g = 0;
//            }

            String lastNuc = scBedList.get(i).getNuc();
            Integer expandLength = 0;

            Integer test1 = cpgPosList.get(startIndex + expandLength);
            Integer test2 = mapPos;
            boolean test3 = test1 == test2;
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

    public static boolean isNumeric(String str){
        for (int i = str.length(); --i >= 0; ){
            if (!Character.isDigit(str.charAt(i))){
                return false;
            }
        }
        return true;
    }

}
