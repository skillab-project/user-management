package gr.uom.user_management.services;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import gr.uom.user_management.models.Skill;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
public class CVService {

    @Value("${cvextractor.api.url}")
    private String cvExtractorUrl;

//    public List<Skill> analyzeCV() {
//        List<Skill> skillList = new ArrayList<>();
//        skillList.add(new Skill("http://data.europa.eu/esco/skill/19a8293b-8e95-4de3-983f-77484079c389","Java (computer programming)"));
//        skillList.add(new Skill("http://data.europa.eu/esco/skill/3cd569a2-4f88-4c1e-9995-8dce8c5e51a7","JavaScript"));
//        skillList.add(new Skill("http://data.europa.eu/esco/skill/7369f779-4b71-4aab-8836-48b69c676eec","operate relational database management system"));
//
//        return skillList;
//    }

    private final RestTemplate restTemplate;

    public CVService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public List<Skill> analyzeCV(MultipartFile file) {
        ExtractSkillsResponse response = callExtractor(file);
        return toSkillList(response);
    }

    private ExtractSkillsResponse callExtractor(MultipartFile file) {
        String url = UriComponentsBuilder.fromHttpUrl(cvExtractorUrl)
                .path("/extract-skills/")
                .queryParam("normalize", true)
                .queryParam("only_matched", true)
                .toUriString();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("file", toResource(file));

        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

        // TEMP: print the raw response for testing. Remove once you've verified the DTOs match.
        ResponseEntity<String> raw = restTemplate.exchange(url, HttpMethod.POST, requestEntity, String.class);
        System.out.println("CV extractor raw response: " + raw.getBody());

        ExtractSkillsResponse response;
        try {
            response = restTemplate.postForObject(url, requestEntity, ExtractSkillsResponse.class);
        } catch (RestClientException e) {
            System.err.println("Failed to call CV extractor service at " + url +" "+ e);
            throw new RuntimeException("Failed to call CV extractor service at " + url, e);
        }

        if (response == null || !"success".equalsIgnoreCase(response.status)) {
            throw new RuntimeException(
                    "CV extractor did not return a successful result for file: " + file.getOriginalFilename());
        }
        return response;
    }

    private ByteArrayResource toResource(MultipartFile file) {
        try {
            byte[] bytes = file.getBytes();
            return new ByteArrayResource(bytes) {
                @Override
                public String getFilename() {
                    return file.getOriginalFilename();
                }
            };
        } catch (IOException e) {
            throw new RuntimeException("Unable to read uploaded CV file", e);
        }
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
