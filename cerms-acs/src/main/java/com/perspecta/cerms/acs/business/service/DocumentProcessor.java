package com.perspecta.cerms.acs.business.service;

import com.perspecta.cerms.acs.business.service.resource.DocumentResource;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.FileSystemUtils;

import java.awt.print.Pageable;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

import static com.perspecta.cerms.acs.business.service.util.TimeUtils.getCurrentDate;

@Service
@AllArgsConstructor(onConstructor = @__(@Autowired))
@Slf4j
public class DocumentProcessor {

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
				processedFileMap.put(file, false);
				//sdFileProcessor.processSDFile(file);
			} else {
				processedFileMap.put(file, true);
				//dfsFileProcessor.processDFSFile(file);
			}
		}

		moveProcessedFiles(processedFileMap);

	}

	private void moveProcessedFiles(Map<File, Boolean> processedFileMap) {

		try {
			File source = new File(documentResource.getSourceFolderPath());
			File successDestination = new File(documentResource.getSuccessDestinationFolderPath());
			File failureDestination = new File(documentResource.getFailureDestinationFolderPath());
			processedFileMap.entrySet().forEach(processedEntry -> {
				File file = processedEntry.getKey();
				String sourcePath = documentResource.getSourceFolderPath() + "\\" + file.getName();
				String fileName = FilenameUtils.getBaseName(file.getName());
				String fileExtension = FilenameUtils.getExtension(file.getName());
				String processedFileName = fileName + "_" + getCurrentDate() + "." + fileExtension;
				String filePath = "";
				if(BooleanUtils.isTrue(processedEntry.getValue())) {
					filePath = documentResource.getSuccessDestinationFolderPath()+ "\\" + processedFileName;
				} else {
					filePath = documentResource.getFailureDestinationFolderPath()+"\\" + processedFileName;
				}
				try {
					FileUtils.moveFile(new File(sourcePath), new File(filePath));
				} catch (Exception ex) {

				}

			});
//
//			File source = new File(documentResource.getSourceFolderPath());
//			File destination = new File(documentResource.getSuccessDestinationFolderPath());

//			FileSystemUtils.copyRecursively(source, destination);
//			FileUtils.cleanDirectory(source);

			FileUtils.cleanDirectory(source);

		} catch (Exception e) {
			e.printStackTrace();
			log.warn("File move operation in could not be completed successfully");
		}
	}


}
