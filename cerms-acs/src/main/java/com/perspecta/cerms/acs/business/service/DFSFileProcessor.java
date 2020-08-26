package com.perspecta.cerms.acs.business.service;

import com.perspecta.cerms.acs.business.domain.dfs.CermsAcs;
import com.perspecta.cerms.acs.business.domain.error.FileProcessLog;
import com.perspecta.cerms.acs.business.service.delegate.converter.CermsAcsConverter;
import com.perspecta.cerms.acs.business.service.delegate.persister.DataPersister;
import com.perspecta.cerms.acs.business.service.delegate.validator.DFSFileValidator;
import com.perspecta.cerms.acs.business.service.dto.DFSCsvRow;
import com.perspecta.cerms.acs.business.service.util.DocumentCsvExtractor;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

@Service
@AllArgsConstructor(onConstructor = @__(@Autowired))
@Slf4j
public class DFSFileProcessor {

	private final DocumentCsvExtractor documentCsvExtractor;
	private final DFSFileValidator dfsFileValidator;
	private final CermsAcsConverter cermsAcsConverter;
	private final DataPersister dataPersister;

	public boolean processDFSFile(File file) {
		List<FileProcessLog> fileProcessLogs = new ArrayList<>();
		List<CermsAcs> cermsAcsRecords = new ArrayList<>();

		try {
			InputStream inputStream = new FileInputStream(file);

			List<DFSCsvRow> dfsCsvRows = documentCsvExtractor.extractDfsRows(inputStream);

			dfsFileValidator.validate(file.getName(), dfsCsvRows, fileProcessLogs);

			if(CollectionUtils.isEmpty(fileProcessLogs)) {
				cermsAcsRecords = cermsAcsConverter.dfsToCermsAcs(dfsCsvRows);
			}

			cermsAcsConverter.finalizeProcessLogs(fileProcessLogs, file.getName());

			dataPersister.persistData(cermsAcsRecords, fileProcessLogs);

		} catch (Exception ex) {
			log.info("Could not process dfs file: " + ex);
		}

		return fileProcessLogs.size() == 1;

	}
}
