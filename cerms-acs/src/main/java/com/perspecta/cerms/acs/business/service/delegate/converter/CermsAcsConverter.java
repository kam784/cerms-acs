package com.perspecta.cerms.acs.business.service.delegate.converter;

import com.perspecta.cerms.acs.business.domain.cerms_acs.CermsAcs;
import com.perspecta.cerms.acs.business.domain.cerms_acs.CermsAcsRepository;
import com.perspecta.cerms.acs.business.domain.log.FileProcessLog;
import com.perspecta.cerms.acs.business.service.dto.DFSCsvRow;
import com.perspecta.cerms.acs.business.service.dto.NixieCoaRow;
import com.perspecta.cerms.acs.business.service.dto.SDCsvRow;
import com.perspecta.cerms.acs.business.service.dto.constant.FileProcessErrorMessage;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

import static com.perspecta.cerms.acs.business.service.util.TimeUtils.getCurrentDate;
import static com.perspecta.cerms.acs.business.service.util.TimeUtils.getCurrentDateWithTime;

@Component
@AllArgsConstructor(onConstructor = @__(@Autowired))
@Slf4j
public class CermsAcsConverter {

	private final CermsAcsRepository cermsAcsRepository;

	private static final Long DOCTYPE_ID = 100862L;
	private static final String SD_CODE = "SD";

	public List<CermsAcs> dfsToCermsAcs (List<DFSCsvRow> dfsCsvRows) {

		return dfsCsvRows.stream()
				.map(dfsCsvRow -> {
					CermsAcs cermsAcs = new CermsAcs();
					cermsAcs.setSerialNumber(dfsCsvRow.getSerialNumber());
					cermsAcs.setCaseNumber(dfsCsvRow.getCaseNumber());
					cermsAcs.setDocTypeId(StringUtils.isEmpty(dfsCsvRow.getDocTypeId())? DOCTYPE_ID : Long.valueOf(dfsCsvRow.getDocTypeId()));
					cermsAcs.setMailDate(parseDateString(dfsCsvRow.getMailDate()));
					cermsAcs.setAddDate(getCurrentDateWithTime());
					return cermsAcs;
				})
				.collect(Collectors.toList());
	}

	public List<CermsAcs> sdToCermsAcs(List<SDCsvRow> sdCsvRows) {

		return sdCsvRows.stream()
				.map(sdCsvRow -> {
					String serialNumber = sdCsvRow.getRawSerialNumber().replaceAll(
							"[^a-zA-Z0-9]", "").substring(sdCsvRow.getRawSerialNumber().length() - 9);

					CermsAcs cermsAcs = Optional.ofNullable(cermsAcsRepository.findBySerialNumber(serialNumber)).orElse(null);

					if(Objects.nonNull(cermsAcs)) {
						String scanDateString = sdCsvRow.getDestructionDate() + " " + sdCsvRow.getDestructionTime();

						Date scanDate = parseDateString(scanDateString);

						if(Objects.isNull(cermsAcs.getDeliverabilityCode()) && Objects.isNull(cermsAcs.getResponseDate())){
							cermsAcs.setDeliverabilityCode(SD_CODE);
							cermsAcs.setResponseDate(scanDate);
						}

						cermsAcs.setDestructionDate(scanDate);
					}

					return cermsAcs;
				})
				.collect(Collectors.toList());
	}

	public List<CermsAcs> nixieCoaToCermsAcs(List<NixieCoaRow> nixieCoaRows) {

		String responseDateString = nixieCoaRows.stream()
				.filter(nixieCoaRow ->
						!StringUtils.isEmpty(nixieCoaRow.getResponseDate())
				)
				.map(NixieCoaRow::getResponseDate)
				.findFirst()
				.orElse(null);

		Date responseDate = parseDateStringToNewFormat(responseDateString);

		nixieCoaRows.removeIf(nixieCoaRow -> nixieCoaRow.getRecordHeaderCode().equalsIgnoreCase("H"));

		return nixieCoaRows.stream()
				.map(nixieCoaRow -> {

					String serialNumber = nixieCoaRow.getSerialNumber().replaceAll(
							"[^a-zA-Z0-9]", "");

					CermsAcs cermsAcs = Optional.ofNullable(cermsAcsRepository.findBySerialNumber(serialNumber)).orElse(null);

					if(Objects.nonNull(cermsAcs) &&
							!StringUtils.isEmpty(nixieCoaRow.getRecordHeaderCode()) &&
							nixieCoaRow.getRecordHeaderCode().equalsIgnoreCase("D") &&
							BooleanUtils.isTrue(nixieCoaRow.isValid())) {
						cermsAcs.setDeliverabilityCode(nixieCoaRow.getDeliverabilityCode());
						cermsAcs.setResponseDate(responseDate);
						cermsAcs.setCoaInfo(StringUtils.isEmpty(nixieCoaRow.getDeliverabilityCode().trim())? nixieCoaRow.getChangeOfAddress():null);
						cermsAcs.setUpdateDate(new Date());
					}

					return cermsAcs;
				})
				.collect(Collectors.toList());
	}

	public List<FileProcessLog> finalizeProcessLogs(List<FileProcessLog> fileProcessLogs, String fileName, FileProcessLog.FileType fileType) {
		FileProcessLog fileProcessLog = new FileProcessLog();
		fileProcessLog.setFileName(fileName);
		fileProcessLog.setProcessedDate(getCurrentDate());
		fileProcessLog.setFileType(fileType);

		fileProcessLogs.forEach(fileLog -> fileLog.setFileType(fileType));

		if(CollectionUtils.isNotEmpty(fileProcessLogs)) {
			fileProcessLog.setLogEntry(String.format(FileProcessErrorMessage.FILE_PROCESS_ERROR_MESSAGE.getMessage(), fileName, fileProcessLogs.size()));
			fileProcessLog.setLogStatus(FileProcessLog.LogStatus.FAILED);
		} else {
			fileProcessLog.setLogEntry(String.format(FileProcessErrorMessage.FILE_PROCESS_SUCCESS_MESSAGE.getMessage(), fileName));
			fileProcessLog.setLogStatus(FileProcessLog.LogStatus.SUCCESS);
		}

		fileProcessLogs.add(fileProcessLog);

		return fileProcessLogs;
	}

	private Date parseDateStringToNewFormat(String dateString) {
		try {
			DateFormat originalFormat = new SimpleDateFormat("yyyyMMdd", Locale.ENGLISH);
			DateFormat targetFormat = new SimpleDateFormat("MM/dd/yyyy");
			Date date = originalFormat.parse(dateString);
			String formattedDate = targetFormat.format(date);
			return targetFormat.parse(formattedDate);
		}  catch (Exception ex) {
			log.warn("Could not parse date. " + ex);
		}
		return null;
	}

	private Date parseDateString(String dateString){
		try {
			DateFormat formatter = new SimpleDateFormat("MM/dd/yyyy", Locale.ENGLISH);
			return formatter.parse(dateString);
		} catch (Exception ex) {
			log.warn("Could not parse date. " + ex);
		}
		return null;
	}

}
