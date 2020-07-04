package edu.upf.taln.lastus;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.util.EntityUtils;
import org.elasticsearch.client.Response;
import org.elasticsearch.client.RestClient;

import java.io.IOException;
import java.util.*;

public class MAGMetaData {
    private Float logprob;
    private Float prob;
    @JsonProperty("Id")
    private Long id;
    @JsonProperty("Ti")
    private String title;
    @JsonProperty("L")
    private String language;
    @JsonProperty("Y")
    private Integer year;
    @JsonProperty("D")
    private Date date;
    @JsonProperty("CC")
    private Integer citationCount;
    @JsonProperty("ECC")
    private Integer estimatedCitationCount;
    @JsonProperty("AA")
    private AA[] authors;
    @JsonProperty("F")
    private F[] fields;
    @JsonProperty("J")
    private J journal;
    @JsonProperty("C")
    private C conference;
    @JsonProperty("RId")
    private Long[] referencedPapersIDs;
    @JsonProperty("W")
    private String[] paperTitleAndAbstractWords;
    @JsonProperty("E")
    private String extendedMetaData;
    @JsonProperty("PK")
    private Long PK;

    //Extended
    @JsonProperty("DN")
    private String paperDisplayName;
    @JsonProperty("S")
    private MAGExtendedMetaData.S[] sources;
    @JsonProperty("VFN")
    private String venueFullName;
    @JsonProperty("VSN")
    private String venueShortName;
    @JsonProperty("V")
    private String journalVolume;
    @JsonProperty("I")
    private String journalIssue;
    @JsonProperty("FP")
    private String firstPage;
    @JsonProperty("LP")
    private String lastPage;
    @JsonProperty("DOI")
    private String doi;
    @JsonProperty("PR")
    private String[] referencePapers;
    @JsonProperty("CitCon")
    private LinkedHashMap citationContexts;
    @JsonProperty("IA")
    private MAGExtendedMetaData.IA invertedAbstract;
    @JsonProperty("BV")
    private String BV;
    @JsonProperty("BT")
    private String BT;
    @JsonProperty("PB")
    private String PB;


    //Helping Methods
    public static MAGMetaData getMAGMetaDataFromID(Long id, RestClient restClient) throws IOException {
        JsonNode respond = Utilities.searchMAG(restClient, "academic", "v1.0", "evaluate", "Id=" + id);
        if (respond == null)
            return null;
        for (JsonNode node : respond.path("entities")) {
            return MAGMetaData.getMAGMetaDataFromJsonString(node.toString());
        }
        return null;
    }

    public static MAGMetaData getMAGElasticMetaDataFromID(Long id, RestClient restClient) throws IOException {
        Map<String, String> params = new HashMap<>()/*Collections.<String, String>emptyMap()*/;
        String request = "/elastic";
        request += "/mag";
        request += "/paper";
        request += "/" + id;

        if (Utilities.idExistsElasticSearch(id.toString(), "mag", "paper", restClient)) {
            Response response = restClient.performRequest(
                    "GET",
                    request,
                    params);
            if (response == null)
                return null;
            ObjectMapper mapper = new ObjectMapper();
            return MAGMetaData.getMAGMetaDataFromJsonString(mapper.readTree(EntityUtils.toString(response.getEntity())).path("_source").toString());
        }
        else {
            return null;
        }
    }

    public static MAGMetaData getMAGMetaDataFromTitle(String title, RestClient restClient) throws IOException {
        JsonNode respond = Utilities.searchMAG(restClient, "academic", "v1.0", "evaluate", "Ti='" + title.toLowerCase() + "'");
        if (respond == null) {
            return null;
        }
        for (JsonNode node : respond.path("entities")) {
            return MAGMetaData.getMAGMetaDataFromJsonString(node.toString());
        }
        return null;
    }

