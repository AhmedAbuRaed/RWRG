package edu.upf.taln.lastus;

import com.google.common.primitives.Doubles;
import com.google.common.primitives.Ints;
import com.opencsv.CSVParserBuilder;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import com.opencsv.exceptions.CsvException;
import gate.Annotation;
import gate.Document;
import gate.Factory;
import gate.creole.ResourceInstantiationException;
import gate.util.InvalidOffsetException;
import org.apache.commons.lang3.ArrayUtils;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

public class Cluster {
    protected String name;
    protected List<Reference> references;

    //N -> sentence compression level: total number of sentences to generate in case a summarization was called
    //M -> number of sentences to generate per reference
    //K -> number of reference papers
    Cluster(String name, String referencesScoredSentencesFolderPath, String preprocessedReferencesFoldePath) {
        this.name = name;
        this.references = new ArrayList<>();
        for (File referenceScoredSentencesFile : new File(referencesScoredSentencesFolderPath).listFiles()) {
            try {
                if (!referenceScoredSentencesFile.getName().endsWith(".csv")) {
                    System.out.println("Unknown references scored sentences Files format ...");
                    System.exit(-1);
                }

                String referencePaper = referenceScoredSentencesFile.getName().substring(0, referenceScoredSentencesFile.getName().indexOf(".csv"));
                Reader reader = Files.newBufferedReader(Paths.get(referenceScoredSentencesFile.getPath()));
                CSVReader csvReader = new CSVReaderBuilder(reader).withCSVParser(new CSVParserBuilder().withSeparator(',').build()).build();
                List<String[]> records = csvReader.readAll();

                List<ScoredSentence> scoredSentences = new ArrayList<ScoredSentence>();
                for (int i = 0; i < records.size(); i++) {
                    String[] sentencePair = records.get(i);
                    if (sentencePair.length != 2) {
                        System.out.println("Unknown references scored sentences Files format ...");
                        System.exit(-1);
                    }
                    ScoredSentence scoredSentence = new ScoredSentence(sentencePair[0], Float.valueOf(sentencePair[1]));
                    scoredSentences.add(scoredSentence);
                }

                //Start storing title and abstract sentences
                Document doc = Factory.newDocument(new URL("file:///" + preprocessedReferencesFoldePath + File.separator + name
                        + "/ref/preprocessed/" + referencePaper + File.separator + referencePaper + "_PreProcessed_gate.xml"), "UTF-8");
                List<Annotation> refTitle = doc.getAnnotations("Original markups").get("title").inDocumentOrder();
                List<Annotation> refAbstract = doc.getAnnotations("Original markups").get("abstract").inDocumentOrder();
                List<Annotation> refAbstractText = doc.getAnnotations("Original markups").get("abstract_text").inDocumentOrder();
                List<Annotation> refSentences = doc.getAnnotations("Original markups").get("Sentence").inDocumentOrder();
                String title = "";
                String Abstract = "";

                if (refTitle.size() > 0) {
                    title = doc.getContent().getContent(refTitle.get(0).getStartNode().getOffset(), refTitle.get(0).getEndNode().getOffset()).toString();
                }
                if (refAbstract.size() > 0) {
                    Abstract = doc.getContent().getContent(refAbstract.get(0).getStartNode().getOffset(), refAbstract.get(0).getEndNode().getOffset()).toString();
                } else if (refAbstractText.size() > 0) {
                    Abstract = doc.getContent().getContent(refAbstractText.get(0).getStartNode().getOffset(), refAbstractText.get(0).getEndNode().getOffset()).toString();
                }

                List<Sentence> sentences = new ArrayList<Sentence>();
                for (int i = 0; i < refSentences.size(); i++) {
                    Annotation refSentence = refSentences.get(i);
                    String sid = refSentence.getFeatures().get("sid").toString();
                    String text = doc.getContent().getContent(refSentence.getStartNode().getOffset(), refSentence.getEndNode().getOffset()).toString();
                    Sentence sentence = new Sentence(sid, text);
                    sentences.add(sentence);
                }

                Reference reference = new Reference(referencePaper, title, Abstract, sentences, scoredSentences);
                this.references.add(reference);
                reference.elasticRestClient.close();
                reference.madRestClient.close();
                Factory.deleteResource(doc);
            } catch (IOException e) {
                e.printStackTrace();
            } catch (CsvException e) {
                e.printStackTrace();
            } catch (ResourceInstantiationException e) {
                e.printStackTrace();
            } catch (InvalidOffsetException e) {
                e.printStackTrace();
            }
        }
    }

