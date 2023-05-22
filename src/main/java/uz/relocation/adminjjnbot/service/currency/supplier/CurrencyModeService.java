package uz.relocation.adminjjnbot.service.currency.supplier;

import uz.relocation.adminjjnbot.entity.currency.Currency;

public interface CurrencyModeService {

    Currency getOriginalCurrency(long chatId);

    Currency getTargetCurrency(long chatId);

    void setOriginalCurrency(long chatId, Currency currency);

    void setTargetCurrency(long chatId, Currency currency);

}
