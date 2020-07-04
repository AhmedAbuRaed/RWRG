package edu.upf.taln.lastus;

import com.opencsv.CSVParserBuilder;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import com.opencsv.exceptions.CsvException;
import org.apache.commons.io.FileUtils;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

public class CNNPostProcessScores {
    public static void main(String args[]) {
        if (args.length > 0) {
            if (args.length != 4) {
                System.out.println("Please pass three arguments : resources file, source folder and target folder paths ...");
                System.exit(-1);
            }
            File allFiles = new File(args[1]);
            File inputFolder = new File(args[2]);
            File outputFolder = new File(args[3]);

            if (!outputFolder.exists()) {
                outputFolder.mkdir();
            }

            if (!allFiles.exists()) {
                System.out.println("Please pass a valid resource file path as a first argument ...");
                System.exit(-1);
            }
            if (!inputFolder.exists()) {
                System.out.println("Please pass a valid input folder that contains scores of referenece papers from the CNN system as a second argument ...");
                System.exit(-1);
            }

            HashMap<String, List<String>> clustersReferences = new HashMap<String, List<String>>();
            try {
                BufferedReader br = new BufferedReader(new FileReader(allFiles));
                String line;
                while ((line = br.readLine()) != null) {
                    String[] fields = line.split("_");
                    String cluster = fields[0];
                    String reference = fields[1];
                    if (clustersReferences.containsKey(cluster)) {
                        List<String> references = clustersReferences.get(cluster);
                        references.add(reference);
                        clustersReferences.put(cluster, references);
                    } else {
                        List<String> references = new ArrayList<>();
                        references.add(reference);
                        clustersReferences.put(cluster, references);
                    }
                }
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

            for (String clusterKey : clustersReferences.keySet()) {
                System.out.println(clusterKey);

                File clusterFolder = new File(outputFolder.getPath() + File.separator + clusterKey);
                clusterFolder.mkdir();

                for (File file : inputFolder.listFiles()) {
                    String referenceName = file.getName().substring(0, file.getName().indexOf("_"));
                    System.out.println(referenceName);
                    if (referenceName.contains("@")) {
                        String clusterName = referenceName.substring(referenceName.indexOf("@") + 1);
                        referenceName = referenceName.substring(0, referenceName.indexOf("@"));

                        if (clusterKey.equals(clusterName)) {
                            //System.out.println(clusterName + "->" + referenceName);
                            try {
                                FileUtils.copyFile(file, new File(clusterFolder.getPath() + File.separator + file.getName()));
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    } else {
                        Set<String> clusters = getKeysByValue(clustersReferences, referenceName);
                        if (clusters.size() == 0) {
                            System.out.println("No cluster found for a reference paper");
                        } else {
                            if (clusters.size() > 1) {
                                System.out.println("More than one cluster belong to the same reference ." + referenceName);
                            }
                        }

                        if (clusters.contains(clusterKey)) {
                            //System.out.println(clusterName + "->" + referenceName);
                            try {
                                FileUtils.copyFile(file, new File(clusterFolder.getPath() + File.separator + file.getName()));
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
            }

        } else {
            System.out.println("Please pass three arguments : resources file, source folder and target folder paths ...");
            System.exit(-1);
        }
    }

    private static Set<String> getKeysByValue(Map<String, List<String>> map, String value) {
        return map.entrySet()
                .stream()
                .filter(entry -> entry.getValue().contains(value))
                .map(Map.Entry::getKey)
                .collect(Collectors.toSet());
    }
}