    int[] computeReferencesMValues(int N) {
        int[] M = new int[this.references.size()];

        int required = N;
        //start by assigning a value of 1 for all references (minimum value)
        for (int i = 0; i < this.references.size(); i++) {
            M[i] = 1;
            required--;
        }

        if (required > 0) {
            //assign based on Weight
            double[] numCitingPapers = new double[this.references.size()];
            for (int i = 0; i < this.references.size(); i++) {
                numCitingPapers[i] = this.references.get(i).numberOfCitingPapers;
            }
            int left = required;
            double[] ratios = Utilities.Softmax(numCitingPapers);

            while (required > 0) {
                for (int i = 0; i < ratios.length; i++) {
                    int value = ((int) (((ratios[i] * 0.01) * left) + 0.5));
                    M[i] += value;
                    required -= value;
                    if (required <= 0) {
                        break;
                    }
                }
            }
        }

        return M;
    }

    String generateSummary(int N, List<Integer> referecesTopics) {
        StringBuilder summary = new StringBuilder();
        //at minimum all reference papers should be mentioned
        int K = this.references.size();
        if (N < K) {
            N = K;
            System.out.println("value of N is set to " + N + ". At minimum each reference paper will be presented with a sentence.");
        }
        if (this.references.size() != referecesTopics.size()) {
            System.out.println("All references should be presented in the topics file. Number of references does not match the number of topics assigned provided by the topics file  ...");
            System.exit(-1);
        }

        int[] M = computeReferencesMValues(N);
        System.out.println(Arrays.toString(M));

        List<List<String>> refOrderedSelectedSentences = new ArrayList<>();
        for (int i = 0; i < this.references.size(); i++) {
            List<Sentence> sentencesList = this.references.get(i).sentencesList;
            List<ScoredSentence> scoredSentences = this.references.get(i).scoredSentencesList;

            List<ScoredSentence> selectedSentences = scoredSentences.subList(0, M[i]);
            List<String> selectedSentencesSIDs = new ArrayList<>();
            for (int ss = 0; ss < selectedSentences.size(); ss++) {
                selectedSentencesSIDs.add(selectedSentences.get(ss).getSid());
            }

            List<String> selectedSentencesOrderedSIDs = new ArrayList<>();
            for (int s = 0; s < sentencesList.size(); s++) {
                Sentence sentence = sentencesList.get(s);
                if (selectedSentencesSIDs.contains(sentence.sid)) {
                    selectedSentencesOrderedSIDs.add(sentence.sid);
                }
            }
            refOrderedSelectedSentences.add(selectedSentencesOrderedSIDs);
        }
        System.out.println(ArrayUtils.toString(refOrderedSelectedSentences));
        HashMap<Integer, List<Integer>> topicReferenceIndices = new HashMap<Integer, List<Integer>>();
        for (int i = 0; i < this.references.size(); i++) {
            if (topicReferenceIndices.containsKey(referecesTopics.get(i))) {
                List<Integer> indeces = topicReferenceIndices.get(referecesTopics.get(i));
                indeces.add(i);
                topicReferenceIndices.put(referecesTopics.get(i), indeces);
            } else {
                List<Integer> indeces = new ArrayList<>();
                indeces.add(i);
                topicReferenceIndices.put(referecesTopics.get(i), indeces);
            }
        }

        topicReferenceIndices.forEach((key, value) -> System.out.println(key + " " + value));

        Set<Integer> done = new HashSet<>();
        int[] citingCopy = M;
        while (done.size() != this.references.size()) {
            int largest = Utilities.getIndexOfLargest(Doubles.toArray(Ints.asList(citingCopy)));
            List<Sentence> sentencesList = this.references.get(largest).sentencesList;
            summary.append("(" + this.references.get(largest).name.replaceAll("-", " ") + ") ");
            List<String> sids = refOrderedSelectedSentences.get(largest);
            for (int i = 0; i < sids.size(); i++) {
                summary.append(getSentenceFromSID(sentencesList, sids.get(i)) + " ");
            }
            done.add(largest);
            citingCopy[largest] = 0;

            List<Integer> indices = getSimilarTopicRefereneces(topicReferenceIndices, largest);
            for (Integer index : indices) {
                summary.append("(" + this.references.get(index).name.replaceAll("-", " ") + ") ");
                List<Sentence> adjSentencesList = this.references.get(index).sentencesList;
                List<String> sidsAdj = refOrderedSelectedSentences.get(index);
                for (int i = 0; i < sidsAdj.size(); i++) {
                    summary.append(getSentenceFromSID(adjSentencesList, sidsAdj.get(i)) + " ");
                }
                done.add(index);
                citingCopy[index] = 0;
            }
        }

        return summary.toString();
    }

