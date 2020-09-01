package com.perspecta.cerms.acs.business.service;

import com.perspecta.cerms.acs.business.service.delegate.email.ApplicationEmailSender;
import com.perspecta.cerms.acs.business.service.delegate.email.ApplicationHTMLCreator;
import com.perspecta.cerms.acs.business.service.resource.DocumentResource;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.perspecta.cerms.acs.business.service.util.TimeUtils.getCurrentDateWithTimeString;

@Service
@AllArgsConstructor(onConstructor = @__(@Autowired))
@Slf4j
public class DocumentProcessor {

	private static final String PATH_DELIMITER = "\\";
	private static final String TEMPLATE = "FileProcessError";

	private final DFSFileProcessor dfsFileProcessor;
	private final NixieCOAFileProcessor nixieCOAFileProcessor;
	private final SDFileProcessor sdFileProcessor;
	private final ApplicationHTMLCreator applicationHTMLCreator;
	private final ApplicationEmailSender applicationEmailSender;
	private final DocumentResource documentResource;

	public void processDocuments() {

		Map<File, Boolean> processedFileMap = new HashMap<>();

		File folder = new File(documentResource.getSourceFolderPath());

		File[] acsFiles = folder.listFiles();

		for(File file: acsFiles) {
			// P stands for NixieFile
			if(file.getName().startsWith("P")) {
				processedFileMap.put(file, nixieCOAFileProcessor.processNixieCOAFile(file));
			} else if(file.getName().startsWith("D")) {
				processedFileMap.put(file, sdFileProcessor.processSDFile(file));
			} else {
				processedFileMap.put(file, dfsFileProcessor.processDFSFile(file));
			}
		}

		sendErrorEmail(processedFileMap);

	//	moveProcessedFiles(processedFileMap);

	}

	private void sendErrorEmail(Map<File, Boolean> processedFileMap) {

		try {
			List<File> errorFiles = processedFileMap.entrySet().stream()
					.filter(entry -> BooleanUtils.isFalse(entry.getValue()))
					.map(Map.Entry::getKey)
					.collect(Collectors.toList());
			if(!CollectionUtils.isEmpty(errorFiles)) {
				String emailBody = applicationHTMLCreator.withTemplate(TEMPLATE).process();
				applicationEmailSender.sendEmail(errorFiles, emailBody);
			}
		} catch (Exception ex) {
			log.error("Error while sending the file log email. " + ex);
		}
	}

	private void moveProcessedFiles(Map<File, Boolean> processedFileMap) {

		try {
			File source = new File(documentResource.getSourceFolderPath());
			processedFileMap.forEach((file, isSuccessfullyProcessed) -> {
				String sourcePath = documentResource.getSourceFolderPath() + PATH_DELIMITER + file.getName();
				String fileName = FilenameUtils.getBaseName(file.getName());
				String fileExtension = FilenameUtils.getExtension(file.getName());
				String processedFileName = fileName + "_" + getCurrentDateWithTimeString() + "." + fileExtension;
				String filePath;
				if(BooleanUtils.isTrue(isSuccessfullyProcessed)) {
					filePath = documentResource.getSuccessDestinationFolderPath()+ PATH_DELIMITER + processedFileName;
				} else {
					filePath = documentResource.getFailureDestinationFolderPath() + PATH_DELIMITER + processedFileName;
				}
				try {
					FileUtils.moveFile(new File(sourcePath), new File(filePath));
				} catch (Exception ex) {
					log.error("File move exception: " + ex);
				}

			});

			FileUtils.cleanDirectory(source);

		} catch (Exception e) {
			log.warn("File move operation in could not be completed successfully" + e);
		}
	}


}
