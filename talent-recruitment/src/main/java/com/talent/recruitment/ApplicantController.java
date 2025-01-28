package com.talent.recruitment;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/applicants")
class ApplicantController {

    @Autowired
    private ApplicantRepository applicantRepository;

    @Autowired
    private JobPostingRepository jobPostingRepository;

    // Get all applicants with optional filters
    @GetMapping
    public ResponseEntity<Page<Applicant>> getApplicants(@RequestParam Optional<Integer> page,
                                                          @RequestParam Optional<Integer> size,
                                                          @RequestParam Optional<String> skill,
                                                          @RequestParam Optional<String> city,
                                                          @RequestParam Optional<Double> salary) {
        Pageable pageable = PageRequest.of(page.orElse(0), size.orElse(10));
        try {
            Page<Applicant> applicants = applicantRepository.findWithFilters(skill.orElse(""), city.orElse(""), salary.orElse(0.0), pageable);
            return ResponseEntity.ok(applicants);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }
    //Get based on id
    @GetMapping("/{id}")
    public ResponseEntity<Applicant> getApplicantById(@PathVariable Long id) {
        try {
            Applicant applicant = applicantRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Applicant not found with id " + id));
            return ResponseEntity.ok(applicant);
        } catch (Exception e) {
            e.printStackTrace(); // Log the error stack trace
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null); // Return internal server error
        }
    }

    // Create or update an applicant
    @PostMapping
    public ResponseEntity<Applicant> createOrUpdateApplicant(@RequestBody Applicant applicant) {
        try {
            Applicant savedApplicant = applicantRepository.save(applicant);
            return ResponseEntity.ok(savedApplicant);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    // Update an applicant by ID
    @PutMapping("/{id}")
    public ResponseEntity<Applicant> updateApplicant(@PathVariable Long id, @RequestBody Applicant updatedApplicant) {
        try {
            Applicant existingApplicant = applicantRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Applicant not found"));

            existingApplicant.setFullName(updatedApplicant.getFullName());
            existingApplicant.setEmailAddress(updatedApplicant.getEmailAddress());
            existingApplicant.setPhoneNumber(updatedApplicant.getPhoneNumber());
            existingApplicant.setCity(updatedApplicant.getCity());
            existingApplicant.setExpectedSalary(updatedApplicant.getExpectedSalary());

            // Update other fields as necessary

            Applicant savedApplicant = applicantRepository.save(existingApplicant);
            return ResponseEntity.ok(savedApplicant);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    
    // Delete an applicant by ID
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteApplicant(@PathVariable Long id) {
        try {
            Applicant existingApplicant = applicantRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Applicant not found"));
            applicantRepository.delete(existingApplicant);
            return ResponseEntity.noContent().build();  // HTTP 204 No Content
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // Get eligible jobs for a specific applicant
    @GetMapping("/{id}/eligible-jobs")
    public ResponseEntity<List<JobPosting>> getEligibleJobs(@PathVariable Long id) {
        try {
            Applicant applicant = applicantRepository.findById(id).orElseThrow(() -> new RuntimeException("Applicant not found"));

            List<JobPosting> jobPostings = jobPostingRepository.findAll();
            List<JobPosting> eligibleJobs = new ArrayList<>();

            for (JobPosting job : jobPostings) {
                if (job.getJobLocation().equalsIgnoreCase(applicant.getCity()) &&
                    job.getSalaryBudget() >= applicant.getExpectedSalary() * 0.8 &&
                    job.getSalaryBudget() <= applicant.getExpectedSalary() * 1.2) {

                    Set<String> jobSkills = new HashSet<>(Arrays.asList(job.getRequiredSkills().split(",")));
                    Set<String> applicantSkills = new HashSet<>();
                    applicant.getSkillSet().forEach(s -> applicantSkills.add(s.getSkill()));

                    long matchingSkills = applicantSkills.stream().filter(jobSkills::contains).count();

                    if ((double) matchingSkills / applicantSkills.size() >= 0.75) {
                        eligibleJobs.add(job);
                    }
                }
            }
            return ResponseEntity.ok(eligibleJobs);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }
}
