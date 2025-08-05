package gr.uom.user_management.services;

import gr.uom.user_management.models.Analysis;
import gr.uom.user_management.repositories.AnalysisRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

@Service
public class AnalysisAsyncService {

    @Autowired
    RestTemplate restTemplate;
    @Autowired
    AnalysisRepository analysisRepository;

    @Value("${tracker.api.url}")
    private String apiUrlTracker;

    @Async
    public void runAnalysisInBackground(Analysis analysis) {
        try {
            String baseUrl = "http://labor-market-demand:8872/load_data";
            StringBuilder url = new StringBuilder(baseUrl);
            url.append("?user_id=").append(URLEncoder.encode(analysis.getUserId(), StandardCharsets.UTF_8));
            url.append("&session_id="+analysis.getCompleteSessionId());

            // Add encoded job API URL
            String encodedApiUrl = URLEncoder.encode(apiUrlTracker + "/api/"+analysis.getSessionId(), StandardCharsets.UTF_8);
            url.append("&url=").append(encodedApiUrl);

            // Build the body parameter with dynamic filters
            List<String> bodyParams = new ArrayList<>();

            if (analysis.getFilterOccupation() != null && !analysis.getFilterOccupation().isEmpty()) {
                bodyParams.add("occupation_ids=" + analysis.getFilterOccupation());
            }

            if (analysis.getFilterSources() != null && !analysis.getFilterSources().isEmpty()) {
                bodyParams.add("sources=" + analysis.getFilterSources());
            }

            if (analysis.getFilterMinDate() != null && !analysis.getFilterMinDate().isEmpty()) {
                bodyParams.add("min_upload_date=" + analysis.getFilterMinDate());
            }

            if (analysis.getFilterMaxDate() != null && !analysis.getFilterMaxDate().isEmpty()) {
                bodyParams.add("max_upload_date=" + analysis.getFilterMaxDate());
            }

            if (!bodyParams.isEmpty()) {
                String rawBody = String.join("&", bodyParams);
                String encodedBody = URLEncoder.encode(rawBody, StandardCharsets.UTF_8);
                url.append("&body=").append(encodedBody);
            }

            System.out.println("Calling labor-market-demand with URL: " + url);

            // Make GET request
            // Create and send GET request
            URI uri = URI.create(url.toString());
            HttpHeaders headers = new HttpHeaders();
            HttpEntity<Void> requestEntity = new HttpEntity<>(headers);
            ResponseEntity<String> response = restTemplate.exchange(
                    uri,
                    HttpMethod.GET,
                    requestEntity,
                    String.class
            );
            int statusCode = response.getStatusCodeValue();
            String responseBody = response.getBody();
            System.out.println("Labor market demand, load data, responded with code: " + statusCode);
            System.out.println("Response body: " + responseBody);


            // Make first descriptive analysis
            uri = URI.create("http://labor-market-demand:8872/analytics_descriptive?user_id=" +analysis.getUserId()+
                    "&session_id=" +analysis.getCompleteSessionId()+ "&storage_name=skills&features_query=skills");
            headers = new HttpHeaders();
            requestEntity = new HttpEntity<>(headers);
            response = restTemplate.exchange(
                    uri,
                    HttpMethod.GET,
                    requestEntity,
                    String.class
            );
            statusCode = response.getStatusCodeValue();
            responseBody = response.getBody();
            System.out.println("Labor market demand, first descriptive, responded with code: " + statusCode);
            System.out.println("Response body: " + responseBody);


            // Make second descriptive analysis
            if(!analysis.getSessionId().equals("courses")) {
                uri = URI.create("http://labor-market-demand:8872/analytics_descriptive?user_id=" + analysis.getUserId() +
                        "&session_id=" + analysis.getCompleteSessionId() + "&storage_name=location&features_query=location");
                headers = new HttpHeaders();
                requestEntity = new HttpEntity<>(headers);
                response = restTemplate.exchange(
                        uri,
                        HttpMethod.GET,
                        requestEntity,
                        String.class
                );
                statusCode = response.getStatusCodeValue();
                responseBody = response.getBody();
                System.out.println("Labor market demand, second descriptive, responded with code: " + statusCode);
                System.out.println("Response body: " + responseBody);
            }


            // Make exploratory analysis
            if(!analysis.getSessionId().equals("courses")) {
                uri = URI.create("http://labor-market-demand:8872/analytics_exploratory?user_id=" + analysis.getUserId() +
                        "&session_id=" + analysis.getCompleteSessionId() + "&storage_name=skills-location&features_query=skills;;location");
                headers = new HttpHeaders();
                requestEntity = new HttpEntity<>(headers);
                response = restTemplate.exchange(
                        uri,
                        HttpMethod.GET,
                        requestEntity,
                        String.class
                );
                statusCode = response.getStatusCodeValue();
                responseBody = response.getBody();
                System.out.println("Labor market demand, exploratory, responded with code: " + statusCode);
                System.out.println("Response body: " + responseBody);
            }


            // Make trend analysis
            if(analysis.getSessionId().equals("jobs")) {
                uri = URI.create("http://labor-market-demand:8872/trend_analysis?user_id=" + analysis.getUserId() +
                        "&session_id=" + analysis.getCompleteSessionId() + "&storage_name=trending&date_field=upload_date"+
                        "&features_query=location&date_format=%25Y-%25m-%25d&what=month");
                headers = new HttpHeaders();
                requestEntity = new HttpEntity<>(headers);
                response = restTemplate.exchange(
                        uri,
                        HttpMethod.GET,
                        requestEntity,
                        String.class
                );
                statusCode = response.getStatusCodeValue();
                responseBody = response.getBody();
                System.out.println("Labor market demand, trending, responded with code: " + statusCode);
                System.out.println("Response body: " + responseBody);
            }


            //Make clustering analysis
            String noClustering = "2";
            uri = URI.create("http://labor-market-demand:8872/skillcluster?type_now=kmeans&user_id=" +analysis.getUserId()+
                    "&session_id=" +analysis.getCompleteSessionId()+ "&storage_name=skillcluster-" +noClustering+ "&weight_now=ii_weight" +
                    "&no_clust_now=" +noClustering+ "&threshold=0.1&umap_nn=5&umap_dim=2&vectors_type=weighting");
            headers = new HttpHeaders();
            requestEntity = new HttpEntity<>(headers);
            response = restTemplate.exchange(
                    uri,
                    HttpMethod.GET,
                    requestEntity,
                    String.class
            );
            statusCode = response.getStatusCodeValue();
            responseBody = response.getBody();
            System.out.println("Labor market demand, clustering, responded with code: " + statusCode);
            System.out.println("Response body: " + responseBody);


            // Mark analysis as finished
            analysis.setFinished(true);
            analysisRepository.save(analysis);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    @Async
    public void runNewClusteringAnalysisInBackground(Analysis analysis, int noClustering) {
        URI uri = URI.create("http://labor-market-demand:8872/skillcluster?type_now=kmeans&user_id=" +analysis.getUserId()+
                "&session_id=" +analysis.getCompleteSessionId()+ "&storage_name=skillcluster-" +noClustering+ "&weight_now=ii_weight" +
                "&no_clust_now=" +noClustering+ "&threshold=0.1&umap_nn=5&umap_dim=2&vectors_type=weighting");
        HttpHeaders headers = new HttpHeaders();
        HttpEntity<Void> requestEntity = new HttpEntity<>(headers);
        ResponseEntity<String> response = restTemplate.exchange(
                uri,
                HttpMethod.GET,
                requestEntity,
                String.class
        );
        int statusCode = response.getStatusCodeValue();
        String responseBody = response.getBody();
        System.out.println("Labor market demand, clustering, responded with code: " + statusCode);
        System.out.println("Response body: " + responseBody);
    }
}
