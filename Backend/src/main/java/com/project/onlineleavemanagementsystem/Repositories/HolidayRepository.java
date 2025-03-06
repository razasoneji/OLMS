package com.project.onlineleavemanagementsystem.Repositories;


import com.project.onlineleavemanagementsystem.Entities.Holiday;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;

@Repository
public interface HolidayRepository extends JpaRepository<Holiday, Long> {
    @Query("SELECT COUNT(h) FROM Holiday h WHERE h.date BETWEEN :startDate AND :endDate")
    int countHolidaysBetween(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    long countByDateBetween(LocalDate startDate, LocalDate endDate);
}
