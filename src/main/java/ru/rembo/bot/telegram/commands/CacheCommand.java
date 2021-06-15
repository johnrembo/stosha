package ru.rembo.bot.telegram.commands;
import org.telegram.telegrambots.extensions.bots.commandbot.commands.IBotCommand;
import org.telegram.telegrambots.extensions.bots.commandbot.commands.helpCommand.IManCommand;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.commands.BotCommand;
import org.telegram.telegrambots.meta.bots.AbsSender;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import ru.rembo.bot.telegram.GlobalLogger;
import ru.rembo.bot.telegram.GlobalProperties;
import ru.rembo.bot.telegram.updatehandlers.CacheCommandBot;

import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;

/**
 * Post a message to specified chat
 *
 * @author Rembo
 */

public class CacheCommand extends BotCommand implements IBotCommand, IManCommand {
    public CacheCommand() {
        super("cache", "Manage cache");
    }

    @Override
    public String getCommandIdentifier() {
        return "cache";
    }

    public List<String> chop(String longString, int chunkSize, String separator) {
        List<String> list = new ArrayList<>();
        String[] strings = longString.split(separator);
        StringBuilder chunk = new StringBuilder();
        for (String string : strings) {
            if (chunk.length() + string.length() <= chunkSize) {
                chunk.append(string).append("\n");
            } else {
                list.add(chunk.toString());
                while (string.length() > chunkSize) {
                    list.add(string.substring(0, chunkSize - 1));
                    string = string.substring(chunkSize);
                }
                chunk = new StringBuilder(string + "\n");
            }
        }
        list.add(chunk.toString());
        return list;
    }

    private Long parseDepth(String arg) throws DateTimeParseException {
        LocalDate date = LocalDate.parse(arg, DateTimeFormatter.ofPattern("yyyyMMdd"));
        LocalDate today = LocalDate.now();
        return Math.abs(Duration.between(today.atStartOfDay(), date.atStartOfDay()).toDays()) + 1;
    }

