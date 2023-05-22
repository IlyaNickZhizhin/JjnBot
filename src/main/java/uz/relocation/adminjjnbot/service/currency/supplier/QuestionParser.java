package uz.relocation.adminjjnbot.service.currency.supplier;

import java.util.Optional;

public interface QuestionParser<T> {

    Optional<T> parse(String question);

}
