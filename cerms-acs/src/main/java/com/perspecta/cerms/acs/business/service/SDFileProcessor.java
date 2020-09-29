package com.perspecta.cerms.acs.business.service;

import com.perspecta.cerms.acs.business.domain.cerms_acs.CermsAcs;
import com.perspecta.cerms.acs.business.domain.log.FileProcessLog;
import com.perspecta.cerms.acs.business.service.delegate.converter.CermsAcsConverter;
import com.perspecta.cerms.acs.business.service.delegate.persister.DataPersister;
import com.perspecta.cerms.acs.business.service.delegate.validator.SDValidator;
import com.perspecta.cerms.acs.business.service.dto.SDCsvRow;
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
public class SDFileProcessor {

	private final DocumentCsvExtractor documentCsvExtractor;
	private final SDValidator sdValidator;
	private final CermsAcsConverter cermsAcsConverter;
	private final DataPersister dataPersister;

	public boolean processSDFile(File file) {
		List<FileProcessLog> fileProcessLogs = new ArrayList<>();
		List<CermsAcs> cermsAcsRecords = new ArrayList<>();

		try {
			InputStream inputStream = new FileInputStream(file);

			List<SDCsvRow> sdCsvRows = documentCsvExtractor.extractSdRows(inputStream);

			sdValidator.validate(file.getName(), sdCsvRows, fileProcessLogs);

			cermsAcsRecords = cermsAcsConverter.sdToCermsAcs(sdCsvRows);

			cermsAcsConverter.finalizeProcessLogs(fileProcessLogs, file.getName(), FileProcessLog.FileType.SD, sdCsvRows.size());

			dataPersister.persistData(cermsAcsRecords, fileProcessLogs);

		} catch (Exception ex) {
			log.warn("Could not process sd file: " + ex);
		}

		return fileProcessLogs.size() == 1;

	}
}
