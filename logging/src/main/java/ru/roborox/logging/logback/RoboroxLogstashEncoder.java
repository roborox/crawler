package ru.roborox.logging.logback;

import com.fasterxml.jackson.databind.ObjectMapper;
import net.logstash.logback.encoder.LogstashEncoder;

import java.net.InetAddress;
import java.util.Map;


/**
 * Automatically adds hostname add env
 * 
 * @author dmitry
 *
 */
public class RoboroxLogstashEncoder extends LogstashEncoder {

	@Override
	public void setCustomFields(String customFields) {
		ObjectMapper mapper = new ObjectMapper();
		String processedCustomFields = customFields;
		try {
			String hostname = InetAddress.getLocalHost().getHostName();
			Map<String, String> fields = mapper.readValue(customFields, Map.class);
			fields.put("hostname", hostname);
			if (fields.getOrDefault("version", "SNAPSHOT").contains("SNAPSHOT")) {
				fields.put("env", "dev");
			} else {
				fields.put("env", "prod");
			}
			processedCustomFields = mapper.writeValueAsString(fields);
		} catch (Exception e) {
			e.printStackTrace();
		}
		super.setCustomFields(processedCustomFields);
	}
}
