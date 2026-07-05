package by.delmark.portal.labor_cost_bot;

import by.delmark.portal.labor_cost_bot.portal.PortalClient;
import com.pengrad.telegrambot.TelegramBot;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@SpringBootTest
class LaborCostBotApplicationTests {

	@MockitoBean
	private PortalClient portalClient;

	@MockitoBean
	private TelegramBot telegramBot;

	@Test
	void contextLoads() {
	}

}
