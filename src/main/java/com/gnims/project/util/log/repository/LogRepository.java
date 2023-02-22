package com.gnims.project.util.log.repository;

import com.gnims.project.util.log.entity.Log;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LogRepository extends JpaRepository<Log, Long> {
}
