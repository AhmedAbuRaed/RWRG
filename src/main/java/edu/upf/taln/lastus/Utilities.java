package edu.upf.taln.lastus;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.opencsv.CSVParserBuilder;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import com.opencsv.exceptions.CsvException;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.entity.ContentType;
import org.apache.http.nio.entity.NStringEntity;
import org.apache.http.util.EntityUtils;
import org.elasticsearch.client.Response;
import org.elasticsearch.client.ResponseException;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;

import java.io.*;
import java.util.*;
import java.util.stream.Collectors;

public class Utilities {
    protected static RestClient buildRestClientConnection(String host, int port, String protocol, Optional<String> prefix, Optional<Header[]> headers) {
        RestClientBuilder restClientBuilder = RestClient.builder(new HttpHost(host, port, protocol)).setRequestConfigCallback(new RestClientBuilder.RequestConfigCallback() {
            @Override
            public RequestConfig.Builder customizeRequestConfig(RequestConfig.Builder requestConfigBuilder) {
                return requestConfigBuilder.setConnectTimeout(5000)
                        .setSocketTimeout(100000000);
            }
        }).setMaxRetryTimeoutMillis(60000000);

        if (prefix.isPresent()) {
            restClientBuilder = restClientBuilder.setPathPrefix(prefix.get());
        }
        if (headers.isPresent()) {
            restClientBuilder = restClientBuilder.setDefaultHeaders(headers.get());
        }
        return restClientBuilder.build();
    }

    protected static JsonNode searchElasticSearch(RestClient restClient, String index, String type, String field, String term) {
        try {
            Map<String, String> params = Collections.emptyMap();
            String request = "";
            request += "/" + index;
            request += "/" + type;
            request += "/_search";
            HttpEntity entity = new NStringEntity("{\"query\":{\"query_string\":{\"default_field\":\"" + field + "\",\"query\":\"" + term + "\"}}}", ContentType.APPLICATION_JSON);
            Response response = restClient.performRequest(
                    "GET",
                    request,
                    params,
                    entity);
            ObjectMapper mapper = new ObjectMapper();
            return mapper.readTree(EntityUtils.toString(response.getEntity()));
        } catch (JsonProcessingException e) {
            return null;
        } catch (IOException e) {
            return null;
        }
    }

    protected static JsonNode searchMAG(RestClient restClient, String index, String type, String feature, String expr) {
        try {
            Map<String, String> params = new HashMap<String, String>();
            params.put("expr", expr);
            params.put("attributes", "Id,Ti,L,Y,D,CC,ECC,AA.AuN,AA.AuId,AA.AfN,AA.AfId,AA.S,F.FN,F.FId,J.JN,J.JId,C.CN,C.CId,RId,W,E");
            params.put("count", "1000");
            String request = "";
            request += "/" + index;
            request += "/" + type;
            request += "/" + feature;

            Response response = restClient.performRequest(
                    "GET",
                    request,
                    params);
            ObjectMapper mapper = new ObjectMapper();
            return mapper.readTree(EntityUtils.toString(response.getEntity()));
        } catch (JsonProcessingException e) {
            return null;
        } catch (IOException e) {
            return null;
        }
    }

    protected static boolean idExistsElasticSearch(String id, String index, String type, RestClient restClient) {
        try {
            Map<String, String> params = Collections.emptyMap();
            String request = "/elastic";
            request += "/" + index;
            request += "/" + type;
            request += "/" + id;
            Response response = restClient.performRequest(
                    "GET",
                    request,
                    params);
            if (response.getStatusLine().getStatusCode() == 200) {
                return true;
            }
        } catch (ResponseException response) {
            if (response.getResponse().getStatusLine().getStatusCode() == 404) {
                return false;
            } else {
                response.getStackTrace();
            }
        } catch (IOException e) {
            e.getStackTrace();
        }
        return false;
    }

    protected static double[] Softmax(double[] values) {
        /*double max = values[0];

        for (int i = 0; i < values.length; i++) {
            if (values[i] > max) {
                max = values[i];
            }
        }
        double scale = 0.0;
        for (int i = 0; i < values.length; i++) {
            scale += Math.exp(values[i] - max);
        }
        double[] result = new double[values.length];
        for (int i = 0; i < values.length; i++) {
            result[i] = Math.exp(values[i] - max) / scale;
        }

        return result;*/
        int sum = 0;
        double percentage[] = new double[values.length];
        for(int i = 0; i < values.length; i++){
            sum += values[i];
        }

        for(int j = 0; j < values.length; j++){
            percentage[j] = Math.round(100.0 * values[j] / sum);
        }
        return percentage;
    }

    protected static int getIndexOfLargest(double[] array) {
        if (array == null || array.length == 0) return -1; // null or empty

        int largest = 0;
        for (int i = 1; i < array.length; i++) {
            if (array[i] > array[largest]) largest = i;
        }
        return largest; // position of the first largest found
    }

    protected static List<Integer> getReferencesTopicsIDs(File referecesTopics, List<Reference> references) {
        List<Integer> topicIDs = new ArrayList<>(references.size());
        try {
            CSVReader reader = new CSVReaderBuilder(new FileReader(referecesTopics)).withCSVParser(new CSVParserBuilder().withSeparator('\t').build()).build();
            List<String[]> records = reader.readAll();
            for (int i = 0; i < references.size(); i++) {
                Reference reference = references.get(i);
                for (int j = 0; j < records.size(); j++) {
                    String[] record = records.get(j);
                    if (record[0].equals(reference.name)) {
                        double max = -1;
                        int topic = -1;

                        for (int t = 1; t < record.length; t++) {
                            String[] fields = record[t].split(",");
                            int tID = Integer.valueOf(fields[0]);
                            Double tProp = Double.valueOf(fields[1]);

                            if (tProp > max) {
                                max = tProp;
                                topic = tID;
                            }
                        }
                        topicIDs.add(i, topic);
                        break;
                    }
                }
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (CsvException e) {
            e.printStackTrace();
        }
        return topicIDs;
    }
}
