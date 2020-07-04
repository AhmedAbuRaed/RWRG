package edu.upf.taln.lastus;

import java.io.*;
import java.util.*;

public class UnsupervisedFitScoredSentencesFilesFormat {
    public static void main(String[] args) {
        File inputFolder = new File("D:\\Research\\UPF\\Projects\\RWRG\\Systems\\MJScoredSentences");
        File outputFolder = new File("D:\\Research\\UPF\\Projects\\RWRG\\Systems\\ReferencesScoredSentences\\MJScoredSentences");

        for(File folder : inputFolder.listFiles())
        {
            File outputF = new File(outputFolder.getPath() + File.separator + folder.getName());
            outputF.mkdir();
            fitCSVFormat(folder.getPath(), outputF.getPath());
        }
    }

    private static void fitCSVFormat(String inputFolderPath, String outputFolderPath) {
        try {
            for (File file : new File(inputFolderPath).listFiles()) {
                BufferedReader reader = new BufferedReader(new FileReader(file));

                Map<Double, List<String>> map = new TreeMap<Double, List<String>>(Collections.reverseOrder());
                String line;
                while ((line = reader.readLine()) != null) {
                    Double key = Double.valueOf(line.split(",")[1]);
                    List<String> l = map.get(key);
                    if (l == null) {
                        l = new LinkedList<String>();
                        map.put(key, l);
                    }
                    l.add(line);
                }

                reader.close();
                FileWriter writer = new FileWriter(new File(outputFolderPath + File.separator + file.getName()));
                for (List<String> list : map.values()) {
                    for (String val : list) {
                        writer.write(val);
                        writer.write("\n");
                    }
                }
                writer.close();
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
