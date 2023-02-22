package com;

import com.args.*;
import com.common.Annotation;
import org.apache.commons.cli.*;

import java.lang.reflect.Field;
import java.util.LinkedHashSet;
import java.util.Set;

public class Main {
    static Convert convert = new Convert();
    static Tanghulu tanghulu = new Tanghulu();
    static R2 r2 = new R2();
    static MHBDiscovery mhbDiscovery = new MHBDiscovery();
    static HemiM hemiM = new HemiM();
    static Track track = new Track();
    static Flinkage flinkage = new Flinkage();
    static Stat stat = new Stat();

    public static void main(String[] args) throws Exception {
        System.setProperty("java.awt.headless", "true");

        if (args != null && args[0] != null && !"".equals(args[0])) {
            if (args[0].equals("convert")) {
                ConvertArgs convertArgs = parseConvert(args);
                if (convertArgs != null) {
                    convert.convert(convertArgs);
                }
            } else if (args[0].equals("tanghulu")) {
                TanghuluArgs tanghuluArgs = parseTanghulu(args);
                if (tanghuluArgs != null) {
                    tanghulu.tanghulu(tanghuluArgs);
                }
            } else if (args[0].equals("R2")) {
                R2Args r2Args = parseR2(args);
                if (r2Args != null) {
                    r2.R2(r2Args);
                }
            } else if (args[0].equals("MHBDiscovery")) {
                MHBDiscoveryArgs mhbDiscoveryArgs = parseMHBDiscovery(args);
                if (mhbDiscoveryArgs != null) {
                    mhbDiscovery.MHBDiscovery(mhbDiscoveryArgs);
                }
            } else if (args[0].equals("hemiM")) {
                HemiMArgs hemiMArgs = parseHemiM(args);
                if (hemiMArgs != null) {
                    hemiM.hemiM(hemiMArgs);
                }
            } else if (args[0].equals("track")) {
                TrackArgs trackMArgs = parseTrack(args);
                if (trackMArgs != null) {
                    track.track(trackMArgs);
                }
            } else if (args[0].equals("flinkage")) {
                FlinkageArgs flinkageMArgs = parseFlinkage(args);
                if (flinkageMArgs != null) {
                    flinkage.flinkage(flinkageMArgs);
                }
            } else if (args[0].equals("stat")) {
                StatArgs statMArgs = parseStat(args);
                if (statMArgs != null) {
                    stat.stat(statMArgs);
                }
            } else {
                System.out.println("unrecognized command:" + args[0]);
            }
        } else { // show the help message

        }
    }

    private static Options getOptions(Field[] declaredFields) {
        Options options = new Options();
        Option helpOption = OptionBuilder.withLongOpt("help").withDescription("help").create("h");
        options.addOption(helpOption);
        Field[] fields = declaredFields;
        for(Field field : fields) {
            String annotation = field.getAnnotation(Annotation.class).value();
            Option option = null;
            if (field.getType().equals(boolean.class)) {
                option = OptionBuilder.withLongOpt(field.getName()).withDescription(annotation).create(field.getName());
            } else {
                option = OptionBuilder.withLongOpt(field.getName()).hasArg().withDescription(annotation).create(field.getName());
            }
            options.addOption(option);
        }
        return options;
    }

    public static String getStringFromMultiValueParameter(CommandLine commandLine, String args) {
        String value = commandLine.getOptionValue(args);
        if (commandLine.getArgs().length > 1) {
            for (int i = 1; i < commandLine.getArgs().length; i++) {
                value += " " + commandLine.getArgs()[i];
            }
        }
        // 去除重复的值
        String[] valueList = value.split(" ");
        Set<Object> haoma = new LinkedHashSet<Object>();
        for (int i = 0; i < valueList.length; i++) {
            haoma.add(valueList[i]);
        }

        String realValue = "";
        for (int i = 0; i < haoma.size(); i++) {
            realValue += " " + haoma.toArray()[i];
        }

        return realValue.trim();
    }