    String generateBaselineSummary(int N, List<Integer> referecesTopics) {
        StringBuilder summary = new StringBuilder();
        //at minimum all reference papers should be mentioned
        int K = this.references.size();
        if (N < K) {
            N = K;
            System.out.println("value of N is set to " + N + ". At minimum each reference paper will be presented with a sentence.");
        }
        if (this.references.size() != referecesTopics.size()) {
            System.out.println("All references should be presented in the topics file. Number of references does not match the number of topics assigned provided by the topics file  ...");
            System.exit(-1);
        }

        int m = (int) ((N/ (double) K) + 0.5);
        List<Integer> values = new ArrayList<>();
        for(int i=0; i< this.references.size(); i++)
        {
            values.add(m);
        }
        int [] M = values.stream().mapToInt(i->i).toArray();
        System.out.println(Arrays.toString(M));

        List<List<String>> refOrderedSelectedSentences = new ArrayList<>();
        for (int i = 0; i < this.references.size(); i++) {
            List<Sentence> sentencesList = this.references.get(i).sentencesList;
            List<ScoredSentence> scoredSentences = this.references.get(i).scoredSentencesList;

            List<ScoredSentence> selectedSentences = scoredSentences.subList(0, M[i]);
            List<String> selectedSentencesSIDs = new ArrayList<>();
            for (int ss = 0; ss < selectedSentences.size(); ss++) {
                selectedSentencesSIDs.add(selectedSentences.get(ss).getSid());
            }

            List<String> selectedSentencesOrderedSIDs = new ArrayList<>();
            for (int s = 0; s < sentencesList.size(); s++) {
                Sentence sentence = sentencesList.get(s);
                if (selectedSentencesSIDs.contains(sentence.sid)) {
                    selectedSentencesOrderedSIDs.add(sentence.sid);
                }
            }
            refOrderedSelectedSentences.add(selectedSentencesOrderedSIDs);
        }
        System.out.println(ArrayUtils.toString(refOrderedSelectedSentences));
        HashMap<Integer, List<Integer>> topicReferenceIndices = new HashMap<Integer, List<Integer>>();
        for (int i = 0; i < this.references.size(); i++) {
            if (topicReferenceIndices.containsKey(referecesTopics.get(i))) {
                List<Integer> indeces = topicReferenceIndices.get(referecesTopics.get(i));
                indeces.add(i);
                topicReferenceIndices.put(referecesTopics.get(i), indeces);
            } else {
                List<Integer> indeces = new ArrayList<>();
                indeces.add(i);
                topicReferenceIndices.put(referecesTopics.get(i), indeces);
            }
        }

        topicReferenceIndices.forEach((key, value) -> System.out.println(key + " " + value));

        Set<Integer> done = new HashSet<>();
        int[] citingCopy = M;
        while (done.size() != this.references.size()) {
            int largest = Utilities.getIndexOfLargest(Doubles.toArray(Ints.asList(citingCopy)));
            List<Sentence> sentencesList = this.references.get(largest).sentencesList;
            summary.append("(" + this.references.get(largest).name.replaceAll("-", " ") + ") ");
            List<String> sids = refOrderedSelectedSentences.get(largest);
            for (int i = 0; i < sids.size(); i++) {
                summary.append(getSentenceFromSID(sentencesList, sids.get(i)) + " ");
            }
            done.add(largest);
            citingCopy[largest] = 0;

            List<Integer> indices = getSimilarTopicRefereneces(topicReferenceIndices, largest);
            for (Integer index : indices) {
                summary.append("(" + this.references.get(index).name.replaceAll("-", " ") + ") ");
                List<Sentence> adjSentencesList = this.references.get(index).sentencesList;
                List<String> sidsAdj = refOrderedSelectedSentences.get(index);
                for (int i = 0; i < sidsAdj.size(); i++) {
                    summary.append(getSentenceFromSID(adjSentencesList, sidsAdj.get(i)) + " ");
                }
                done.add(index);
                citingCopy[index] = 0;
            }
        }

        return summary.toString();
    }

