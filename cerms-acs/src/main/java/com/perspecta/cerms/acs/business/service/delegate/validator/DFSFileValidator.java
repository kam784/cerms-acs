package com.perspecta.cerms.acs.business.service.delegate.validator;

import com.perspecta.cerms.acs.business.domain.cerms_acs.CermsAcs;
import com.perspecta.cerms.acs.business.domain.cerms_acs.CermsAcsRepository;
import com.perspecta.cerms.acs.business.domain.log.FileProcessLog;
import com.perspecta.cerms.acs.business.service.dto.DFSCsvRow;
import com.perspecta.cerms.acs.business.service.dto.constant.FileProcessErrorMessage;
import io.micrometer.core.instrument.util.StringUtils;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.BooleanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

import static com.perspecta.cerms.acs.business.service.util.TimeUtils.getCurrentDate;
import static com.perspecta.cerms.acs.business.service.util.TimeUtils.getCurrentDateWithTime;

@Component
@AllArgsConstructor(onConstructor = @__(@Autowired))
@Slf4j
public class DFSFileValidator {

    private static final String SERIAL_NUMBER = "Serial";
    private static final String CASE_NUMBER = "CaseNumb";
    private static final String MAIL_DATE = "MailDate";
    private static final String MAIL_DATE_FORMAT = "MM/dd/yyyy";
    private static final Long serialRangeLimit = 2499999L;

    private final CermsAcsRepository cermsAcsRepository;

    public void validate(String fileName, List<DFSCsvRow> dfsCsvRows, List<FileProcessLog> fileProcessLogs) {

        AtomicInteger integer = new AtomicInteger(1);

        List<String> serialNumbers = new ArrayList<>();

        dfsCsvRows.forEach(dfsCsvRow -> {
            Integer rowNumber = integer.getAndIncrement();

            // check for empty fields and add to fileProcessLogs if error.
            checkForEmptyFields(fileName, dfsCsvRow, fileProcessLogs, rowNumber);

            // check for valid serial number (valid range).
            checkForValidSerialNumber(fileName, dfsCsvRow, fileProcessLogs, rowNumber);

            // check for duplicate serial number (in db or in the file itself).
            checkForDuplicate(fileName, dfsCsvRow, fileProcessLogs, serialNumbers, rowNumber);

            // check for valid mail date format.
            checkForMailDateFormat(fileName, dfsCsvRow, fileProcessLogs, rowNumber);

            if (StringUtils.isNotBlank(dfsCsvRow.getSerialNumber())) {
                serialNumbers.add(parseSerialNumber(dfsCsvRow.getSerialNumber()));
            }
        });

        dfsCsvRows.removeIf(dfsCsvRow -> BooleanUtils.isFalse(dfsCsvRow.isValid()));
        fileProcessLogs.forEach(fileProcessLog -> fileProcessLog.setLogStatus(FileProcessLog.LogStatus.ERROR));
    }

    private void checkForEmptyFields(String fileName, DFSCsvRow dfsCsvRow, List<FileProcessLog> fileProcessLogs, Integer integer) {
        FileProcessLog fileProcessLog;

        if (StringUtils.isBlank(dfsCsvRow.getSerialNumber())) {
            fileProcessLog = new FileProcessLog();
            fileProcessLog.setFileName(fileName);
            fileProcessLog.setLogEntry(String.format(FileProcessErrorMessage.EMPTY_FIELD.getMessage(), integer, SERIAL_NUMBER));
            fileProcessLog.setProcessedDate(getCurrentDateWithTime());
            dfsCsvRow.setValid(false);
            fileProcessLogs.add(fileProcessLog);
        }

        if (StringUtils.isBlank(dfsCsvRow.getCaseNumber())) {
            fileProcessLog = new FileProcessLog();
            fileProcessLog.setSerialNumber(StringUtils.isBlank(dfsCsvRow.getSerialNumber()) ? null : parseSerialNumber(dfsCsvRow.getSerialNumber()));
            fileProcessLog.setFileName(fileName);
            fileProcessLog.setLogEntry(String.format(FileProcessErrorMessage.EMPTY_FIELD.getMessage(), integer, CASE_NUMBER));
            fileProcessLog.setProcessedDate(getCurrentDateWithTime());
            dfsCsvRow.setValid(false);
            fileProcessLogs.add(fileProcessLog);
        }

        if (StringUtils.isBlank(dfsCsvRow.getMailDate())) {
            fileProcessLog = new FileProcessLog();
            fileProcessLog.setSerialNumber(StringUtils.isBlank(dfsCsvRow.getSerialNumber()) ? null : parseSerialNumber(dfsCsvRow.getSerialNumber()));
            fileProcessLog.setFileName(fileName);
            fileProcessLog.setLogEntry(String.format(FileProcessErrorMessage.EMPTY_FIELD.getMessage(), integer, MAIL_DATE));
            fileProcessLog.setProcessedDate(getCurrentDateWithTime());
            dfsCsvRow.setValid(false);
            fileProcessLogs.add(fileProcessLog);
        }
    }

