package gr.uom.user_management.services;

import gr.uom.user_management.models.Analysis;
import gr.uom.user_management.repositories.AnalysisRepository;
import kong.unirest.GetRequest;
import kong.unirest.HttpResponse;
import kong.unirest.JsonNode;
import kong.unirest.Unirest;
import kong.unirest.json.JSONObject;
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
    AnalysisRepository analysisRepository;

    @Value("${tracker.api.url}")
    private String apiUrlTracker;
    @Value("${tracker.api.username}")
    private String apiUrlTrackerUsername;
    @Value("${tracker.api.password}")
    private String apiUrlTrackerPassword;

    @Async
    public void runAnalysisInBackground(Analysis analysis) {
        try {
            // 0. Login and get the Bearer Token
            String token = loginAndGetToken();
            if (token == null) {
                System.err.println("Authentication failed for Analysis ID: " + analysis.getId());
                return;
            }

            // Base parameters
            String occId = analysis.getFilterOccupation();
            String source = analysis.getFilterSources();
            Integer limit = analysis.getLimitData();

            // 1,1. Descriptive Analysis
            String descriptiveData = fetchJsonFromTracker(apiUrlTracker + "/api/descriptive-analytics/jobs", token, occId, source, limit);
            analysis.setDescriptiveResult(descriptiveData);

            // 1.2. Location Analysis
            String descriptiveLocationData = fetchJsonFromTracker(apiUrlTracker + "/api/descriptive-analytics/jobs/locations", token, occId, source, limit);
            analysis.setDescriptiveLocationResult(descriptiveLocationData);

            // 2. Exploratory Analysis
            String exploratoryData = fetchJsonFromTracker(apiUrlTracker + "/api/exploratory-analytics/jobs/skills-by-location", token, occId, source, limit);
            analysis.setExploratoryResult(exploratoryData);

            // 3. Trend Analysis
            String trendData = fetchJsonFromTracker(apiUrlTracker + "/api/trend-analytics/jobs/skills-by-location", token, occId, source, limit);
            analysis.setTrendResult(trendData);

            // Mark analysis as finished and save
            analysis.setFinished(true);
            analysisRepository.save(analysis);

            System.out.println("Analysis completed successfully for ID: " + analysis.getId());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    /**
     * Helper method using Unirest to fetch data
     */
    private String fetchJsonFromTracker(String endpoint, String token, String occupationId, String source, Integer limit) {
        try {
            GetRequest request = Unirest.get(endpoint)
                    .header("Authorization", "Bearer " + token);

            // Conditionally add occupation_id
            if (occupationId != null && !occupationId.trim().isEmpty()) {
                request.queryString("occupation_id", occupationId);
            }

            // Conditionally add sources
            if (source != null && !source.trim().isEmpty()) {
                request.queryString("source", source);
            }

            // Conditionally add limit
            if (limit != null && limit > 0) {
                request.queryString("limit", limit);
            }

            // Execute the request
            HttpResponse<String> response = request.asString();

            if (response.getStatus() == 200) {
                System.out.println("Analysis endpoint finished: "+endpoint);
                return response.getBody();
            } else {
                return "{\"error\": \"Tracker API returned " + response.getStatus() + "\"}";
            }
        } catch (Exception e) {
            System.err.println("Connection error for " + endpoint + ": " + e.getMessage());
            return "{\"error\": \"Connection to tracker failed\"}";
        }
    }


    /**
     * Method to authenticate and retrieve the Bearer token
     */
    private String loginAndGetToken() {
        try {
            // Construct the JSON body for login
            JSONObject loginBody = new JSONObject();
            loginBody.put("username", apiUrlTrackerUsername);
            loginBody.put("password", apiUrlTrackerPassword);

            HttpResponse<String> response = Unirest.post(apiUrlTracker + "/api/login")
                    .header("accept", "application/json")
                    .header("Content-Type", "application/json")
                    .body(loginBody)
                    .asString();

            if (response.getStatus() == 200) {
                String token = response.getBody();
                // If the response is a JSON string literal (e.g., "token_value" with quotes),
                // we strip the surrounding double quotes.
                if (token != null && token.startsWith("\"") && token.endsWith("\"")) {
                    token = token.substring(1, token.length() - 1);
                }
                return token;
            } else {
                System.err.println("Login failed: " + response.getStatusText());
                return null;
            }
        } catch (Exception e) {
            System.err.println("Login error: " + e.getMessage());
            return null;
        }
    }

}
