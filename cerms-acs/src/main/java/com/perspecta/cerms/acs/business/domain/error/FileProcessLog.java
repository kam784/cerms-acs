package com.perspecta.cerms.acs.business.domain.error;

import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import java.util.Date;

@Data
@Entity
public class FileProcessLog {

	@Id
	@GeneratedValue
	@Column(name = "fileProcessLogId")
	private long id;

	private String fileName;
	private String comment;
	private Date processedDate;
}
