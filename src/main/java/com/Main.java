package com;

import com.args.*;
import org.apache.commons.cli.*;

public class Main {
    static Convert convert = new Convert();
    static Tanghulu tanghulu = new Tanghulu();
    static R2 r2 = new R2();
    static MHBDiscovery mhbDiscovery = new MHBDiscovery();
    static HemiM hemiM = new HemiM();
    static Track track = new Track();
    static Flinkage flinkage = new Flinkage();

    public static void main(String[] args) throws Exception {
        System.setProperty("java.awt.headless", "true");

        if (args != null && args[0] != null && !"".equals(args[0])) {
            if (args[0].equals("convert")) {
                ConvertArgs convertArgs = parseConvert(args);
                convert.convert(convertArgs);
            } else if (args[0].equals("tanghulu")) {
                TanghuluArgs tanghuluArgs = parseTanghulu(args);
                tanghulu.tanghulu(tanghuluArgs);
            } else if (args[0].equals("R2")) {
                R2Args r2Args = parseR2(args);
                r2.R2(r2Args);
            } else if (args[0].equals("MHBDiscovery")) {
                MHBDiscoveryArgs mhbDiscoveryArgs = parseMHBDiscovery(args);
                mhbDiscovery.MHBDiscovery(mhbDiscoveryArgs);
            } else if (args[0].equals("hemiM")) {
                HemiMArgs hemiMArgs = parseHemiM(args);
                hemiM.hemiM(hemiMArgs);
            } else if (args[0].equals("track")) {
                TrackArgs trackMArgs = parseTrack(args);
                track.track(trackMArgs);
            } else if (args[0].equals("flinkage")) {
                FlinkageArgs flinkageMArgs = parseFlinkage(args);
                flinkage.flinkage(flinkageMArgs);
            } else {
                System.out.println("unrecognized command:" + args[0]);
            }
        } else { // show the help message

        }
    }

    private static ConvertArgs parseConvert(String[] args) throws ParseException {
        Options options = new Options();
        Option option1 = OptionBuilder.withArgName("com/args").withLongOpt("bedPath").isRequired().hasArg().withDescription("bedPath").create("bedPath");
        Option option2 = OptionBuilder.withArgName("com/args").withLongOpt("cpgPath").isRequired().hasArg().withDescription("cpgPath").create("cpgPath");
        Option option3 = OptionBuilder.withArgName("com/args").withLongOpt("outputDir").isRequired().hasArg().withDescription("outputDir").create("outputDir");
        Option option4 = OptionBuilder.withArgName("com/args").withLongOpt("tag").isRequired().hasArg().withDescription("tag").create("tag");
        options.addOption(option1).addOption(option2).addOption(option3).addOption(option4);

        BasicParser parser = new BasicParser();
        ConvertArgs convertArgs = new ConvertArgs();

        CommandLine commandLine = parser.parse(options, args);
        if (commandLine.getOptions().length > 0) {
            if (commandLine.hasOption('h')) {
                HelpFormatter hf = new HelpFormatter();
                hf.printHelp("Options", options);
            } else {
                convertArgs.setBedPath(commandLine.getOptionValue("bedPath"));
                convertArgs.setCpgPath(commandLine.getOptionValue("cpgPath"));
                convertArgs.setOutputDir(commandLine.getOptionValue("outputDir"));
                convertArgs.setTag(commandLine.getOptionValue("tag"));
            }
        } else {
            System.out.println("The paramter is null");
        }

        return convertArgs;
    }

