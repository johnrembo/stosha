package ru.rembo.bot.telegram.holdem;

public enum Chip {
    ONE(1),
    FIVE(5),
    TEN(10),
    TWENTY_FIVE(25),
    FIFTY(50),
    HUNDRED(100),
    FIVE_HUNDRED(500),
    THOUSAND(1000);

    int value;

    Chip(int value) {
        this.value = value;
    }

    public static Chip valueOf(int value) {
        switch (value) {
            case 1: return ONE;
            case 5: return FIVE;
            case 10: return TEN;
            case 25: return TWENTY_FIVE;
            case 50: return FIFTY;
            case 100: return HUNDRED;
            case 500: return FIVE_HUNDRED;
            case 1000: return THOUSAND;
            default: throw new BadConditionException("No chip for " + value);
        }
    }

    public int getValue() {
        return value;
    }
}
