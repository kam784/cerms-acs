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
		SUCCESS, FAILED
	}

	@Id
	@GeneratedValue
	@Column(name = "fileProcessLogId")
	private long id;

	private Long serialNumber;
	private String fileName;
	private String logEntry;
	private Date processedDate;

	@Enumerated(EnumType.STRING)
	private LogStatus logStatus;


}
