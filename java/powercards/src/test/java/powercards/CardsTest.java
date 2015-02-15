package powercards;

import org.junit.Before;
import org.junit.Test;
import powercards.cards.Copper;
import powercards.cards.Duchy;
import powercards.cards.Estate;
import powercards.cards.Gold;
import powercards.cards.Province;
import powercards.cards.Silver;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotSame;

public class CardsTest {
  private Card card1;
  private Card card2;
  private Card card3;
  private Card card4;
  private Card card5;
  private Card card6;
  private RecordedInputOutput inout;

  @Before
  public void setUp() {
    card1 = new Copper();
    card2 = new Silver();
    card3 = new Gold();
    card4 = new Estate();
    card5 = new Duchy();
    card6 = new Province();
    inout = new RecordedInputOutput();
  }

  @Test
  public void cardsShouldReferenceCompare() {
    assertNotEquals(card1, card3);
    assertNotSame(card1, card3);
    assertNotEquals(card2, card4);
    assertNotSame(card2, card4);
  }

  @Test
  public void shouldMoveOne() {
    List<Card> source = new ArrayList<>(Arrays.asList(card1, card2, card3));
    List<Card> target = new ArrayList<>(Arrays.asList(card4));

    Card card = Cards.moveOne(source, target, 1);

    assertEquals(Arrays.asList(card1, card3), source);
    assertEquals(Arrays.asList(card4, card2), target);
    assertEquals(card2, card);
  }

  @Test
  public void shouldMoveOneFromPile() {
    Pile pile = new Pile(Copper::new, 10);
    List<Card> target = new ArrayList<>(Arrays.asList(card1, card2));

    Card card = Cards.moveOne(pile, target);

    assertEquals(9, pile.size());
    assertEquals(3, target.size());
    assertEquals(Arrays.asList(card1, card2, card), target);
  }

  @Test
  public void shouldMoveSome() {
    List<Card> source = new ArrayList<>(Arrays.asList(card1, card2, card3));
    List<Card> target = new ArrayList<>(Arrays.asList(card4));
    int[] sourceIndexes = new int[] { 2, 0 };

    List<Card> cards = Cards.moveMany(source, target, sourceIndexes);

    assertEquals(Arrays.asList(card2), source);
    assertEquals(Arrays.asList(card4, card3, card1), target);
    assertEquals(Arrays.asList(card3, card1), cards);
    assertArrayEquals(new int[]{ 2, 0 }, sourceIndexes);
  }

  @Test
  public void shouldMoveALot() {
    List<Card> source = new ArrayList<>(Arrays.asList(card1, card2, card3, card4));
    List<Card> target = new ArrayList<>();
    int[] sourceIndexes = new int[] { 3, 1 };

    List<Card> cards = Cards.moveMany(source, target, sourceIndexes);

    assertEquals(Arrays.asList(card1, card3), source);
    assertEquals(Arrays.asList(card4, card2), target);
    assertEquals(Arrays.asList(card4, card2), cards);
    assertArrayEquals(new int[]{ 3, 1 }, sourceIndexes);
  }

  @Test
  public void shouldMoveAll() {
    List<Card> source = new ArrayList<>(Arrays.asList(card1, card2, card3, card4));
    List<Card> target = new ArrayList<>(Arrays.asList(card5));

    Cards.moveAll(source, target);

    assertEquals(0, source.size());
    assertEquals(Arrays.asList(card5, card1, card2, card3, card4), target);
  }

  @Test
  public void shouldInverseIndexes() {
    int[] original = new int[] { 1, 2, 4 };
    int[] result = Cards.inverseIndexes(original, 6);
    assertArrayEquals(new int[]{0, 3, 5}, result);
    assertArrayEquals(new int[]{1, 2, 4}, original);
    assertNotSame(original, result);
  }

  @Test
  public void shouldDrawCards() {
    inout.queueShuffle(Collections::shuffle);

    Player player = new Player("wes", inout);
    player.getDeck().clear();
    player.getDeck().addAll(Arrays.asList(card1, card2, card3));
    player.getHand().clear();
    player.getHand().add(card4);

    List<Card> result = Cards.drawCards(player, 2, inout);

    assertEquals(Arrays.asList(card3, card2), result);
    assertEquals(Arrays.asList(card1), player.getDeck());
    assertEquals(Arrays.asList(card4, card3, card2), player.getHand());
  }

