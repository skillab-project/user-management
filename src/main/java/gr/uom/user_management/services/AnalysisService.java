package gr.uom.user_management.services;

import gr.uom.user_management.models.Analysis;
import gr.uom.user_management.repositories.AnalysisRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class AnalysisService {

    @Autowired
    AnalysisRepository analysisRepository;
    @Autowired
    AnalysisAsyncService analysisAsyncService;


    public Analysis checkIfSameAnalysisExists(String sessionId, String filterOccupation, String filterMinDate, String filterMaxDate, String filterSources, Integer limitData) {
        Optional<Analysis> existing = analysisRepository
                .findBySessionIdAndFilterOccupationAndFilterMinDateAndFilterMaxDateAndFilterSourcesAndLimitData(
                        sessionId, filterOccupation, filterMinDate, filterMaxDate, filterSources, limitData
                );

        if(existing.isPresent())
            return existing.get();

        throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Analysis with these filters doesn't exist!");
    }


    public void startNewAnalysis(String userId, String sessionId, String filterOccupation, String filterMinDate, String filterMaxDate, String filterSources, Integer limitData) {
        // check if it already exists
        Optional<Analysis> existing = analysisRepository
                .findBySessionIdAndFilterOccupationAndFilterMinDateAndFilterMaxDateAndFilterSourcesAndLimitData(
                        sessionId, filterOccupation, filterMinDate, filterMaxDate, filterSources, limitData
                );
        if(existing.isPresent()){
            throw new ResponseStatusException(HttpStatus.NOT_ACCEPTABLE, "Analysis with these filters already exist!");
        }

        // create entry in db
        Analysis newAnalysis = new Analysis(userId, sessionId, false, filterOccupation, filterMinDate, filterMaxDate, filterSources, limitData);
        analysisRepository.save(newAnalysis);

        // Start analysis in background
        analysisAsyncService.runAnalysisInBackground(newAnalysis);
        System.out.println("Continue without waiting");
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

    public void startNewAnalysisClustering(String sessionId, String filterOccupation, String filterMinDate, String filterMaxDate, String filterSources, int clusteringNumber, Integer limitData) {
        // check if it already exists
        Optional<Analysis> existing = analysisRepository
                .findBySessionIdAndFilterOccupationAndFilterMinDateAndFilterMaxDateAndFilterSourcesAndLimitData(
                        sessionId, filterOccupation, filterMinDate, filterMaxDate, filterSources, limitData
                );
        if(!existing.isPresent()){
            throw new ResponseStatusException(HttpStatus.NOT_ACCEPTABLE, "Analysis with these filters doesn't exist!");
        }

        // Start new clustering analysis in background
        analysisAsyncService.runNewClusteringAnalysisInBackground(existing.get(), clusteringNumber);
        System.out.println("Continue without waiting");
    }
}