    private static ConvertArgs parseConvert(String[] args) throws ParseException {
        Options options = getOptions(ConvertArgs.class.getDeclaredFields());
        BasicParser parser = new BasicParser();
        ConvertArgs convertArgs = new ConvertArgs();
        CommandLine commandLine = parser.parse(options, args);
        if (commandLine.getOptions().length > 0) {
            if (commandLine.hasOption('h')) {
                HelpFormatter hf = new HelpFormatter();
                hf.printHelp("Options", options);
                return null;
            } else {
                convertArgs.setInputPath(commandLine.getOptionValue("inputPath"));
                convertArgs.setCpgPath(commandLine.getOptionValue("cpgPath"));
                convertArgs.setOutputDir(commandLine.getOptionValue("outputDir"));
                convertArgs.setTag(commandLine.getOptionValue("tag"));
                if (commandLine.hasOption("nanopolish")) {
                    convertArgs.setNanopolish(true);
                }
            }
        } else {
            System.out.println("The paramter is null");
        }

        return convertArgs;
    }

    private static TanghuluArgs parseTanghulu(String[] args) throws ParseException {
        Options options = getOptions(TanghuluArgs.class.getDeclaredFields());
        BasicParser parser = new BasicParser();
        TanghuluArgs tanghuluArgs = new TanghuluArgs();
        CommandLine commandLine = parser.parse(options, args);
        if (commandLine.getOptions().length > 0) {
            if (commandLine.hasOption('h')) {
                HelpFormatter hf = new HelpFormatter();
                hf.printHelp("Options", options);
                return null;
            } else {
                tanghuluArgs.setMhapPath(commandLine.getOptionValue("mhapPath"));
                tanghuluArgs.setCpgPath(commandLine.getOptionValue("cpgPath"));
                tanghuluArgs.setRegion(commandLine.getOptionValue("region"));
                tanghuluArgs.setBcFile(commandLine.getOptionValue("bcFile"));
                if (commandLine.hasOption("outcut")) {
                    tanghuluArgs.setOutcut(Integer.valueOf(commandLine.getOptionValue("outcut")));
                }
                tanghuluArgs.setOutputDir(commandLine.getOptionValue("outputDir"));
                tanghuluArgs.setTag(commandLine.getOptionValue("tag"));
            }
        } else {
            System.out.println("The paramter is null");
        }

        return tanghuluArgs;
    }

    private static R2Args parseR2(String[] args) throws ParseException {
        Options options = getOptions(R2Args.class.getDeclaredFields());
        BasicParser parser = new BasicParser();
        R2Args r2Args = new R2Args();
        CommandLine commandLine = parser.parse(options, args);
        if (commandLine.getOptions().length > 0) {
            if (commandLine.hasOption('h')) {
                HelpFormatter hf = new HelpFormatter();
                hf.printHelp("Options", options);
                return null;
            } else {
                r2Args.setMhapPath(commandLine.getOptionValue("mhapPath"));
                r2Args.setCpgPath(commandLine.getOptionValue("cpgPath"));
                r2Args.setRegion(commandLine.getOptionValue("region"));
                if (commandLine.hasOption("bcFile")) {
                    r2Args.setBcFile(commandLine.getOptionValue("bcFile"));
                }
                r2Args.setOutputDir(commandLine.getOptionValue("outputDir"));
                r2Args.setTag(commandLine.getOptionValue("tag"));
                if (commandLine.hasOption("mHapView")) {
                    r2Args.setMhapView(true);
                }
                if (commandLine.hasOption("strand")) {
                    r2Args.setStrand(commandLine.getOptionValue("strand"));
                }
                if (commandLine.hasOption("longrange")) {
                    r2Args.setLongrange(true);
                }
                if (commandLine.hasOption("bedFile")) {
                    r2Args.setBedFile(commandLine.getOptionValue("bedFile"));
                }
            }
        } else {
            System.out.println("The paramter is null");
        }

        return r2Args;
    }

