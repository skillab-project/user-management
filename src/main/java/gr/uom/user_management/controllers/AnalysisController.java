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


    @PostMapping("manually/new")
    ResponseEntity<?> manuallyCreateNewAnalysis(@RequestParam String userId,
                                       @RequestParam String sessionId,
                                       @RequestParam(required = false) String filterOccupation,
                                       @RequestParam(required = false) String filterMinDate,
                                       @RequestParam(required = false) String filterMaxDate,
                                       @RequestParam(required = false) String filterSources,
                                       @RequestParam(required = false) Integer limitData){
        analysisService.manuallyCreateNewAnalysis(userId, sessionId, filterOccupation, filterMinDate, filterMaxDate, filterSources, limitData);
        return ResponseEntity.ok().build();
    }

    @PutMapping("manually/new")
    Analysis manuallyChangeAnalysis(@RequestParam String id, @RequestParam(required = false) Boolean finished, @RequestParam(required = false) String userId){
        return analysisService.manuallyChangeAnalysis(id, finished, userId);
    }
}
