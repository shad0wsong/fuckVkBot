package com.danya.tech.config;

import com.danya.tech.bot.FuckVkBot;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

@Configuration
public class FuckVkBotConfig {

    @Bean
    public TelegramBotsApi telegramBotsApi(FuckVkBot fuckVkBot) throws TelegramApiException {
        var api = new TelegramBotsApi(DefaultBotSession.class);
        api.registerBot(fuckVkBot);
        return api;
    }
}
