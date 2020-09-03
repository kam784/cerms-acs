package com.perspecta.cerms.acs.business.service.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class NixieCoaRow {

	private String recordHeaderCode;
	private String deliverabilityCode;
	private String responseDate;
	private String countyId;
	private String serialNumber;
	private String changeOfAddress;
	private boolean valid;

}
