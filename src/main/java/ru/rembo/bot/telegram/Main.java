package ru.rembo.bot.telegram;

import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;
import ru.rembo.bot.telegram.updatehandlers.CommandsHandler;
import ru.rembo.bot.telegram.updatehandlers.PHPBotRunner;

/**
 * ru.rembo.bot.telegram.Main class to create all bots
 * @version 1.0.1
 * @author Ruben Bermudez, Rembo
 * @since 07.03.2021
 */

public class Main {

    public static void main(String[] args) {

        /* Init section */
        GlobalLogger.info("Starting bots");

        /* Bot section */
        try {
            TelegramBotsApi botsApi = new TelegramBotsApi(DefaultBotSession.class);
            CommandsHandler telegramCommands = new CommandsHandler();
            PHPBotRunner jsonRunner = new PHPBotRunner();
            try {
                GlobalLogger.info("Register " + telegramCommands.getBotUsername() +  " command bot");
                botsApi.registerBot(telegramCommands);
//                GlobalLogger.info("Register " + jsonRunner.getBotUsername() +  " PHPBot");
//                botsApi.registerBot(jsonRunner);
            } catch (TelegramApiException e) {
                jsonRunner.process.destroy();
                GlobalLogger.severe("Failed bot registration");
                throw new RuntimeException("SEVERE: Bots not registered", e);
            }
        } catch (TelegramApiException e) {
            GlobalLogger.severe("Failed bot init");
            throw new RuntimeException("SEVERE: Bots not initialized", e);
        }
    }

}
