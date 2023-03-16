package mil.army.rcce.kstreamrouterdatagen;

import mil.army.rcce.kstreamrouterdatagen.kafka.KafkaMessageProducer;
import mil.army.rcce.kstreamrouterdatagen.service.LogMessageGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@SpringBootApplication
public class KstreamrouterdatagenApplication implements ApplicationRunner {

	Logger logger = LoggerFactory.getLogger(KstreamrouterdatagenApplication.class);

	public static void main(String[] args) {
		SpringApplication.run(KstreamrouterdatagenApplication.class, args);
	}


	@Autowired
	private KafkaMessageProducer kafkaMessageProducer;

	@Autowired
	private LogMessageGenerator logMessageGenerator;

	@Override
	public void run(ApplicationArguments args) throws Exception {
		/*
		   Generate sample events
		 */
		logger.info("Inside run method...");
		int messageCount = logMessageGenerator.getMessageCount();

		for (int i = 0; i < messageCount; i++) {
			kafkaMessageProducer.generateLogEvent(logMessageGenerator.getNextMessage());
		}
	}


}
