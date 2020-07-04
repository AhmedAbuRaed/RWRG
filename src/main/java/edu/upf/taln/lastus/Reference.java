package edu.upf.taln.lastus;

import org.apache.http.Header;
import org.apache.http.message.BasicHeader;
import org.elasticsearch.client.RestClient;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

public class Reference {
    protected String name;
    protected String title;
    protected String Abstract;
    protected int numberOfCitingPapers;
    protected List<Sentence> sentencesList;
    protected List<ScoredSentence> scoredSentencesList;

    RestClient elasticRestClient = Utilities.buildRestClientConnection("scipub-taln.upf.edu", 80, "http", Optional.of("elastic"), Optional.empty());
    Header header = new BasicHeader("Ocp-Apim-Subscription-Key", "ad6ec753e04f4f7ea0aa9e171d74025c");
    RestClient madRestClient = Utilities.buildRestClientConnection("api.labs.cognitive.microsoft.com", 443, "https", Optional.empty(), Optional.of(new Header[]{header}));

    public Reference(String name, String title, String Abstract, List<Sentence> sentencesList,List<ScoredSentence> scoredSentences) {
        this.name = name;
        this.title = title;
        this.Abstract = Abstract;
        computeNumberOfCitingPapers(this.title);
        this.sentencesList = sentencesList;
        this.scoredSentencesList = scoredSentences;
    }

    private void computeNumberOfCitingPapers(String title) {
        try {
            int numberOfCitingPapers = 0;
            //Add the citing paper Weight
            S2CorpusMetaData referenceS2CorpusMetaData = S2CorpusMetaData.getS2CorpusMetaDataFromTitle(title.toLowerCase(), elasticRestClient);
            MAGMetaData referenceMAGMetaData = MAGMetaData.getMAGMetaDataFromTitle(title.toLowerCase(), madRestClient);
            ACLMetaData referenceACLMetaData = ACLMetaData.getACLMetaDataFromTitle(title.toLowerCase(), elasticRestClient);

            if (referenceS2CorpusMetaData != null) {
                numberOfCitingPapers = referenceS2CorpusMetaData.getInCitations().size();
            } else if (referenceMAGMetaData != null) {
                numberOfCitingPapers = MAGMetaData.getCitingPapersMetaData(referenceMAGMetaData, madRestClient).size();
            } else if (referenceACLMetaData != null) {
                numberOfCitingPapers = ACLMetaData.getCitingPapersMetaData(referenceACLMetaData, elasticRestClient).size();
            } else {
                System.out.println("Nothing Found for Paper Named : " + this.name +  " Titled: " + title);
            }

            this.numberOfCitingPapers = numberOfCitingPapers;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}