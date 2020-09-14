package com.perspecta.cerms.acs.business.domain.cerms_acs;

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

	private String serialNumber;
	private String caseNumber;
	private long docTypeId;
	private Date mailDate;
	private Date destructionDate;
	private Date responseDate;
	private String deliverabilityCode;
	private String coaInfo;
	private Date addDate;
	private Date updateDate;
	private Date notificationDate;
}
