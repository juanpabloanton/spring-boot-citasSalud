package com.citassalud;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest
@TestPropertySource(properties = {
		"whatsapp.cloud-api.numero-telefono-id=test-phone-id",
		"whatsapp.cloud-api.token-acceso=test-token",
		"whatsapp.webhook.token-verificacion=test-verify-token"
})
class SpringBootCitasSaludApplicationTests {

	@Test
	void contextLoads() {
	}

}
