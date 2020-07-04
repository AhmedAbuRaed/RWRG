package edu.upf.taln.lastus;

import gate.Gate;
import gate.util.GateException;

import java.io.File;

public class TopicModelingPreProcessMain {
    public static void main(String args[]) {
        if (args.length == 3) {
            try {
                Gate.init();
            } catch (GateException e) {
                e.printStackTrace();
            }
            String datasetFolderPath = args[1];
            String outputFolderPath = args[2];
            for (File clusterFolder : new File(datasetFolderPath).listFiles()) {
                System.out.println(clusterFolder);
                TopicModelingPreProcessCluster cluster = new TopicModelingPreProcessCluster(new File(clusterFolder.getPath() + "/ref/preprocessed"), new File(outputFolderPath + File.separator + clusterFolder.getName() + ".txt"));
            }
        } else {
            System.out.println("Please pass the dataset folder Path alongside the output folder path ...");
        }
    }
}
