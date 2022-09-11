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
        Option option3 = OptionBuilder.withArgName("com/args").withLongOpt("tag").isRequired().hasArg().withDescription("tag").create("tag");
        options.addOption(option1).addOption(option2).addOption(option3);

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
        Option option7 = OptionBuilder.withArgName("com/args").withLongOpt("outputFile").isRequired().hasArg().withDescription("outputFile").create("outputFile");
        options.addOption(option1).addOption(option2).addOption(option3).addOption(option5).addOption(option6).addOption(option7);

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
                tanghuluArgs.setOutputFile(commandLine.getOptionValue("outputFile"));
            }
        } else {
            System.out.println("The paramter is null");
        }

        return tanghuluArgs;
    }

    private static R2Args parseR2(String[] args) throws ParseException {
        Options options = new Options();
        Option option1 = OptionBuilder.withArgName("com/args").withLongOpt("tag").isRequired().hasArg().withDescription("tag").create("tag");
        Option option2 = OptionBuilder.withArgName("com/args").withLongOpt("mhapPath").isRequired().hasArg().withDescription("mhapPath").create("mhapPath");
        Option option3 = OptionBuilder.withArgName("com/args").withLongOpt("cpgPath").isRequired().hasArg().withDescription("cpgPath").create("cpgPath");
        Option option4 = OptionBuilder.withArgName("com/args").withLongOpt("region").isRequired().hasArg().withDescription("region").create("region");
        Option option5 = OptionBuilder.withArgName("com/args").withLongOpt("bcFile").hasArg().withDescription("bcFile").create("bcFile");
        Option option6 = OptionBuilder.withArgName("com/args").withLongOpt("outputDir").isRequired().hasArg().withDescription("outputDir").create("outputDir");
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
                r2Args.setTag(commandLine.getOptionValue("tag"));
                r2Args.setMhapPath(commandLine.getOptionValue("mhapPath"));
                r2Args.setCpgPath(commandLine.getOptionValue("cpgPath"));
                r2Args.setRegion(commandLine.getOptionValue("region"));
                if (commandLine.hasOption("bcFile")) {
                    r2Args.setBcFile(commandLine.getOptionValue("bcFile"));
                }
                r2Args.setOutputDir(commandLine.getOptionValue("outputDir"));
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
        Option option1 = OptionBuilder.withArgName("com/args").withLongOpt("htGZ").isRequired().hasArg().withDescription("htGZ").create("H");
        Option option2 = OptionBuilder.withArgName("com/args").withLongOpt("bFile").hasArg().withDescription("bFile").create("B");
        Option option3 = OptionBuilder.withArgName("com/args").withLongOpt("bcFile").hasArg().withDescription("bcFile").create("S");
        Option option4 = OptionBuilder.withArgName("com/args").withLongOpt("cgGZ").isRequired().hasArg().withDescription("cgGZ").create("C");
        Option option5 = OptionBuilder.withArgName("com/args").withLongOpt("region").hasArg().withDescription("region").create("R");
        Option option6 = OptionBuilder.withArgName("com/args").withLongOpt("window").hasArg().withDescription("window").create("W");
        Option option7 = OptionBuilder.withArgName("com/args").withLongOpt("r_square").hasArg().hasArg().withDescription("r_square").create("R2");
        Option option8 = OptionBuilder.withArgName("com/args").withLongOpt("p_value").hasArg().withDescription("p_value").create("PV");
        Option option9 = OptionBuilder.withArgName("com/args").withLongOpt("oFile").hasArg().withDescription("oFile").create("O");
        Option option10 = OptionBuilder.withArgName("com/args").withLongOpt("qc").withDescription("qc").create("QC");
        options.addOption(option1).addOption(option2).addOption(option3).addOption(option4).addOption(option5).
                addOption(option6).addOption(option7).addOption(option8).addOption(option9).addOption(option10);

        BasicParser parser = new BasicParser();
        MHBDiscoveryArgs mhbDiscoveryArgs = new MHBDiscoveryArgs();

        CommandLine commandLine = parser.parse(options, args);
        if (commandLine.getOptions().length > 0) {
            if (commandLine.hasOption('h')) {
                HelpFormatter hf = new HelpFormatter();
                hf.printHelp("Options", options);
            } else {
                mhbDiscoveryArgs.setmHapPath(commandLine.getOptionValue("H"));
                if (commandLine.hasOption("R")) {
                    mhbDiscoveryArgs.setBedFile(commandLine.getOptionValue("R"));
                }
                if (commandLine.hasOption("S")) {
                    mhbDiscoveryArgs.setBcFile(commandLine.getOptionValue("S"));
                }
                mhbDiscoveryArgs.setCpgPath(commandLine.getOptionValue("C"));
                if (commandLine.hasOption("R")) {
                    mhbDiscoveryArgs.setRegion(commandLine.getOptionValue("R"));
                }
                if (commandLine.hasOption("W")) {
                    mhbDiscoveryArgs.setWindow(Integer.valueOf(commandLine.getOptionValue("W")));
                }
                if (commandLine.hasOption("R2")) {
                    mhbDiscoveryArgs.setrSquare(Double.valueOf(commandLine.getOptionValue("R2")));
                }
                if (commandLine.hasOption("PV")) {
                    mhbDiscoveryArgs.setpValue(Double.valueOf(commandLine.getOptionValue("PV")));
                }
                if (commandLine.hasOption("O")) {
                    mhbDiscoveryArgs.setOutFile(commandLine.getOptionValue("O"));
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
        Option option1 = OptionBuilder.withArgName("com/args").withLongOpt("tag").isRequired().hasArg().withDescription("tag").create("tag");
        Option option2 = OptionBuilder.withArgName("com/args").withLongOpt("mhapPath").isRequired().hasArg().withDescription("mhapPath").create("mhapPath");
        Option option3 = OptionBuilder.withArgName("com/args").withLongOpt("cpgPath").isRequired().hasArg().withDescription("cpgPath").create("cpgPath");
        Option option4 = OptionBuilder.withArgName("com/args").withLongOpt("bFile").hasArg().withDescription("bFile").create("bFile");
        Option option5 = OptionBuilder.withArgName("com/args").withLongOpt("region").hasArg().withDescription("region").create("region");
        Option option6 = OptionBuilder.withArgName("com/args").withLongOpt("bcFile").hasArg().withDescription("bcFile").create("bcFile");
        options.addOption(option1).addOption(option2).addOption(option3).addOption(option4).addOption(option5).addOption(option6);

        BasicParser parser = new BasicParser();
        HemiMArgs hemiMArgs = new HemiMArgs();

        CommandLine commandLine = parser.parse(options, args);
        if (commandLine.getOptions().length > 0) {
            if (commandLine.hasOption('h')) {
                HelpFormatter hf = new HelpFormatter();
                hf.printHelp("Options", options);
            } else {
                hemiMArgs.setTag(commandLine.getOptionValue("tag"));
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
}
