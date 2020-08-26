package com.perspecta.cerms.acs.business.service.delegate.validator;

import com.perspecta.cerms.acs.business.domain.dfs.CermsAcs;
import com.perspecta.cerms.acs.business.domain.dfs.CermsAcsRepository;
import com.perspecta.cerms.acs.business.domain.error.FileProcessLog;
import com.perspecta.cerms.acs.business.service.dto.SDCsvRow;
import com.perspecta.cerms.acs.business.service.dto.constant.FileProcessErrorMessage;
import io.micrometer.core.instrument.util.StringUtils;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

import static com.perspecta.cerms.acs.business.service.util.TimeUtils.getCurrentDate;

@Component
@AllArgsConstructor(onConstructor = @__(@Autowired))
@Slf4j
public class SDValidator {

	private static final String COUNTY_ID = "CountyId";
	private static final String SERIAL_NUMBER = "Serial";
	private static final String SCAN_DATE = "ScanDate";
	private static final String DATE_FORMAT = "MM/dd/yyyy";
	private static final String COUNTY_ID_VALUE = "202010";

	private final CermsAcsRepository cermsAcsRepository;

	public void validate(String fileName, List<SDCsvRow> sdCsvRows, List<FileProcessLog> fileProcessLogs) {

		AtomicInteger integer = new AtomicInteger(0);

		if (CollectionUtils.isNotEmpty(sdCsvRows) &&
				StringUtils.isNotBlank(sdCsvRows.get(0).getCode()) &&
				sdCsvRows.get(0).getCode().equals("H")) {

			sdCsvRows.removeIf(dfsCsvRow -> BooleanUtils.isTrue(dfsCsvRow.getCode().equals("H")));

			sdCsvRows.stream()
					.forEach(sdCsvRow -> {
						Integer rowNumber = integer.getAndIncrement();

						sdCsvRow.setValid(true);
						parseRawSerialNumber(sdCsvRow);
						checkForEmptyFields(fileName, sdCsvRow, fileProcessLogs, rowNumber);
						checkForErroneousFields(fileName, sdCsvRow, fileProcessLogs, rowNumber);
						checkForMailDateFormat(fileName, sdCsvRow, fileProcessLogs, rowNumber);

					});

			sdCsvRows.removeIf(dfsCsvRow -> BooleanUtils.isFalse(dfsCsvRow.isValid()));
		} else {
			logInvalidFile(fileName, fileProcessLogs);
			sdCsvRows = Collections.emptyList();
		}
	}

	private void checkForEmptyFields(String fileName, SDCsvRow sdCsvRow, List<FileProcessLog> fileProcessLogs, Integer integer) {
		FileProcessLog fileProcessLog = new FileProcessLog();

		if (StringUtils.isBlank(sdCsvRow.getCountyId())) {
			fileProcessLog.setFileName(fileName);
			fileProcessLog.setComment(String.format(FileProcessErrorMessage.EMPTY_FIELD.getMessage(), integer, COUNTY_ID));
			fileProcessLog.setProcessedDate(getCurrentDate());
			sdCsvRow.setValid(false);
			fileProcessLogs.add(fileProcessLog);
		}

		if (StringUtils.isBlank(sdCsvRow.getRawSerialNumber())) {
			fileProcessLog.setFileName(fileName);
			fileProcessLog.setComment(String.format(FileProcessErrorMessage.EMPTY_FIELD.getMessage(), integer, SERIAL_NUMBER));
			fileProcessLog.setProcessedDate(getCurrentDate());
			sdCsvRow.setValid(false);
			fileProcessLogs.add(fileProcessLog);
		}

		if (StringUtils.isBlank(sdCsvRow.getDestructionDate())) {
			fileProcessLog.setFileName(fileName);
			fileProcessLog.setComment(String.format(FileProcessErrorMessage.EMPTY_FIELD.getMessage(), integer, SCAN_DATE));
			fileProcessLog.setProcessedDate(getCurrentDate());
			sdCsvRow.setValid(false);
			fileProcessLogs.add(fileProcessLog);
		}
	}

