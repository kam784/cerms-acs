package com.perspecta.cerms.acs.business.service.delegate;

import com.perspecta.cerms.acs.business.service.resource.DocumentResource;
import com.perspecta.cerms.acs.business.service.util.TimeUtils;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileDeleteStrategy;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.File;
import java.nio.channels.FileLock;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

import static com.perspecta.cerms.acs.business.service.util.TimeUtils.getCurrentDateWithTimeString;

@Slf4j
@Component
@AllArgsConstructor(onConstructor = @__(@Autowired))
public class FileOrganizationProcessor {

    private static final String PATH_DELIMITER = "\\";

    private final DocumentResource documentResource;

    public void moveProcessedFiles(File processedFile, String destination) {

        try {
            String sourcePath = documentResource.getSourceFolderPath() + PATH_DELIMITER + processedFile.getName();
            String fileName = FilenameUtils.getBaseName(processedFile.getName());
            String fileExtension = FilenameUtils.getExtension(processedFile.getName());
            String processedFileName = fileName + "_" + getCurrentDateWithTimeString() + "." + fileExtension;
            destination = destination + PATH_DELIMITER + TimeUtils.getCurrentYear() + PATH_DELIMITER + TimeUtils.getCurrentMonth();
            Path destinationFolderPath = Paths.get(destination);
            Files.createDirectories(destinationFolderPath);

            String fileDestinationPath = destination + PATH_DELIMITER + processedFileName;

            try {
                Files.copy(Paths.get(sourcePath),
                        Paths.get(fileDestinationPath),
                        StandardCopyOption.REPLACE_EXISTING);
                //FileUtils.moveFile(new File(sourcePath), new File(filePath));
            } catch (Exception ex) {
                log.error("File move exception: " + ex);
            }

        } catch (Exception e) {
            log.warn("File move operation could not be completed successfully" + e);
        }
    }

    public void cleanSourceDirectory() {
        try {
            File source = new File(documentResource.getSourceFolderPath());
            for(File file : source.listFiles()) {
                FileUtils.forceDelete(file);
            }
        } catch (Exception ex) {
            log.warn("Could not delete the source directory files.");
        }
    }
}
