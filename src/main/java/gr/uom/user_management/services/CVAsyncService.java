package gr.uom.user_management.services;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import gr.uom.user_management.models.CvAnalysisJob;
import gr.uom.user_management.models.Skill;
import gr.uom.user_management.repositories.CvAnalysisJobRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.*;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class CVAsyncService {

    @Value("${cvextractor.api.url}")
    private String cvExtractorUrl;

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    CvAnalysisJobRepository jobRepository;

    public CVAsyncService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    /**
     * Runs the (slow) CV extractor call off the request thread and records the
     * result on the job row. The file is passed as a byte[] because the original
     * MultipartFile's backing temp file is deleted once the upload request returns.
     */
    @Async
    public void analyzeCVInBackground(UUID jobId, byte[] fileBytes, String filename) {
        CvAnalysisJob job = jobRepository.findById(jobId).orElse(null);
        if (job == null) {
            System.err.println("CV job not found, cannot run extraction: " + jobId);
            return;
        }
        try {
            ExtractSkillsResponse response = callExtractor(fileBytes, filename);
            List<Skill> skills = toSkillList(response);
            job.setSkillsJson(objectMapper.writeValueAsString(skills));
            job.setStatus("DONE");
            System.out.println("CV extraction completed for job: " + jobId);
        } catch (Exception e) {
            System.err.println("CV extraction failed for job " + jobId + ": " + e.getMessage());
            job.setStatus("FAILED");
            job.setError(e.getMessage());
        }
        jobRepository.save(job);
    }

    private ExtractSkillsResponse callExtractor(byte[] fileBytes, String filename) {
        String url = UriComponentsBuilder.fromHttpUrl(cvExtractorUrl)
                .path("/extract-skills/")
                .queryParam("normalize", true)
                .queryParam("only_matched", true)
                .toUriString();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("file", toResource(fileBytes, filename));

        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

        ExtractSkillsResponse response;
        try {
            response = restTemplate.postForObject(url, requestEntity, ExtractSkillsResponse.class);
        } catch (RestClientException e) {
            System.err.println("Failed to call CV extractor service at " + url + " " + e);
            throw new RuntimeException("Failed to call CV extractor service at " + url, e);
        }

        if (response == null || !"success".equalsIgnoreCase(response.status)) {
            throw new RuntimeException(
                    "CV extractor did not return a successful result for file: " + filename);
        }
        return response;
    }

    private ByteArrayResource toResource(byte[] fileBytes, String filename) {
        return new ByteArrayResource(fileBytes) {
            @Override
            public String getFilename() {
                return filename;
            }
        };
    }

    private List<Skill> toSkillList(ExtractSkillsResponse response) {
        List<Skill> skills = new ArrayList<>();
        if (response.escoNormalizedSkills == null || response.escoNormalizedSkills.results == null) {
            return skills;
        }
        for (EscoResult result : response.escoNormalizedSkills.results) {
            // esco_uri can be blank in the upstream response (for now!!);
            // passed through as-is.
            skills.add(new Skill(result.escoUri, result.escoPreferredLabel));
        }
        return skills;
    }


    // ---- Response DTOs, scoped to this endpoint's payload shape ----
    @JsonIgnoreProperties(ignoreUnknown = true)
    private static class ExtractSkillsResponse {
        public String filename;
        public String status;

        @JsonProperty("esco_normalized_skills")
        public EscoNormalizedSkills escoNormalizedSkills;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private static class EscoNormalizedSkills {
        public List<EscoResult> results;

        @JsonProperty("total_count")
        public int totalCount;

        @JsonProperty("filter_applied")
        public boolean filterApplied;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private static class EscoResult {
        @JsonProperty("input_skill")
        public String inputSkill;

        @JsonProperty("similarity_score")
        public double similarityScore;

        @JsonProperty("esco_preferredLabel")
        public String escoPreferredLabel;

        @JsonProperty("esco_description")
        public String escoDescription;

        @JsonProperty("esco_uri")
        public String escoUri;

        @JsonProperty("esco_alt_labels")
        public List<String> escoAltLabels;
    }
}
