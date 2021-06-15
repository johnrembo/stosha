package ru.rembo.bot.telegram.commands;

import org.telegram.telegrambots.extensions.bots.commandbot.commands.IBotCommand;
import org.telegram.telegrambots.extensions.bots.commandbot.commands.helpCommand.IManCommand;
import org.telegram.telegrambots.meta.api.methods.ParseMode;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.commands.BotCommand;
import org.telegram.telegrambots.meta.bots.AbsSender;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import ru.rembo.bot.telegram.GlobalLogger;
import ru.rembo.bot.telegram.GlobalProperties;
import ru.rembo.bot.telegram.updatehandlers.CacheCommandBot;
import ru.rembo.bot.telegram.updatehandlers.CommandsHandler;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Post a message to specified chat
 *
 * @author Rembo
 */

public class PostCommand extends BotCommand implements IBotCommand, IManCommand {
    public PostCommand() {
        super("post", "Post message");
    }

    @Override
    public String getCommandIdentifier() {
        return "post";
    }

    @Override
    public void processMessage(AbsSender absSender, Message message, String[] strings) {
        GlobalLogger.fine("PostCommand.processMessage(" + absSender.toString() +
                ", " + message.getText() +
                ", " + String.join(" ", strings));
        GlobalLogger.finer(message.toString());
        Map<String, String> parseModes = Stream.of(new String[][] {
                {"plain", ""},
                {"md", ParseMode.MARKDOWN},
                {"md2", ParseMode.MARKDOWNV2},
                {"html", ParseMode.HTML},
                {"pulse", ParseMode.HTML},
                {"links", ParseMode.MARKDOWNV2}
        }).collect(Collectors.toMap(data -> data[0], data -> data[1]));
        String format = "plain";
        Message repost = message;
        String parseMode = null;
        Long chatId = message.getChatId();
        StringBuilder prefix = new StringBuilder("/" + getCommandIdentifier() +
                "(@" + GlobalProperties.get("botName") + ")?");
        for (String string : strings) {
            if (parseModes.containsKey(string)) {
                format = string;
                parseMode = parseModes.get(format);
                prefix.append("\\s+").append(string);
            } else {
                try {
                    if (((CacheCommandBot) absSender).chatExists(Long.parseLong(string))) {
                        chatId = Long.parseLong(string);
                        prefix.append("\\s+").append(string);
                    } else if (((CacheCommandBot) absSender).messageExists(Integer.parseInt(string))) {
                        repost = ((CacheCommandBot) absSender).getMessage(Integer.parseInt(string));
                        prefix.append("\\s+").append(string);
                    }
                } catch (NumberFormatException ignored) {
                    break;
                }
            }
        }
        String text;
        if (repost.hasPhoto()) {
            text = repost.getCaption().replaceFirst(prefix.toString(), "");
        } else {
            text = repost.getText().replaceFirst(prefix.toString(), "");
        }
        if (format.equals("links")) {
            Pattern pattern = Pattern.compile("\\[([^]]*)]\\(([^)]*)\\)([^\\[]*)");
            Matcher matcher = pattern.matcher(text);
            StringBuilder escaped = new StringBuilder();
            while (matcher.find()) {
                escaped.append("[")
                        .append(matcher.group(1).replaceAll("([\\\\`*_{}=|\\[\\]()#+\\-.!])", "\\\\$1"))
                        .append("](").append(matcher.group(2)).append(")").append(matcher.group(3));
            }
            text = escaped.toString();
        } else if (format.equals("pulse")) {
            Matcher urlMatcher = Pattern.compile("http[s]?://\\S*").matcher(text);
            StringBuilder escaped = new StringBuilder();
            while (urlMatcher.find()) {
                try {
                    URL url = new URL(urlMatcher.group(0));
                    String title = null;
                    URLConnection conn = url.openConnection();
                    conn.setRequestProperty("Accept-Charset", "UTF-8");
                    conn.setRequestProperty("Content-Type", "text/plain; charset=utf-8");
                    conn.setRequestProperty("User-Agent","Mozilla/5.0 ( compatible ) ");
                    conn.setRequestProperty("Accept","*/*");
                    String encoding = conn.getContentEncoding();
                    String inputLine;
                    if ((encoding == null) || "identity".equals(encoding)) {
                        String contentType = conn.getContentType();
                        String[] values = contentType.split(";"); // values.length should be 2
                        for (String value : values) {
                            value = value.trim();
                            if (value.toLowerCase().startsWith("charset=")) {
                                encoding = value.substring("charset=".length());
                            }
                        }
                        if (encoding == null || "".equals(encoding) || "identity".equals(encoding)) {
                            encoding = "utf-8";
                            URLConnection preload = url.openConnection();
                            preload.setRequestProperty("Accept-Charset", "UTF-8");
                            preload.setRequestProperty("Content-Type", "text/plain; charset=utf-8");
                            preload.setRequestProperty("User-Agent","Mozilla/5.0 ( compatible ) ");
                            preload.setRequestProperty("Accept","*/*");
                            BufferedReader brHeader = new BufferedReader(
                                    new InputStreamReader(preload.getInputStream()));
                            StringBuilder header = new StringBuilder();
                            Pattern cpPattern = Pattern.compile("<meta[^>]*charset=([^'\"]*)");
                            while ((inputLine = brHeader.readLine()) != null) {
                                header.append(inputLine);
                                Matcher cpMatcher = cpPattern.matcher(header.toString());
                                if (cpMatcher.find()) {
                                    encoding = cpMatcher.group(1);
                                    break;
                                }
                            }
                            brHeader.close();
                        }
                    }

                    BufferedReader br = new BufferedReader(
                            new InputStreamReader(conn.getInputStream(), encoding));
                    StringBuilder page = new StringBuilder();
                    Pattern ogPattern = Pattern.compile("<meta[^>]*og:title[^>]*content[^'\"]*('([^']*)'|\"([^\"]*)\")\\s*/?>");
                    while ((inputLine = br.readLine()) != null) {
                        page.append(inputLine);
                        Matcher ogMatcher = ogPattern.matcher(page.toString());
                        if (ogMatcher.find()) {
                            title = (ogMatcher.group(2) == null) ? ogMatcher.group(3) : ogMatcher.group(2);
                            break;
                        }
                    }
                    br.close();

                    if (title == null) {
                        Matcher titleMatcher = Pattern.compile("<title.*>(.*)</title>").matcher(page.toString());
                        if (titleMatcher.find()) {
                            title = titleMatcher.group(1);
                        } else {
                            title = url.getHost() + url.getPath();
                        }
                    }
                    title = title.replace("&laquo;", "«").replace("&raquo;","»");
                    escaped.append("<a href=\"").append(url).append("\">").append(title).append("</a>\n\n");
                } catch (IOException e) {
                    GlobalLogger.warning(e.getLocalizedMessage(), e);
                }
            }
            text = escaped.toString();
            GlobalLogger.fine(text);
        }
        try {
            if (repost.hasPhoto()) {
                SendPhoto answer = new SendPhoto();
                answer.setParseMode(parseMode);
                answer.setChatId(chatId.toString());
                answer.setCaption(text);
                if (!repost.equals(message)) {
                    answer.setCaptionEntities(repost.getCaptionEntities());
                }
                answer.setPhoto(new InputFile(repost.getPhoto().get(0).getFileId()));
                absSender.execute(answer);
            } else {
                SendMessage answer = new SendMessage();
                answer.setParseMode(parseMode);
                answer.disableWebPagePreview();
                answer.setChatId(chatId.toString());
                answer.setText(text);
                if (!repost.equals(message)) {
                    answer.setEntities(repost.getEntities());
                }
                absSender.execute(answer);
            }
        } catch (TelegramApiException e) {
            GlobalLogger.warning(e.getMessage() + "(" + e.getLocalizedMessage() + ")", e);
        }
    }

    @Override
    public String getExtendedDescription() {
        return "Post formatted message to specified chat directly or from cache";
    }

    @Override
    public String toMan() {
        return "Usage: /post [format] [chatId] messageId|formatted_text\n" +
                "format = md|md2|html|links, empty - plain text\n" +
                "\t md - Markdown\n" +
                "\t md2 - Markdown V2\n" +
                "\t html - HTML\n" +
                "\t links - Simple hypertext [label](URL)\n" +
                "chatId - cached internal telegram chat ID, empty - current chat;\n" +
                "messageId - cached internal telegram message ID, empty - formatted_text;\n" +
                "formatted_text - any text in specified format or plain text;\n" +
                "E.g., /post html -100000009 &lt;bold&gt; text&lt;/b&gt;\n" +
                "\t /post md2 *bold _italic bold ~italic bold strikethrough~ __underline italic bold___ bold*\n" +
                "\t /post -100000009 518\n" +
                "\t /post 518 -100000009 plain text\n" +
                "Argument order is arbitary\n" +
                "For further details refer to https://core.telegram.org/bots/api#formatting-options";
    }
}