    String generateNoTMBaselineSummary(int N, List<Integer> referecesTopics) {
        StringBuilder summary = new StringBuilder();
        //at minimum all reference papers should be mentioned
        int K = this.references.size();
        if (N < K) {
            N = K;
            System.out.println("value of N is set to " + N + ". At minimum each reference paper will be presented with a sentence.");
        }
        if (this.references.size() != referecesTopics.size()) {
            System.out.println("All references should be presented in the topics file. Number of references does not match the number of topics assigned provided by the topics file  ...");
            System.exit(-1);
        }

        int m = (int) ((N/ (double) K) + 0.5);
        List<Integer> values = new ArrayList<>();
        for(int i=0; i< this.references.size(); i++)
        {
            values.add(m);
        }
        int [] M = values.stream().mapToInt(i->i).toArray();
        System.out.println(Arrays.toString(M));

        for (int i = 0; i < this.references.size(); i++) {
            summary.append("(" + this.references.get(i).name.replaceAll("-", " ") + ") ");
            List<Sentence> sentencesList = this.references.get(i).sentencesList;
            List<ScoredSentence> scoredSentences = this.references.get(i).scoredSentencesList;

            List<ScoredSentence> selectedSentences = scoredSentences.subList(0, M[i]);
            List<String> selectedSentencesSIDs = new ArrayList<>();
            for (int ss = 0; ss < selectedSentences.size(); ss++) {
                selectedSentencesSIDs.add(selectedSentences.get(ss).getSid());
            }

            List<String> selectedSentencesOrderedSIDs = new ArrayList<>();
            for (int s = 0; s < sentencesList.size(); s++) {
                Sentence sentence = sentencesList.get(s);
                if (selectedSentencesSIDs.contains(sentence.sid)) {
                    summary.append(getSentenceFromSID(sentencesList, sentence.sid) + " ");
                }
            }
        }

        return summary.toString();
    }

    private int getReferenceTopicID(HashMap<Integer, List<Integer>> topicReferenceIndices, int refIndex) {
        for (int key : topicReferenceIndices.keySet()) {
            List<Integer> indeces = topicReferenceIndices.get(key);
            for (int index : indeces) {
                if (index == refIndex) {
                    return key;
                }
            }
        }
        return -1;
    }

    private List<Integer> getSimilarTopicRefereneces(HashMap<Integer, List<Integer>> topicReferenceIndices, int refIndex) {
        List<Integer> res = new ArrayList<>();
        for (int key : topicReferenceIndices.keySet()) {
            List<Integer> indices = topicReferenceIndices.get(key);
            for (int index : indices) {
                if (index == refIndex) {
                    res = indices;
                }
            }
        }
        res.remove((Object) refIndex);
        return res;
    }

    private String getSentenceFromSID(List<Sentence> sentences, String sid) {
        for (int i = 0; i < sentences.size(); i++) {
            Sentence sentence = sentences.get(i);
            if (sentence.sid.equals(sid)) {
                return sentence.text;
            }
        }
        return null;
    }
}
