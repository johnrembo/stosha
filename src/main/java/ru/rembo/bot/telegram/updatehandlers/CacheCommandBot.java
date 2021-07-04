package ru.rembo.bot.telegram.updatehandlers;

import org.telegram.telegrambots.bots.DefaultBotOptions;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.extensions.bots.commandbot.commands.IBotCommand;
import org.telegram.telegrambots.extensions.bots.commandbot.commands.ICommandRegistry;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.bots.AbsSender;
import ru.rembo.bot.telegram.GlobalLogger;
import ru.rembo.bot.telegram.GlobalProperties;
import ru.rembo.bot.telegram.statemachine.EventHandler;

import java.io.*;
import java.time.Instant;
import java.util.*;
import java.util.function.BiConsumer;

public abstract class CacheCommandBot extends TelegramLongPollingBot implements ICommandRegistry {
    private final CacheCommandRegistry commandRegistry;
    protected final HashMap<Long, HashMap<Integer, Message>> chatCache = new HashMap<>();
    protected final String[] botAdmins = GlobalProperties.get("admins").split(";");

    public CacheCommandBot() {
        this(new DefaultBotOptions());
    }

    public CacheCommandBot(DefaultBotOptions options) {
        this(options, true);
    }

    public CacheCommandBot(DefaultBotOptions options, boolean allowCommandsWithUsername) {
        super(options);
        this.commandRegistry = new CacheCommandRegistry(allowCommandsWithUsername, this::getBotUsername);
        loadCache();
    }

    public boolean notAdmin(long chatId) {
        return !Arrays.asList(botAdmins).contains(Long.toString(chatId));
    }

    public List<Integer> getList(long chatId) {
        return new ArrayList<>(chatCache.get(chatId).keySet());
    }

    /**
     * Clear memory message cache
     * @param chatId  specify chatId to clear or 0 - for all
     */
    public void clear(long chatId) {
        if (chatCache.containsKey(chatId)) {
            chatCache.get(chatId).clear();
        } else if (chatId == 0) {
            for (long key : chatCache.keySet()) {
                chatCache.get(key).clear();
            }
        }
    }

    public void clearAll() {
        for (Long chatId : chatCache.keySet()) {
            clear(chatId);
        }
    }

    private int loadMessages (File[] files, long depth) throws IOException {
        int count = 0;
        for (File file : files) {
            if (file.isFile()) {
                try (FileInputStream streamIn = new FileInputStream(file.getAbsolutePath());
                     ObjectInputStream objectinputstream = new ObjectInputStream(streamIn)
                ) {
                    Message message = (Message) objectinputstream.readObject();
                    long now = Instant.now().getEpochSecond();
                    if ((now - message.getDate()) < depth * 60 * 60 * 24) {
                        putMessage(message);
                        count++;
                    }
                } catch (Exception e) {
                    GlobalLogger.warning("Failed loading message from " + file.getAbsolutePath());
                }
            } else {
                GlobalLogger.warning("Stranger file: " + file.getAbsolutePath());
            }
        }
        return count;
    }

    /**
     * Load message cache from persistent memory
     * @param chatId  specify chatId to load messages or 0 - for all
     * @param depth days from current system time inclusive
     */
    public void loadCache (long chatId, long depth) {
        GlobalLogger.info("Loading cache from: " + GlobalProperties.get("cacheDir"));
        File dir = new File(GlobalProperties.get("cacheDir"));
        File[] files = dir.listFiles();
        assert files != null;
        for (File file : files) {
            if (file.isDirectory() && ((chatId == 0) || file.getName().equals(String.valueOf(chatId)))) {
                GlobalLogger.fine("Loading chat id: " + file.getName());
                int count;
                try {
                    count = loadMessages(Objects.requireNonNull(file.listFiles()), depth);
                    GlobalLogger.fine("Loaded " + count + " message(s)");
                } catch (IOException e) {
                    GlobalLogger.warning("Failed loading messages from " + file.getAbsolutePath());
                }
            }
        }
    }

    /**
     * loadCache override with default depth from global properties
     * {@link CacheCommandBot#loadCache(long, long)}
     */
    public void loadCache(long chatId) {
        this.loadCache(chatId, Integer.parseInt(GlobalProperties.get("cacheDepth")));
    }

    /**
     * loadCache override with default depth from global properties and all chats
     * {@link CacheCommandBot#loadCache(long, long)}
     */
    public void loadCache() {
        this.loadCache(0);
    }

