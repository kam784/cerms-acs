package com.perspecta.cerms.acs.business.service;

import com.perspecta.cerms.acs.business.domain.dfs.CermsAcs;
import com.perspecta.cerms.acs.business.domain.log.FileProcessLog;
import com.perspecta.cerms.acs.business.service.util.DocumentCsvExtractor;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

	public boolean processNixieCOAFile(File nixieFile) {

		List<FileProcessLog> fileProcessLogs = new ArrayList<>();
		List<CermsAcs> cermsAcsRecords = new ArrayList<>();

		try {

			List<String> coaRows = documentCsvExtractor.extractNixieCoaRows(nixieFile);

		} catch (Exception ex) {
			log.warn("Could not process nixie file: " + ex);
		}

		return fileProcessLogs.size() == 1;



	}
}
