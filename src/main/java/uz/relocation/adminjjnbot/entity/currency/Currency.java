package uz.relocation.adminjjnbot.entity.currency;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum Currency {
    UZS(0),
    RUB(643),
    BYN(933),
    UAH(980),
    USD(840),
    EUR(978);
    private final int id;
}
