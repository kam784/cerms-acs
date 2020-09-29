package com.perspecta.cerms.acs.business.service.resource;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Getter
@Component
public class DocumentResource {

	private final String sourceFolderPath;
	private final String successDfsDestinationFolderPath;
	private final String successSdDestinationFolderPath;
	private final String successNixieDestinationFolderPath;
	private final String failureDfsDestinationFolderPath;
	private final String failureSdDestinationFolderPath;
	private final String failureNixieDestinationFolderPath;

	@Autowired
	public DocumentResource(@Value("${cerms.acs.folder.path.source}") String sourceFolderPath,
							@Value("${cerms.acs.folder.path.destination.success.dfs}") String successDfsDestinationFolderPath,
							@Value("${cerms.acs.folder.path.destination.success.sd}") String successSdDestinationFolderPath,
							@Value("${cerms.acs.folder.path.destination.success.nixie}") String successNixieDestinationFolderPath,
							@Value("${cerms.acs.folder.path.destination.failure.dfs}") String failureDfsDestinationFolderPath,
							@Value("${cerms.acs.folder.path.destination.failure.sd}") String failureSdDestinationFolderPath,
							@Value("${cerms.acs.folder.path.destination.failure.nixie}") String failureNixieDestinationFolderPath) {
		this.sourceFolderPath = sourceFolderPath;
		this.successDfsDestinationFolderPath = successDfsDestinationFolderPath;
		this.successSdDestinationFolderPath = successSdDestinationFolderPath;
		this.successNixieDestinationFolderPath = successNixieDestinationFolderPath;
		this.failureDfsDestinationFolderPath = failureDfsDestinationFolderPath;
		this.failureSdDestinationFolderPath = failureSdDestinationFolderPath;
		this.failureNixieDestinationFolderPath = failureNixieDestinationFolderPath;
	}
}
