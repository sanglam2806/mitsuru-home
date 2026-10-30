package com.mitsuru.admin;

import org.junit.jupiter.api.Test;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;

// Starts the full app without external services: Postgres is swapped for in-memory H2,
// and RabbitMQ is never contacted because nothing publishes or listens during startup.
@SpringBootTest(properties = {
	"spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.H2Dialect",
	"app.jwt.secret=test-secret-that-is-at-least-32-characters-long",
	"app.admin.password=secret"
})
@AutoConfigureTestDatabase
class AdminApplicationTests {

	@Test
	void contextLoads() {
	}

}
