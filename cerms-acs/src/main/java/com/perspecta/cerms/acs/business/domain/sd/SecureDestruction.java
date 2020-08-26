package com.perspecta.cerms.acs.business.domain.sd;

import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import java.util.Date;

@Data
@Entity
public class SecureDestruction {

	@Id
	@GeneratedValue
	@Column(name = "sdId")
	private long id;

	private long serialNumber;
	private Date scanDate;
}
