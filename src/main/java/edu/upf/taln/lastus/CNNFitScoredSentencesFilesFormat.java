package edu.upf.taln.lastus;

import java.io.*;
import java.util.*;

public class CNNFitScoredSentencesFilesFormat {

    public static void main(String[] args) {
        File inputFolder = new File("D:\\Research\\UPF\\Projects\\RWRG\\Systems\\New folder");
        File outputFolder = new File("D:\\Research\\UPF\\Projects\\RWRG\\Systems\\ReferencesScoredSentences");

        for(File folder : inputFolder.listFiles())
        {
            for(File cluster: folder.listFiles()) {
                File outputF = new File(outputFolder.getPath() + File.separator + folder.getName() + File.separator + cluster.getName());
                outputF.mkdirs();
                fitCSVFormat(cluster.getPath(), outputF.getPath());
            }
        }
    }

    private static void fitCSVFormat(String inputFolderPath, String outputFolderPath) {
        try {
            for (File file : new File(inputFolderPath).listFiles()) {
                BufferedReader reader = new BufferedReader(new FileReader(file));
                FileWriter writer = new FileWriter(new File(outputFolderPath + File.separator + file.getName().substring(0, file.getName().indexOf("_")) + ".csv"));
                String line;
                while ((line = reader.readLine()) != null) {
                    line = line.replaceAll("\t", ",");
                    writer.write(line + "\n");
                }
                reader.close();
                writer.close();
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
