package com.perspecta.cerms.acs.business.service.delegate;

import com.perspecta.cerms.acs.business.service.delegate.email.ApplicationEmailSender;
import com.perspecta.cerms.acs.business.service.delegate.email.ApplicationHTMLCreator;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.io.File;
import java.util.List;

@Component
@AllArgsConstructor(onConstructor = @__(@Autowired))
@Slf4j
public class ErrorEmailNotifier {

    private static final String TEMPLATE = "FileProcessError";

    private final ApplicationHTMLCreator applicationHTMLCreator;
    private final ApplicationEmailSender applicationEmailSender;

    public void sendErrorEmail(List<File> errorFiles) {

        try {
            if (!CollectionUtils.isEmpty(errorFiles)) {
                String emailBody = applicationHTMLCreator.withTemplate(TEMPLATE).process();
                applicationEmailSender.sendEmail(errorFiles, emailBody);
            }
        } catch (Exception ex) {
            log.error("Error while sending the file log email. " + ex);
        }
    }
}
