package by.delmark.portal.labor_cost_bot;

import by.delmark.portal.labor_cost_bot.portal.PortalClient;
import com.pengrad.telegrambot.TelegramBot;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@SpringBootTest
@TestPropertySource(properties = {
		"telegram.bot.token=test-token",
		"telegram.bot.telegram-user-id=0",
		"portal.credentials.username=test",
		"portal.credentials.password=test"
})
class LaborCostBotApplicationTests {

	@MockitoBean
	private PortalClient portalClient;

	@MockitoBean
	private TelegramBot telegramBot;

	@Test
	void contextLoads() {
	}

}
