package ru.rembo.bot.telegram.updatehandlers;

import org.telegram.telegrambots.extensions.bots.commandbot.commands.IBotCommand;
import org.telegram.telegrambots.extensions.bots.commandbot.commands.ICommandRegistry;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.MessageEntity;
import org.telegram.telegrambots.meta.bots.AbsSender;
import ru.rembo.bot.telegram.statemachine.EventHandler;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Supplier;
import java.util.regex.Pattern;

public final class CacheCommandRegistry implements ICommandRegistry {
    private final Map<String, IBotCommand> commandRegistryMap = new HashMap<>();
    private final boolean allowCommandsWithUsername;
    private final Supplier<String> botUsernameSupplier;
    private BiConsumer<AbsSender, Message> defaultConsumer;
    private final Map<String, EventHandler<Message, SendMessage>> eventHandlerMap = new HashMap<>();

    public CacheCommandRegistry(boolean allowCommandsWithUsername, Supplier<String> botUsernameSupplier) {
        this.allowCommandsWithUsername = allowCommandsWithUsername;
        this.botUsernameSupplier = botUsernameSupplier;
    }

    public void registerDefaultAction(BiConsumer<AbsSender, Message> defaultConsumer) {
        this.defaultConsumer = defaultConsumer;
    }

    public final boolean register(IBotCommand botCommand) {
        if (this.commandRegistryMap.containsKey(botCommand.getCommandIdentifier())) {
            return false;
        } else {
            this.commandRegistryMap.put(botCommand.getCommandIdentifier(), botCommand);
            return true;
        }
    }

    public final boolean register(EventHandler<Message, SendMessage> eventHandler) {
        if (this.eventHandlerMap.containsKey(eventHandler.getHandlerIdentifier())) {
            return false;
        } else {
            this.eventHandlerMap.put(eventHandler.getHandlerIdentifier(), eventHandler);
            return true;
        }
    }

    public final Map<IBotCommand, Boolean> registerAll(IBotCommand... botCommands) {
        Map<IBotCommand, Boolean> resultMap = new HashMap<>(botCommands.length);

        for (IBotCommand botCommand : botCommands) {
            resultMap.put(botCommand, this.register(botCommand));
        }

        return resultMap;
    }

    public final boolean deregister(IBotCommand botCommand) {
        if (this.commandRegistryMap.containsKey(botCommand.getCommandIdentifier())) {
            this.commandRegistryMap.remove(botCommand.getCommandIdentifier());
            return true;
        } else {
            return false;
        }
    }

    public final Map<IBotCommand, Boolean> deregisterAll(IBotCommand... botCommands) {
        Map<IBotCommand, Boolean> resultMap = new HashMap<>(botCommands.length);

        for (IBotCommand botCommand : botCommands) {
            resultMap.put(botCommand, this.deregister(botCommand));
        }

        return resultMap;
    }

    public final Collection<IBotCommand> getRegisteredCommands() {
        return this.commandRegistryMap.values();
    }

    public final Collection<EventHandler<Message, SendMessage>> getRegisteredEventHandlers() {
        return this.eventHandlerMap.values();
    }

    public final IBotCommand getRegisteredCommand(String commandIdentifier) {
        return this.commandRegistryMap.get(commandIdentifier);
    }

    public final boolean executeCommand(AbsSender absSender, Message message) {
        String text = "";
        if (message.hasText()) {
            if (message.getText().startsWith("/")) {
                text = message.getText();
            }
        }
        if ((text.isEmpty()) && (message.getCaptionEntities().size() > 0)) {
            for (MessageEntity caption : message.getCaptionEntities()) {
                if (caption.getText().startsWith("/")) {
                    text = caption.getText();
                }
            }
        }
        if (!text.isEmpty()) {
            String commandMessage = text.substring(1);
            String[] commandSplit = commandMessage.split("\\s+");
            String command = this.removeUsernameFromCommandIfNeeded(commandSplit[0]);
            if (this.commandRegistryMap.containsKey(command)) {
                String[] parameters = Arrays.copyOfRange(commandSplit, 1, commandSplit.length);
                this.commandRegistryMap.get(command).processMessage(absSender, message, parameters);
                return true;
            }

            if (this.defaultConsumer != null) {
                this.defaultConsumer.accept(absSender, message);
                return true;
            }
        }

        return false;
    }

    private String removeUsernameFromCommandIfNeeded(String command) {
        if (this.allowCommandsWithUsername) {
            String botUsername = Objects.requireNonNull(this.botUsernameSupplier.get(), "Bot username must not be null");
            return command.replaceAll("(?i)@" + Pattern.quote(botUsername), "").trim();
        } else {
            return command;
        }
    }

}
