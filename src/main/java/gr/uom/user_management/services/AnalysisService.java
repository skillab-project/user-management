package gr.uom.user_management.services;

import gr.uom.user_management.models.Analysis;
import gr.uom.user_management.repositories.AnalysisRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;

import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class AnalysisService {

    @Autowired
    AnalysisRepository analysisRepository;
    @Autowired
    RestTemplate restTemplate;

    @Value("${tracker.api.url}")
    private String apiUrlTracker;

    public String checkIfSameAnalysisExists(String sessionId, String filterOccupation, String filterMinDate, String filterMaxDate, String filterSources) {
        Optional<Analysis> existing = analysisRepository
                .findBySessionIdAndFilterOccupationAndFilterMinDateAndFilterMaxDateAndFilterSources(
                        sessionId, filterOccupation, filterMinDate, filterMaxDate, filterSources
                );

        if(existing.isPresent())
            return existing.get().getUserId();

        throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Analysis with these filters doesn't exist!");
    }


    public void startNewAnalysis(String userId, String sessionId, String filterOccupation, String filterMinDate, String filterMaxDate, String filterSources) {
        // check if it already exists
        Optional<Analysis> existing = analysisRepository
                .findBySessionIdAndFilterOccupationAndFilterMinDateAndFilterMaxDateAndFilterSources(
                        sessionId, filterOccupation, filterMinDate, filterMaxDate, filterSources
                );
        if(existing.isPresent()){
            throw new ResponseStatusException(HttpStatus.NOT_ACCEPTABLE, "Analysis with these filters already exist!");
        }

        // create entry in db
        Analysis newAnalysis = new Analysis(userId, sessionId, false, filterOccupation, filterMinDate, filterMaxDate, filterSources);
        analysisRepository.save(newAnalysis);

        // Start analysis in background
        runAnalysisInBackground(newAnalysis);
    }

    @Async
    public void runAnalysisInBackground(Analysis analysis) {
        try {
            String baseUrl = "http://labor-market-demand:8872/load_data";
            StringBuilder url = new StringBuilder(baseUrl);
            url.append("?user_id=").append(URLEncoder.encode(analysis.getUserId(), StandardCharsets.UTF_8));
            url.append("&session_id="+analysis.getSessionId());

            // Add encoded job API URL
            String encodedApiUrl = URLEncoder.encode(apiUrlTracker + "/api/"+analysis.getSessionId(), StandardCharsets.UTF_8);
            url.append("&url=").append(encodedApiUrl);

            // Build the body parameter with dynamic filters
            List<String> bodyParams = new ArrayList<>();

            if (analysis.getFilterOccupation() != null && !analysis.getFilterOccupation().isEmpty()) {
                bodyParams.add("occupation_ids=" + analysis.getFilterOccupation());
            }

            if (analysis.getFilterSources() != null && !analysis.getFilterSources().isEmpty()) {
                bodyParams.add("source=" + analysis.getFilterSources());
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

            System.out.println("Labor market demand responded with code: " + statusCode);
            System.out.println("Response body: " + responseBody);

            // Mark analysis as finished
            analysis.setFinished(true);
            analysisRepository.save(analysis);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public List<Analysis> getAllAnalyses() {
        return analysisRepository.findAll();
    }


    public void deleteAnalysis(String analysisId) {
        Analysis analysis = analysisRepository.findById(UUID.fromString(analysisId))
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Analysis with id " + analysisId + " doesn't exist!"
                ));
        analysisRepository.delete(analysis);
    }
}
