package com.perspecta.cerms.acs.business.service;

import com.perspecta.cerms.acs.business.domain.cerms_acs.CermsAcs;
import com.perspecta.cerms.acs.business.domain.log.FileProcessLog;
import com.perspecta.cerms.acs.business.service.delegate.converter.CermsAcsConverter;
import com.perspecta.cerms.acs.business.service.delegate.persister.DataPersister;
import com.perspecta.cerms.acs.business.service.delegate.validator.NixieCoaValidator;
import com.perspecta.cerms.acs.business.service.dto.NixieCoaRow;
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
public class NixieCoaFileProcessor {

	private final DocumentCsvExtractor documentCsvExtractor;
	private final NixieCoaValidator nixieCoaValidator;
	private final CermsAcsConverter cermsAcsConverter;
	private final DataPersister dataPersister;

	public boolean processNixieCoaFile(File nixieCoaFile) {

		List<FileProcessLog> fileProcessLogs = new ArrayList<>();
		List<CermsAcs> cermsAcsRecords = new ArrayList<>();

		try {
			List<NixieCoaRow> nixieCoaRows = documentCsvExtractor.extractNixieCoaRows(nixieCoaFile);

			nixieCoaValidator.validate(nixieCoaFile.getName(), nixieCoaRows, fileProcessLogs);

			cermsAcsRecords = cermsAcsConverter.nixieCoaToCermsAcs(nixieCoaRows);

			cermsAcsConverter.finalizeProcessLogs(fileProcessLogs, nixieCoaFile.getName());

			dataPersister.persistData(cermsAcsRecords, fileProcessLogs);


		} catch (Exception ex) {
			log.warn("Could not process nixie file: " + ex);
		}

		return fileProcessLogs.size() == 1;
	}
}