    private static TanghuluArgs parseTanghulu(String[] args) throws ParseException {
        Options options = new Options();
        Option option1 = OptionBuilder.withArgName("com/args").withLongOpt("mhapPath").isRequired().hasArg().withDescription("mhapPath").create("mhapPath");
        Option option2 = OptionBuilder.withArgName("com/args").withLongOpt("cpgPath").isRequired().hasArg().withDescription("cpgPath").create("cpgPath");
        Option option3 = OptionBuilder.withArgName("com/args").withLongOpt("region").isRequired().hasArg().withDescription("region").create("region");
//        Option option4 = OptionBuilder.withArgName("args").withLongOpt("merge").withDescription("merge").create("merge");
//        Option option5 = OptionBuilder.withArgName("args").withLongOpt("simulation").withDescription("simulation").create("simulation");
        Option option5 = OptionBuilder.withArgName("com/args").withLongOpt("bcFile").hasArg().withDescription("bcFile").create("bcFile");
        Option option6 = OptionBuilder.withArgName("com/args").withLongOpt("outcut").hasArg().withDescription("outcut").create("outcut");
        Option option7 = OptionBuilder.withArgName("com/args").withLongOpt("outputDir").isRequired().hasArg().withDescription("outputDir").create("outputDir");
        Option option8 = OptionBuilder.withArgName("com/args").withLongOpt("tag").isRequired().hasArg().withDescription("tag").create("tag");
        options.addOption(option1).addOption(option2).addOption(option3).addOption(option5).addOption(option6).addOption(option7).addOption(option8);

        BasicParser parser = new BasicParser();
        TanghuluArgs tanghuluArgs = new TanghuluArgs();

        CommandLine commandLine = parser.parse(options, args);
        if (commandLine.getOptions().length > 0) {
            if (commandLine.hasOption('h')) {
                HelpFormatter hf = new HelpFormatter();
                hf.printHelp("Options", options);
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
        Options options = new Options();
        Option option1 = OptionBuilder.withArgName("com/args").withLongOpt("mhapPath").isRequired().hasArg().withDescription("mhapPath").create("mhapPath");
        Option option2 = OptionBuilder.withArgName("com/args").withLongOpt("cpgPath").isRequired().hasArg().withDescription("cpgPath").create("cpgPath");
        Option option3 = OptionBuilder.withArgName("com/args").withLongOpt("region").isRequired().hasArg().withDescription("region").create("region");
        Option option4 = OptionBuilder.withArgName("com/args").withLongOpt("bcFile").hasArg().withDescription("bcFile").create("bcFile");
        Option option5 = OptionBuilder.withArgName("com/args").withLongOpt("outputDir").isRequired().hasArg().withDescription("outputDir").create("outputDir");
        Option option6 = OptionBuilder.withArgName("com/args").withLongOpt("tag").isRequired().hasArg().withDescription("tag").create("tag");
        Option option7 = OptionBuilder.withArgName("com/args").withLongOpt("mHapView").withDescription("mHapView").create("mHapView");
        Option option8 = OptionBuilder.withArgName("com/args").withLongOpt("strand").hasArg().withDescription("strand").create("strand");
        Option option9 = OptionBuilder.withArgName("com/args").withLongOpt("longrange").withDescription("longrange").create("longrange");
        Option option10 = OptionBuilder.withArgName("com/args").withLongOpt("bedFile").hasArg().withDescription("bedFile").create("bedFile");
        options.addOption(option1).addOption(option2).addOption(option3).addOption(option4).addOption(option5).
                addOption(option6).addOption(option7).addOption(option8).addOption(option9).addOption(option10);

        BasicParser parser = new BasicParser();
        R2Args r2Args = new R2Args();

        CommandLine commandLine = parser.parse(options, args);
        if (commandLine.getOptions().length > 0) {
            if (commandLine.hasOption('h')) {
                HelpFormatter hf = new HelpFormatter();
                hf.printHelp("Options", options);
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
        Options options = new Options();
        Option option1 = OptionBuilder.withArgName("com/args").withLongOpt("mhapPath").isRequired().hasArg().withDescription("mhapPath").create("mhapPath");
        Option option2 = OptionBuilder.withArgName("com/args").withLongOpt("cpgPath").isRequired().hasArg().withDescription("cpgPath").create("cpgPath");
        Option option3 = OptionBuilder.withArgName("com/args").withLongOpt("bcFile").hasArg().withDescription("bcFile").create("bcFile");
        Option option4 = OptionBuilder.withArgName("com/args").withLongOpt("region").hasArg().withDescription("region").create("region");
        Option option5 = OptionBuilder.withArgName("com/args").withLongOpt("bedFile").hasArg().withDescription("bedFile").create("bedFile");
        Option option6 = OptionBuilder.withArgName("com/args").withLongOpt("window").hasArg().withDescription("window").create("window");
        Option option7 = OptionBuilder.withArgName("com/args").withLongOpt("r2").hasArg().hasArg().withDescription("r2").create("r2");
        Option option8 = OptionBuilder.withArgName("com/args").withLongOpt("pvalue").hasArg().withDescription("pvalue").create("pvalue");
        Option option9 = OptionBuilder.withArgName("com/args").withLongOpt("outputDir").hasArg().withDescription("outputDir").create("outputDir");
        Option option10 = OptionBuilder.withArgName("com/args").withLongOpt("tag").hasArg().withDescription("tag").create("tag");
        Option option11 = OptionBuilder.withArgName("com/args").withLongOpt("qc").withDescription("qc").create("qc");
        options.addOption(option1).addOption(option2).addOption(option3).addOption(option4).addOption(option5).
                addOption(option6).addOption(option7).addOption(option8).addOption(option9).addOption(option10).addOption(option11);

        BasicParser parser = new BasicParser();
        MHBDiscoveryArgs mhbDiscoveryArgs = new MHBDiscoveryArgs();

        CommandLine commandLine = parser.parse(options, args);
        if (commandLine.getOptions().length > 0) {
            if (commandLine.hasOption('h')) {
                HelpFormatter hf = new HelpFormatter();
                hf.printHelp("Options", options);
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
                if (commandLine.hasOption("QC")) {
                    mhbDiscoveryArgs.setQcFlag(true);
                }
            }
        } else {
            System.out.println("The paramter is null");
        }

        return mhbDiscoveryArgs;
    }

    private static HemiMArgs parseHemiM(String[] args) throws ParseException {
        Options options = new Options();
        Option option1 = OptionBuilder.withArgName("com/args").withLongOpt("mhapPath").isRequired().hasArg().withDescription("mhapPath").create("mhapPath");
        Option option2 = OptionBuilder.withArgName("com/args").withLongOpt("cpgPath").isRequired().hasArg().withDescription("cpgPath").create("cpgPath");
        Option option3 = OptionBuilder.withArgName("com/args").withLongOpt("bFile").hasArg().withDescription("bFile").create("bFile");
        Option option4 = OptionBuilder.withArgName("com/args").withLongOpt("region").hasArg().withDescription("region").create("region");
        Option option5 = OptionBuilder.withArgName("com/args").withLongOpt("bcFile").hasArg().withDescription("bcFile").create("bcFile");
        Option option6 = OptionBuilder.withArgName("com/args").withLongOpt("tag").isRequired().hasArg().withDescription("tag").create("tag");
        Option option7 = OptionBuilder.withArgName("com/args").withLongOpt("outputDir").hasArg().withDescription("outputDir").create("outputDir");
        options.addOption(option1).addOption(option2).addOption(option3).addOption(option4).addOption(option5).addOption(option6).addOption(option7);

        BasicParser parser = new BasicParser();
        HemiMArgs hemiMArgs = new HemiMArgs();

        CommandLine commandLine = parser.parse(options, args);
        if (commandLine.getOptions().length > 0) {
            if (commandLine.hasOption('h')) {
                HelpFormatter hf = new HelpFormatter();
                hf.printHelp("Options", options);
            } else {
                hemiMArgs.setMhapPath(commandLine.getOptionValue("mhapPath"));
                hemiMArgs.setCpgPath(commandLine.getOptionValue("cpgPath"));
                if (commandLine.hasOption("bFile")) {
                    hemiMArgs.setBedFile(commandLine.getOptionValue("bFile"));
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
        Options options = new Options();
        Option option1 = OptionBuilder.withArgName("com/args").withLongOpt("mhapPath").isRequired().hasArg().withDescription("mhapPath").create("mhapPath");
        Option option2 = OptionBuilder.withArgName("com/args").withLongOpt("cpgPath").isRequired().hasArg().withDescription("cpgPath").create("cpgPath");
        Option option3 = OptionBuilder.withArgName("com/args").withLongOpt("region").hasArg().withDescription("region").create("region");
        Option option4 = OptionBuilder.withArgName("com/args").withLongOpt("bedFile").hasArg().withDescription("bedFile").create("bedFile");
        Option option5 = OptionBuilder.withArgName("com/args").withLongOpt("outputDir").isRequired().hasArg().withDescription("outputDir").create("outputDir");
        Option option6 = OptionBuilder.withArgName("com/args").withLongOpt("tag").isRequired().hasArg().withDescription("tag").create("tag");
        Option option7 = OptionBuilder.withArgName("com/args").withLongOpt("bcFile").hasArg().withDescription("bcFile").create("bcFile");
        Option option8 = OptionBuilder.withArgName("com/args").withLongOpt("metric").isRequired().hasArg().withDescription("metric").create("metric");
        options.addOption(option1).addOption(option2).addOption(option3).addOption(option4).addOption(option5).
                addOption(option6).addOption(option7).addOption(option8);

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
                if (commandLine.hasOption("metric")) {
                    trackArgs.setMetric(commandLine.getOptionValue("metric"));
                }
            }
        } else {
            System.out.println("The paramter is null");
        }

        return trackArgs;
    }

    private static FlinkageArgs parseFlinkage(String[] args) throws ParseException {
        Options options = new Options();
        Option option1 = OptionBuilder.withArgName("com/args").withLongOpt("mhapPath").isRequired().hasArg().withDescription("mhapPath").create("mhapPath");
        Option option2 = OptionBuilder.withArgName("com/args").withLongOpt("cpgPath").isRequired().hasArg().withDescription("cpgPath").create("cpgPath");
        Option option3 = OptionBuilder.withArgName("com/args").withLongOpt("region1").hasArg().withDescription("region1").create("region1");
        Option option4 = OptionBuilder.withArgName("com/args").withLongOpt("region2").hasArg().withDescription("region2").create("region2");
        Option option5 = OptionBuilder.withArgName("com/args").withLongOpt("bedFile").hasArg().withDescription("bedFile").create("bedFile");
        Option option6 = OptionBuilder.withArgName("com/args").withLongOpt("bcFile").hasArg().withDescription("bcFile").create("bcFile");
        Option option7 = OptionBuilder.withArgName("com/args").withLongOpt("outputDir").isRequired().hasArg().withDescription("outputDir").create("outputDir");
        Option option8 = OptionBuilder.withArgName("com/args").withLongOpt("tag").isRequired().hasArg().withDescription("tag").create("tag");
        Option option9 = OptionBuilder.withArgName("com/args").withLongOpt("limit").hasArg().withDescription("limit").create("limit");
        options.addOption(option1).addOption(option2).addOption(option3).addOption(option4).addOption(option5).
                addOption(option6).addOption(option7).addOption(option8).addOption(option9);

        BasicParser parser = new BasicParser();
        FlinkageArgs flinkageMArgs = new FlinkageArgs();

        CommandLine commandLine = parser.parse(options, args);
        if (commandLine.getOptions().length > 0) {
            if (commandLine.hasOption('h')) {
                HelpFormatter hf = new HelpFormatter();
                hf.printHelp("Options", options);
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
            }
        } else {
            System.out.println("The paramter is null");
        }

        return flinkageMArgs;
    }
}
