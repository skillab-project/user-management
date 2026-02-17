package gr.uom.user_management.services;

import gr.uom.user_management.models.Analysis;
import gr.uom.user_management.repositories.AnalysisRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
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


    public Analysis checkIfSameAnalysisExists(String sessionId, String filterOccupation, String filterSources, Integer limitData) {
        Optional<Analysis> existing = analysisRepository
                .findBySessionIdAndFilterOccupationAndFilterSourcesAndLimitData(
                        sessionId, filterOccupation, filterSources, limitData
                );

        if(existing.isPresent())
            return existing.get();

        throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Analysis with these filters doesn't exist!");
    }


    public Analysis startNewAnalysis(String sessionId, String filterOccupation, String filterSources, Integer limitData) {
        // check if it already exists
        Optional<Analysis> existing = analysisRepository
                .findBySessionIdAndFilterOccupationAndFilterSourcesAndLimitData(
                        sessionId, filterOccupation, filterSources, limitData
                );
        if(existing.isPresent()){
            throw new ResponseStatusException(HttpStatus.NOT_ACCEPTABLE, "Analysis with these filters already exist!");
        }

        // create entry in db
        Analysis newAnalysis = new Analysis(sessionId, false, filterOccupation, filterSources, limitData);
        analysisRepository.save(newAnalysis);

        // Start analysis in background
        analysisAsyncService.runAnalysisInBackground(newAnalysis);
        System.out.println("Continue without waiting");
        return newAnalysis;
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

    @Transactional(readOnly = true)
    public String getDescriptiveResults(String analysisId) {
        Analysis a = analysisRepository.findById(UUID.fromString(analysisId))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        return a.getDescriptiveResult();
    }

    @Transactional(readOnly = true)
    public String getDescriptiveLocationResults(String analysisId) {
        Analysis a = analysisRepository.findById(UUID.fromString(analysisId))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        return a.getDescriptiveLocationResult();
    }

    @Transactional(readOnly = true)
    public String getExploratoryResults(String analysisId) {
        Analysis a = analysisRepository.findById(UUID.fromString(analysisId))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        return a.getExploratoryResult();
    }

    @Transactional(readOnly = true)
    public String getTrendResults(String analysisId) {
        Analysis a = analysisRepository.findById(UUID.fromString(analysisId))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        return a.getTrendResult();
    }

    public Analysis getAnalysisWithId(String analysisId) {
        return analysisRepository.findById(UUID.fromString(analysisId)).orElseThrow();
    }

}
