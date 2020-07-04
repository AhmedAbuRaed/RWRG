package edu.upf.taln.lastus;

import gate.Gate;
import gate.util.GateException;
import org.apache.commons.lang3.tuple.Pair;

import java.io.*;
import java.net.URISyntaxException;
import java.util.HashMap;

public class Main {
    public static void main(String[] args) {
        if (args.length == 5) {
            try {
                Gate.init();
            } catch (GateException e) {
                e.printStackTrace();
            }

            String datasetFolderPath = args[1];
            String scoredSentencesFolderPath = args[2];
            String referencesTopicsFolderPath = args[3];
            String outputSummariesPath = args[4];

            ClassLoader classloader = Thread.currentThread().getContextClassLoader();
            File targetSummariesLengths = null;
            try {
                targetSummariesLengths = new File(classloader.getResource("TargetSummariesLengths.txt").toURI());
            } catch (URISyntaxException e) {
                e.printStackTrace();
            }
            HashMap<String, Pair<String, String>> targetLengths = new HashMap<String, Pair<String, String>>();
            BufferedReader br = null;
            try {
                br = new BufferedReader(new FileReader(targetSummariesLengths));
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            String line;
            try {
                while ((line = br.readLine()) != null) {
                    String[] temp = line.split(",");
                    targetLengths.put(temp[0], Pair.of(temp[1], temp[2]));
                }
                br.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

            for (File systemFolder : new File(scoredSentencesFolderPath).listFiles()) {
                System.out.println("System: " + systemFolder.getName());
                for (File clusterFolder : systemFolder.listFiles()) {
                    if (clusterFolder.getName().equals("C08-1066")) {
                        System.out.println("Cluster: " + clusterFolder.getName());
                        Cluster cluster = new Cluster(clusterFolder.getName(), clusterFolder.getPath(), datasetFolderPath);
                        System.out.println("Cluster Initiated ...");
                        String summary = cluster.generateNoTMBaselineSummary(Integer.parseInt(targetLengths.get(clusterFolder.getName()).getLeft()), Utilities.getReferencesTopicsIDs(new File(referencesTopicsFolderPath + File.separator + clusterFolder.getName() + ".txt"), cluster.references));
                        FileWriter fileWriter = null;
                        try {
                            fileWriter = new FileWriter(new File(outputSummariesPath + File.separator + clusterFolder.getName() + "_BL-" + systemFolder.getName().replaceAll("_", "-") + ".txt"));
                            fileWriter.write(summary);
                            fileWriter.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
                System.gc();
            }
            System.out.println("Done ...");
        } else {
            System.out.println("Please pass the dataset folder Path alongside the output folder path and Topics Folder Path ...");
        }
    }
}
