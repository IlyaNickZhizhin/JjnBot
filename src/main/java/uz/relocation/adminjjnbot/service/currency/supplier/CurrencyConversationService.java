package uz.relocation.adminjjnbot.service.currency.supplier;

import uz.relocation.adminjjnbot.entity.currency.Currency;

public interface CurrencyConversationService {

    double getCurrencyRate(Currency originalCurrency, Currency targetCurrency);

}