    private static MHBDiscoveryArgs parseMHBDiscovery(String[] args) throws ParseException {
        Options options = getOptions(MHBDiscoveryArgs.class.getDeclaredFields());
        BasicParser parser = new BasicParser();
        MHBDiscoveryArgs mhbDiscoveryArgs = new MHBDiscoveryArgs();
        CommandLine commandLine = parser.parse(options, args);
        if (commandLine.getOptions().length > 0) {
            if (commandLine.hasOption('h')) {
                HelpFormatter hf = new HelpFormatter();
                hf.printHelp("Options", options);
                return null;
            } else {
                mhbDiscoveryArgs.setmHapPath(commandLine.getOptionValue("mhapPath"));
                mhbDiscoveryArgs.setCpgPath(commandLine.getOptionValue("cpgPath"));
                if (commandLine.hasOption("bcFile")) {
                    mhbDiscoveryArgs.setBcFile(commandLine.getOptionValue("bcFile"));
                }
                if (commandLine.hasOption("region")) {
                    mhbDiscoveryArgs.setRegion(commandLine.getOptionValue("region"));
                }
                if (commandLine.hasOption("bedFile")) {
                    mhbDiscoveryArgs.setBedFile(commandLine.getOptionValue("bedFile"));
                }
                if (commandLine.hasOption("window")) {
                    mhbDiscoveryArgs.setWindow(Integer.valueOf(commandLine.getOptionValue("window")));
                }
                if (commandLine.hasOption("r2")) {
                    mhbDiscoveryArgs.setR2(Double.valueOf(commandLine.getOptionValue("r2")));
                }
                if (commandLine.hasOption("pvalue")) {
                    mhbDiscoveryArgs.setPvalue(Double.valueOf(commandLine.getOptionValue("pvalue")));
                }
                if (commandLine.hasOption("outputDir")) {
                    mhbDiscoveryArgs.setOutputDir(commandLine.getOptionValue("outputDir"));
                }
                if (commandLine.hasOption("tag")) {
                    mhbDiscoveryArgs.setTag(commandLine.getOptionValue("tag"));
                }
                if (commandLine.hasOption("qc")) {
                    mhbDiscoveryArgs.setQcFlag(true);
                }
            }
        } else {
            System.out.println("The paramter is null");
        }

        return mhbDiscoveryArgs;
    }

    private static HemiMArgs parseHemiM(String[] args) throws ParseException {
        Options options = getOptions(TanghuluArgs.class.getDeclaredFields());
        BasicParser parser = new BasicParser();
        HemiMArgs hemiMArgs = new HemiMArgs();

        CommandLine commandLine = parser.parse(options, args);
        if (commandLine.getOptions().length > 0) {
            if (commandLine.hasOption('h')) {
                HelpFormatter hf = new HelpFormatter();
                hf.printHelp("Options", options);
                return null;
            } else {
                hemiMArgs.setMhapPath(commandLine.getOptionValue("mhapPath"));
                hemiMArgs.setCpgPath(commandLine.getOptionValue("cpgPath"));
                if (commandLine.hasOption("bedFile")) {
                    hemiMArgs.setBedFile(commandLine.getOptionValue("bedFile"));
                }
                if (commandLine.hasOption("region")) {
                    hemiMArgs.setRegion(commandLine.getOptionValue("region"));
                }
                if (commandLine.hasOption("bcFile")) {
                    hemiMArgs.setBcFile(commandLine.getOptionValue("bcFile"));
                }
                hemiMArgs.setOutputDir(commandLine.getOptionValue("outputDir"));
                hemiMArgs.setTag(commandLine.getOptionValue("tag"));
            }
        } else {
            System.out.println("The paramter is null");
        }

        return hemiMArgs;
    }

    private static TrackArgs parseTrack(String[] args) throws ParseException {
        Options options = getOptions(TrackArgs.class.getDeclaredFields());
        BasicParser parser = new BasicParser();
        TrackArgs trackArgs = new TrackArgs();

        CommandLine commandLine = parser.parse(options, args);
        if (commandLine.getOptions().length > 0) {
            if (commandLine.hasOption('h')) {
                HelpFormatter hf = new HelpFormatter();
                hf.printHelp("Options", options);
            } else {
                trackArgs.setMhapPath(commandLine.getOptionValue("mhapPath"));
                trackArgs.setCpgPath(commandLine.getOptionValue("cpgPath"));
                if (commandLine.hasOption("region")) {
                    trackArgs.setRegion(commandLine.getOptionValue("region"));
                }
                if (commandLine.hasOption("bedFile")) {
                    trackArgs.setBedFile(commandLine.getOptionValue("bedFile"));
                }
                trackArgs.setOutputDir(commandLine.getOptionValue("outputDir"));
                trackArgs.setTag(commandLine.getOptionValue("tag"));
                if (commandLine.hasOption("bcFile")) {
                    trackArgs.setBcFile(commandLine.getOptionValue("bcFile"));
                }
                if (commandLine.hasOption("metrics")) {
                    trackArgs.setMetrics(getStringFromMultiValueParameter(commandLine, "metrics"));
                }
            }
        } else {
            System.out.println("The paramter is null");
        }

        return trackArgs;
    }

