package ru.rembo.bot.telegram.commands;

import org.telegram.telegrambots.extensions.bots.commandbot.commands.IBotCommand;
import org.telegram.telegrambots.extensions.bots.commandbot.commands.helpCommand.IManCommand;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.commands.BotCommand;
import org.telegram.telegrambots.meta.bots.AbsSender;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import ru.rembo.bot.telegram.GlobalLogger;
import ru.rembo.bot.telegram.holdem.*;
import ru.rembo.bot.telegram.statemachine.AbstractEventMap;
import ru.rembo.bot.telegram.statemachine.EventHandler;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Post a message to specified chat
 *
 * @author Rembo
 */

public class HoldemCommand extends BotCommand implements IBotCommand, IManCommand, EventHandler<String> {

    private Table table;
    private Casino casino;
    private Deck deck;
    private Game game;
    private String privateAnswer;
    private String globalAnswer;

    private final AbstractEventMap<HoldemEvent> eventMap = new AbstractEventMap<HoldemEvent>() {
        @Override
        public void initEventMap() {
            put(HoldemEvent.JOIN_PLAYER, game::join);
        }
    };


    public HoldemCommand() {
        super("holdem", "Control Texas Hold'em croupier mode");
    }

    @Override
    public String getCommandIdentifier() {
        return "holdem";
    }

    @Override
    public void processMessage(AbsSender absSender, Message message, String[] strings) {
        GlobalLogger.fine("HoldemCommand.processMessage(" + absSender.toString() +
                ", " + message.getText() +
                ", " + String.join(" ", strings));
        GlobalLogger.finer(message.toString());
        String text = String.join(" ", strings);
        String answerText = null;
        String answerChatId = message.getChatId().toString();
        Matcher startMatcher = Pattern.compile("^start(\\s+(?<small>\\d*[05])\\s*,\\s*(?<big>\\d+[05]))?").matcher(text);
        if (startMatcher.find()) {
            int smallBlind = 5;
            int bigBlind = 10;
            if ((startMatcher.group("small") != null) && (startMatcher.group("big") != null)) {
                smallBlind = Integer.parseInt(startMatcher.group("small"));
                bigBlind = Integer.parseInt(startMatcher.group("big"));
            }
            this.table = new Table(message.getChatId(), message.getChat().getTitle());
            this.casino = new Casino(5);
            this.deck = new Deck();
            this.game = new Game(this.casino, this.table);
            eventMap.initEventMap();
            String chatMessage = this.game.getGlobalMessage();
            this.game.setBlinds(smallBlind, bigBlind);
            chatMessage = chatMessage + "\n" + this.game.getGlobalMessage();
            answerChatId = message.getChatId().toString();
            answerText = chatMessage;
        }
        if (answerText != null) {
            try {
                SendMessage answer = new SendMessage();
                answer.setChatId(answerChatId);
                answer.setText(answerText);
                absSender.execute(answer);
            } catch (TelegramApiException e) {
                GlobalLogger.warning(e.getMessage() + "(" + e.getLocalizedMessage() + ")", e);
            }
        }
    }

    @Override
    public String getExtendedDescription() {
        return "Play Texas Hold'em Poker with bot as croupier. " +
                "Bot does not play itself";
    }

    @Override
    public String toMan() {
        return "Usage: /holdem command [params]\n" +
                "command = start|stop|stats|top|history\n" +
                "\t start [small blind, big blind] - Start Texas Hold'em croupier mode\n" +
                "\t stop - Stop taking game commands\n" +
                "\t stats - View player stats\n" +
                "\t top - View top player ratings\n" +
                "\t view [game id] - View game history\n" +
                "small blind, big blind - set initial blinds separated by comma. Default is 5, 10;\n" +
                "game id - unique game id;\n" +
                "E.g., /holdem start 5, 10";
    }

    @Override
    public String toString() {
        return "/" + this.getCommand() + "\n" + this.getDescription();
    }

    @Override
    public Runnable getAction(String text) {
        HoldemEvent event = parse(text);
        return this.eventMap.get(event);
    }

    @Override
    public boolean handles(String text) {
        HoldemEvent event = parse(text);
        return this.eventMap.containsKey(event);
    }

    private HoldemEvent parse(String text) {
        return text.contains("в игре") ? HoldemEvent.JOIN_PLAYER : null;
    }

    @Override
    public String getPrivateAnswer() {
        return privateAnswer;
    }

    @Override
    public String getGlobalAnswer() {
        return game.getGlobalMessage();
    }

    @Override
    public String getHandlerIdentifier() {
        return getCommandIdentifier();
    }


}
