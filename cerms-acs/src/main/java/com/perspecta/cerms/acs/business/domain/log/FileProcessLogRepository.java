package com.perspecta.cerms.acs.business.domain.log;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FileProcessLogRepository extends JpaRepository<FileProcessLog, Long> {
}
