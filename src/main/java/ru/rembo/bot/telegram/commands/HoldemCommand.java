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
import ru.rembo.bot.telegram.holdem.*;
import ru.rembo.bot.telegram.statemachine.AbstractEventMap;
import ru.rembo.bot.telegram.statemachine.BadStateException;
import ru.rembo.bot.telegram.statemachine.EventHandler;
import ru.rembo.bot.telegram.updatehandlers.CacheCommandBot;

import java.util.HashMap;
import java.util.HashSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Post a message to specified chat
 *
 * @author Rembo
 */

public class HoldemCommand extends BotCommand implements IBotCommand, IManCommand, EventHandler<Message, SendMessage> {

    private final HashMap<Long, Game> games = new HashMap<>();
    private final HashMap<Long, AbstractEventMap<HoldemEvent, HoldemCommand>> eventMaps = new HashMap<>();
    private Message message;
    private HoldemParsedEvent parsedEvent;
    private CacheCommandBot sender;

    public HoldemCommand() {
        super("holdem", "control Texas Hold'em croupier mode v 1.0 alfa");
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
        this.sender = (CacheCommandBot) absSender;
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
            if (!message.getChat().getType().equals("group") && !message.getChat().getType().equals("supergroup")) {
                answerText = "/" + getCommand() + " can be used only in group chats";
            } else {
                if (games.containsKey(message.getChatId())) {
                    answerText = "Game for " + message.getChat().getTitle() + " already started";
                } else {
                    games.put(message.getChatId(), new Game(message.getChat().getTitle()));
                    AbstractEventMap<HoldemEvent, HoldemCommand> eventMap = new AbstractEventMap<HoldemEvent, HoldemCommand>() {
                        @Override
                        public void initEventMap(HoldemCommand handler) {
                            put(HoldemEvent.HELP, games.get(message.getChatId())::doShowHelp);
                            put(HoldemEvent.NEW_PLAYER, games.get(message.getChatId())::doCreate);
                            put(HoldemEvent.JOIN_PLAYER, games.get(message.getChatId())::doJoin);
                            put(HoldemEvent.BUY_CHIPS, games.get(message.getChatId())::doByChips);
                            put(HoldemEvent.GO_AWAY, games.get(message.getChatId())::doGoAway);
                            put(HoldemEvent.COME_BACK, games.get(message.getChatId())::doComeBack);
                            put(HoldemEvent.GIVE_DECK, games.get(message.getChatId())::doGiveDeck);
                            put(HoldemEvent.SHUFFLE_DECK, games.get(message.getChatId())::doShuffle);
                            put(HoldemEvent.DEAL, games.get(message.getChatId())::doDeal);
                            put(HoldemEvent.SMALL_BLIND, games.get(message.getChatId())::doSmallBlind);
                            put(HoldemEvent.BIG_BLIND, games.get(message.getChatId())::doBigBlind);
                            put(HoldemEvent.BET, games.get(message.getChatId())::doBet);
                            put(HoldemEvent.CHECK, games.get(message.getChatId())::doCheck);
                            put(HoldemEvent.CALL, games.get(message.getChatId())::doCall);
                            put(HoldemEvent.RAISE, games.get(message.getChatId())::doRaise);
                            put(HoldemEvent.FOLD, games.get(message.getChatId())::doFold);
                            put(HoldemEvent.ALL_IN, games.get(message.getChatId())::doAllIn);
                            put(HoldemEvent.SHOW_FLOP, games.get(message.getChatId())::doShowFlop);
                            put(HoldemEvent.SHOW_TURN, games.get(message.getChatId())::doShowTurn);
                            put(HoldemEvent.SHOW_RIVER, games.get(message.getChatId())::doShowRiver);
                            put(HoldemEvent.RANK, games.get(message.getChatId())::doRank);
                            put(HoldemEvent.HIDDEN_RANK, games.get(message.getChatId())::doHiddenRank);
                            put(HoldemEvent.DISCARD, games.get(message.getChatId())::doDiscard);
                            put(HoldemEvent.SHOW_CARD, games.get(message.getChatId())::doShowCard);
                            put(HoldemEvent.ASK_CHANGE, games.get(message.getChatId())::doAskChange);
                            put(HoldemEvent.SELL_CHIPS, games.get(message.getChatId())::doSellChips);
                        }
                    };
                    eventMaps.put(message.getChatId(), eventMap);
                    eventMaps.get(message.getChatId()).initEventMap(this);
                    String chatMessage = games.get(message.getChatId()).getGlobalResult();

                    games.get(message.getChatId()).setBlinds(smallBlind, bigBlind);
                    chatMessage = chatMessage + "\n" + this.games.get(message.getChatId()).getGlobalResult();
                    answerText = chatMessage;
                }
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
        message = event;
        if (games.containsKey(message.getChatId())) {
            if ((parsedEvent == null) || !parsedEvent.getText().equals(event.getText())) {
                parsedEvent = new HoldemParsedEvent(event);
                games.get(message.getChatId()).setParsedEvent(parsedEvent);
            }
            return eventMaps.get(message.getChatId()).containsKey(parsedEvent.getEvent());
        }
        return false;
    }

    @Override
    public void handle(Message event) {
        if (handles(event)) {
            try {
                if (!parsedEvent.getEvent().equals(HoldemEvent.NEW_PLAYER)
                        && games.get(message.getChatId()).playerNotExists(parsedEvent.getId())) {
                    Player player = new Player(parsedEvent.getId(), parsedEvent.getName(), parsedEvent.getLocale());
                    games.get(message.getChatId()).getTable().addPlayer(player);
                }
                if (HoldemEvent.requireRound().contains(parsedEvent.getEvent())
                        && (games.get(message.getChatId()).roundNotStarted()))
                    throw new RuleViolationException("ROUND_NOT_STARTED");
                eventMaps.get(message.getChatId()).get(parsedEvent.getEvent()).run();
            } catch (BadStateException e) {
//                if (HoldemEvent.requireRound().contains(parsedEvent.getEvent())) {
                    throw new RuntimeException(e.getLocalizedMessage(parsedEvent.getLocale(), message.getFrom().getFirstName()), e);
//                }
            } catch (RuleViolationException e) {
                throw new RuntimeException(e.getLocalizedMessage(parsedEvent.getLocale()), e);
            } catch (BadConditionException e) {
                throw new RuntimeException(e.getMessage(), e);
            }
        }
    }

    public HashSet<SendMessage> getBulkAnswer(Message event) {
        HashSet<SendMessage> messages = new HashSet<>();
        games.get(event.getChatId()).getBulkResult().forEach((id, text) -> {
            SendMessage sendMessage = new SendMessage();
            if (sender.chatExists(id.longValue())) {
                sendMessage.setChatId(id.toString());
                sendMessage.setText("@" + games.get(event.getChatId()).getName() + ": " + text);
            } else {
                sendMessage.setChatId(event.getChatId().toString());
                sendMessage.setText("@" +  games.get(event.getChatId()).getTable().getById(id).getName()
                        + GlobalProperties.getRandomException("NO_PRIVATE_CHAT", GlobalProperties.defaultLocale) );
            }
            messages.add(sendMessage);
        });
        return messages;
    }

    public void clearBulkAnswer(Message event) {
        games.get(event.getChatId()).clearBulkResult();
    }

    @Override
    public String getHandlerIdentifier() {
        return getCommandIdentifier();
    }

    @Override
    public SendMessage getGlobalAnswer(Message event) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(event.getChatId().toString());
        sendMessage.setText(games.get(event.getChatId()).getGlobalResult());
        return sendMessage;
    }

    @Override
    public void clearGlobalAnswer(Message event) {
        games.get(event.getChatId()).clearGlobalResult();
    }

}
