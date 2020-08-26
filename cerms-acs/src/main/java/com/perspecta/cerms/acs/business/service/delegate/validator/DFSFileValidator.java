package com.perspecta.cerms.acs.business.service.delegate.validator;

import com.perspecta.cerms.acs.business.domain.dfs.CermsAcs;
import com.perspecta.cerms.acs.business.domain.dfs.CermsAcsRepository;
import com.perspecta.cerms.acs.business.domain.error.FileProcessLog;
import com.perspecta.cerms.acs.business.service.dto.DFSCsvRow;
import com.perspecta.cerms.acs.business.service.dto.constant.FileProcessErrorMessage;
import io.micrometer.core.instrument.util.StringUtils;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.BooleanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

import static com.perspecta.cerms.acs.business.service.util.TimeUtils.getCurrentDate;

@Component
@AllArgsConstructor(onConstructor = @__(@Autowired))
@Slf4j
public class DFSFileValidator {

	private static final String SERIAL_NUMBER = "Serial";
	private static final String CASE_NUMBER = "CaseNumb";
	private static final String MAIL_DATE = "MailDate";
	private static final String MAIL_DATE_FORMAT = "MM/dd/yyyy";

	private final CermsAcsRepository cermsAcsRepository;

	public void validate(String fileName, List<DFSCsvRow> dfsCsvRows, List<FileProcessLog> fileProcessLogs) {

		AtomicInteger integer = new AtomicInteger(0);

		dfsCsvRows.stream()
				.forEach(dfsCsvRow -> {
					Integer rowNumber = integer.getAndIncrement();

					checkForEmptyFields(fileName, dfsCsvRow, fileProcessLogs, rowNumber);
					checkForDuplicate(fileName, dfsCsvRow, fileProcessLogs, rowNumber);
					checkForMailDateFormat(fileName, dfsCsvRow, fileProcessLogs, rowNumber);

				});

		dfsCsvRows.removeIf(dfsCsvRow -> BooleanUtils.isFalse(dfsCsvRow.isValid()));
	}

	private void checkForEmptyFields(String fileName, DFSCsvRow dfsCsvRow, List<FileProcessLog> fileProcessLogs, Integer integer){
		FileProcessLog fileProcessLog = new FileProcessLog();

		if(StringUtils.isBlank(dfsCsvRow.getSerialNumber())) {
			fileProcessLog.setFileName(fileName);
			fileProcessLog.setComment(String.format(FileProcessErrorMessage.EMPTY_FIELD.getMessage(), integer, SERIAL_NUMBER));
			fileProcessLog.setProcessedDate(getCurrentDate());
			dfsCsvRow.setValid(false);
			fileProcessLogs.add(fileProcessLog);
		}

		if(StringUtils.isBlank(dfsCsvRow.getCaseNumber())) {
			fileProcessLog.setFileName(fileName);
			fileProcessLog.setComment(String.format(FileProcessErrorMessage.EMPTY_FIELD.getMessage(), integer, CASE_NUMBER));
			fileProcessLog.setProcessedDate(getCurrentDate());
			dfsCsvRow.setValid(false);
			fileProcessLogs.add(fileProcessLog);
		}

		if(StringUtils.isBlank(dfsCsvRow.getMailDate())) {
			fileProcessLog.setFileName(fileName);
			fileProcessLog.setComment(String.format(FileProcessErrorMessage.EMPTY_FIELD.getMessage(), integer, MAIL_DATE));
			fileProcessLog.setProcessedDate(getCurrentDate());
			dfsCsvRow.setValid(false);
			fileProcessLogs.add(fileProcessLog);
		}
	}

	private void checkForDuplicate(String fileName, DFSCsvRow dfsCsvRow, List<FileProcessLog> fileProcessLogs, Integer integer) {

		CermsAcs cermsAcs = cermsAcsRepository.findBySerialNumber(Long.valueOf(dfsCsvRow.getSerialNumber()));

		if(Objects.nonNull(cermsAcs)) {
			FileProcessLog fileProcessLog = new FileProcessLog();
			fileProcessLog.setFileName(fileName);
			fileProcessLog.setComment(String.format(FileProcessErrorMessage.DUPLICATE_FIELD.getMessage(), integer, SERIAL_NUMBER));
			fileProcessLogs.add(fileProcessLog);
			fileProcessLog.setProcessedDate(getCurrentDate());
			dfsCsvRow.setValid(false);
		}
	}

	private void checkForMailDateFormat(String fileName, DFSCsvRow dfsCsvRow, List<FileProcessLog> fileProcessLogs, Integer integer) {
		Date date = null;
		boolean validFormat = true;
		try {
			SimpleDateFormat sdf = new SimpleDateFormat(MAIL_DATE_FORMAT);
			date = sdf.parse(dfsCsvRow.getMailDate());
			if (!dfsCsvRow.getMailDate().equals(sdf.format(date))) {
				FileProcessLog fileProcessLog = new FileProcessLog();
				fileProcessLog.setFileName(fileName);
				fileProcessLog.setComment(String.format(FileProcessErrorMessage.INVALID_MAIL_DATE.getMessage(), integer, MAIL_DATE));
				fileProcessLogs.add(fileProcessLog);
				fileProcessLog.setProcessedDate(getCurrentDate());
				validFormat = false;
			}
		} catch (Exception ex) {
			log.info("The mail date could not be parsed. " + ex);
			validFormat = false;
		}

		dfsCsvRow.setValid(validFormat);
	}
}
