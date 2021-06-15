package ru.rembo.bot.telegram.poker;

import java.util.HashSet;

public class TestRanking {

    public static void main(String[] args) {
        HashSet<Card> hand = new HashSet<>();
        hand.add(new Card(Suit.SPADES, Rank.ACE));
        hand.add(new Card(Suit.SPADES, Rank.TWO));
        hand.add(new Card(Suit.SPADES, Rank.THREE));
        hand.add(new Card(Suit.SPADES, Rank.FOUR));
        hand.add(new Card(Suit.SPADES, Rank.FIVE));
        hand.add(new Card(Suit.DIAMONDS, Rank.FOUR));
        hand.add(new Card(Suit.DIAMONDS, Rank.FIVE));
        System.out.println(HandRanking.calcCombo(hand));

        hand.clear();
        hand.add(new Card(Suit.SPADES, Rank.ACE));
        hand.add(new Card(Suit.SPADES, Rank.TEN));
        hand.add(new Card(Suit.SPADES, Rank.JACK));
        hand.add(new Card(Suit.SPADES, Rank.QUEEN));
        hand.add(new Card(Suit.SPADES, Rank.KING));
        hand.add(new Card(Suit.DIAMONDS, Rank.FOUR));
        hand.add(new Card(Suit.DIAMONDS, Rank.FIVE));
        System.out.println(HandRanking.calcCombo(hand));

        hand.clear();
        hand.add(new Card(Suit.SPADES, Rank.ACE));
        hand.add(new Card(Suit.SPADES, Rank.TEN));
        hand.add(new Card(Suit.HEARTS, Rank.JACK));
        hand.add(new Card(Suit.SPADES, Rank.QUEEN));
        hand.add(new Card(Suit.SPADES, Rank.KING));
        hand.add(new Card(Suit.DIAMONDS, Rank.FOUR));
        hand.add(new Card(Suit.DIAMONDS, Rank.FIVE));
        System.out.println(HandRanking.calcCombo(hand));

        hand.clear();
        hand.add(new Card(Suit.CLUBS, Rank.ACE));
        hand.add(new Card(Suit.SPADES, Rank.ACE));
        hand.add(new Card(Suit.HEARTS, Rank.ACE));
        hand.add(new Card(Suit.SPADES, Rank.KING));
        hand.add(new Card(Suit.HEARTS, Rank.KING));
        hand.add(new Card(Suit.DIAMONDS, Rank.SIX));
        hand.add(new Card(Suit.DIAMONDS, Rank.FIVE));
        System.out.println(HandRanking.calcCombo(hand));

        hand.clear();
        hand.add(new Card(Suit.CLUBS, Rank.ACE));
        hand.add(new Card(Suit.SPADES, Rank.ACE));
        hand.add(new Card(Suit.HEARTS, Rank.ACE));
        hand.add(new Card(Suit.SPADES, Rank.QUEEN));
        hand.add(new Card(Suit.HEARTS, Rank.KING));
        hand.add(new Card(Suit.DIAMONDS, Rank.SIX));
        hand.add(new Card(Suit.DIAMONDS, Rank.FIVE));
        System.out.println(HandRanking.calcCombo(hand));

        hand.clear();
        hand.add(new Card(Suit.CLUBS, Rank.THREE));
        hand.add(new Card(Suit.SPADES, Rank.EIGHT));
        hand.add(new Card(Suit.HEARTS, Rank.QUEEN));
        hand.add(new Card(Suit.SPADES, Rank.FOUR));
        hand.add(new Card(Suit.HEARTS, Rank.KING));
        hand.add(new Card(Suit.DIAMONDS, Rank.SIX));
        hand.add(new Card(Suit.DIAMONDS, Rank.FIVE));
        System.out.println(HandRanking.calcCombo(hand));

    }

}
