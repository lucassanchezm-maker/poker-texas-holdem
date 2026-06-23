package com.pokergame.App;

/**
 * Representa una carta de la baraja: un rango (2..A) y un palo (♠ ♥ ♦ ♣).
 * Equivalente al objeto {r, s} usado en el JavaScript original.
 */
public class Card {
    public final String rank; // "2".."10","J","Q","K","A"
    public final String suit; // "♠","♥","♦","♣"

    public Card(String rank, String suit) {
        this.rank = rank;
        this.suit = suit;
    }

    /** Valor numérico del rango: 2..14 (A=14), igual al mapa RV del JS. */
    public int value() {
        switch (rank) {
            case "J": return 11;
            case "Q": return 12;
            case "K": return 13;
            case "A": return 14;
            default:  return Integer.parseInt(rank);
        }
    }

    /** true si el palo es rojo (♥ o ♦) — usado solo para pintar en la GUI. */
    public boolean isRed() {
        return suit.equals("♥") || suit.equals("♦");
    }

    /** Representación corta tipo "A♠" usada para guardar en el historial. */
    @Override
    public String toString() {
        return rank + suit;
    }
}
