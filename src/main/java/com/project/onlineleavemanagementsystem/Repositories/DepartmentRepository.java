package com.project.onlineleavemanagementsystem.Repositories;

import com.project.onlineleavemanagementsystem.Entities.Department;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.security.config.annotation.method.configuration.EnableReactiveMethodSecurity;
import org.springframework.stereotype.Repository;

@Repository
public interface DepartmentRepository extends JpaRepository<Department, Integer> {
}
