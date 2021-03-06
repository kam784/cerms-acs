package com.perspecta.cerms.acs.schedular;

import com.perspecta.cerms.acs.business.service.DocumentProcessor;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import static com.perspecta.cerms.acs.business.service.util.TimeUtils.getCurrentDate;

@Component
@EnableScheduling
@AllArgsConstructor(onConstructor = @__(@Autowired))
@Slf4j
public class DocumentsPoller {

	private final DocumentProcessor documentProcessor;

	@Scheduled(cron = "${cerms.acs.document.poller.schedulerInterval:0 0/1 * ? * *}")
	public void process() {
		// Main entry point : starting process documents
		log.info("Documents Poller executing @ " + getCurrentDate());

		documentProcessor.processDocuments();

		log.info("Documents Poller executed @ " + getCurrentDate());
	}
}
