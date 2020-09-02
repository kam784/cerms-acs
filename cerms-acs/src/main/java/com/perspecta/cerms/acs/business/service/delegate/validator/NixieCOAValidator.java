package com.perspecta.cerms.acs.business.service.delegate.validator;

import com.perspecta.cerms.acs.business.domain.cerms_acs.CermsAcs;
import com.perspecta.cerms.acs.business.domain.cerms_acs.CermsAcsRepository;
import com.perspecta.cerms.acs.business.domain.log.FileProcessLog;
import com.perspecta.cerms.acs.business.service.dto.NixieCOARow;
import com.perspecta.cerms.acs.business.service.dto.constant.FileProcessErrorMessage;
import io.micrometer.core.instrument.util.StringUtils;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

import static com.perspecta.cerms.acs.business.service.util.TimeUtils.getCurrentDate;

@Component
@AllArgsConstructor(onConstructor = @__(@Autowired))
@Slf4j
public class NixieCOAValidator {

	private static final String COUNTY_ID = "CountyId";
	private static final String SERIAL_NUMBER = "SerialNumber";
	private static final String COA = "ChangeOfAddress";
	private static final Long COUNTY_ID_VALUE = 202010L;

	private final CermsAcsRepository cermsAcsRepository;

	public void validate(String fileName, List<NixieCOARow> nixieCOARows, List<FileProcessLog> fileProcessLogs) {

		AtomicInteger integer = new AtomicInteger(1);

		if (CollectionUtils.isNotEmpty(nixieCOARows) &&
				StringUtils.isNotBlank(nixieCOARows.get(0).getRecordHeaderCode()) &&
				nixieCOARows.get(0).getRecordHeaderCode().equalsIgnoreCase("H") &&
				StringUtils.isNotBlank(nixieCOARows.get(0).getResponseDate())) {

			nixieCOARows.forEach(nixieCOARow -> {
				Integer rowNumber = integer.getAndIncrement();
				checkForEmptyFields(fileName, nixieCOARow, fileProcessLogs, rowNumber);
				checkForErroneousFields(fileName, nixieCOARow, fileProcessLogs, rowNumber);
			});

		} else {

			if(StringUtils.isBlank(nixieCOARows.get(0).getRecordHeaderCode()) &&
					!nixieCOARows.get(0).getRecordHeaderCode().equalsIgnoreCase("H")) {
				logInvalidFile(fileName, fileProcessLogs);
			}

			if(StringUtils.isBlank(nixieCOARows.get(0).getResponseDate())) {
				logInvalidFileMissingResponseDate(fileName, fileProcessLogs);
			}
		}
	}

	private void checkForEmptyFields(String fileName, NixieCOARow nixieCOARow, List<FileProcessLog> fileProcessLogs, Integer integer) {
		FileProcessLog fileProcessLog;

		if (StringUtils.isBlank(nixieCOARow.getSerialNumber())) {
			fileProcessLog = new FileProcessLog();
			fileProcessLog.setSerialNumber(StringUtils.isBlank(nixieCOARow.getSerialNumber()) ? null : Long.valueOf(nixieCOARow.getSerialNumber()));
			fileProcessLog.setFileName(fileName);
			fileProcessLog.setLogEntry(String.format(FileProcessErrorMessage.EMPTY_FIELD.getMessage(), integer, SERIAL_NUMBER));
			fileProcessLog.setProcessedDate(getCurrentDate());
			fileProcessLogs.add(fileProcessLog);
		}

		if (StringUtils.isBlank(nixieCOARow.getCountyId())) {
			fileProcessLog = new FileProcessLog();
			fileProcessLog.setSerialNumber(StringUtils.isBlank(nixieCOARow.getSerialNumber()) ? null : Long.valueOf(nixieCOARow.getSerialNumber()));
			fileProcessLog.setFileName(fileName);
			fileProcessLog.setLogEntry(String.format(FileProcessErrorMessage.EMPTY_FIELD.getMessage(), integer, COUNTY_ID));
			fileProcessLog.setProcessedDate(getCurrentDate());
			fileProcessLogs.add(fileProcessLog);
		}

		if(StringUtils.isBlank(nixieCOARow.getDeliverabilityCode()) && StringUtils.isBlank(nixieCOARow.getChangeOfAddress())) {
			fileProcessLog = new FileProcessLog();
			fileProcessLog.setSerialNumber(StringUtils.isBlank(nixieCOARow.getSerialNumber()) ? null : Long.valueOf(nixieCOARow.getSerialNumber()));
			fileProcessLog.setFileName(fileName);
			fileProcessLog.setLogEntry(String.format(FileProcessErrorMessage.EMPTY_FIELD.getMessage(), integer, COA));
			fileProcessLog.setProcessedDate(getCurrentDate());
			fileProcessLogs.add(fileProcessLog);
		}
	}

	private void checkForErroneousFields(String fileName, NixieCOARow nixieCOARow, List<FileProcessLog> fileProcessLogs, Integer integer) {

		Long serialNumber = Long.parseLong(nixieCOARow.getSerialNumber().replaceAll(
				"[^a-zA-Z0-9]", ""));

		CermsAcs cermsAcs = cermsAcsRepository.findBySerialNumber(serialNumber);

		if (Objects.isNull(cermsAcs)) {
			FileProcessLog fileProcessLog = new FileProcessLog();
			fileProcessLog.setSerialNumber(StringUtils.isBlank(nixieCOARow.getSerialNumber()) ? null : serialNumber);
			fileProcessLog.setFileName(fileName);
			fileProcessLog.setLogEntry(String.format(FileProcessErrorMessage.SERIAL_NUMBER_NOT_PRESENT.getMessage(), integer, SERIAL_NUMBER));
			fileProcessLog.setProcessedDate(getCurrentDate());
			fileProcessLogs.add(fileProcessLog);
		}


		if (!Long.valueOf(nixieCOARow.getCountyId()).equals(COUNTY_ID_VALUE)) {
			FileProcessLog fileProcessLog = new FileProcessLog();
			fileProcessLog.setSerialNumber(StringUtils.isBlank(nixieCOARow.getSerialNumber()) ? null : Long.valueOf(nixieCOARow.getSerialNumber()));
			fileProcessLog.setFileName(fileName);
			fileProcessLog.setLogEntry(String.format(FileProcessErrorMessage.INVALID_COUNTY_ID.getMessage(), integer, SERIAL_NUMBER));
			fileProcessLog.setProcessedDate(getCurrentDate());
			fileProcessLogs.add(fileProcessLog);
		}
	}

	private void logInvalidFile(String fileName, List<FileProcessLog> fileProcessLogs) {
		FileProcessLog fileProcessLog = new FileProcessLog();
		fileProcessLog.setFileName(fileName);
		fileProcessLog.setLogEntry(String.format(FileProcessErrorMessage.INVALID_FILE.getMessage(), fileName));
		fileProcessLog.setProcessedDate(getCurrentDate());
		fileProcessLogs.add(fileProcessLog);
	}

	private void logInvalidFileMissingResponseDate(String fileName, List<FileProcessLog> fileProcessLogs) {
		FileProcessLog fileProcessLog = new FileProcessLog();
		fileProcessLog.setFileName(fileName);
		fileProcessLog.setLogEntry(String.format(FileProcessErrorMessage.INVALID_FILE_RESPONSEDATE.getMessage(), fileName));
		fileProcessLog.setProcessedDate(getCurrentDate());
		fileProcessLogs.add(fileProcessLog);
	}

}
