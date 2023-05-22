package uz.relocation.adminjjnbot.service.currency;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageReplyMarkup;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import uz.relocation.adminjjnbot.entity.currency.Currency;
import uz.relocation.adminjjnbot.service.currency.supplier.CurrencyConversationService;
import uz.relocation.adminjjnbot.service.currency.supplier.CurrencyModeService;
import uz.relocation.adminjjnbot.service.currency.supplier.QuestionParser;
import uz.relocation.adminjjnbot.service.currency.supplier.impl.CbuCurrencyConversionService;
import uz.relocation.adminjjnbot.service.currency.supplier.impl.HashMapCurrencyModelService;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Optional;

@Component
public class CurrencyLogic implements CurrencyModeService, CurrencyConversationService, QuestionParser<Double> {

    HashMapCurrencyModelService currencyModeService;
    CbuCurrencyConversionService cbuCurrencyConversionService;
    private final Map<Long, Integer> COMMAND_MESSAGE = new HashMap<>(1);
    private final Map<Long, Integer> KEYBOARD_MESSAGE = new HashMap<>(1);

    @Autowired
    public CurrencyLogic(HashMapCurrencyModelService currencyModeService, CbuCurrencyConversionService cbuCurrencyConversionService) {
        this.currencyModeService = currencyModeService;
        this.cbuCurrencyConversionService = cbuCurrencyConversionService;
    }

    public void init(CallbackQuery callbackQuery) {
        Message message = callbackQuery.getMessage();
        String[] param = callbackQuery.getData().split("!");
        String action = param[0];
        switch (action) {
            case "ORIGINAL":
                setOriginalCurrency(message.getChatId(), Currency.valueOf(param[1]));
                break;
            case "TARGET":
                setTargetCurrency(message.getChatId(), Currency.valueOf(param[1]));
                break;
        }
    }

    public EditMessageReplyMarkup getCurrencyKeyboard(Message message) {
        return EditMessageReplyMarkup.builder()
                .chatId(message.getChatId().toString())
                .messageId(message.getMessageId())
                .replyMarkup(InlineKeyboardMarkup.builder().keyboard(
                        setKeyboard(message)).build())
                .build();
    }

    public DeleteMessage deleteQuestion(Message message) {
        return DeleteMessage.builder()
                .chatId(message.getChatId().toString())
                .messageId(message.getMessageId())
                .build();
    }
    public DeleteMessage deleteKeyboard(Message message) {
        return DeleteMessage.builder()
                .chatId(message.getChatId().toString())
                .messageId(KEYBOARD_MESSAGE.get(message.getChatId()))
                .build();
    }
    public DeleteMessage deleteCommand(Message message) {
        return DeleteMessage.builder()
                .chatId(message.getChatId().toString())
                .messageId(COMMAND_MESSAGE.get(message.getChatId()))
                .build();
    }

    public List<List<InlineKeyboardButton>> setKeyboard(Message message) {
        KEYBOARD_MESSAGE.put(message.getChatId(), message.getMessageId());
        Currency originalCurrency = currencyModeService.getOriginalCurrency(message.getChatId());
        Currency targetCurrency = currencyModeService.getTargetCurrency(message.getChatId());
        List<List<InlineKeyboardButton>> buttons = new ArrayList<>();
        for (Currency cur :
                Currency.values()) {
            buttons.add(
                    Arrays.asList(
                            InlineKeyboardButton.builder()
                                    .text(getOriginalCurrencyButton(originalCurrency, cur))
                                    .callbackData("ORIGINAL!" + cur)
                                    .build(),
                            InlineKeyboardButton.builder()
                                    .text(getTargetCurrencyButton(targetCurrency, cur))
                                    .callbackData("TARGET!" + cur)
                                    .build()));
        }
        return buttons;
    }

    private String getOriginalCurrencyButton (Currency saved, Currency current){
        return saved == current ? (current.name() + "✅") : (current.name());
    }

    private String getTargetCurrencyButton (Currency saved, Currency current){
        return saved == current ? ("✅" + current.name()) : (current.name());
    }

    public SendMessage getCurrencyMessage(Message message) {
        COMMAND_MESSAGE.put(message.getChatId(), message.getMessageId());
        return SendMessage.builder()
                .text("Выберите валюту перевода")
                .chatId(message.getChatId().toString())
                .replyMarkup(InlineKeyboardMarkup.builder().keyboard(
                        this.setKeyboard(message)).build())
                .build();
    }

    public SendMessage MainLogic(Message message) {
        String messageText = message.getText();
        Optional<Double> value = parseDouble(messageText);
        Currency originalCurrency = getOriginalCurrency(message.getChatId());
        Currency targetCurrency = getTargetCurrency(message.getChatId());
        Double rate = getCurrencyRate(originalCurrency, targetCurrency);
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");
        String date = now.format(dateFormatter);
        String time = now.format(timeFormatter);
        return (SendMessage.builder()
                .text(String.format("На %s %s по курсу c опубликованном на сайте %s %4.2f %s это %4.2f %s",
                        date,
                        time,
                        cbuCurrencyConversionService.getBankName(),
                        value.get(),
                        originalCurrency,
                        value.get() * rate,
                        targetCurrency))
                .chatId(message.getChatId().toString())
                .build());
    }

    public void clear(Message message) {
        currencyModeService.clear(message.getChatId());
        COMMAND_MESSAGE.remove(message.getChatId());
        KEYBOARD_MESSAGE.remove(message.getChatId());
    }

    private Optional<Double> parseDouble(String messageText) {
        try {
            return Optional.of(Double.parseDouble(messageText));
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    @Override
    public Currency getOriginalCurrency(long chatId) {
        return currencyModeService.getOriginalCurrency(chatId);
    }

    @Override
    public Currency getTargetCurrency(long chatId) {
        return currencyModeService.getTargetCurrency(chatId);
    }

    @Override
    public void setOriginalCurrency(long chatId, Currency currency) {
        currencyModeService.setOriginalCurrency(chatId, currency);
    }

    @Override
    public void setTargetCurrency(long chatId, Currency currency) {
        currencyModeService.setTargetCurrency(chatId, currency);
    }

    @Override
    public double getCurrencyRate(Currency originalCurrency, Currency targetCurrency) {
        return cbuCurrencyConversionService.getCurrencyRate(originalCurrency, targetCurrency);
    }

    @Override
    public Optional<Double> parse(String question) {
        try {
            return Optional.of(Double.parseDouble(question));
        } catch (Exception e) {
            return Optional.empty();
        }
    }

}
