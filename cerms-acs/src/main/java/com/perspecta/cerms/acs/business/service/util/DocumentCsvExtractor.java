package com.perspecta.cerms.acs.business.service.util;

import com.fasterxml.jackson.databind.MappingIterator;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;
import com.perspecta.cerms.acs.business.service.dto.DFSCsvRow;
import com.perspecta.cerms.acs.business.service.dto.SDCsvRow;
import lombok.extern.slf4j.Slf4j;
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

			MappingIterator<DFSCsvRow> iterator = dfsREADER.readValues(csvInputStream);

			while (iterator.hasNext()) {
				try {
					DFSCsvRow csvRow = iterator.next();
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

	public List<String> extractNixieCoaRows(File nixieCoaFile) {

		log.info("Extracting sd rows");

		List<String> nixieRows = new ArrayList<>();

		try {

			Long totalRows = 0L;
			Long successfulRows = 0L;

			Scanner scan = new Scanner(nixieCoaFile);

			while(scan.hasNextLine()){
				try {
					String line = scan.nextLine();
					nixieRows.add(line);

					totalRows++;
					successfulRows++;
				} catch (Throwable throwable) {
					log.warn(String.format("Nixie Row [%d] will be skipped - %s", totalRows, throwable.getMessage()));
					totalRows++;
				}
			}

			log.info(String.format("%d/%d nixie rows extracted successfully", successfulRows, totalRows));


		} catch (Throwable throwable) {
			log.error("Error while extracting nixie coa rows", throwable);
		}

		return nixieRows;

	}
}
