package uz.relocation.adminjjnbot.service.currency.supplier.impl;

import lombok.SneakyThrows;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Component;
import uz.relocation.adminjjnbot.entity.currency.Currency;
import uz.relocation.adminjjnbot.service.currency.supplier.CurrencyConversationService;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

@Component
public class CbuCurrencyConversionService implements CurrencyConversationService {

    private final String BankName = "https://cbu.uz";

    public String getBankName() {
        return BankName;
    }

    @Override
    public double getCurrencyRate(Currency originalCurrency, Currency targetCurrency) {
        double originalCurrencyRate = getRate(originalCurrency);
        double targetCurrencyRate = getRate(targetCurrency);
        return originalCurrencyRate / targetCurrencyRate;
    }

    @SneakyThrows
    private double getRate(Currency currency) {
        if (currency == Currency.UZS) {
            return 1;
        }
        Document document = Jsoup.connect("https://cbu.uz/ru/arkhiv-kursov-valyut/#").get();
        Elements tables = document.select("table");
        for (Element table:
             tables) {
            Elements rows = table.select("tr");
            for (int countTR = 1; countTR < rows.size(); countTR++) {
                Elements data = rows.get(countTR).select("td");
                Integer ask = Integer.parseInt(data.get(3).text());
                if (ask.equals(currency.getId())) {
                    return Double.parseDouble(data.select(".text-right .currency_exchange").text());
                    }
                }
        }
        return 0;
    }
}
