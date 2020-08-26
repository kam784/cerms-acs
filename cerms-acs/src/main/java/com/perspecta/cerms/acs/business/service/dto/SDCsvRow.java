package com.perspecta.cerms.acs.business.service.dto;


import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SDCsvRow {

	@JsonProperty("code")
	private String code;

	@JsonProperty("countyId")
	private String countyId;

	@JsonProperty("rawSerialNumber")
	private String rawSerialNumber;

	@JsonProperty("destructionDate")
	private String destructionDate;

	@JsonProperty("destructionTime")
	private String destructionTime;

	@JsonProperty("randomNumber")
	private String randomNumber;

	private boolean valid;
}
