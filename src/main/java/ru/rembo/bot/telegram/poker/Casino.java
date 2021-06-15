package ru.rembo.bot.telegram.poker;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class Casino extends Stack {
    private int total = 0;
    private final Chip smallestChip;

    public Casino(int smallestChipValue) {
        smallestChip = Chip.valueOf(smallestChipValue);
    }

    public Chip getSmallestChip() {
        return smallestChip;
    }

    public int getTotal() {
        return total;
    }

    public Stack change(int sum) {
        Stack chips = new Stack();
        int remain = sum;
        List<Chip> values = Arrays.asList(Chip.values().clone());
        Collections.reverse(values);
        for (Chip chip : values) {
            if (chip.getValue() >= smallestChip.getValue()) {
                int count = remain / chip.getValue() / 2;
                for (int i = 0; i < count; i++) {
                    chips.add(chip);
                }
                remain -= count * chip.getValue();
            }
        }
        for (int i = remain / smallestChip.getValue(); i > 0; i--) {
            chips.add(smallestChip);
            remain -= smallestChip.getValue();
        }
        if (remain > 0) {
            throw new BadConditionException("Cannot change " + remain + " with " + smallestChip);
        }
        total += sum;
        System.out.println("Bank changes chips: " + chips + " for " + sum);
        return chips;
    }

    public Stack change(Stack chips) {
        return change(chips.getSum());
    }

    public int cashOut(Stack chips) {
        int sum = 0;
        for (Chip chip : chips) {
            sum += chip.getValue();
        }
        total -= sum;
        System.out.println("Bank cashes out " + sum + " for chips: " + chips);
        return sum;
    }
}
