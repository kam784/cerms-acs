package com.perspecta.cerms.acs.business.service.dto.constant;

import lombok.Getter;

@Getter
public enum FileProcessErrorMessage {

	EMPTY_FIELD("Line %s field %s is missing."),
	DUPLICATE_FIELD("Line %s field %s is a duplicate."),
	DUPLICATE_FIELD_FILE("Duplicate %s in the same file at line %s."),
	INVALID_MAIL_DATE("Line %s %s has invalid format."),
	INVALID_FILE("Invalid file %s, missing header record."),
	INVALID_FILE_RESPONSEDATE("Invalid file %s, header record does not have response date."),
	INVALID_COUNTY_ID("Line %s field %s is invalid."),
	INVALID_FORMAT("Line %s field %s has invalid format."),
	INVALID_RANGE("Line %s field %s has invalid range."),
	SERIAL_NUMBER_NOT_PRESENT("Line %s field %s, no record matched the serial number."),
	FILE_PROCESS_ERROR_MESSAGE("File %s processed with %s errors"),
	FILE_PROCESS_SUCCESS_MESSAGE("File %s processed successfully");

	private String message;

	FileProcessErrorMessage(String message) {
		this.message = message;
	}
}
