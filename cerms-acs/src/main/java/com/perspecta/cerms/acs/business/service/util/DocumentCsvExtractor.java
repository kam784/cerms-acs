package com.perspecta.cerms.acs.business.service.util;

import com.fasterxml.jackson.databind.MappingIterator;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;
import com.perspecta.cerms.acs.business.service.dto.DFSCsvRow;
import com.perspecta.cerms.acs.business.service.dto.NixieCoaRow;
import com.perspecta.cerms.acs.business.service.dto.SDCsvRow;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.util.Strings;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.stream.Collectors;

@Component
@Slf4j
public class DocumentCsvExtractor {

	private static final ObjectReader dfsREADER =
			new CsvMapper().readerFor(DFSCsvRow.class)
					.with(
							CsvSchema.builder()
									.setSkipFirstDataRow(Boolean.TRUE)
									.setColumnSeparator('\t')
									.addColumn("serialNumber")
									.addColumn("caseNumber")
									.addColumn("docTypeId")
									.addColumn("mailDate")
									.build()
					);

	private static final ObjectReader sdREADER =
			new CsvMapper().readerFor(SDCsvRow.class)
					.with(
							CsvSchema.builder()
									.setSkipFirstDataRow(Boolean.FALSE)
									.addColumn("code")
									.addColumn("countyId")
									.addColumn("rawSerialNumber")
									.addColumn("destructionDate")
									.addColumn("destructionTime")
									.addColumn("randomNumber")
									.build()
					);

	private static final ObjectReader nixieREADER =
			new CsvMapper().readerFor(DFSCsvRow.class)
					.with(
							CsvSchema.builder()
									.setColumnSeparator('\t')
									.setSkipFirstDataRow(Boolean.TRUE)
									.addColumn("Serial")
									.addColumn("CaseNumb")
									.addColumn("DocTypeID")
									.addColumn("MailDate")
									.build()
					);

	public List<DFSCsvRow> extractDfsRows(InputStream csvInputStream){

		log.info("Extracting dfs rows");

		List<DFSCsvRow> dfsCsvRows = new ArrayList<>();

		try {
			Long totalRows = 0L;
			Long successfulRows = 0L;

			//extracting record in iterator
			MappingIterator<DFSCsvRow> iterator = dfsREADER.readValues(csvInputStream);

			while (iterator.hasNext()) {
				try {
					DFSCsvRow csvRow = iterator.next();
					// adding record in dfsCsvRows list
					dfsCsvRows.add(csvRow);
					totalRows++;
					successfulRows++;
				} catch (Throwable throwable) {
					log.warn(String.format("DFS Row [%d] will be skipped - %s", totalRows, throwable.getMessage()));
					totalRows++;
				}
			}

			log.info(String.format("%d/%d dfs rows extracted successfully", successfulRows, totalRows));

		} catch (Throwable throwable) {
			log.error("Error while extracting dfs rows", throwable);
		}

		return dfsCsvRows.stream()
				.map(dfsCsvRow -> {
					DFSCsvRow dfsCsv = new DFSCsvRow();
					dfsCsv.setSerialNumber(Strings.trimToNull(dfsCsvRow.getSerialNumber()));
					dfsCsv.setCaseNumber(Strings.trimToNull(dfsCsvRow.getCaseNumber()));
					dfsCsv.setDocTypeId(Strings.trimToNull(dfsCsvRow.getDocTypeId()));
					dfsCsv.setMailDate(Strings.trimToNull(dfsCsvRow.getMailDate()));
					return dfsCsv;
				})
				.collect(Collectors.toList());
	}

	public List<SDCsvRow> extractSdRows(InputStream csvInputStream){

		log.info("Extracting sd rows");

		List<SDCsvRow> sdCsvRows = new ArrayList<>();

		try {
			Long totalRows = 0L;
			Long successfulRows = 0L;

			MappingIterator<SDCsvRow> iterator = sdREADER.readValues(csvInputStream);

			while (iterator.hasNext()) {
				try {
					SDCsvRow csvRow = iterator.next();
					sdCsvRows.add(csvRow);
					totalRows++;
					successfulRows++;
				} catch (Throwable throwable) {
					log.warn(String.format("SD Row [%d] will be skipped - %s", totalRows, throwable.getMessage()));
					totalRows++;
				}
			}

			log.info(String.format("%d/%d sd rows extracted successfully", successfulRows, totalRows));

		} catch (Throwable throwable) {
			log.error("Error while extracting sd rows", throwable);
		}

		return sdCsvRows.stream()
				.map(sdCsvRow -> {
					SDCsvRow sdCsv = new SDCsvRow();
					sdCsv.setCode(Strings.trimToNull(sdCsvRow.getCode()));
					sdCsv.setCountyId(Strings.trimToNull(sdCsvRow.getCountyId()));
					sdCsv.setRawSerialNumber(Strings.trimToNull(sdCsvRow.getRawSerialNumber()));
					sdCsv.setDestructionDate(Strings.trimToNull(sdCsvRow.getDestructionDate()));
					sdCsv.setDestructionTime(Strings.trimToNull(sdCsvRow.getDestructionTime()));
					sdCsv.setRandomNumber(Strings.trimToNull(sdCsvRow.getRandomNumber()));
					return sdCsv;
				})
				.collect(Collectors.toList());
	}

	public List<NixieCoaRow> extractNixieCoaRows(File nixieCoaFile) {

		log.info("Extracting nixie coa rows");

		List<NixieCoaRow> nixieCoaRows = new ArrayList<>();

		try {

			Long totalRows = 0L;
			Long successfulRows = 0L;

			Scanner scan = new Scanner(nixieCoaFile);

			while(scan.hasNextLine()){
				try {
					String line = scan.nextLine();
					if (line.charAt(0) == 'H' || line.charAt(0) == 'D') {
						nixieCoaRows.add(parseNixieCOARecord(line));
						totalRows++;
						successfulRows++;
					}

				} catch (Throwable throwable) {
					log.warn(String.format("Nixie Row [%d] will be skipped - %s", totalRows, throwable.getMessage()));
					totalRows++;
				}
			}

			scan.close();
			log.info(String.format("%d/%d nixie rows extracted successfully", successfulRows, totalRows));


		} catch (Throwable throwable) {
			log.error("Error while extracting nixie coa rows", throwable);
		}

		return nixieCoaRows;
	}

	private NixieCoaRow parseNixieCOARecord(String record) {
		return NixieCoaRow.builder()
				.recordHeaderCode(record.substring(0,1))
				.responseDate(record.charAt(0) == 'H'? record.substring(9,17): null)
				.deliverabilityCode(record.charAt(0) == 'D'? record.substring(45,46):null)
				.countyId(record.charAt(0) == 'D'? record.substring(11,17): null)
				.serialNumber(record.charAt(0) == 'D'? record.substring(20,29):null)
				.changeOfAddress((record.charAt(0) == 'D' && StringUtils.isBlank(record.substring(45,46)))? parseChangeOfAddress(record):null)
				.build();
	}

	private String parseChangeOfAddress(String record) {
		return String.format("%s, %s, %s %s-%s",
				record.substring(373, 438).trim(),
				record.substring(317, 345).trim(),
				record.substring(345, 347).trim(),
				record.substring(347, 352).trim(),
				record.substring(354, 359).trim());

	}
}
