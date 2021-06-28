package ru.rembo.bot.telegram.updatehandlers;

import org.telegram.telegrambots.extensions.bots.commandbot.commands.helpCommand.HelpCommand;
import org.telegram.telegrambots.meta.api.objects.Message;
import ru.rembo.bot.telegram.GlobalLogger;
import ru.rembo.bot.telegram.commands.*;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import ru.rembo.bot.telegram.GlobalProperties;
import ru.rembo.bot.telegram.holdem.BadConditionException;
import ru.rembo.bot.telegram.holdem.RuleViolationException;
import ru.rembo.bot.telegram.statemachine.BadStateException;
import ru.rembo.bot.telegram.statemachine.EventHandler;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;

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
        HoldemCommand holdemCommand = new HoldemCommand();
        register(holdemCommand);
        registerEventHandler(holdemCommand);
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
            } else if (message.getChat().getType().equals("group") || message.getChat().getType().equals("supergroup")) {
                Collection<EventHandler<Message, SendMessage>> handlers = getRegisteredEventHandlers();
                for (EventHandler<Message, SendMessage> handler : handlers) {
                    if (handler.handles(message)) {
                        try {
                            handler.handle(message);
                            handler.getGlobalAnswer(message).setChatId(message.getChatId().toString());
                            if (!handler.getGlobalAnswer(message).getText().isEmpty()) {
                                execute(handler.getGlobalAnswer(message));
                                handler.clearGlobalAnswer(message);
                            }
                            HashSet<SendMessage> bulkAnswer = handler.getBulkAnswer(message);
                            for (SendMessage sendMessage : bulkAnswer) {
                                execute(sendMessage);
                            }
                            handler.clearBulkAnswer(message);
                        } catch (RuntimeException e) {
                            SendMessage errorAnswer = new SendMessage();
                            errorAnswer.setChatId(message.getChatId().toString());
                            errorAnswer.setText(e.getMessage());
                            GlobalLogger.finer(Arrays.toString(e.getStackTrace()));
                            try {
                                execute(errorAnswer);
                            } catch (TelegramApiException ex) {
                                GlobalLogger.warning(e.getLocalizedMessage(), e);
                                GlobalLogger.finer(Arrays.toString(e.getStackTrace()));
                            }
                        } catch (TelegramApiException e) {
                            GlobalLogger.warning(e.getLocalizedMessage(), e);
                            GlobalLogger.finer(Arrays.toString(e.getStackTrace()));
                        }
                    }
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