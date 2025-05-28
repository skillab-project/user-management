package gr.uom.user_management;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
@Import(TestMailConfig.class)
class UserManagementApplicationTests {

	@Test
	void contextLoads() {
	}

}
