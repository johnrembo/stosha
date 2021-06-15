package ru.rembo.bot.telegram.updatehandlers;

import org.telegram.telegrambots.extensions.bots.commandbot.commands.helpCommand.HelpCommand;
import org.telegram.telegrambots.meta.api.objects.Message;
import ru.rembo.bot.telegram.GlobalLogger;
import ru.rembo.bot.telegram.commands.CacheCommand;
import ru.rembo.bot.telegram.commands.ForwardCommand;
import ru.rembo.bot.telegram.commands.MarkdownCommand;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import ru.rembo.bot.telegram.GlobalProperties;
import ru.rembo.bot.telegram.commands.PostCommand;

import java.util.HashMap;

/**
 * Telegram @pulse_day Java Bot
 * @see <a href="https://t.me/pulse_day">https://t.me/pulse_day</a>
 *
 * Borrowed and updated by Rembo
 * @see <a href="https://github.com/rubenlagus/TelegramBotsExample">Telegram Bot Java Library</a>
 * @see <a href="https://github.com/rubenlagus/TelegramBots">Telegram Bot Java Library</a>
 * @author Timo Schulz (Mit0x2)
 */
public class CommandsHandler extends CacheCommandBot {

    public final String botName;
    public final String botToken;

    /**
     * Constructor.
     */
    public CommandsHandler(String botName, String botToken) {
        super();
        this.botName = botName;
        this.botToken = botToken;
        register(new MarkdownCommand());
        register(new PostCommand());
        register(new ForwardCommand());
        register(new CacheCommand());
        HelpCommand helpCommand = new HelpCommand();
        register(helpCommand);

        registerDefaultAction((absSender, message) -> {
            SendMessage commandUnknownMessage = new SendMessage();
            commandUnknownMessage.setChatId(message.getChatId().toString());
            commandUnknownMessage.setText("The command '" + message.getText() + "' is not known by this bot. Here comes some help ");
            try {
                absSender.execute(commandUnknownMessage);
            } catch (TelegramApiException e) {
                e.printStackTrace();
            }
            helpCommand.execute(absSender, message.getFrom(), message.getChat(), new String[] {});
        });
    }

    public CommandsHandler() {
        this(GlobalProperties.get("botName"), GlobalProperties.get("botToken"));
    }

    @Override
    public void processNonCommandUpdate(Update update) {
        if (update.hasMessage()) {
            Message message = update.getMessage();
            if (message.getChat().getType().equals("private")) {
                SendMessage echoMessage = new SendMessage();
                echoMessage.setChatId(message.getChatId().toString());
                echoMessage.setText("Cached message id: " +  message.getMessageId());
                try {
                    execute(echoMessage);
                } catch (TelegramApiException e) {
                    GlobalLogger.warning(e.getLocalizedMessage(), e);
                }
            }
        }
    }

    @Override
    public String getBotToken() {
        return this.botToken;
    }

    @Override
    public String getBotUsername() {
        return this.botName;
    }

}