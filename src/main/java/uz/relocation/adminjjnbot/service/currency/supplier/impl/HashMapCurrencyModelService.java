package uz.relocation.adminjjnbot.service.currency.supplier.impl;

import lombok.NoArgsConstructor;
import org.springframework.stereotype.Component;
import uz.relocation.adminjjnbot.entity.currency.Currency;
import uz.relocation.adminjjnbot.service.currency.supplier.CurrencyModeService;

import java.util.HashMap;
import java.util.Map;

@NoArgsConstructor
@Component
public class HashMapCurrencyModelService implements CurrencyModeService {

    private final Map<Long, Currency> originalCurrencyMap = new HashMap<>();
    private final Map<Long, Currency> targetCurrencyMap = new HashMap<>();

    @Override
    public Currency getOriginalCurrency(long chatId) {
        return originalCurrencyMap.get(chatId);
    }

    @Override
    public Currency getTargetCurrency(long chatId) {
        return targetCurrencyMap.get(chatId);
    }

    @Override
    public void setOriginalCurrency(long chatId, Currency currency) {
        originalCurrencyMap.put(chatId, currency);
    }

    @Override
    public void setTargetCurrency(long chatId, Currency currency) {
        targetCurrencyMap.put(chatId, currency);
    }

    public void clear(Long chatId){
        originalCurrencyMap.remove(chatId);
        targetCurrencyMap.remove(chatId);
    }
}
