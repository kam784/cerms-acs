package com.perspecta.cerms.acs.business.service.delegate.email;

import com.perspecta.cerms.acs.configuration.MailConfig;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.mail.javamail.MimeMessagePreparator;
import org.springframework.stereotype.Component;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.io.File;
import java.io.IOException;
import java.util.List;

@Component
@Slf4j
public class ApplicationEmailSender {

	@Autowired
	private JavaMailSender mailSender;

	public void sendEmail(List<File> errorFiles, String message) throws MessagingException, IOException {
		MimeMessagePreparator preparator =  new MimeMessagePreparator() {
			public void prepare(MimeMessage mimeMessage) throws Exception {
				MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true);

				helper.setFrom("khurram.sultan@uspsector.com");
				helper.setTo(new InternetAddress("khurram.sultan@uspsector.com"));
				helper.setSubject("File Process Error Report");
				helper.setText(message, true);

				errorFiles.forEach(file -> {
					try {
						FileSystemResource fileAttachment = new FileSystemResource(file);
						helper.addAttachment(file.getName(), fileAttachment);
					}catch (Exception ex) {
						log.error("Error while attaching the file.");
					}
				});
			}
		};

		try {
			mailSender.send(preparator);
		}
		catch (MailException ex) {
			// simply log it and go on...
			log.warn(ex.getMessage());
		}

	}

}
