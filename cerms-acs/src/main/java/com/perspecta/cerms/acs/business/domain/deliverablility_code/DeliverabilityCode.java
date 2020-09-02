package com.perspecta.cerms.acs.business.domain.deliverablility_code;

import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

@Data
@Entity
public class DeliverabilityCode {

	@Id
	@GeneratedValue
	@Column(name = "deliverabilityCodeId")
	private int id;

	private String value;
	private String description;

}