  @Test
  public void shouldDrawCardsWhenDeckIsEmpty() {
    inout.queueShuffle(Collections::shuffle);
    inout.queueShuffle(Cards::doNothing);

    Player player = new Player("wes", inout);
    player.getDeck().clear();
    player.getHand().clear();
    player.getHand().addAll(Arrays.asList(card1, card2));
    player.getDiscard().clear();
    player.getDiscard().addAll(Arrays.asList(card3));

    List<Card> result = Cards.drawCards(player, 1, inout);

    assertEquals(Arrays.asList(card3), result);
    assertEquals(0, player.getDeck().size());
    assertEquals(Arrays.asList(card1, card2, card3), player.getHand());
    assertEquals(0, player.getDiscard().size());
  }

  @Test
  public void shouldDrawCardsFullDeck() {
    inout.queueShuffle(Collections::shuffle);

    Player player = new Player("wes", inout);
    player.getDeck().clear();
    player.getDeck().addAll(Arrays.asList(card1, card2, card3, card4));
    player.getHand().clear();
    player.getDiscard().clear();
    player.getDiscard().addAll(Arrays.asList(card5, card6));

    List<Card> result = Cards.drawCards(player, 4, inout);

    assertEquals(Arrays.asList(card4, card3, card2, card1), result);
    assertEquals(0, player.getDeck().size());
    assertEquals(Arrays.asList(card4, card3, card2, card1), player.getHand());
    assertEquals(Arrays.asList(card5, card6), player.getDiscard());
  }

  @Test
  public void shouldDrawCardsAndRecycleDiscard() {
    inout.queueShuffle(Collections::shuffle);
    inout.queueShuffle(Cards::doNothing);

    Player player = new Player("wes", inout);
    player.getDeck().clear();
    player.getDeck().addAll(Arrays.asList(card1, card2, card3));
    player.getHand().clear();
    player.getHand().add(card4);
    player.getDiscard().clear();
    player.getDiscard().addAll(Arrays.asList(card5, card6));

    List<Card> result = Cards.drawCards(player, 4, inout);

    assertEquals(Arrays.asList(card3, card2, card1, card6), result);
    assertEquals(Arrays.asList(card5), player.getDeck());
    assertEquals(Arrays.asList(card4, card3, card2, card1, card6), player.getHand());
    assertEquals(0, player.getDiscard().size());
  }

  @Test
  public void shouldDrawAllCardsAndRecycleDiscard() {
    inout.queueShuffle(Collections::shuffle);
    inout.queueShuffle(Cards::doNothing);

    Player player = new Player("wes", inout);
    player.getDeck().clear();
    player.getDeck().addAll(Arrays.asList(card1, card2, card3));
    player.getHand().clear();
    player.getHand().add(card4);
    player.getDiscard().clear();
    player.getDiscard().addAll(Arrays.asList(card5, card6));

    List<Card> result = Cards.drawCards(player, 5, inout);

    assertEquals(Arrays.asList(card3, card2, card1, card6, card5), result);
    assertEquals(Arrays.asList(card4, card3, card2, card1, card6, card5), player.getHand());
    assertEquals(0, player.getDeck().size());
    assertEquals(0, player.getDiscard().size());
  }

  @Test
  public void shouldDrawCardsButStopIfNoMoreCardsToDraw() {
    inout.queueShuffle(Collections::shuffle);
    inout.queueShuffle(Cards::doNothing);

    Player player = new Player("wes", inout);
    player.getDeck().clear();
    player.getDeck().addAll(Arrays.asList(card1, card2));
    player.getHand().clear();
    player.getHand().addAll(Arrays.asList(card3, card4));
    player.getDiscard().clear();
    player.getDiscard().addAll(Arrays.asList(card5, card6));

    List<Card> result = Cards.drawCards(player, 5, inout);

    assertEquals(Arrays.asList(card2, card1, card6, card5), result);
    assertEquals(Arrays.asList(card3, card4, card2, card1, card6, card5), player.getHand());
    assertEquals(0, player.getDeck().size());
    assertEquals(0, player.getDiscard().size());
  }
}
