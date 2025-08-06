package gr.uom.user_management.controllers;

import gr.uom.user_management.models.Analysis;
import gr.uom.user_management.models.ClusteringAnalysis;
import gr.uom.user_management.repositories.AnalysisRepository;
import gr.uom.user_management.services.AnalysisService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/analysis")
public class AnalysisController {

    @Autowired
    AnalysisService analysisService;
    @Autowired
    AnalysisRepository analysisRepository;

    @GetMapping("/check")
    Analysis checkIfSameAnalysisExists(@RequestParam String sessionId,
                                       @RequestParam(required = false) String filterOccupation,
                                       @RequestParam(required = false) String filterMinDate,
                                       @RequestParam(required = false) String filterMaxDate,
                                       @RequestParam(required = false) String filterSources,
                                       @RequestParam(required = false) Integer limitData){
        return analysisService.checkIfSameAnalysisExists(sessionId, filterOccupation, filterMinDate, filterMaxDate, filterSources, limitData);
    }

    @PostMapping("new")
    ResponseEntity<?> startNewAnalysis(@RequestParam String userId,
                                       @RequestParam String sessionId,
                                       @RequestParam(required = false) String filterOccupation,
                                       @RequestParam(required = false) String filterMinDate,
                                       @RequestParam(required = false) String filterMaxDate,
                                       @RequestParam(required = false) String filterSources,
                                       @RequestParam(required = false) Integer limitData){
        analysisService.startNewAnalysis(userId, sessionId, filterOccupation, filterMinDate, filterMaxDate, filterSources, limitData);
        return ResponseEntity.ok().build();
    }

    @PostMapping("new/{noClustering}")
    ResponseEntity<?> startNewAnalysisClustering(@RequestParam String sessionId,
                                                 @PathVariable String noClustering,
                                                 @RequestParam(required = false) String filterOccupation,
                                                 @RequestParam(required = false) String filterMinDate,
                                                 @RequestParam(required = false) String filterMaxDate,
                                                 @RequestParam(required = false) String filterSources,
                                                 @RequestParam(required = false) Integer limitData){
        try {
            int clusteringNumber = Integer.parseInt(noClustering);
            analysisService.startNewAnalysisClustering(sessionId, filterOccupation, filterMinDate, filterMaxDate, filterSources, clusteringNumber, limitData);
            return ResponseEntity.ok().build();
        } catch (NumberFormatException e) {
            return ResponseEntity.badRequest().body("Invalid clustering number: must be an integer");
        }
    }

    @GetMapping()
    List<Analysis> getAllAnalyses(){
        return analysisService.getAllAnalyses();
    }

    @DeleteMapping()
    ResponseEntity<?> deleteAnalysis(@RequestParam String analysisId){
        analysisService.deleteAnalysis(analysisId);
        return ResponseEntity.ok().build();
    }



    // Get Results of analyses
    @GetMapping("/{id}/firstDescriptiveAnalysis")
    public ResponseEntity<String> getFirstDescriptiveAnalysis(@PathVariable UUID id) {
        Optional<Analysis> analysisOpt = analysisRepository.findById(id);
        if (analysisOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        String jsonResponse = analysisOpt.get().getFirstDescriptiveAnalysis();
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(jsonResponse);
    }

    @GetMapping("/{id}/secondDescriptiveAnalysis")
    public ResponseEntity<String> getSecondDescriptiveAnalysis(@PathVariable UUID id) {
        Optional<Analysis> analysisOpt = analysisRepository.findById(id);
        if (analysisOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        String jsonResponse = analysisOpt.get().getSecondDescriptiveAnalysis();
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(jsonResponse);
    }

    @GetMapping("/{id}/exploratoryAnalysis")
    public ResponseEntity<String> getExploratoryAnalysis(@PathVariable UUID id) {
        Optional<Analysis> analysisOpt = analysisRepository.findById(id);
        if (analysisOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        String jsonResponse = analysisOpt.get().getExploratoryAnalysis();
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(jsonResponse);
    }

    @GetMapping("/{id}/trendAnalysis")
    public ResponseEntity<String> getTrendAnalysis(@PathVariable UUID id) {
        Optional<Analysis> analysisOpt = analysisRepository.findById(id);
        if (analysisOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        String jsonResponse = analysisOpt.get().getTrendAnalysis();
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(jsonResponse);
    }

    @GetMapping("/{id}/clusteringAnalysis")
    public ResponseEntity<String> getClusteringJson(@PathVariable UUID id, @RequestParam("clusters") Integer numberOfClusters) {
        Optional<Analysis> analysisOpt = analysisRepository.findById(id);
        if (analysisOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        List<ClusteringAnalysis> clusteringAnalysis = analysisOpt.get().getClusteringAnalysis();
        for (ClusteringAnalysis result : clusteringAnalysis) {
            if (result.getNumberOfClusters() == numberOfClusters) {
                return ResponseEntity.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(result.getClusteringResult());
            }
        }
        return ResponseEntity.notFound().build();
    }

}
