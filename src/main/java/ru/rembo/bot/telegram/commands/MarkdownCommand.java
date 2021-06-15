package ru.rembo.bot.telegram.commands;

import org.telegram.telegrambots.extensions.bots.commandbot.commands.IBotCommand;
import org.telegram.telegrambots.extensions.bots.commandbot.commands.helpCommand.IManCommand;
import org.telegram.telegrambots.meta.api.methods.ParseMode;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.commands.BotCommand;
import org.telegram.telegrambots.meta.bots.AbsSender;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import ru.rembo.bot.telegram.GlobalLogger;
import ru.rembo.bot.telegram.GlobalProperties;

import java.util.Arrays;

/**
 * Echo formatted Markdown (v2) multiline text
 *
 * @see <a href="https://core.telegram.org/bots/api#formatting-options">Formatting options</a>
 * @author Rembo
 */
public class MarkdownCommand extends BotCommand implements IBotCommand, IManCommand {

    public MarkdownCommand() {
        super("markdown", "Echo markdown");
    }

    @Override
    public String getCommandIdentifier() {
        return "markdown";
    }

    @Override
    public void processMessage(AbsSender absSender, Message message, String[] strings) {
        GlobalLogger.fine("MarkdownCommand.processMessage(" + absSender.toString() +
                    ", " + message.getText() +
                    ", " + String.join(" ", strings));
        String text = message.getText()
                .replaceFirst("/" + getCommandIdentifier() + "(@"+ GlobalProperties.get("botName") + ")?\\s*",
                        "");
        SendMessage answer = new SendMessage();
        answer.setChatId(message.getChat().getId().toString());
        answer.setText(text);
        answer.setParseMode(ParseMode.MARKDOWNV2);
        try {
            absSender.execute(answer);
            GlobalLogger.fine("Sent: " + text);
        } catch (TelegramApiException e) {
            GlobalLogger.warning("Send message failure");
            GlobalLogger.fine(absSender + ".execute(" + text +
                    ") failure: " + Arrays.toString(e.getStackTrace()));
        }
   }

    @Override
    public String getExtendedDescription() {
        return "Echo MarkdownV2 text";
    }

    @Override
    public String toMan() {
        return "Usage: /markdown markdown_text\n" +
                "markdown_text - Telegram API MarkdownV2 style text\n" +
                "E.g., /markdown *bold _italic bold ~italic bold strikethrough~ __underline italic bold___ bold*\n" +
                "\t[inline URL](http://www.example.com/)\n" +
                "For further details refer to https://core.telegram.org/bots/api#formatting-options";
    }
}