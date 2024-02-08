package io.confluent;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import io.confluent.kstreamrouter.config.RoutingConfig;

//@SpringBootTest
class KstreamrouterApplicationTests {

	@Autowired
	RoutingConfig routingConfig;

	@Test
	void contextLoads() {
	}

}
