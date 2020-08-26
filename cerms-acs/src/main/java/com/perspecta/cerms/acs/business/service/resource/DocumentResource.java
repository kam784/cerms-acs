package com.perspecta.cerms.acs.business.service.resource;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Getter
@Component
public class DocumentResource {

	private final String sourceFolderPath;
	private final String successDestinationFolderPath;
	private final String failureDestinationFolderPath;

	@Autowired
	public DocumentResource(@Value("${cerms.acs.folder.path.source}") String sourceFolderPath,
							@Value("${cerms.acs.folder.path.destination.success}") String successDestinationFolderPath,
							@Value("${cerms.acs.folder.path.destination.failure}") String failureDestinationFolderPath) {
		this.sourceFolderPath = sourceFolderPath;
		this.successDestinationFolderPath = successDestinationFolderPath;
		this.failureDestinationFolderPath = failureDestinationFolderPath;
	}
}
