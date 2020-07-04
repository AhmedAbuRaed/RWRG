package edu.upf.taln.lastus;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

public class S2MetaData {
    private List<Author> authors;
    private Integer citationVelocity;
    private List<CitationoReference> citations;
    @JsonProperty("abstract")
    private String paperAbstract;
    private String arxivId;
    private String doi;
    private Integer influentialCitationCount;
    private String paperId;
    private List<CitationoReference> references;
    private List<Topics> topics;
    private String title;
    @JsonProperty
    private String url;
    private String venue;
    private Integer year;

    //Helping Methods
    public static S2MetaData getS2MetaDataFromS2PaperID(String s2PaperID) {
        try {
            URL u = new URL("http://api.semanticscholar.org/v1/paper/" + s2PaperID);
            InputStream is = null;
            try {
                is = u.openStream();
            } catch (IOException e) {
                return null;
            }
            DataInputStream dis = new DataInputStream(new BufferedInputStream(is));
            StringBuffer sb = new StringBuffer();
            String s;
            while ((s = dis.readLine()) != null) {
                sb.append(s);
            }
            ObjectMapper mapper = new ObjectMapper();
            return mapper.readValue(sb.toString(), S2MetaData.class);
        } catch (JsonParseException e) {
            e.printStackTrace();
        } catch (JsonMappingException e) {
            e.printStackTrace();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public List<Author> getAuthors() {
        return authors;
    }

    public void setAuthors(List<Author> authors) {
        this.authors = authors;
    }

    public Integer getCitationVelocity() {
        return citationVelocity;
    }

    public void setCitationVelocity(Integer citationVelocity) {
        this.citationVelocity = citationVelocity;
    }

    public List<CitationoReference> getCitations() {
        return citations;
    }

    public void setCitations(List<CitationoReference> citations) {
        this.citations = citations;
    }

    public String getDoi() {
        return doi;
    }

    public void setDoi(String doi) {
        this.doi = doi;
    }

    public Integer getInfluentialCitationCount() {
        return influentialCitationCount;
    }

    public void setInfluentialCitationCount(Integer influentialCitationCount) {
        this.influentialCitationCount = influentialCitationCount;
    }

    public String getPaperId() {
        return paperId;
    }

    public void setPaperId(String paperId) {
        this.paperId = paperId;
    }

    public List<CitationoReference> getReferences() {
        return references;
    }

    public void setReferences(List<CitationoReference> references) {
        this.references = references;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
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

    public String getPaperAbstract() {
        return paperAbstract;
    }

    public void setPaperAbstract(String paperAbstract) {
        this.paperAbstract = paperAbstract;
    }

    public String getArxivId() {
        return arxivId;
    }

    public void setArxivId(String arxivId) {
        this.arxivId = arxivId;
    }

    public List<Topics> getTopics() {
        return topics;
    }

    public void setTopics(List<Topics> topics) {
        this.topics = topics;
    }

    public static class Author {
        private String authorId;
        private String name;
        @JsonProperty
        private String url;

        public String getAuthorId() {
            return authorId;
        }

        public void setAuthorId(String authorId) {
            this.authorId = authorId;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }
    }

    public static class Topics {
        private String topic;
        private String topicId;
        @JsonProperty
        private String url;


        public String getTopic() {
            return topic;
        }

        public void setTopic(String topic) {
            this.topic = topic;
        }

        public String getTopicId() {
            return topicId;
        }

        public void setTopicId(String topicId) {
            this.topicId = topicId;
        }

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }
    }

    public static class CitationoReference {
        @JsonProperty("isInfluential")
        private boolean isInfluential;
        private String paperId;
        private String title;
        @JsonProperty
        private String url;
        private String venue;
        private Integer year;
        private String arxivId;
        private List<Author> authors;
        private String doi;
        private List<String> intent;


        public String getPaperId() {
            return paperId;
        }

        public void setPaperId(String paperId) {
            this.paperId = paperId;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
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

        public boolean isInfluential() {
            return isInfluential;
        }

        public void setInfluential(boolean influential) {
            isInfluential = influential;
        }

        public String getArxivId() {
            return arxivId;
        }

        public void setArxivId(String arxivId) {
            this.arxivId = arxivId;
        }

        public List<Author> getAuthors() {
            return authors;
        }

        public void setAuthors(List<Author> authors) {
            this.authors = authors;
        }

        public String getDoi() {
            return doi;
        }

        public void setDoi(String doi) {
            this.doi = doi;
        }

        public List<String> getIntent() {
            return intent;
        }

        public void setIntent(List<String> intent) {
            this.intent = intent;
        }
    }

}
