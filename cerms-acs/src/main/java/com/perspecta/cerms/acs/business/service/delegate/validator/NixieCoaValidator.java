package com.perspecta.cerms.acs.business.service.delegate.validator;

import com.perspecta.cerms.acs.business.domain.cerms_acs.CermsAcs;
import com.perspecta.cerms.acs.business.domain.cerms_acs.CermsAcsRepository;
import com.perspecta.cerms.acs.business.domain.log.FileProcessLog;
import com.perspecta.cerms.acs.business.service.dto.NixieCoaRow;
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
public class NixieCoaValidator {

	private static final String COUNTY_ID = "CountyId";
	private static final String SERIAL_NUMBER = "SerialNumber";
	private static final String COA = "ChangeOfAddress";
	private static final Long COUNTY_ID_VALUE = 202010L;

	private final CermsAcsRepository cermsAcsRepository;

	public void validate(String fileName, List<NixieCoaRow> nixieCoaRows, List<FileProcessLog> fileProcessLogs) {

		AtomicInteger integer = new AtomicInteger(1);

		if (CollectionUtils.isNotEmpty(nixieCoaRows) &&
				StringUtils.isNotBlank(nixieCoaRows.get(0).getRecordHeaderCode()) &&
				nixieCoaRows.get(0).getRecordHeaderCode().equalsIgnoreCase("H") &&
				StringUtils.isNotBlank(nixieCoaRows.get(0).getResponseDate())) {

			nixieCoaRows.forEach(nixieCoaRow -> {
				Integer rowNumber = integer.getAndIncrement();
				if(!nixieCoaRow.getRecordHeaderCode().equalsIgnoreCase("H")) {
					nixieCoaRow.setValid(true);
					checkForEmptyFields(fileName, nixieCoaRow, fileProcessLogs, rowNumber);
					checkForErroneousFields(fileName, nixieCoaRow, fileProcessLogs, rowNumber);
				}
			});

		} else {

			if(StringUtils.isBlank(nixieCoaRows.get(0).getRecordHeaderCode()) &&
					!nixieCoaRows.get(0).getRecordHeaderCode().equalsIgnoreCase("H")) {
				logInvalidFile(fileName, fileProcessLogs);
			}

			if(StringUtils.isBlank(nixieCoaRows.get(0).getResponseDate())) {
				logInvalidFileMissingResponseDate(fileName, fileProcessLogs);
			}
		}
	}

	private void checkForEmptyFields(String fileName, NixieCoaRow nixieCOARow, List<FileProcessLog> fileProcessLogs, Integer integer) {
		FileProcessLog fileProcessLog;

		if (StringUtils.isBlank(nixieCOARow.getSerialNumber())) {
			fileProcessLog = new FileProcessLog();
			fileProcessLog.setSerialNumber(StringUtils.isBlank(nixieCOARow.getSerialNumber()) ? null : nixieCOARow.getSerialNumber());
			fileProcessLog.setFileName(fileName);
			fileProcessLog.setLogEntry(String.format(FileProcessErrorMessage.EMPTY_FIELD.getMessage(), integer, SERIAL_NUMBER));
			fileProcessLog.setProcessedDate(getCurrentDate());
			fileProcessLogs.add(fileProcessLog);
			nixieCOARow.setValid(false);
		}

		if (StringUtils.isBlank(nixieCOARow.getCountyId())) {
			fileProcessLog = new FileProcessLog();
			fileProcessLog.setSerialNumber(StringUtils.isBlank(nixieCOARow.getSerialNumber()) ? null : nixieCOARow.getSerialNumber());
			fileProcessLog.setFileName(fileName);
			fileProcessLog.setLogEntry(String.format(FileProcessErrorMessage.EMPTY_FIELD.getMessage(), integer, COUNTY_ID));
			fileProcessLog.setProcessedDate(getCurrentDate());
			fileProcessLogs.add(fileProcessLog);
			nixieCOARow.setValid(false);
		}

		if(StringUtils.isBlank(nixieCOARow.getDeliverabilityCode()) && StringUtils.isBlank(nixieCOARow.getChangeOfAddress())) {
			fileProcessLog = new FileProcessLog();
			fileProcessLog.setSerialNumber(StringUtils.isBlank(nixieCOARow.getSerialNumber()) ? null : nixieCOARow.getSerialNumber());
			fileProcessLog.setFileName(fileName);
			fileProcessLog.setLogEntry(String.format(FileProcessErrorMessage.EMPTY_FIELD.getMessage(), integer, COA));
			fileProcessLog.setProcessedDate(getCurrentDate());
			fileProcessLogs.add(fileProcessLog);
			nixieCOARow.setValid(false);
		}
	}

	private void checkForErroneousFields(String fileName, NixieCoaRow nixieCOARow, List<FileProcessLog> fileProcessLogs, Integer integer) {

		String serialNumber = nixieCOARow.getSerialNumber().replaceAll(
				"[^a-zA-Z0-9]", "");

		CermsAcs cermsAcs = cermsAcsRepository.findBySerialNumber(serialNumber);

		if (Objects.isNull(cermsAcs)) {
			FileProcessLog fileProcessLog = new FileProcessLog();
			fileProcessLog.setSerialNumber(StringUtils.isBlank(nixieCOARow.getSerialNumber()) ? null : serialNumber);
			fileProcessLog.setFileName(fileName);
			fileProcessLog.setLogEntry(String.format(FileProcessErrorMessage.SERIAL_NUMBER_NOT_PRESENT.getMessage(), integer, SERIAL_NUMBER));
			fileProcessLog.setProcessedDate(getCurrentDate());
			fileProcessLogs.add(fileProcessLog);
			nixieCOARow.setValid(false);
		}


		if (!Long.valueOf(nixieCOARow.getCountyId()).equals(COUNTY_ID_VALUE)) {
			FileProcessLog fileProcessLog = new FileProcessLog();
			fileProcessLog.setSerialNumber(StringUtils.isBlank(nixieCOARow.getSerialNumber()) ? null : nixieCOARow.getSerialNumber());
			fileProcessLog.setFileName(fileName);
			fileProcessLog.setLogEntry(String.format(FileProcessErrorMessage.INVALID_COUNTY_ID.getMessage(), integer, SERIAL_NUMBER));
			fileProcessLog.setProcessedDate(getCurrentDate());
			fileProcessLogs.add(fileProcessLog);
			nixieCOARow.setValid(false);
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
