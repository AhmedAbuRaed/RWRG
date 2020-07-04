package edu.upf.taln.lastus;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;
import org.apache.commons.io.IOUtils;
import org.apache.http.util.EntityUtils;
import org.elasticsearch.client.Response;
import org.elasticsearch.client.ResponseException;
import org.elasticsearch.client.RestClient;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Ahmed on 2/15/17.
 */
public class ACLMetaData {
    private String id;
    private ArrayList<String> authors;
    private String title;
    private String venue;
    private String year;

    //Helping Methods
    public static ACLMetaData getACLMetaDataFromID(String id, RestClient restClient) throws IOException {
        try {
            Map<String, String> params = Collections.<String, String>emptyMap();
            String request = "";
            request += "/" + "acl";
            request += "/" + "metadata";
            request += "/" + id;
            Response response = restClient.performRequest(
                    "GET",
                    request,
                    params);
            if (response.getStatusLine().getStatusCode() == 200) {
                ObjectMapper mapper = new ObjectMapper();
                return ACLMetaData.getACLMetaDataFromJsonString(mapper.readTree(EntityUtils.toString(response.getEntity())).path("_source").toString());
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

    public static ACLMetaData getACLMetaDataFromTitle(String title, RestClient restClient) throws IOException {
        JsonNode respond = Utilities.searchElasticSearch(restClient, "acl", "metadata", "title", title.toString());
        if (respond == null) {
            return null;
        }
        for (JsonNode node : respond.path("hits").path("hits")) {
            JsonNode source = node.path("_source");
            return ACLMetaData.getACLMetaDataFromJsonString(source.toString());
        }
        return null;
    }

    public static ACLMetaData getACLMetaDataFromJsonString(String jsonString) throws IOException {
        try {
            ObjectMapper mapper = new ObjectMapper();
            return mapper.readValue(jsonString, ACLMetaData.class);
        } catch (Exception e) {
            return null;
        }
    }

    public static ArrayList<ACLMetaData> getCitingPapersMetaData(ACLMetaData targetPaperMetaData, RestClient restClient) {
        ArrayList<ACLMetaData> citingPapers = new ArrayList<ACLMetaData>();
        ClassLoader classloader = Thread.currentThread().getContextClassLoader();

        try {
            File aclFile = new File(classloader.getResource("acl.txt").toURI());
            BufferedReader readerACLFile = new BufferedReader(new InputStreamReader(new FileInputStream(aclFile), "UTF-8"));

            String line;

            while ((line = readerACLFile.readLine()) != null) {
                String[] temp = line.split("==>");
                if (targetPaperMetaData.getId().equals(temp[1].trim())) {
                    if (!citingPapers.contains(temp[0].trim())) {
                        citingPapers.add(ACLMetaData.getACLMetaDataFromID(temp[0].trim(), restClient));
                    }
                }
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        return citingPapers;
    }

    public static Integer getCitingPapersCount(File workingDir, String aclPaperID) {
        Integer count = 0;
        File aclFile = new File(workingDir.getAbsoluteFile() + File.separator + "ACL Anthology Network/2014/acl.txt");
        BufferedReader readerACLFile = null;
        try {
            readerACLFile = new BufferedReader(new FileReader(aclFile));

            String line;
            readerACLFile = new BufferedReader(
                    new InputStreamReader(
                            new FileInputStream(aclFile), "UTF-8"));

            while ((line = readerACLFile.readLine()) != null) {
                String[] temp = line.split("==>");
                if (aclPaperID.equals(temp[1].trim())) {
                    count++;
                }
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return count;
    }

    public static String getPDFFromACLAnthologyReferenceCorpus(String aclIDToExtract, String aclGZFolderPath, File extractionDestinationFile) {
        try {
            String entryNameToGet = aclIDToExtract.substring(0, 1) + "/" + aclIDToExtract.substring(0, aclIDToExtract.indexOf("-")) + "/" + aclIDToExtract + ".pdf";

            TarArchiveInputStream tarInput = new TarArchiveInputStream(new GzipCompressorInputStream(new FileInputStream(aclGZFolderPath + File.separator + aclIDToExtract.substring(0, aclIDToExtract.indexOf("-")) + ".gz")));
            TarArchiveEntry entry = null;
            int offset;
            FileOutputStream outputFile = null;
            while ((entry = tarInput.getNextTarEntry()) != null) {
                if (entry.getName().equals(entryNameToGet)) {
                    byte[] content = new byte[(int) entry.getSize()];
                    offset = 0;
                    tarInput.read(content, offset, content.length - offset);
                    outputFile = new FileOutputStream(extractionDestinationFile);
                    IOUtils.write(content, outputFile);
                    outputFile.close();
                }
            }
            tarInput.close();
        } catch (IOException e) {
            return null;
        }
        return aclIDToExtract;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public ArrayList<String> getAuthors() {
        return authors;
    }

    public void setAuthors(ArrayList<String> authors) {
        this.authors = authors;
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

    public String getYear() {
        return year;
    }

    public void setYear(String year) {
        this.year = year;
    }
}
