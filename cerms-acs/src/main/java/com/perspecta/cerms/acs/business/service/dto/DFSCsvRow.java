package com.perspecta.cerms.acs.business.service.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;


@Getter
@Setter
public class DFSCsvRow {

	@JsonProperty("serialNumber")
	private String serialNumber;

	@JsonProperty("caseNumber")
	private String caseNumber;

	@JsonProperty("docTypeId")
	private String docTypeId;

	@JsonProperty("mailDate")
	private String mailDate;

	private boolean valid;

}
