package uz.relocation.adminjjnbot.service.openai;

import org.springframework.stereotype.Component;
import uz.relocation.adminjjnbot.service.openai.supplier.AskingOpenAI;

@Component
public class OpenAILogic implements AskingOpenAI {

    @Override
    public String ask(String question) {
        return null;
    }
}
