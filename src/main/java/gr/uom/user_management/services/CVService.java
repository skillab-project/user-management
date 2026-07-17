package gr.uom.user_management.services;

import gr.uom.user_management.models.CvAnalysisJob;
import gr.uom.user_management.repositories.CvAnalysisJobRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.util.UUID;

@Service
public class CVService {

    @Autowired
    CvAnalysisJobRepository jobRepository;
    @Autowired
    CVAsyncService cvAsyncService;

    /**
     * Creates a job, hands the extraction off to the background, and returns
     * immediately so the caller doesn't block on the slow extractor service.
     */
    public CvAnalysisJob startAnalysis(String userId, MultipartFile file) {
        CvAnalysisJob job = jobRepository.save(new CvAnalysisJob(userId));

        byte[] bytes;
        try {
            // Read the bytes on the request thread — the MultipartFile's temp file
            // is gone by the time the async worker runs.
            bytes = file.getBytes();
        } catch (IOException e) {
            job.setStatus("FAILED");
            job.setError("Unable to read uploaded CV file");
            return jobRepository.save(job);
        }

        cvAsyncService.analyzeCVInBackground(job.getId(), bytes, file.getOriginalFilename());
        return job;
    }

    /**
     * Returns the current job state. Once the job is DONE, the row is deleted
     * right after it is loaded.
     */
    public CvAnalysisJob getJob(String jobId) {
        CvAnalysisJob job = jobRepository.findById(UUID.fromString(jobId))
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "CV analysis job not found: " + jobId));

        if (job.getStatus().equals("DONE")) {
            jobRepository.delete(job);
        }
        return job;
    }
}