    private static FlinkageArgs parseFlinkage(String[] args) throws ParseException {
        Options options = getOptions(FlinkageArgs.class.getDeclaredFields());
        BasicParser parser = new BasicParser();
        FlinkageArgs flinkageMArgs = new FlinkageArgs();

        CommandLine commandLine = parser.parse(options, args);
        if (commandLine.getOptions().length > 0) {
            if (commandLine.hasOption('h')) {
                HelpFormatter hf = new HelpFormatter();
                hf.printHelp("Options", options);
                return null;
            } else {
                flinkageMArgs.setMhapPath(commandLine.getOptionValue("mhapPath"));
                flinkageMArgs.setCpgPath(commandLine.getOptionValue("cpgPath"));
                if (commandLine.hasOption("region1")) {
                    flinkageMArgs.setRegion1(commandLine.getOptionValue("region1"));
                }
                if (commandLine.hasOption("region2")) {
                    flinkageMArgs.setRegion2(commandLine.getOptionValue("region2"));
                }
                if (commandLine.hasOption("bedFile")) {
                    flinkageMArgs.setBedFile(commandLine.getOptionValue("bedFile"));
                }
                if (commandLine.hasOption("bcFile")) {
                    flinkageMArgs.setBcFile(commandLine.getOptionValue("bcFile"));
                }
                flinkageMArgs.setOutputDir(commandLine.getOptionValue("outputDir"));
                flinkageMArgs.setTag(commandLine.getOptionValue("tag"));
                if (commandLine.hasOption("limit")) {
                    flinkageMArgs.setLimit(Integer.valueOf(commandLine.getOptionValue("limit")));
                }
                if (commandLine.hasOption("r2Cov")) {
                    flinkageMArgs.setR2Cov(Integer.valueOf(commandLine.getOptionValue("r2Cov")));
                }
            }
        } else {
            System.out.println("The paramter is null");
        }

        return flinkageMArgs;
    }

    private static StatArgs parseStat(String[] args) throws ParseException {
        Options options = getOptions(StatArgs.class.getDeclaredFields());
        BasicParser parser = new BasicParser();
        StatArgs statArgs = new StatArgs();
        CommandLine commandLine = parser.parse(options, args);
        if (commandLine.getOptions().length > 0) {
            if (commandLine.hasOption('h')) {
                HelpFormatter helpFormatter = new HelpFormatter();
                helpFormatter.printHelp("Options", options);
                return null;
            } else {
                statArgs.setMetrics(getStringFromMultiValueParameter(commandLine, "metrics"));
                statArgs.setMhapPath(commandLine.getOptionValue("mhapPath"));
                statArgs.setCpgPath(commandLine.getOptionValue("cpgPath"));
                if (commandLine.hasOption("region")) {
                    statArgs.setRegion(commandLine.getOptionValue("region"));
                }
                if (commandLine.hasOption("bedPath")) {
                    statArgs.setBedPath(commandLine.getOptionValue("bedPath"));
                }
                if (commandLine.hasOption("bcFile")) {
                    statArgs.setBcFile(commandLine.getOptionValue("bcFile"));
                }
                statArgs.setOutputFile(commandLine.getOptionValue("outputFile"));
                if (commandLine.hasOption("minK")) {
                    statArgs.setMinK(Integer.valueOf(commandLine.getOptionValue("minK")));
                }
                if (commandLine.hasOption("maxK")) {
                    statArgs.setMaxK(Integer.valueOf(commandLine.getOptionValue("maxK")));
                }
                if (commandLine.hasOption("K")) {
                    statArgs.setK(Integer.valueOf(commandLine.getOptionValue("K")));
                }
                if (commandLine.hasOption("strand")) {
                    statArgs.setStrand(commandLine.getOptionValue("strand"));
                }
                if (commandLine.hasOption("r2Cov")) {
                    statArgs.setR2Cov(Integer.valueOf(commandLine.getOptionValue("r2Cov")));
                }
            }
        } else {
            System.out.println("The paramter is null");
        }

        return statArgs;
    }
}
