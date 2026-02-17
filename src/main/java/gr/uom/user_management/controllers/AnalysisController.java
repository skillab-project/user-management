package gr.uom.user_management.controllers;

import gr.uom.user_management.models.Analysis;
import gr.uom.user_management.repositories.AnalysisRepository;
import gr.uom.user_management.services.AnalysisService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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
                                       @RequestParam(required = false) String filterSources,
                                       @RequestParam(required = false) Integer limitData){
        return analysisService.checkIfSameAnalysisExists(sessionId, filterOccupation, filterSources, limitData);
    }

    @PostMapping("new")
    Analysis startNewAnalysis(@RequestParam String sessionId,
                                       @RequestParam(required = false) String filterOccupation,
                                       @RequestParam(required = false) String filterSources,
                                       @RequestParam(required = false) Integer limitData){
        return analysisService.startNewAnalysis(sessionId, filterOccupation, filterSources, limitData);
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

    @GetMapping("/{id}/check")
    public Analysis getAnalysisWithId(@PathVariable String id){
        return analysisService.getAnalysisWithId(id);
    }

    @GetMapping("/{id}/descriptive")
    public ResponseEntity<String> getDescriptive(@PathVariable String id) {
        String result = analysisService.getDescriptiveResults(id);
        return ResponseEntity.ok()
                .header("Content-Type", "application/json")
                .body(result);
    }

    @GetMapping("/{id}/descriptivelocation")
    public ResponseEntity<String> getDescriptiveLocation(@PathVariable String id) {
        String result = analysisService.getDescriptiveLocationResults(id);
        return ResponseEntity.ok()
                .header("Content-Type", "application/json")
                .body(result);
    }

    @GetMapping("/{id}/exploratory")
    public ResponseEntity<String> getExploratory(@PathVariable String id) {
        String result = analysisService.getExploratoryResults(id);
        return ResponseEntity.ok()
                .header("Content-Type", "application/json")
                .body(result);
    }

    @GetMapping("/{id}/trend")
    public ResponseEntity<String> getTrend(@PathVariable String id) {
        String result = analysisService.getTrendResults(id);
        return ResponseEntity.ok()
                .header("Content-Type", "application/json")
                .body(result);
    }
}
