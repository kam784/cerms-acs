package com.perspecta.cerms.acs.business.service;

import com.perspecta.cerms.acs.business.domain.cerms_acs.CermsAcs;
import com.perspecta.cerms.acs.business.domain.log.FileProcessLog;
import com.perspecta.cerms.acs.business.service.delegate.converter.CermsAcsConverter;
import com.perspecta.cerms.acs.business.service.delegate.persister.DataPersister;
import com.perspecta.cerms.acs.business.service.delegate.validator.NixieCOAValidator;
import com.perspecta.cerms.acs.business.service.dto.NixieCOARow;
import com.perspecta.cerms.acs.business.service.util.DocumentCsvExtractor;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

@Service
@AllArgsConstructor(onConstructor = @__(@Autowired))
@Slf4j
public class NixieCOAFileProcessor {

	private final DocumentCsvExtractor documentCsvExtractor;
	private final NixieCOAValidator nixieCOAValidator;
	private final CermsAcsConverter cermsAcsConverter;
	private final DataPersister dataPersister;

	public boolean processNixieCOAFile(File nixieFile) {

		List<FileProcessLog> fileProcessLogs = new ArrayList<>();
		List<CermsAcs> cermsAcsRecords = new ArrayList<>();

		try {
			List<NixieCOARow> nixieCOARows = documentCsvExtractor.extractNixieCoaRows(nixieFile);

			nixieCOAValidator.validate(nixieFile.getName(), nixieCOARows, fileProcessLogs);

			if(CollectionUtils.isEmpty(fileProcessLogs)) {
			//	cermsAcsRecords = cermsAcsConverter.nixieCOAToCermsAcs(nixieCOARows);
			}

			cermsAcsConverter.finalizeProcessLogs(fileProcessLogs, nixieFile.getName());

			dataPersister.persistData(cermsAcsRecords, fileProcessLogs);


		} catch (Exception ex) {
			log.warn("Could not process nixie file: " + ex);
		}

		return fileProcessLogs.size() == 1;
	}
}
