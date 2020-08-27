package com.perspecta.cerms.acs.business.service.delegate.email;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class ApplicationHTMLCreator {

	private final TemplateEngine templateEngine;

	public Initiator withTemplate(String template){
		return new Initiator(template, this);
	}


	private String process(String template, Map<String, Object> variables) {
		Context context = new Context(Locale.US);
		context.setVariables(variables);

		return thymeleaf(context, template);
	}

	private String thymeleaf(Context thymeleafContext, String template) {
		try {
			return templateEngine.process(template, thymeleafContext);
		} catch (Throwable throwable) {
			log.warn(String.format("Error while processing template [%s] with thymeleaf engine", template), throwable);

			return null;
		}
	}

	public static class Initiator {
		private final String template;
		private final ApplicationHTMLCreator creator;
		private final Map<String, Object> variables;

		public Initiator(String template, ApplicationHTMLCreator creator) {
			this.template = template;
			this.creator = creator;
			this.variables = new HashMap<>();
		}

		public Initiator addVariable(String name, Object value) {
			this.variables.put(name, value);
			return this;
		}

		public String process() {
			return creator.process(template, variables);
		}
	}
}
