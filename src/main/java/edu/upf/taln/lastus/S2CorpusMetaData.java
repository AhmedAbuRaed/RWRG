package edu.upf.taln.lastus;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.util.EntityUtils;
import org.elasticsearch.client.Response;
import org.elasticsearch.client.ResponseException;
import org.elasticsearch.client.RestClient;

import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class S2CorpusMetaData {
    private List<Author> authors;
    private String id;
    private List<String> inCitations;
    private List<String> keyPhrases;
    private List<String> outCitations;
    private String paperAbstract;
    @JsonProperty
    private List<String> pdfUrls;
    @JsonProperty
    private String s2Url;
    private String title;
    private String venue;
    private Integer year;

    public List<Author> getAuthors() {
        return authors;
    }

    public void setAuthors(List<Author> authors) {
        this.authors = authors;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public List<String> getInCitations() {
        return inCitations;
    }

    public void setInCitations(List<String> inCitations) {
        this.inCitations = inCitations;
    }

    public List<String> getKeyPhrases() {
        return keyPhrases;
    }

    public void setKeyPhrases(List<String> keyPhrases) {
        this.keyPhrases = keyPhrases;
    }

    public List<String> getOutCitations() {
        return outCitations;
    }

    public void setOutCitations(List<String> outCitations) {
        this.outCitations = outCitations;
    }

    public String getPaperAbstract() {
        return paperAbstract;
    }

    public void setPaperAbstract(String paperAbstract) {
        this.paperAbstract = paperAbstract;
    }

    public List<String> getPdfUrls() {
        return pdfUrls;
    }

    public void setPdfUrls(List<String> pdfUrls) {
        this.pdfUrls = pdfUrls;
    }

    public String getS2Url() {
        return s2Url;
    }

    public void setS2Url(String s2Url) {
        this.s2Url = s2Url;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getVenue() {
        return venue;
    }

    public void setVenue(String venue) {
        this.venue = venue;
    }

    public Integer getYear() {
        return year;
    }

    public void setYear(Integer year) {
        this.year = year;
    }

    public static class Author {
        private List<String> ids;
        private String name;

        public List<String> getIds() {
            return ids;
        }

        public void setIds(List<String> ids) {
            this.ids = ids;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }

    //Helping Methods
    public static S2CorpusMetaData getS2CorpusMetaDataFromID(String id, RestClient restClient) throws IOException {
        try {
            Map<String, String> params = Collections.<String, String>emptyMap();
            String request = "";
            request += "/" + "semantic_scholar";
            request += "/" + "paper";
            request += "/" + id;
            Response response = restClient.performRequest(
                    "GET",
                    request,
                    params);
            if (response.getStatusLine().getStatusCode() == 200) {
                ObjectMapper mapper = new ObjectMapper();
                return S2CorpusMetaData.getS2CorpusMetaDataFromJsonString(mapper.readTree(EntityUtils.toString(response.getEntity())).path("_source").toString());
            }
        } catch (ResponseException response) {
            if (response.getResponse().getStatusLine().getStatusCode() == 404) {
                return null;
            } else {
                response.getStackTrace();
            }
        } catch (IOException e) {
            e.getStackTrace();
        }
        return null;
    }

    public static S2CorpusMetaData getS2CorpusMetaDataFromTitle(String title, RestClient restClient) throws IOException {
        JsonNode respond = Utilities.searchElasticSearch(restClient, "semantic_scholar", "paper", "title", title.toLowerCase());
        if(respond == null){return null;}
        for (JsonNode node : respond.path("hits").path("hits")) {
            JsonNode source = node.path("_source");
            return S2CorpusMetaData.getS2CorpusMetaDataFromJsonString(source.toString());
        }
        return null;
    }

    public static S2CorpusMetaData getS2CorpusMetaDataFromJsonString(String jsonElementString) throws IOException {
        try {
            ObjectMapper mapper = new ObjectMapper();
            return mapper.readValue(jsonElementString, S2CorpusMetaData.class);
        } catch (Exception e) {
            return null;
        }
    }

    public static ArrayList<S2CorpusMetaData> getCitingPapersMetaData(S2CorpusMetaData targetPaperMetaData, RestClient restClient) throws IOException {
        ArrayList<S2CorpusMetaData> citingPapers = new ArrayList<S2CorpusMetaData>();
        for (String citingPaperID : targetPaperMetaData.getInCitations()) {
            citingPapers.add(S2CorpusMetaData.getS2CorpusMetaDataFromID(citingPaperID, restClient));
        }
        return citingPapers;
    }
}
