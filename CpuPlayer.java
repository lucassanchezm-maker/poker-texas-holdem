package com.pokergame.App;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Inteligencia artificial de la CPU. Traducción directa de cpuTurn() del JS original.
 *
 * Decisión basada en:
 *  1) La fuerza de su mano actual (HandEvaluator.evalHand)
 *  2) Un número aleatorio para variar su comportamiento
 *  3) Cuánto le cuesta igualar (callAmt)
 *
 * No es una IA perfecta (no calcula probabilidades exactas de ganar, ni hace
 * bluffs complejos), pero simula un jugador con reglas de sentido común:
 * apuesta fuerte con manos buenas, es cauteloso con manos débiles, y se
 * retira si la apuesta es muy alta comparada con lo que tiene.
 */
public class CpuPlayer {
    private final Random random = new Random();

    public CpuDecision decide(GameState g) {
        int callAmt = Math.max(0, g.currentBet - g.cpuBet);
        List<Card> all = new ArrayList<>(g.cpuHand);
        all.addAll(g.community);
        HandResult ev = HandEvaluator.evalHand(all);
        double r = random.nextDouble();

        ActionType action;
        int amount = 0;

        if (ev.rank >= 2) { // Trio o mejor: juega agresivo
            if (r < 0.55) {
                action = ActionType.RAISE;
                amount = Math.min((int) Math.floor(callAmt + g.pot * 0.4), g.cpuChips);
            } else {
                action = ActionType.CALL;
            }
        } else if (ev.rank >= 0) { // Par: juega con cautela
            if (callAmt == 0) {
                action = r < 0.35 ? ActionType.RAISE : ActionType.CHECK;
                amount = Math.min(35, g.cpuChips);
            } else if (callAmt <= 120) {
                action = r < 0.65 ? ActionType.CALL : ActionType.FOLD;
            } else {
                action = r < 0.35 ? ActionType.CALL : ActionType.FOLD;
            }
        } else { // Carta alta: tiende a retirarse ante apuestas grandes
            if (callAmt == 0) {
                action = r < 0.15 ? ActionType.RAISE : ActionType.CHECK;
                amount = Math.min(20, g.cpuChips);
            } else if (callAmt <= 60) {
                action = r < 0.45 ? ActionType.CALL : ActionType.FOLD;
            } else {
                action = ActionType.FOLD;
            }
        }

        if (action == ActionType.RAISE) {
            amount = Math.max(amount, callAmt + 10);
        }
        return new CpuDecision(action, amount);
    }
}
