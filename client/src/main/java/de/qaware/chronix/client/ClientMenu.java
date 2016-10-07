package de.qaware.chronix.client;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by mcqueen666 on 06.10.16.
 */
public class ClientMenu {
    public static void main(String[] args){
        if(args.length == 0){
            printUsage();
            printFunctions();
            return;
        }

        String function = args[0];
        List<String> arguments = new ArrayList<String>(Arrays.asList(args));
        arguments.remove(0);
        args = arguments.toArray(new String[]{});

        switch (function){
            case "setup": {
                BenchmarkSetup.main(args);
                break;
            }

            case "import":{
                BenchmarkImport.main(args);
                break;
            }
            case "convert":
            case "benchmark":
            case "build":
            case "start":
            case "stop":
            case "clean":

        }

    }

    private static void printUsage(){
        System.out.println("Usage: java -jar chronixClient.jar [function]");
        System.out.println("For more help per function call with function only.\n");
    }

    private static void printFunctions(){
        System.out.println("Functions: \n");
        System.out.println("setup:      configure client and server.");
        System.out.println("import:     import json time series.");
        System.out.println("convert:    convert .csv to json time series.");
        System.out.println("benchmark:  run the benchmark (after you have imported something)");
        System.out.println("build:      builds docker containers on the server");
        System.out.println("start:      starts docker containers on the server");
        System.out.println("stop:       stops docker containers on the server");
        System.out.println("clean:      purge all data from TSDBs on the server");
    }


}
