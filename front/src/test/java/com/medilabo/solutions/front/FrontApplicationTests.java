package com.medilabo.solutions.front;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import com.medilabo.solutions.front.client.GatewayServiceClient;

@SpringBootTest
@TestPropertySource(properties = {
		"eureka.client.enabled=false",
		"spring.cloud.discovery.enabled=false"
})
@DisplayName("Front Application Tests")
public class FrontApplicationTests {

	@Autowired
	private ApplicationContext applicationContext;

	@MockitoBean
	private GatewayServiceClient gatewayServiceClient;

	@Test
	@DisplayName("Should load application context successfully")
	void contextLoads() {
		assertNotNull(applicationContext);
		assertTrue(applicationContext.containsBean("homeController"));
		assertTrue(applicationContext.containsBean("notesController"));
		assertTrue(applicationContext.containsBean("patientFormController"));
	}

	@Test
	@DisplayName("Should start application without errors")
	void shouldStartApplicationWithoutErrors() {
		assertTrue(true);
	}
}