    private void saveMessage(Message message) {
        String fileName = GlobalProperties.get("cacheDir") +
                File.separator +message.getChatId() + File.separator + message.getMessageId();
        File file = new File(fileName);
        if (!file.getParentFile().exists() && !file.getParentFile().mkdirs()) {
            throw new IllegalStateException("Couldn't create dir: " + file.getParentFile());
        }
        try {
            final boolean newFile = file.createNewFile();
        } catch (IOException e) {
            GlobalLogger.warning("Could not create (chatId: " + fileName);
        }
        try (FileOutputStream fileOut = new FileOutputStream(fileName, true);
             ObjectOutputStream objectOut = new ObjectOutputStream(fileOut)) {
            objectOut.writeObject(message);
        } catch (IOException e) {
            GlobalLogger.warning("Could not save cache (chatId: " + message.getChatId() +
                    ", messageId: " + message.getMessageId());
        }
    }

    public void putMessage(Message message) {
        if (!chatCache.containsKey(message.getChatId())) {
            chatCache.put(message.getChatId(), new HashMap<>());
        }
        chatCache.get(message.getChatId()).put(message.getMessageId(), message);
        saveMessage(message);
        GlobalLogger.finest("Cached message id: " + message.getMessageId() + " in chat id: " + message.getChatId());
        GlobalLogger.finer(message.toString());
    }

    public boolean chatExists(Long chatId) {
        return chatCache.containsKey(chatId);
    }

    public boolean messageExists(Integer messageId) {
        for (HashMap<Integer, Message> entry : chatCache.values()) {
            if (entry.containsKey(messageId)) {
                return true;
            }
        }
        return false;
    }

    public boolean messageExists(Long chatId,  Integer messageId) {
        if (chatCache.containsKey(chatId)) {
            return chatCache.get(chatId).containsKey(messageId);
        }
        return false;
    }

    public Message getMessage(Integer messageId) {
        Message last = null;
        for (HashMap<Integer, Message> entry : chatCache.values()) {
            if (entry.containsKey(messageId)) {
                last = entry.get(messageId);
            }
        }
        return last;
    }

    public Message getMessage(Long chatId, Integer messageId) {
        if (chatCache.containsKey(chatId)) {
            if (chatCache.get(chatId).containsKey(messageId)) {
                return  chatCache.get(chatId).get(messageId);
            }
        }
        return null;
    }

    public final void onUpdateReceived(Update update) {
        try {
            if (update.hasMessage()) {
                Message message = update.getMessage();
                this.putMessage(message);
                if (message.isCommand() && !this.filter(message)) {
                    if (!this.commandRegistry.executeCommand(this, message)) {
                        this.processInvalidCommandUpdate(update);
                    }
                } else {
                    this.processNonCommandUpdate(update);
                }
            }
        } catch (Exception e) {
            GlobalLogger.warning(e.getLocalizedMessage(), e);
        }
    }

    protected void processInvalidCommandUpdate(Update update) {
        this.processNonCommandUpdate(update);
    }

    protected boolean filter(Message message) {
        return false;
    }

    public final boolean register(IBotCommand botCommand) {
        return this.commandRegistry.register(botCommand);
    }

    public final boolean registerEventHandler(EventHandler<Message, SendMessage> eventHandler) {
        return this.commandRegistry.register(eventHandler);
    }

    public final Map<IBotCommand, Boolean> registerAll(IBotCommand... botCommands) {
        return this.commandRegistry.registerAll(botCommands);
    }

    public final boolean deregister(IBotCommand botCommand) {
        return this.commandRegistry.deregister(botCommand);
    }

    @org.jetbrains.annotations.NotNull
    public final Map<IBotCommand, Boolean> deregisterAll(IBotCommand... botCommands) {
        return this.commandRegistry.deregisterAll(botCommands);
    }

    public final Collection<IBotCommand> getRegisteredCommands() {
        return this.commandRegistry.getRegisteredCommands();
    }

    public void registerDefaultAction(BiConsumer<AbsSender, Message> defaultConsumer) {
        this.commandRegistry.registerDefaultAction(defaultConsumer);
    }

    public final IBotCommand getRegisteredCommand(String commandIdentifier) {
        return this.commandRegistry.getRegisteredCommand(commandIdentifier);
    }

    public final Collection<EventHandler<Message, SendMessage>> getRegisteredEventHandlers() {
        return this.commandRegistry.getRegisteredEventHandlers();
    }

    public abstract String getBotUsername();

    public abstract void processNonCommandUpdate(Update var1);

}
