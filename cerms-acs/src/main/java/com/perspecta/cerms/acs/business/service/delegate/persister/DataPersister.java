package com.perspecta.cerms.acs.business.service.delegate.persister;

import com.perspecta.cerms.acs.business.domain.dfs.CermsAcs;
import com.perspecta.cerms.acs.business.domain.dfs.CermsAcsRepository;
import com.perspecta.cerms.acs.business.domain.error.FileProcessLog;
import com.perspecta.cerms.acs.business.domain.error.FileProcessLogRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import java.util.List;

@Component
@AllArgsConstructor(onConstructor = @__(@Autowired))
@Slf4j
public class DataPersister {

	private final CermsAcsRepository cermsAcsRepository;
	private final FileProcessLogRepository fileProcessLogRepository;

	public void persistData(List<CermsAcs> cermsAcsRecords, List<FileProcessLog> fileProcessLogs) {
		cermsAcsRepository.saveAll(cermsAcsRecords);
		fileProcessLogRepository.saveAll(fileProcessLogs);
	}

}
