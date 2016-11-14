package de.qaware.chronix.client.benchmark.util;

import de.qaware.chronix.common.QueryUtil.CsvImporter;

import java.io.File;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by mcqueen666 on 07.10.16.
 */
public class CsvConverter {
    public static void main(String[] args){
        if(args.length < 1){
            printUsage();
            return;
        }

        List<File> directories = new LinkedList<>();
        for(String entry : args) {
            System.out.println("Directory: " + entry);
            File dir = new File(entry);
            // checking is done at conversion
            directories.add(dir);
        }

        System.out.println("\nConverting ... (this may take some time)");
        CsvImporter csvImporter = new CsvImporter();
        csvImporter.convertCsvToJson(directories);

    }

    public static void printUsage(){
        System.out.println("CsvConverter usage: convert [csvFilesDirectory1] [csvFilesDirectory2] ... ");
        System.out.println("Converted files will be saved to ~/chronixBenchmark/timeseries_records/");
        System.out.println("NOTICE: paths have to be absolute paths!");
    }
}
