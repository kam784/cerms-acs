package com.perspecta.cerms.acs.business.service;

import com.perspecta.cerms.acs.business.domain.cerms_acs.CermsAcs;
import com.perspecta.cerms.acs.business.domain.log.FileProcessLog;
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
		boolean fileProcessedSuccessully = true;
		List<FileProcessLog> fileProcessLogs = new ArrayList<>();
		List<CermsAcs> cermsAcsRecords = new ArrayList<>();

		try {
			InputStream inputStream = new FileInputStream(file);

			// Extracting file records into the DFSCsvRow dto list.
			List<DFSCsvRow> dfsCsvRows = documentCsvExtractor.extractDfsRows(inputStream);

			// Validating DFSCsvRow list records and adding error records in fileProcessLogs
			dfsFileValidator.validate(file.getName(), dfsCsvRows, fileProcessLogs);

			// Converting DFSCsvRow dto list to CermsAcs list (to be saved later in database).
			cermsAcsRecords = cermsAcsConverter.dfsToCermsAcs(dfsCsvRows);

			// Massaging the final logs.
			cermsAcsConverter.finalizeProcessLogs(fileProcessLogs, file.getName(), FileProcessLog.FileType.DFS, dfsCsvRows.size());

			// Saving cermsAcsRecords and logs in database.
			dataPersister.persistData(cermsAcsRecords, fileProcessLogs);

			fileProcessedSuccessully = fileProcessLogs.size() == 1;

			inputStream.close();


		} catch (Exception ex) {
			log.warn("Could not process dfs file: " + ex);
		}

		return fileProcessedSuccessully;

	}
}
