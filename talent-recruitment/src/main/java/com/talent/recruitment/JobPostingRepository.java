package com.talent.recruitment;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
interface JobPostingRepository extends JpaRepository<JobPosting, Long> {
	
}
