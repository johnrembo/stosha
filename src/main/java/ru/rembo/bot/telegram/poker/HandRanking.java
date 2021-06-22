package ru.rembo.bot.telegram.poker;

import java.util.*;

public class HandRanking {

    public static Combo calcCombo(HashSet<Card> hand) {

        HashMap<Rank, ArrayList<Card>> oneOfAKinds = new HashMap<>();
        HashMap<Suit, ArrayList<Card>> flushDraws = new HashMap<>();
        HashMap<Card, ArrayList<Card>> straightDraws = new HashMap<>();
        HashMap<Card, ArrayList<Card>> flushStraightDraws = new HashMap<>();

        List<Card> search = new ArrayList<>(hand);
        search.sort(Comparator.comparingInt(o -> o.rank.ordinal()));

        for (Card card : search) {
            if (!oneOfAKinds.containsKey(card.rank)) {
                oneOfAKinds.put(card.rank, new ArrayList<>());
            }
            oneOfAKinds.get(card.rank).add(card);

            if (!flushDraws.containsKey(card.suit)) {
                flushDraws.put(card.suit, new ArrayList<>());
            }
            flushDraws.get(card.suit).add(card);
        }

        ArrayList<Card> aces = new ArrayList<>();
        for (Card card : search) {
            if (card.rank.equals(Rank.ACE)) {
                aces.add(card);
            }
        }
        search.addAll(aces);

        for (Card card : search) {
            if (!straightDraws.containsKey(card)) {
                straightDraws.put(card, new ArrayList<>());
                straightDraws.get(card).add(card);
            }
            straightDraws.forEach((k, v) -> {
                if ((card.rank.ordinal() - v.get(v.size() - 1).rank.ordinal() == 1)
                        || (card.rank.equals(Rank.ACE) && v.get(v.size() - 1).rank.equals(Rank.KING))) {
                    v.add(card);
                } else if ((card.rank.ordinal() - v.get(v.size() - 1).rank.ordinal() == 0)
                        || (card.rank.equals(Rank.ACE) && v.get(v.size() - 1).rank.equals(Rank.ACE))) {
                    straightDraws.get(card).clear();
                    straightDraws.get(card).addAll(v);
                    straightDraws.get(card).add(card);
                }
            });

            if (!flushStraightDraws.containsKey(card)) {
                flushStraightDraws.put(card, new ArrayList<>());
                flushStraightDraws.get(card).add(card);
            }
            flushStraightDraws.forEach((k, v) -> {
                if ((card.suit == k.suit) && ((card.rank.ordinal() - v.get(v.size() - 1).rank.ordinal() == 1)
                        || (card.rank.equals(Rank.ACE) && v.get(v.size() - 1).rank.equals(Rank.KING)))) {
                    v.add(card);
                }
            });
        }

        HashMap<Card, ArrayList<Card>> flushes = filterDraws(flushDraws.values());
        HashMap<Card, ArrayList<Card>> straights = filterDraws(straightDraws.values());
        HashMap<Card, ArrayList<Card>> flushStraights = filterDraws(flushStraightDraws.values());

        Combo combo = null;

        for (Card highCard : flushStraights.keySet()) {
            if (highCard.rank.equals(Rank.ACE)) {
                combo = new Combo(Combination.ROYAL_FLUSH, highCard, flushStraights.get(highCard));
                break;
            } else {
                if ((combo == null) || (combo.highCard.rank.ordinal() < highCard.rank.ordinal())) {
                    combo = new Combo(Combination.STRAIGHT_FLUSH, highCard, flushStraights.get(highCard));
                }
            }
        }

        if (combo == null) {
            for (Rank highRank : oneOfAKinds.keySet()) {
                if (oneOfAKinds.get(highRank).size() >= 4) {
                    if ((combo == null) || (combo.highCard.rank.ordinal() < highRank.ordinal())) {
                        combo = new Combo(Combination.CARE, oneOfAKinds.get(highRank).get(0), oneOfAKinds.get(highRank));
                    }
                }
            }
        }

        Combo setCombo = null;
        if (combo == null) {
            for (Rank highRank : oneOfAKinds.keySet()) {
                if (oneOfAKinds.get(highRank).size() >= 3) {
                    if ((setCombo == null) || (setCombo.highCard.rank.ordinal() < highRank.ordinal())) {
                        setCombo = new Combo(Combination.SET, oneOfAKinds.get(highRank).get(0), oneOfAKinds.get(highRank));
                    }
                }
            }
            if (setCombo != null) {
                Combo pairOrSetCombo = null;
                HashSet<Rank> others = new HashSet<>(oneOfAKinds.keySet());
                others.remove(setCombo.highCard.rank);
                for (Rank highRank : others) {
                    if (oneOfAKinds.get(highRank).size() >= 2) {
                        if ((pairOrSetCombo == null) || (pairOrSetCombo.highCard.rank.ordinal() < highRank.ordinal())) {
                            pairOrSetCombo = new Combo(Combination.PAIR, oneOfAKinds.get(highRank).get(0), oneOfAKinds.get(highRank));
                        }
                    }
                }
                if (pairOrSetCombo != null) {
                    ArrayList<Card> fullHouseHand = new ArrayList<>(setCombo.hand);
                    fullHouseHand.addAll(pairOrSetCombo.hand);
                    combo = new Combo(Combination.FULL_HOUSE, setCombo.highCard, fullHouseHand);
                }
            }
        }

        if (combo == null) {
            for (Card highCard : flushes.keySet()) {
                if ((combo == null) || (combo.highCard.rank.ordinal() < highCard.rank.ordinal())) {
                    combo = new Combo(Combination.FLUSH, highCard, flushes.get(highCard));
                }
            }
        }

        if (combo == null) {
            for (Card highCard : straights.keySet()) {
                if ((combo == null) || (combo.highCard.rank.ordinal() < highCard.rank.ordinal())) {
                    combo = new Combo(Combination.STRAIGHT, highCard, straights.get(highCard));
                }
            }
        }

        if ((combo == null) && setCombo != null) {
            combo = setCombo;
        }

        Combo pairCombo = null;
        if (combo == null) {
            for (Rank highRank : oneOfAKinds.keySet()) {
                if (oneOfAKinds.get(highRank).size() == 2) {
                    if ((pairCombo == null) || (pairCombo.highCard.rank.ordinal() < highRank.ordinal())) {
                        pairCombo = new Combo(Combination.PAIR, oneOfAKinds.get(highRank).get(0), oneOfAKinds.get(highRank));
                    }
                }
            }
            if (pairCombo != null) {
                Combo smallPairCombo = null;
                HashSet<Rank> others = new HashSet<>(oneOfAKinds.keySet());
                others.remove(pairCombo.highCard.rank);
                for (Rank highRank : others) {
                    if (oneOfAKinds.get(highRank).size() == 2) {
                        if ((smallPairCombo == null) || (smallPairCombo.highCard.rank.ordinal() < highRank.ordinal())) {
                            smallPairCombo = new Combo(Combination.PAIR, oneOfAKinds.get(highRank).get(0), oneOfAKinds.get(highRank));
                        }
                    }
                }
                if (smallPairCombo != null) {
                    combo = new Combo(Combination.TWO_PAIRS, pairCombo.highCard, pairCombo.hand);
                    combo.add(smallPairCombo);
                }
            }
        }

        if ((combo == null) && (pairCombo != null)) {
            combo = pairCombo;
        }

        if (combo == null) {
            ArrayList<Card> highCardHand = new ArrayList<>();
            highCardHand.add(search.get(search.size() - 1));
            combo = new Combo(Combination.HIGH_CARD, search.get(search.size() - 1), highCardHand);
        }

        List<Card> kickers = new ArrayList<>(hand);
        combo.hand.forEach(kickers::remove);
        kickers.sort(Comparator.comparingInt(Card::getHiAceRank).reversed());
        for (Card kicker : kickers) {
            if (combo.kickers.size() + combo.hand.size() >= 5) break;
            combo.kickers.add(kicker);
        }

        return combo;
    }

    private static HashMap<Card, ArrayList<Card>> filterDraws(Collection<ArrayList<Card>> draws) {
        HashMap<Card, ArrayList<Card>> result = new HashMap<>();
        for (ArrayList<Card> draw : draws) {
            if (draw.size() >= 5) {
                if (draw.get(0).rank.equals(Rank.ACE) && draw.get(draw.size() - 1).rank.equals(Rank.KING)) {
                    result.put(draw.get(0), draw);
                } else {
                    result.put(draw.get(draw.size() - 1), draw);
                }
            }
        }
        return result;
    }

}


