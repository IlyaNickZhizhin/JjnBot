package uz.relocation.adminjjnbot.controllerBot;

import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.MessageEntity;
import org.telegram.telegrambots.meta.api.objects.Update;
import uz.relocation.adminjjnbot.config.BotConfig;
import uz.relocation.adminjjnbot.service.currency.CurrencyLogic;
import uz.relocation.adminjjnbot.service.openai.OpenAILogic;

import java.util.Optional;

@Component
public class TelegramBot extends TelegramLongPollingBot {

    final BotConfig config;
    private final CurrencyLogic currencyLogic;
    private final OpenAILogic openAILogic;

    @Autowired
    public TelegramBot(BotConfig config, CurrencyLogic currencyLogic, OpenAILogic openAILogic) {
        this.config = config;
        this.currencyLogic = currencyLogic;
        this.openAILogic = openAILogic;
    }

    @Override
    public String getBotUsername() {
        return config.getBotName();
    }

    @Override
    public String getBotToken() {
        return config.getBotToken();
    }

    @Override
    @SneakyThrows
    public void onUpdateReceived(Update update) {
        if (update.hasCallbackQuery()) {
            handleCallback(update.getCallbackQuery());
        }
        if (update.hasMessage()) {
            handleMessage(update.getMessage());
        }
    }

    @SneakyThrows
    private void handleCallback(CallbackQuery callbackQuery) {
        if (callbackQuery.getData().contains("ORIGINAL") || callbackQuery.getData().contains("TARGET")) {
            currencyLogic.init(callbackQuery);
            execute(currencyLogic.getCurrencyKeyboard(callbackQuery.getMessage()));
        }
    }


    @SneakyThrows
    private void handleMessage (Message message){
        if (message.hasText() && message.hasEntities()) {
            Optional<MessageEntity> commandEntity =
                    message.getEntities().stream().filter(e -> "bot_command".equals(e.getType())).findFirst();
            if (commandEntity.isPresent()) {
                String command =
                        message
                                .getText()
                                .substring(commandEntity.get().getOffset(), commandEntity.get().getLength());
                switch (command) {
                    case "/set_currency":
                        execute(currencyLogic.getCurrencyMessage(message));
                        execute(currencyLogic.deleteCommand(message));
                        return;
                    case "/answer":
                        execute(
                                SendMessage.builder()
                                        .text("включен в режим ответа на вопросы")
                                        .chatId(message.getChatId().toString())
                                        .build());
                        return;
                }
            }
        }
        if (message.hasText()) {
            if (currencyLogic.parse(message.getText()).isPresent()){
                execute(currencyLogic.MainLogic(message));
                execute(currencyLogic.deleteQuestion(message));
                execute(currencyLogic.deleteKeyboard(message));
                currencyLogic.clear(message);
            }
        }
    }

    private Optional<String> parseQuestion(String messageText) {
        if (messageText.contains("❓Внимание, вопрос")) {
            try {
                return Optional.of(messageText.split("!")[1]);
            } catch (Exception e) {
                return Optional.empty();
            }
        }
        else return Optional.empty();
    }

}
