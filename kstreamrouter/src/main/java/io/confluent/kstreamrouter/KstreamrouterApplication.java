package io.confluent.kstreamrouter;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class KstreamrouterApplication {

	public static void main(String[] args) {
		System.out.println("in main");
		SpringApplication.run(KstreamrouterApplication.class, args);
	}

}