	private void logInvalidFile(String fileName, List<FileProcessLog> fileProcessLogs) {
		FileProcessLog fileProcessLog = new FileProcessLog();
		fileProcessLog.setFileName(fileName);
		fileProcessLog.setComment(String.format(FileProcessErrorMessage.INVALID_FILE.getMessage(), fileName));
		fileProcessLog.setProcessedDate(getCurrentDate());
		fileProcessLogs.add(fileProcessLog);
	}

	private void checkForErroneousFields(String fileName, SDCsvRow sdCsvRow, List<FileProcessLog> fileProcessLogs, Integer integer) {

		//checkForNonNumericRawSerialNumber(fileName, sdCsvRow, fileProcessLogs, integer);

		Long serialNumber = Long.parseLong(sdCsvRow.getRawSerialNumber().replaceAll(
				"[^a-zA-Z0-9]", "").substring(sdCsvRow.getRawSerialNumber().length() - 9));

		CermsAcs cermsAcs = cermsAcsRepository.findBySerialNumber(serialNumber);

		if (Objects.isNull(cermsAcs)) {
			FileProcessLog fileProcessLog = new FileProcessLog();
			fileProcessLog.setFileName(fileName);
			fileProcessLog.setComment(String.format(FileProcessErrorMessage.SERIAL_NUMBER_NOT_PRESENT.getMessage(), integer, SERIAL_NUMBER));
			fileProcessLog.setProcessedDate(getCurrentDate());
			fileProcessLogs.add(fileProcessLog);
			sdCsvRow.setValid(false);
		}


		if (!sdCsvRow.getCountyId().equals(COUNTY_ID_VALUE)) {
			FileProcessLog fileProcessLog = new FileProcessLog();
			fileProcessLog.setFileName(fileName);
			fileProcessLog.setComment(String.format(FileProcessErrorMessage.INVALID_COUNTY_ID.getMessage(), integer, SERIAL_NUMBER));
			fileProcessLog.setProcessedDate(getCurrentDate());
			fileProcessLogs.add(fileProcessLog);
			sdCsvRow.setValid(false);
		}
	}

	private void checkForMailDateFormat(String fileName, SDCsvRow sdCsvRow, List<FileProcessLog> fileProcessLogs, Integer integer) {
		Date date = null;
		boolean validFormat = true;
		try {
			SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT);
			date = sdf.parse(sdCsvRow.getDestructionDate());
			if (!sdCsvRow.getDestructionDate().equals(sdf.format(date))) {
				FileProcessLog fileProcessLog = new FileProcessLog();
				fileProcessLog.setFileName(fileName);
				fileProcessLog.setComment(String.format(FileProcessErrorMessage.INVALID_MAIL_DATE.getMessage(), integer, SCAN_DATE));
				fileProcessLog.setProcessedDate(getCurrentDate());
				fileProcessLogs.add(fileProcessLog);
				validFormat = false;
			}
		} catch (Exception ex) {
			log.info("The scan date could not be parsed. " + ex);
			validFormat = false;
		}

		sdCsvRow.setValid(validFormat);
	}

	private void checkForNonNumericRawSerialNumber(String fileName, SDCsvRow sdCsvRow, List<FileProcessLog> fileProcessLogs, int integer) {
		try {
			Long.valueOf(sdCsvRow.getRawSerialNumber());
		} catch (Exception ex) {
			log.warn("The raw serial number could not be parsed to long. ");

			FileProcessLog fileProcessLog = new FileProcessLog();
			fileProcessLog.setFileName(fileName);
			fileProcessLog.setComment(String.format(FileProcessErrorMessage.INVALID_FORMAT.getMessage(), integer, SERIAL_NUMBER));
			fileProcessLog.setProcessedDate(getCurrentDate());
			fileProcessLogs.add(fileProcessLog);
			sdCsvRow.setValid(false);
		}
	}

	private void parseRawSerialNumber(SDCsvRow sdCsvRow) {
		if(StringUtils.isNotBlank(sdCsvRow.getRawSerialNumber())) {
			sdCsvRow.setRawSerialNumber(sdCsvRow.getRawSerialNumber().replaceAll("[^a-zA-Z0-9]", ""));
		}
	}
}
