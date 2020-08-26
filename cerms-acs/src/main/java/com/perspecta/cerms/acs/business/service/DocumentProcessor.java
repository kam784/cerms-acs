package com.perspecta.cerms.acs.business.service;

import com.perspecta.cerms.acs.business.service.resource.DocumentResource;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.io.File;
import java.util.HashMap;
import java.util.Map;
import static com.perspecta.cerms.acs.business.service.util.TimeUtils.getCurrentDateString;

@Service
@AllArgsConstructor(onConstructor = @__(@Autowired))
@Slf4j
public class DocumentProcessor {

	private static final String PATH_DELIMITER = "\\";

	private final DFSFileProcessor dfsFileProcessor;
	private final NixieCOAFileProcessor nixieCOAFileProcessor;
	private final SDFileProcessor sdFileProcessor;
	private final DocumentResource documentResource;

	public void processDocuments() {

		Map<File, Boolean> processedFileMap = new HashMap<>();

		File folder = new File(documentResource.getSourceFolderPath());

		File[] acsFiles = folder.listFiles();

		for(File file: acsFiles) {
			// P stands for NixieFile
			if(file.getName().startsWith("P")) {

			} else if(file.getName().startsWith("D")) {
				processedFileMap.put(file, sdFileProcessor.processSDFile(file));
			} else {
				processedFileMap.put(file, dfsFileProcessor.processDFSFile(file));
			}
		}

		moveProcessedFiles(processedFileMap);

	}

	private void moveProcessedFiles(Map<File, Boolean> processedFileMap) {

		try {
			File source = new File(documentResource.getSourceFolderPath());
			processedFileMap.forEach((file, isSuccessfullyProcessed) -> {
				String sourcePath = documentResource.getSourceFolderPath() + PATH_DELIMITER + file.getName();
				String fileName = FilenameUtils.getBaseName(file.getName());
				String fileExtension = FilenameUtils.getExtension(file.getName());
				String processedFileName = fileName + "_" + getCurrentDateString() + "." + fileExtension;
				String filePath;
				if(BooleanUtils.isTrue(isSuccessfullyProcessed)) {
					filePath = documentResource.getSuccessDestinationFolderPath()+ PATH_DELIMITER + processedFileName;
				} else {
					filePath = documentResource.getFailureDestinationFolderPath() + PATH_DELIMITER + processedFileName;
				}
				try {
					FileUtils.moveFile(new File(sourcePath), new File(filePath));
				} catch (Exception ex) {
					log.info("File move exception: " + ex);
				}

			});

			FileUtils.cleanDirectory(source);

		} catch (Exception e) {
			log.warn("File move operation in could not be completed successfully" + e);
		}
	}


}
