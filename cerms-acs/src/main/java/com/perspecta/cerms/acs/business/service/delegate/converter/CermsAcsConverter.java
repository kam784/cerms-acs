package com.perspecta.cerms.acs.business.service.delegate.converter;

import com.perspecta.cerms.acs.business.domain.dfs.CermsAcs;
import com.perspecta.cerms.acs.business.domain.dfs.CermsAcsRepository;
import com.perspecta.cerms.acs.business.domain.log.FileProcessLog;
import com.perspecta.cerms.acs.business.service.dto.DFSCsvRow;
import com.perspecta.cerms.acs.business.service.dto.SDCsvRow;
import com.perspecta.cerms.acs.business.service.dto.constant.FileProcessErrorMessage;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StreamUtils;
import org.springframework.util.StringUtils;

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

	public List<CermsAcs> dfsToCermsAcs (List<DFSCsvRow> dfsCsvRows) {

		return dfsCsvRows.stream()
				.map(dfsCsvRow -> {
					CermsAcs cermsAcs = new CermsAcs();
					cermsAcs.setSerialNumber(Long.valueOf(dfsCsvRow.getSerialNumber()));
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
					long serialNumber = Long.parseLong(sdCsvRow.getRawSerialNumber().replaceAll(
							"[^a-zA-Z0-9]", "").substring(sdCsvRow.getRawSerialNumber().length() - 9));

					CermsAcs cermsAcs = Optional.ofNullable(cermsAcsRepository.findBySerialNumber(serialNumber)).orElse(null);

					if(Objects.nonNull(cermsAcs)) {
						String scanDateString = sdCsvRow.getDestructionDate() + " " + sdCsvRow.getDestructionTime();
						cermsAcs.setDestructionDate(parseDateString(scanDateString));
					}

					return cermsAcs;
				})
				.collect(Collectors.toList());
	}

	public List<FileProcessLog> finalizeProcessLogs(List<FileProcessLog> fileProcessLogs, String fileName) {
		FileProcessLog fileProcessLog = new FileProcessLog();
		fileProcessLog.setFileName(fileName);
		fileProcessLog.setProcessedDate(getCurrentDate());

		if(CollectionUtils.isNotEmpty(fileProcessLogs)) {
			fileProcessLog.setComment(String.format(FileProcessErrorMessage.FILE_PROCESS_ERROR_MESSAGE.getMessage(), fileName, fileProcessLogs.size()));
		} else {
			fileProcessLog.setComment(String.format(FileProcessErrorMessage.FILE_PROCESS_SUCCESS_MESSAGE.getMessage(), fileName));
		}

		fileProcessLogs.add(fileProcessLog);

		return fileProcessLogs;
	}

	private Date parseDateString(String dateString){
		try {
			SimpleDateFormat formatter = new SimpleDateFormat("MM/dd/yyyy", Locale.ENGLISH);
			return formatter.parse(dateString);
		} catch (Exception ex) {
			log.warn("Could not parse date. " + ex);
		}
		return null;
	}

}
