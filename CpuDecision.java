package com.pokergame.App;

/** Decisión tomada por la CPU en su turno: qué acción y, si aplica, cuánto apuesta. */
public class CpuDecision {
    public final ActionType type;
    public final int amount;

    public CpuDecision(ActionType type, int amount) {
        this.type = type;
        this.amount = amount;
    }
}
