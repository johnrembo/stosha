package ru.rembo.bot.telegram.commands;

import org.telegram.telegrambots.extensions.bots.commandbot.commands.IBotCommand;
import org.telegram.telegrambots.extensions.bots.commandbot.commands.helpCommand.IManCommand;
import org.telegram.telegrambots.meta.api.methods.send.SendAnimation;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.commands.BotCommand;
import org.telegram.telegrambots.meta.bots.AbsSender;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import ru.rembo.bot.telegram.GlobalLogger;
import ru.rembo.bot.telegram.holdem.*;
import ru.rembo.bot.telegram.statemachine.AbstractEventMap;
import ru.rembo.bot.telegram.statemachine.BadStateException;
import ru.rembo.bot.telegram.statemachine.EventHandler;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Post a message to specified chat
 *
 * @author Rembo
 */

public class HoldemCommand extends BotCommand implements IBotCommand, IManCommand, EventHandler<Message, SendMessage> {

    private Message message;
    private Game game;
    private final SendMessage privateAnswer = new SendMessage();
    private final SendMessage globalAnswer = new SendMessage();

    private HoldemParsedEvent parsedEvent;
    private final AbstractEventMap<HoldemEvent, HoldemCommand> eventMap = new AbstractEventMap<HoldemEvent, HoldemCommand>() {
        @Override
        public void initEventMap(HoldemCommand handler) {
            put(HoldemEvent.NEW_PLAYER, game::doCreate);
            put(HoldemEvent.JOIN_PLAYER, game::doJoin);
            put(HoldemEvent.BUY_CHIPS, game::doByChips);

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
        this.message = message;
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
            if (this.game == null) {
                this.game = new Game(message.getChatId(), message.getChat().getTitle());
                eventMap.initEventMap(this);
                String chatMessage = this.game.getGlobalResult();
                this.game.setBlinds(smallBlind, bigBlind);
                chatMessage = chatMessage + "\n" + this.game.getGlobalResult();
                answerText = chatMessage;
            } else {
                answerText = "Game already started";
            }

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
    public boolean handles(Message event) {
        if (game != null) {
            this.message = event;
            if ((parsedEvent == null) || !parsedEvent.getText().equals(event.getText())) {
                parsedEvent = new HoldemParsedEvent(event);
                game.setEvent(parsedEvent);
            }
            return this.eventMap.containsKey(parsedEvent.getEvent());
        }
        return false;
    }

    @Override
    public void handle(Message event) {
        if (handles(event)) {
            try {
                if (!game.playerExists(parsedEvent.getId())) {
                    Player player = new Player(parsedEvent.getId(), parsedEvent.getName());
                    game.getTable().addPlayer(player);
                }
                this.eventMap.get(parsedEvent.getEvent()).run();
                globalAnswer.setText(game.getGlobalResult());
                privateAnswer.setChatId(message.getFrom().getId().toString());
                if (game.getPrivateResult() != null) {
                    privateAnswer.setText(game.getPrivateResult());
                }
            } catch (BadStateException e) {
                globalAnswer.setText(String.format(e.getLocalizedMessage(parsedEvent.getLocale())
                        , message.getFrom().getFirstName()));
            }
            globalAnswer.setChatId(message.getChatId().toString());
        }
    }

    @Override
    public SendMessage getPrivateAnswer() {
        return privateAnswer;
    }

    @Override
    public SendMessage getGlobalAnswer() {
        return globalAnswer;
    }

    @Override
    public String getHandlerIdentifier() {
        return getCommandIdentifier();
    }
}