    public static ArrayList<MAGMetaData> getCitingPapersMetaData(MAGMetaData targetPaperMetaData, RestClient restClient) throws IOException {
        ArrayList<MAGMetaData> citingPapers = new ArrayList<MAGMetaData>();
        JsonNode respond = Utilities.searchMAG(restClient, "academic", "v1.0", "evaluate", "RId=" + targetPaperMetaData.getId());
        if (respond == null) {
            return null;
        }
        for (JsonNode node : respond.path("entities")) {
            citingPapers.add(MAGMetaData.getMAGMetaDataFromJsonString(node.toString()));
        }
        return citingPapers;
    }

    public static MAGMetaData getMAGMetaDataFromJsonString(String jsonElementString) throws IOException {
        try {
            ObjectMapper mapper = new ObjectMapper()/*.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)*/;
            return mapper.readValue(jsonElementString, MAGMetaData.class);
        } catch (Exception e) {
            return null;
        }
    }

    public static String getJsonStringFromMAGMetaData(MAGMetaData magMetaData) throws IOException {
        try {
            ObjectMapper mapper = new ObjectMapper()/*.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)*/;
            return mapper.writeValueAsString(magMetaData);
        } catch (Exception e) {
            return null;
        }
    }

    public Float getLogprob() {
        return logprob;
    }

    public void setLogprob(Float logprob) {
        this.logprob = logprob;
    }

    public Float getProb() {
        return prob;
    }

