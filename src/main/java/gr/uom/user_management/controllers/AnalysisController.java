package gr.uom.user_management.controllers;

import gr.uom.user_management.services.AnalysisService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/analysis")
public class AnalysisController {

    @Autowired
    AnalysisService analysisService;

    @GetMapping("/check")
    String checkIfSameAnalysisExists(@RequestParam String sessionId,
                                     @RequestParam(required = false) String filterOccupation,
                                     @RequestParam(required = false) String filterMinDate,
                                     @RequestParam(required = false) String filterMaxDate,
                                     @RequestParam(required = false) String filterSources){
        return analysisService.checkIfSameAnalysisExists(sessionId, filterOccupation, filterMinDate, filterMaxDate, filterSources);
    }

    @PostMapping("new")
    ResponseEntity<?> startNewAnalysis(@RequestParam String userId,
                                       @RequestParam String sessionId,
                                       @RequestParam(required = false) String filterOccupation,
                                       @RequestParam(required = false) String filterMinDate,
                                       @RequestParam(required = false) String filterMaxDate,
                                       @RequestParam(required = false) String filterSources){
        analysisService.startNewAnalysis(userId, sessionId, filterOccupation, filterMinDate, filterMaxDate, filterSources);
        return ResponseEntity.ok().build();
    }

}
