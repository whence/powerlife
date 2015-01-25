package powercards;

import powercards.cards.Copper;
import powercards.cards.Estate;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class Player {
  private final String name;
  private final List<Card> deck;
  private final List<Card> hand;
  private final List<Card> played;
  private final List<Card> discard;
  private int actions;
  private int buys;
  private int coins;

  public Player(String name) {
    this.name = name;

    List<Card> fullDeck = Stream.concat(
        IntStream.range(0, 3).mapToObj(n -> new Estate()),
        IntStream.range(0, 7).mapToObj(n -> new Copper()))
        .collect(Collectors.toList());
    Collections.shuffle(fullDeck);
    deck = new ArrayList<>(fullDeck.subList(0, 5));
    hand = new ArrayList<>(fullDeck.subList(5, 10));
    played = new ArrayList<>();
    discard = new ArrayList<>();
    actions = 0;
    buys = 0;
    coins = 0;
  }

  public String getName() {
    return name;
  }

  public int getActions() {
    return actions;
  }

  public void setActions(int actions) {
    this.actions = actions;
  }

  public void addActions(int actions) {
    this.actions += actions;
  }

  public int getBuys() {
    return buys;
  }

  public void setBuys(int buys) {
    this.buys = buys;
  }

  public void addBuys(int buys) {
    this.buys += buys;
  }

  public int getCoins() {
    return coins;
  }

  public void setCoins(int coins) {
    this.coins = coins;
  }

  public void addCoins(int coins) {
    this.coins += coins;
  }

  public List<Card> getDeck() {
    return deck;
  }

  public List<Card> getHand() {
    return hand;
  }

  public List<Card> getPlayed() {
    return played;
  }

  public List<Card> getDiscard() {
    return discard;
  }

  @Override
  public String toString() {
    return "Player{" +
        "name='" + name + '\'' +
        '}';
  }
}
