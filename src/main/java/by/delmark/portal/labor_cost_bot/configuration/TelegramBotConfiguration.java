package by.delmark.portal.labor_cost_bot.configuration;

import com.pengrad.telegrambot.TelegramBot;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties("telegram.bot")
@Getter
public class TelegramBotConfiguration {

    @Getter @Setter
    private String token;

    @Bean
    public TelegramBot telegramBot() {
        return new TelegramBot(token);
    }
}
