package ru.rembo.bot.telegram.updatehandlers;

import com.google.gson.Gson;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import ru.rembo.bot.telegram.GlobalProperties;

import java.io.*;
import java.util.Scanner;

public class PHPBotRunner extends TelegramLongPollingBot {

    ProcessBuilder builder;
    private String name;
    public Process process = null;
    public BufferedReader reader;
    public BufferedWriter writer;
    public Gson gson = new Gson();
    private String botToken;
    public OutputStream stdin = null;
    public InputStream stdout = null;

    public PHPBotRunner(String name, String botToken, String workdir, String shellScript) {
        super();
        this.name = name;
        this.botToken = botToken;
        builder = new ProcessBuilder(shellScript);
        builder.directory(new File(workdir));

        try {
            process = builder.start();
            stdin = process.getOutputStream();
            stdout = process.getInputStream();
            reader = new BufferedReader(new InputStreamReader(stdout));
            writer = new BufferedWriter(new OutputStreamWriter(stdin));
            //builder.
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public PHPBotRunner() {
        this(GlobalProperties.get("PHPBotName"),
                GlobalProperties.get("PHPBotToken"),
                GlobalProperties.get("PHPBotWorkDir"),
                GlobalProperties.get("PHPBotShellScript"));
    }

    public String getBotUsername() {
        return this.name;
    }

    public String getBotToken() {
        return this.botToken;
    }

    public void onUpdateReceived(Update update) {
        String requestStr;
        PHPBotRequest request = new PHPBotRequest();

        class WaitBotOutput implements Runnable {
            private PHPBotRequest req;
            public WaitBotOutput(PHPBotRequest request) {
                this.req = request;
            }

            public void run() {
                String line;
                StringBuilder output = new StringBuilder();
                SendMessage message = new SendMessage();
                PHPBotResponse response;
                Scanner scanner = new Scanner(stdout);
                boolean flush = false;
                while ((!flush) && (scanner.hasNextLine())) {
                    line = scanner.nextLine();
                    if (!line.isEmpty()) {
                        System.out.println(line);
                        response = gson.fromJson(line.trim(), PHPBotResponse.class);
                        if ((response.recipient.equals("parentProcess"))
                                && (response.body.equals("flush"))
                                && (response.sender.equals(getBotUsername()))) {
                            flush = true;
                        } else {
                            output.append(line);
                        }
                    }
                }
                if (output.length() > 0) {
                    response = gson.fromJson(output.toString().trim(), PHPBotResponse.class);
                    if (response.recipient.equals("parentProcess")) {
                        if ((response.body.equals("shutdown"))
                                && (response.sender.equals(getBotUsername()))) {
                            System.exit(0);
                        }
                    }
                    message.setChatId(response.recipient);
                    message.setText(response.body);
                    try {
                        execute(message); // Call method to send the message
                    } catch (TelegramApiException e) {
                        e.printStackTrace();
                    }
                }
            }
        }

        // We check if the update has a message and the message has text
        if (update.hasMessage() && update.getMessage().hasText()) {
            request.from = update.getMessage().getChatId().toString();
            request.body = update.getMessage().getText();
            System.out.println(request.from + ", " + request.body);

            // Call router
            requestStr = gson.toJson(request);
            System.out.println(requestStr);
            try {
                writer.write(requestStr);
                writer.write("\n");
                writer.flush();

                new WaitBotOutput(request).run();

            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    }

}
