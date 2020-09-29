package com.perspecta.cerms.acs.business.service.delegate.persister;

import com.perspecta.cerms.acs.business.domain.cerms_acs.CermsAcs;
import com.perspecta.cerms.acs.business.domain.cerms_acs.CermsAcsRepository;
import com.perspecta.cerms.acs.business.domain.log.FileProcessLog;
import com.perspecta.cerms.acs.business.domain.log.FileProcessLogRepository;
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
		cermsAcsRecords.forEach(cermsAcs -> {
			log.debug("The record is " + cermsAcs);
			cermsAcsRepository.save(cermsAcs);
		});
		//cermsAcsRepository.saveAll(cermsAcsRecords);
		fileProcessLogRepository.saveAll(fileProcessLogs);
	}

}
