package com.pokergame.App;

import java.util.List;

/**
 * Resultado de evaluar una mano de poker.
 * Equivalente al objeto {rank, name, tb} que devolvía evalFive()/evalHand() en JS.
 */
public class HandResult {
    public final int rank;        // -1 (carta alta) .. 8 (escalera real)
    public final String name;     // nombre en español, ej. "Trio"
    public final List<Integer> tiebreakers; // valores para desempatar manos del mismo rank

    public HandResult(int rank, String name, List<Integer> tiebreakers) {
        this.rank = rank;
        this.name = name;
        this.tiebreakers = tiebreakers;
    }

    /**
     * Compara esta mano con otra. >0 si esta gana, <0 si pierde, 0 si empatan.
     * Equivalente a: pEv.rank - cEv.rank || cmpTB(pEv.tb, cEv.tb)
     */
    public int compareTo(HandResult other) {
        if (this.rank != other.rank) return this.rank - other.rank;
        int n = Math.min(this.tiebreakers.size(), other.tiebreakers.size());
        for (int i = 0; i < n; i++) {
            int diff = this.tiebreakers.get(i) - other.tiebreakers.get(i);
            if (diff != 0) return diff;
        }
        return 0;
    }
}
