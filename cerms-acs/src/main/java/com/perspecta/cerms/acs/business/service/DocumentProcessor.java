package com.perspecta.cerms.acs.business.service;

import com.perspecta.cerms.acs.business.service.delegate.ErrorEmailNotifier;
import com.perspecta.cerms.acs.business.service.delegate.FileOrganizationProcessor;
import com.perspecta.cerms.acs.business.service.resource.DocumentResource;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

@Service
@AllArgsConstructor(onConstructor = @__(@Autowired))
@Slf4j
public class DocumentProcessor {

    private final DFSFileProcessor dfsFileProcessor;
    private final NixieCoaFileProcessor nixieCoaFileProcessor;
    private final SDFileProcessor sdFileProcessor;
    private final DocumentResource documentResource;
    private final FileOrganizationProcessor fileOrganizationProcessor;
    private final ErrorEmailNotifier errorEmailNotifier;

    public void processDocuments() {

        List<File> errorFiles = new ArrayList<>();

        log.info("The source path is: " + documentResource.getSourceFolderPath());

        File folder = new File(documentResource.getSourceFolderPath());

        File[] acsFiles = folder.listFiles();

        List<File> dfsFiles = new ArrayList<>();
        List<File> nixieCoaFiles = new ArrayList<>();
        List<File> sdFiles = new ArrayList<>();

        if (ArrayUtils.isNotEmpty(acsFiles)) {
            // Organizing files for processing. Grouping files in three lists for each file type.
            organizeFiles(acsFiles, dfsFiles, nixieCoaFiles, sdFiles);

            // Processing lists in sequence (1. DFS, 2. NIXIE, 3.SD).
            processFiles(errorFiles, dfsFiles, nixieCoaFiles, sdFiles);

            // Emailing list of error/exception files.
            errorEmailNotifier.sendErrorEmail(errorFiles);

            // Cleaning the source directory.
            fileOrganizationProcessor.cleanSourceDirectory();
        }

    }

    private void processFiles(List<File> errorFiles, List<File> dfsFiles, List<File> nixieCoaFiles, List<File> sdFiles) {
        log.info("Number of dfs fies to process: " + dfsFiles.size());

        dfsFiles.forEach(dfsFile -> {
            // Processing each dfs file and adding it in errorFiles list if there is exception/error. Moving files to either success/fail folder.
            boolean isSuccessfullyProcessed = dfsFileProcessor.processDFSFile(dfsFile);
            if (!isSuccessfullyProcessed) {
                errorFiles.add(dfsFile);
                fileOrganizationProcessor.moveProcessedFiles(dfsFile, documentResource.getFailureDfsDestinationFolderPath());
            } else {

                fileOrganizationProcessor.moveProcessedFiles(dfsFile, documentResource.getSuccessDfsDestinationFolderPath());
            }
        });

        log.info("Number of nixie fies to process: " + nixieCoaFiles.size());


        nixieCoaFiles.forEach(nixieCoaFile -> {
            // Processing each nixie file and adding it in errorFiles list if there exception/error. Moving files to either success/fail folder.
            boolean isSuccessfullyProcessed = nixieCoaFileProcessor.processNixieCoaFile(nixieCoaFile);
            if (!isSuccessfullyProcessed) {
                errorFiles.add(nixieCoaFile);
                fileOrganizationProcessor.moveProcessedFiles(nixieCoaFile, documentResource.getFailureNixieDestinationFolderPath());
            } else {

                fileOrganizationProcessor.moveProcessedFiles(nixieCoaFile, documentResource.getSuccessNixieDestinationFolderPath());
            }
        });

        log.info("Number of sd fies to process: " + sdFiles.size());


        sdFiles.forEach(sdFile -> {
            // Processing each sd file and adding it in errorFiles list if there is exception/error. Moving files to either success/fail folder.
            boolean isSuccessfullyProcessed = sdFileProcessor.processSDFile(sdFile);
            if (!isSuccessfullyProcessed) {
                errorFiles.add(sdFile);
                fileOrganizationProcessor.moveProcessedFiles(sdFile, documentResource.getFailureSdDestinationFolderPath());
            } else {

                fileOrganizationProcessor.moveProcessedFiles(sdFile, documentResource.getSuccessSdDestinationFolderPath());
            }
        });
    }

    private void organizeFiles(File[] acsFiles, List<File> dfsFiles, List<File> nixieCoaFiles, List<File> sdFiles) {
        for (File file : acsFiles) {
            if (file.getName().startsWith("D")) {
                sdFiles.add(file);
            } else if (file.getName().startsWith("P")) {
                nixieCoaFiles.add(file);
            } else {
                dfsFiles.add(file);
            }
        }
    }

}