    @Override
    public void processMessage(AbsSender absSender, Message message, String[] strings) {
        GlobalLogger.fine("CacheCommand.processMessage(" + absSender.toString() +
                ", " + message.getText() +
                ", " + String.join(" ", strings));
        GlobalLogger.finer(message.toString());
        CacheCommandBot sender = (CacheCommandBot) absSender;
        String text = "";
        try {
            if (strings.length == 0) {
                throw new IllegalArgumentException("No cache operation given");
            } else {
                long chatId = message.getChatId();
                int messageId = 0;
                long depth = Long.parseLong(GlobalProperties.get("cacheDepth"));
                if (strings[0].equals("list") || strings[0].equals("load")
                        || strings[0].equals("show") || strings[0].equals("clear")) {
                    if (strings.length > 1) {
                        if (sender.notAdmin(message.getChatId())) {
                            throw new IllegalArgumentException("Access denied");
                        }
                        if (strings[1].equals("all")) {
                            chatId = 0;
                        } else {
                            try {
                                int decSepPos = strings[1].indexOf('.');
                                if (decSepPos != -1) {
                                    chatId = Long.parseLong(strings[1].substring(0, decSepPos));
                                    messageId = Integer.parseInt(strings[1].substring(decSepPos + 1));
                                } else {
                                    chatId = Long.parseLong(strings[1]);
                                }
                                if (!sender.chatExists(chatId)) {
                                    throw new IllegalArgumentException("Chat does not exist");
                                }
                            } catch (NumberFormatException e) {
                                throw new IllegalArgumentException("Invalid chatId");
                            }
                            if (strings[1].matches("^\\d{8}$")) {
                                try {
                                    depth = parseDepth(strings[1]);
                                } catch (DateTimeParseException e) {
                                    // Ignore
                                }
                            } else if (strings.length > 2) {
                                try {
                                    depth = parseDepth(strings[2]);
                                } catch (DateTimeParseException e) {
                                    // Ignore
                                }
                            }
                        }
                    }
                }
                switch (strings[0]) {
                    case "list":
                        List<Integer> list = sender.getList(chatId);
                        list.sort(Comparator.naturalOrder());
                        List<String> stringList = new ArrayList<>();
                        Message listMessage;
                        for (Integer item : list) {
                            listMessage = sender.getMessage(chatId, item);
                            SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy");
                            Instant instant = Instant.ofEpochSecond(listMessage.getDate());
                            Date messageTime = Date.from(instant);
                            String messageText = "";
                            if (listMessage.hasText()) {
                                messageText = listMessage.getText();
                            } else if (listMessage.getCaption() != null) {
                                messageText = listMessage.getCaption();
                            }
                            messageText = (messageText.length() > 38) ? messageText.substring(0, 37) + "..." : messageText;
                            stringList.add(item.toString() + " [" + dateFormat.format(messageTime) + "] " + messageText);
                        }
                        text = String.join("\n", stringList);
                        if (text.length() == 0) {
                            text = "Cache is empty";
                        }
                        break;
                    case "clear":
                        if (sender.notAdmin(message.getChatId())) {
                            throw new IllegalArgumentException("Access denied");
                        }
                        if (chatId == 0) {
                            sender.clearAll();
                            text = "Cache cleared";
                        } else {
                            sender.clear(chatId);
                            text = "Chat #" + chatId + " cache cleared";
                        }
                        break;
                    case "load":
                        if (sender.notAdmin(message.getChatId())) {
                            throw new IllegalArgumentException("Access denied");
                        }
                        sender.loadCache(chatId, depth);
                        if (chatId == 0) {
                            text = "Cache loaded";
                        } else {
                            text = "Chat #" + chatId + " cache loaded";
                        }
                        break;
                    case "show":
                        if (messageId == 0) {
                            throw new IllegalArgumentException("Message not specifies");
                        } else if (sender.messageExists(messageId)) {
                            if (sender.getMessage(messageId).hasText()) {
                                text = sender.getMessage(messageId).getText();
                            } else if (sender.getMessage(messageId).getCaption() != null) {
                                text = sender.getMessage(messageId).getCaption();
                            } else {
                                text = "Message text is empty";
                            }
                        }
                        break;
                    case "depth":
                        if (strings.length > 1) {
                            if (sender.notAdmin(message.getChatId())) {
                                throw new IllegalArgumentException("Access denied");
                            }
                            try {
                                if (Integer.parseInt(strings[1]) < 1) {
                                    throw new NumberFormatException("Negative or zero depth given");
                                }
                                GlobalProperties.set("cacheDepth", strings[1]);
                                text = "Default cache depth is set to " + strings[1] + " days";
                            } catch (NumberFormatException e) {
                                throw new IllegalArgumentException("Cache depth must be whole positive number", e);
                            }
                        } else {
                            text = "Default cache depth is " + GlobalProperties.get("cacheDepth") + " days";
                        }
                        break;
                }
            }
        } catch (IllegalArgumentException recoverable) {
            text = "Error: " + recoverable.getMessage() + "\n" + toMan();
        }
        try {
            List<String> chunks = chop(text, 4096, "\n");
            for (String chunk : chunks) {
                SendMessage answer = new SendMessage();
                answer.setParseMode(null);
                answer.setChatId(message.getChatId().toString());
                answer.setText(chunk);
                absSender.execute(answer);
            }
        } catch (TelegramApiException e) {
            GlobalLogger.warning(e.getLocalizedMessage(), e);
        }
    }

    @Override
    public String getExtendedDescription() {
        return "Load, clear, tune memory cache";
    }

    @Override
    public String toMan() {
        return "Usage: /cache operation [args]\n" +
                "operation = list|show|load|clear|depth\n" +
                "\t list [chatId] - view list of cached message IDs from specified or current chat\n" +
                "\t show [chatId.]messageId - view message from specified or current chat\n" +
                "\t load [chatId|all] [YYYYMMDD] - load messages to specified or current chat from persistent cache to memory starting from specified date or using default depth\n" +
                "\t clear [chatId|all] - clear memory cache of specified chat or current chat\n" +
                "\t depth [days] - show or set default cache depth \n" +
                "chatId - cached internal telegram chat ID, empty - current chat;\n" +
                "messageId - cached internal telegram message ID, empty - formatted_text;\n" +
                "all - all chats in memory cache;\n" +
                "days - cache history depth in days from present (inclusive);\n" +
                "Note: Specifying chatId and using load, clear and depth set operations require admin privilege\n" +
                "E.g., /cache list -100000009\n" +
                "\t /cache show -100000009.512\n" +
                "\t /cache clear all\n" +
                "\t /depth 30\n";
    }
}
