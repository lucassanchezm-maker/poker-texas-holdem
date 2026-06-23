package com.pokergame.App;

/** Fases de una ronda, igual al arreglo PHASES del JS original. */
public enum Phase {
    IDLE, PREFLOP, FLOP, TURN, RIVER, SHOWDOWN;

    public String label() {
        switch (this) {
            case PREFLOP: return "Pre-Flop";
            case FLOP: return "Flop";
            case TURN: return "Turn";
            case RIVER: return "River";
            case SHOWDOWN: return "Showdown";
            default: return "Idle";
        }
    }
}
