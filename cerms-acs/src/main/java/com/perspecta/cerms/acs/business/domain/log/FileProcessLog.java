package com.perspecta.cerms.acs.business.domain.log;

import lombok.Data;
import lombok.Getter;

import javax.persistence.*;
import java.util.Date;

@Data
@Table(name = "CERMSACS_Log")
@Entity
public class FileProcessLog {

	public enum LogStatus {
		SUCCESS, FAILED, EXCEPTION, ERROR
	}

	public enum FileType {
		DFS, NIXIE, SD
	}

	@Id
	@GeneratedValue
	@Column(name = "fileProcessLogId")
	private long id;

	private String serialNumber;
	private String fileName;
	private String logEntry;
	private Date processedDate;

	@Enumerated(EnumType.STRING)
	private LogStatus logStatus;

	@Enumerated(EnumType.STRING)
	private FileType fileType;


}
