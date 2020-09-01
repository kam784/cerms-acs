package com.perspecta.cerms.acs.business.domain.dfs;

import lombok.Data;

import javax.persistence.*;
import java.util.Date;

@Data
@Table(name = "CERMSACS")
@Entity
public class CermsAcs {

	@Id
	@GeneratedValue
	@Column(name = "cermsAcsId")
	private int id;

	private long serialNumber;
	private String caseNumber;
	private long docTypeId;
	private Date mailDate;
	private Date destructionDate;
	private Date returnDate;
	private String coaInfo;
	private Date addDate;
	private Date updateDate;
}
