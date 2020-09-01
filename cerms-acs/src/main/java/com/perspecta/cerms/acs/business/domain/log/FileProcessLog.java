package com.perspecta.cerms.acs.business.domain.log;

import lombok.Data;

import javax.persistence.*;
import java.util.Date;

@Data
@Table(name = "CERMSACS_Log")
@Entity
public class FileProcessLog {

	@Id
	@GeneratedValue
	@Column(name = "fileProcessLogId")
	private long id;

	private Long serialNumber;
	private String fileName;
	private String comment;
	private Date processedDate;
}
