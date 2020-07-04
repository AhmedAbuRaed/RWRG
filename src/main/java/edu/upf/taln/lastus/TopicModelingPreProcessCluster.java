package edu.upf.taln.lastus;

import com.opencsv.CSVParserBuilder;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import com.opencsv.exceptions.CsvException;
import gate.Annotation;
import gate.Document;
import gate.Factory;
import gate.creole.ResourceInstantiationException;
import gate.util.InvalidOffsetException;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class TopicModelingPreProcessCluster {
    TopicModelingPreProcessCluster(File preprocessedReferencesFoldePath, File outputFile) {
        FileWriter fileWriter = null;
        try {
            fileWriter = new FileWriter(outputFile);
        } catch (IOException e) {
            e.printStackTrace();
        }

        for (File referencePreProcessedFile : preprocessedReferencesFoldePath.listFiles()) {
            try {
                String referencePaper = referencePreProcessedFile.getName();
                System.out.println(referencePaper);
                //Start storing title and abstract sentences
                Document doc = Factory.newDocument(new URL("file:///" + preprocessedReferencesFoldePath.getPath() + File.separator + referencePaper + File.separator + referencePaper + "_PreProcessed_gate.xml"), "UTF-8");
                List<Annotation> refTitle = doc.getAnnotations("Original markups").get("title").inDocumentOrder();
                List<Annotation> refAbstract = doc.getAnnotations("Original markups").get("abstract").inDocumentOrder();
                List<Annotation> refAbstractText = doc.getAnnotations("Original markups").get("abstract_text").inDocumentOrder();

                Long titleStart = 0l, titleEnd = 0l, abstractStart = 0l, abstractEnd = 0l;

                if (refTitle.size() > 0) {
                    titleStart = refTitle.get(0).getStartNode().getOffset();
                    titleEnd = refTitle.get(0).getEndNode().getOffset();
                }
                if (refAbstract.size() > 0) {
                    abstractStart = refAbstract.get(0).getStartNode().getOffset();
                    abstractEnd = refAbstract.get(0).getEndNode().getOffset();
                } else if (refAbstractText.size() > 0) {
                    abstractStart = refAbstractText.get(0).getStartNode().getOffset();
                    abstractEnd = refAbstractText.get(0).getEndNode().getOffset();
                }

                List<Annotation> titleTokens = doc.getAnnotations("Original markups").get("Token").get(titleStart, titleEnd).inDocumentOrder();
                List<Annotation> abstractTokens = doc.getAnnotations("Original markups").get("Token").get(abstractStart, abstractEnd).inDocumentOrder();

                StringBuilder line = new StringBuilder();
                line.append("\"" + referencePaper + "\",");

                for(int tt = 0; tt< titleTokens.size(); tt++)
                {
                    Annotation token = titleTokens.get(tt);
                    if ((token.getFeatures().get("category").toString().equals("NN") ||
                            token.getFeatures().get("category").toString().equals("NNS") ||
                            token.getFeatures().get("category").toString().equals("NNP") ||
                            token.getFeatures().get("category").toString().equals("NNPS") ||
                            token.getFeatures().get("category").toString().equals("VB") ||
                            token.getFeatures().get("category").toString().equals("VBD") ||
                            token.getFeatures().get("category").toString().equals("VBG") ||
                            token.getFeatures().get("category").toString().equals("VBN") ||
                            token.getFeatures().get("category").toString().equals("VBP") ||
                            token.getFeatures().get("category").toString().equals("VBZ") ||
                            token.getFeatures().get("category").toString().equals("JJ") ||
                            token.getFeatures().get("category").toString().equals("JJR") ||
                            token.getFeatures().get("category").toString().equals("JJS") ||
                            token.getFeatures().get("category").toString().equals("RB") ||
                            token.getFeatures().get("category").toString().equals("RBR") ||
                            token.getFeatures().get("category").toString().equals("RBS")) &&
                            (token.getEndNode().getOffset() - token.getStartNode().getOffset() > 2))
                    {
                        line.append(doc.getContent().getContent(token.getStartNode().getOffset(), token.getEndNode().getOffset()).toString().toLowerCase().trim().replaceAll(",", "") + ",");
                    }
                }
                for(int at = 0; at < abstractTokens.size(); at++)
                {
                    Annotation token = abstractTokens.get(at);
                    if ((token.getFeatures().get("category").toString().equals("NN") ||
                            token.getFeatures().get("category").toString().equals("NNS") ||
                            token.getFeatures().get("category").toString().equals("NNP") ||
                            token.getFeatures().get("category").toString().equals("NNPS") ||
                            token.getFeatures().get("category").toString().equals("VB") ||
                            token.getFeatures().get("category").toString().equals("VBD") ||
                            token.getFeatures().get("category").toString().equals("VBG") ||
                            token.getFeatures().get("category").toString().equals("VBN") ||
                            token.getFeatures().get("category").toString().equals("VBP") ||
                            token.getFeatures().get("category").toString().equals("VBZ") ||
                            token.getFeatures().get("category").toString().equals("JJ") ||
                            token.getFeatures().get("category").toString().equals("JJR") ||
                            token.getFeatures().get("category").toString().equals("JJS") ||
                            token.getFeatures().get("category").toString().equals("RB") ||
                            token.getFeatures().get("category").toString().equals("RBR") ||
                            token.getFeatures().get("category").toString().equals("RBS")) &&
                            (token.getEndNode().getOffset() - token.getStartNode().getOffset() > 2))
                    {
                        line.append(doc.getContent().getContent(token.getStartNode().getOffset(), token.getEndNode().getOffset()).toString().toLowerCase().trim().replaceAll(",", "") + ",");
                    }
                }
                line.deleteCharAt(line.length() - 1);
                line.append("\n");
                fileWriter.write(line.toString());
                Factory.deleteResource(doc);

            } catch (IOException e) {
                e.printStackTrace();
            } catch (ResourceInstantiationException e) {
                e.printStackTrace();
            } catch (InvalidOffsetException e) {
                e.printStackTrace();
            }
        }
        try {
            fileWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
