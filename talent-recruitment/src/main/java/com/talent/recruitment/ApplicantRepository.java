package com.talent.recruitment;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
interface ApplicantRepository extends JpaRepository<Applicant, Long> {

    @Query("SELECT a FROM Applicant a " +
           "WHERE (:skill IS NULL OR :skill IN (SELECT s.skill FROM a.skillSet s)) " +
           "AND (:city IS NULL OR a.city = :city) " +
           "AND (:salary IS NULL OR a.expectedSalary <= :salary)")
    Page<Applicant> findWithFilters(@Param("skill") String skill,
                                     @Param("city") String city,
                                     @Param("salary") Double salary,
                                     Pageable pageable);
}