    private void checkForValidSerialNumber(String fileName, DFSCsvRow dfsCsvRow, List<FileProcessLog> fileProcessLogs, Integer integer) {
        if (StringUtils.isNotBlank(dfsCsvRow.getSerialNumber())) {
            Long serialNumber = null;
            try {
                serialNumber = Long.valueOf(dfsCsvRow.getSerialNumber());
            } catch (Exception ex) {
                log.warn("Invalid serial number (could not be converted to number value");
                addFileProcessLog(fileName, dfsCsvRow, fileProcessLogs, FileProcessErrorMessage.INVALID_FORMAT.getMessage(), SERIAL_NUMBER, integer);
            }

            if (Objects.nonNull(serialNumber)) {
                if (serialNumber <= 0 || serialNumber > serialRangeLimit) {
                    addFileProcessLog(fileName, dfsCsvRow, fileProcessLogs, FileProcessErrorMessage.INVALID_RANGE.getMessage(), SERIAL_NUMBER, integer);
                }
            }
        }
    }

    private void checkForDuplicate(String fileName, DFSCsvRow dfsCsvRow, List<FileProcessLog> fileProcessLogs, List<String> serialNumbers, Integer integer) {

        if (StringUtils.isNotBlank(dfsCsvRow.getSerialNumber())) {

            String serialNumber = parseSerialNumber(dfsCsvRow.getSerialNumber());
            CermsAcs cermsAcs = cermsAcsRepository.findBySerialNumber(serialNumber);

            if (Objects.nonNull(cermsAcs)) {
                FileProcessLog fileProcessLog = new FileProcessLog();
                fileProcessLog.setSerialNumber(serialNumber);
                fileProcessLog.setFileName(fileName);
                fileProcessLog.setLogEntry(String.format(FileProcessErrorMessage.DUPLICATE_FIELD.getMessage(), integer, SERIAL_NUMBER));
                fileProcessLogs.add(fileProcessLog);
                fileProcessLog.setProcessedDate(getCurrentDateWithTime());
                dfsCsvRow.setValid(false);
            }

            if (serialNumbers.contains(serialNumber)) {
                FileProcessLog fileProcessLog = new FileProcessLog();
                fileProcessLog.setSerialNumber(serialNumber);
                fileProcessLog.setFileName(fileName);
                fileProcessLog.setLogEntry(String.format(FileProcessErrorMessage.DUPLICATE_FIELD_FILE.getMessage(), SERIAL_NUMBER, integer));
                fileProcessLogs.add(fileProcessLog);
                fileProcessLog.setProcessedDate(getCurrentDateWithTime());
                dfsCsvRow.setValid(false);
            }
        }


    }

    private void checkForMailDateFormat(String fileName, DFSCsvRow dfsCsvRow, List<FileProcessLog> fileProcessLogs, Integer integer) {
        Date date;
        boolean validFormat = true;
        try {
            SimpleDateFormat sdf = new SimpleDateFormat(MAIL_DATE_FORMAT);
            date = sdf.parse(dfsCsvRow.getMailDate());
            if (!dfsCsvRow.getMailDate().equals(sdf.format(date))) {
                FileProcessLog fileProcessLog = new FileProcessLog();
                fileProcessLog.setSerialNumber(StringUtils.isBlank(dfsCsvRow.getSerialNumber()) ? null : parseSerialNumber(dfsCsvRow.getSerialNumber()));
                fileProcessLog.setFileName(fileName);
                fileProcessLog.setLogEntry(String.format(FileProcessErrorMessage.INVALID_MAIL_DATE.getMessage(), integer, MAIL_DATE));
                fileProcessLogs.add(fileProcessLog);
                fileProcessLog.setProcessedDate(getCurrentDateWithTime());
                validFormat = false;
            }
        } catch (Exception ex) {
            log.info("The mail date could not be parsed. " + ex);
            validFormat = false;
        }

        dfsCsvRow.setValid(validFormat);
    }

    private void addFileProcessLog(String fileName, DFSCsvRow dfsCsvRow, List<FileProcessLog> fileProcessLogs, String message, String field, Integer integer) {
        FileProcessLog fileProcessLog = new FileProcessLog();
        fileProcessLog.setSerialNumber(dfsCsvRow.getSerialNumber());
        fileProcessLog.setFileName(fileName);
        fileProcessLog.setLogEntry(String.format(message, integer, field));
        fileProcessLogs.add(fileProcessLog);
        fileProcessLog.setProcessedDate(getCurrentDateWithTime());
        dfsCsvRow.setValid(false);
    }

    private String parseSerialNumber(String serialNumber) {
        return serialNumber.replaceAll(
                "[^a-zA-Z0-9]", "");
    }
}