    public void setProb(Float prob) {
        this.prob = prob;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public Integer getYear() {
        return year;
    }

    public void setYear(Integer year) {
        this.year = year;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public Integer getCitationCount() {
        return citationCount;
    }

    public void setCitationCount(Integer citationCount) {
        this.citationCount = citationCount;
    }

    public Integer getEstimatedCitationCount() {
        return estimatedCitationCount;
    }

    public void setEstimatedCitationCount(Integer estimatedCitationCount) {
        this.estimatedCitationCount = estimatedCitationCount;
    }

    public AA[] getAuthors() {
        return authors;
    }

    public void setAuthors(AA[] authors) {
        this.authors = authors;
    }

    public F[] getFields() {
        return fields;
    }

    public void setFields(F[] fields) {
        this.fields = fields;
    }

    public J getJournal() {
        return journal;
    }

    public void setJournal(J journal) {
        this.journal = journal;
    }

    public C getConference() {
        return conference;
    }

    public void setConference(C conference) {
        this.conference = conference;
    }

    public Long[] getReferencedPapersIDs() {
        return referencedPapersIDs;
    }

    public void setReferencedPapersIDs(Long[] referencedPapersIDs) {
        this.referencedPapersIDs = referencedPapersIDs;
    }

    public String[] getPaperTitleAndAbstractWords() {
        return paperTitleAndAbstractWords;
    }

    public void setPaperTitleAndAbstractWords(String[] paperTitleAndAbstractWords) {
        this.paperTitleAndAbstractWords = paperTitleAndAbstractWords;
    }

    public String getExtendedMetaData() {
        return extendedMetaData;
    }

    public void setExtendedMetaData(String extendedMetaData) {
        this.extendedMetaData = extendedMetaData;
    }

    public Long getPK() {
        return PK;
    }

    public void setPK(Long PK) {
        this.PK = PK;
    }

    public String getPaperDisplayName() {
        return paperDisplayName;
    }

    public void setPaperDisplayName(String paperDisplayName) {
        this.paperDisplayName = paperDisplayName;
    }

    public MAGExtendedMetaData.S[] getSources() {
        return sources;
    }

    public void setSources(MAGExtendedMetaData.S[] sources) {
        this.sources = sources;
    }

    public String getVenueFullName() {
        return venueFullName;
    }

    public void setVenueFullName(String venueFullName) {
        this.venueFullName = venueFullName;
    }

    public String getVenueShortName() {
        return venueShortName;
    }

    public void setVenueShortName(String venueShortName) {
        this.venueShortName = venueShortName;
    }

    public String getJournalVolume() {
        return journalVolume;
    }

    public void setJournalVolume(String journalVolume) {
        this.journalVolume = journalVolume;
    }

    public String getJournalIssue() {
        return journalIssue;
    }

    public void setJournalIssue(String journalIssue) {
        this.journalIssue = journalIssue;
    }

    public String getFirstPage() {
        return firstPage;
    }

    public void setFirstPage(String firstPage) {
        this.firstPage = firstPage;
    }

    public String getLastPage() {
        return lastPage;
    }

    public void setLastPage(String lastPage) {
        this.lastPage = lastPage;
    }

    public String getDoi() {
        return doi;
    }

    public void setDoi(String doi) {
        this.doi = doi;
    }

    public String[] getReferencePapers() {
        return referencePapers;
    }

    public void setReferencePapers(String[] referencePapers) {
        this.referencePapers = referencePapers;
    }

    public LinkedHashMap getCitationContexts() {
        return citationContexts;
    }

    public void setCitationContexts(LinkedHashMap citationContexts) {
        this.citationContexts = citationContexts;
    }

    public MAGExtendedMetaData.IA getInvertedAbstract() {
        return invertedAbstract;
    }

    public void setInvertedAbstract(MAGExtendedMetaData.IA invertedAbstract) {
        this.invertedAbstract = invertedAbstract;
    }

    public String getBV() {
        return BV;
    }

    public void setBV(String BV) {
        this.BV = BV;
    }

    public String getBT() {
        return BT;
    }

    public void setBT(String BT) {
        this.BT = BT;
    }

    public String getPB() {
        return PB;
    }

    public void setPB(String PB) {
        this.PB = PB;
    }


    public static class AA {
        @JsonProperty("AuN")
        private String authorName;
        @JsonProperty("AuId")
        private Long authorID;
        @JsonProperty("AfN")
        private String authorAffiliationName;
        @JsonProperty("AfId")
        private Long authorAffiliationID;
        @JsonProperty("S")
        private Integer authorOrderInPaper;

        public String getAuthorName() {
            return authorName;
        }

        public void setAuthorName(String authorName) {
            this.authorName = authorName;
        }

        public Long getAuthorID() {
            return authorID;
        }

        public void setAuthorID(Long authorID) {
            this.authorID = authorID;
        }

        public String getAuthorAffiliationName() {
            return authorAffiliationName;
        }

        public void setAuthorAffiliationName(String authorAffiliationName) {
            this.authorAffiliationName = authorAffiliationName;
        }

        public Long getAuthorAffiliationID() {
            return authorAffiliationID;
        }

        public void setAuthorAffiliationID(Long authorAffiliationID) {
            this.authorAffiliationID = authorAffiliationID;
        }

        public Integer getAuthorOrderInPaper() {
            return authorOrderInPaper;
        }

        public void setAuthorOrderInPaper(Integer authorOrderInPaper) {
            this.authorOrderInPaper = authorOrderInPaper;
        }
    }

    public static class F {
        @JsonProperty("FN")
        private String fieldName;
        @JsonProperty("FId")
        private Long fieldID;

        public String getFieldName() {
            return fieldName;
        }

        public void setFieldName(String fieldName) {
            this.fieldName = fieldName;
        }

        public Long getFieldID() {
            return fieldID;
        }

        public void setFieldID(Long fieldID) {
            this.fieldID = fieldID;
        }
    }

    public static class J {
        @JsonProperty("JN")
        private String journalName;
        @JsonProperty("JId")
        private Long journalID;

        public String getJournalName() {
            return journalName;
        }

        public void setJournalName(String journalName) {
            this.journalName = journalName;
        }

        public Long getJournalID() {
            return journalID;
        }

        public void setJournalID(Long journalID) {
            this.journalID = journalID;
        }
    }

    public static class C {
        @JsonProperty("CN")
        private String conferenceSeriesName;
        @JsonProperty("CId")
        private Long ConferenceSeriesID;

        public String getConferenceSeriesName() {
            return conferenceSeriesName;
        }

        public void setConferenceSeriesName(String conferenceSeriesName) {
            this.conferenceSeriesName = conferenceSeriesName;
        }

        public Long getConferenceSeriesID() {
            return ConferenceSeriesID;
        }

        public void setConferenceSeriesID(Long conferenceSeriesID) {
            ConferenceSeriesID = conferenceSeriesID;
        }
    }
}
