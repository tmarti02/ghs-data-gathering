package gov.epa.exp_data_gathering.parse;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;

import kong.unirest.Unirest;

public class UtilitiesUnirest {
	
	public static void configUnirest(boolean turnOffLogging) {
		
		try {//Need to suppress logging because it slows things down when have big data sets...

			if (turnOffLogging) {
				Set<String> artifactoryLoggers = new HashSet<String>(Arrays.asList("org.apache.http"," org.apache.http.wire", "groovyx.net.http"));
				for(String log:artifactoryLoggers) {
					ch.qos.logback.classic.Logger artLogger = (ch.qos.logback.classic.Logger) org.slf4j.LoggerFactory.getLogger(log);
					artLogger.setLevel(ch.qos.logback.classic.Level.WARN);
					artLogger.setAdditive(false);
				}
			}
			
			Unirest.config()
	        .followRedirects(true)   
			.socketTimeout(000)
	           .connectTimeout(000);

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		
          

	}
}
