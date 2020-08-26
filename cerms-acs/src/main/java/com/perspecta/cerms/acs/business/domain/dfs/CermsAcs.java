package com.perspecta.cerms.acs.business.domain.dfs;

import lombok.Data;

import javax.persistence.*;
import java.util.Date;

@Data
@Table(name = "Acs")
@Entity
public class CermsAcs {

	@Id
	@GeneratedValue
	@Column(name = "acsId")
	private long id;

	private long serialNumber;
	private String caseNumber;
	private long docTypeId;
	private Date mailDate;
	private Date destructionDate;
	private Date returnDate;
	private String coaInfo;
}
