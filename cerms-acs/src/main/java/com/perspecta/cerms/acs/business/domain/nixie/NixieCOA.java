package com.perspecta.cerms.acs.business.domain.nixie;

import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import java.util.Date;

@Data
@Entity
public class NixieCOA {

	@Id
	@GeneratedValue
	@Column(name = "nixieId")
	private long id;

	private long serialNumber;
	private Date receivedDate;
	private String deliveryCode;
}
