package ru.rembo.bot.telegram.holdem;

import ru.rembo.bot.telegram.statemachine.BadStateException;

import java.util.*;

public class Stack extends ArrayList<Chip> {

    public Stack() {
        super();
    }

    public Stack(Stack chips) {
        super();
        addAll(chips);
    }


    public int getSum() {
        int sum = 0;
        for (Chip chip : this) {
            sum += chip.getValue();
        }
        return sum;
    }

    public void deposit(Stack chips) {
        addAll(chips);
    }

    public Stack withdraw(Stack chips) {
        chips.forEach(chip -> { if (indexOf(chip) != -1) remove(indexOf(chip)); });
        return chips;
    }

    public Stack withdraw(int sum) {
        Stack part = getPart(sum);
        withdraw(part);
        return part;
    }

    public Stack withdrawAll(int sum) {
        if (sum >= getSum()) {
            return withdraw(getSum());
        } else {
            return withdraw(sum);
        }
    }

    public Stack withdrawAll() {
        return withdrawAll(getSum());
    }

    public Stack getPart(int sum) {
        Stack part = new Stack();
        Stack remainingChips = new Stack(this);
        Collections.sort(remainingChips);
        int remain = sum;
        int step = remainingChips.size() - 1;
        while (!remainingChips.isEmpty() && (remain >= remainingChips.get(0).getValue())) {
            Chip chip = remainingChips.get(step--);
            if (remain >= chip.getValue()) {
                part.add(chip);
                remainingChips.remove(chip);
                remain -= chip.getValue();
            }
        }
        if ((remain > 0) && !remainingChips.isEmpty()) {
            throw new RuleViolationException("STACK_NO_CHANGE", remain);
        } else if (remain > 0) {
            throw new RuleViolationException("STACK_EMPTY", sum);
        }
        return part;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        forEach(builder::append);
        return "[" + builder + "]";
    }
}
