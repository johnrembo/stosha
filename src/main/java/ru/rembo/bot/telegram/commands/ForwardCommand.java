package ru.rembo.bot.telegram.commands;

import org.telegram.telegrambots.extensions.bots.commandbot.commands.IBotCommand;
import org.telegram.telegrambots.extensions.bots.commandbot.commands.helpCommand.IManCommand;
import org.telegram.telegrambots.meta.api.methods.ForwardMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.commands.BotCommand;
import org.telegram.telegrambots.meta.bots.AbsSender;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import ru.rembo.bot.telegram.GlobalLogger;
import ru.rembo.bot.telegram.updatehandlers.CacheCommandBot;

/**
 * Post a message to specified chat
 *
 * @author Rembo
 */


public class ForwardCommand extends BotCommand implements IBotCommand, IManCommand {

    public ForwardCommand() {
        super("forward", "Forward message");
    }

    @Override
    public String getCommandIdentifier() {
        return "forward";
    }

    @Override
    public void processMessage(AbsSender absSender, Message message, String[] strings) {
        GlobalLogger.fine("ForwardCommand.processMessage(" + absSender.toString() +
                ", " + message.getText() +
                ", " + String.join(" ", strings));
        GlobalLogger.finer(message.toString());
        CacheCommandBot sender = (CacheCommandBot) absSender;
        try {
            if (strings.length > 0) {
                Long sourceChatId = message.getChatId();
                int messageId;
                int decSepPos = strings[0].indexOf('.');
                if (decSepPos != -1) {
                    sourceChatId = Long.parseLong(strings[0].substring(0, decSepPos));
                    messageId = Integer.parseInt(strings[0].substring(decSepPos + 1));
                } else {
                    messageId = Integer.parseInt(strings[0]);
                }
                if (sender.messageExists(sourceChatId, messageId)) {
                    Message source = sender.getMessage(sourceChatId, messageId);
                    ForwardMessage forward = new ForwardMessage();
                    forward.setFromChatId(sourceChatId.toString());
                    forward.setMessageId(messageId);
                    forward.setChatId(message.getChatId().toString());
                    try {
                        if ((strings.length > 1) && sender.chatExists(Long.parseLong(strings[1]))) {
                            if (sender.notAdmin(message.getChatId())) {
                                throw new IllegalArgumentException("Access denied");
                            }
                            forward.setChatId(strings[1]);
                        }
                    } catch (NumberFormatException ignored) {
                        // Ignore
                    }
                    absSender.execute(forward);
                }
            } else {
                throw new IllegalArgumentException("Invalid messageId");
            }
        } catch (TelegramApiException | IllegalArgumentException recoverable) {
            try {
                SendMessage answer = new SendMessage();
                answer.disableWebPagePreview();
                answer.setChatId(message.getChatId().toString());
                answer.setText("Error: " + recoverable.getMessage() + "\n" + toMan());
                absSender.execute(answer);
            } catch (TelegramApiException e) {
                GlobalLogger.warning(e.getLocalizedMessage(), e);
            }
        }
    }

    @Override
    public String getExtendedDescription() {
        return "Forward message to specified chat from cache";
    }

    @Override
    public String toMan() {
        return "Usage: /forward [sourceChatId.]messageId [targetChatId]\n" +
                "messageId - cached internal telegram message ID;\n" +
                "targetChatId - cached internal telegram chat ID, empty - current chat;\n" +
                "Note: Specifying targetChatId requires admin privilege\n" +
                "E.g., /forward 518 -100000009";
    }

}
