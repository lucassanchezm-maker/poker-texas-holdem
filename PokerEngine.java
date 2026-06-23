package com.pokergame.App;

import javax.swing.Timer;
import java.util.List;

/**
 * Motor principal del juego. Traducción de las funciones newRound(), act(),
 * doRaise(), cpuTurn(), advancePhase(), showdown() y awardPot() del JS original.
 *
 * La GUI (PokerFrame) se registra como GameListener y reacciona a los eventos
 * que dispara este motor, en vez de manipular directamente el HTML como hacía
 * el JS original.
 */
public class PokerEngine {
    public final GameState g = new GameState();
    private final Database db = new Database();
    private final CpuPlayer cpu = new CpuPlayer();
    private GameListener listener;

    public void setListener(GameListener listener) {
        this.listener = listener;
    }

    public Database getDatabase() {
        return db;
    }

    /** Arranque: carga el estado guardado, igual al bloque init() del JS. */
    public void start() {
        db.loadStateInto(g);
        listener.onStateChanged();
        listener.onLog(db.isAvailable()
                ? "Datos cargados desde la base de datos local. Bienvenido de vuelta."
                : "No se pudo acceder al almacenamiento local. Se jugará sin guardar progreso.");
        listener.onRoundEnded(); // muestra "Nueva Ronda"/"Reiniciar"
    }

    /** Comienza una nueva ronda: reparte cartas y cobra las ciegas. */
    public void newRound() {
        g.round++;
        g.deck = new Deck();
        g.playerHand.clear();
        g.cpuHand.clear();
        g.community.clear();
        g.playerHand.add(g.deck.draw());
        g.playerHand.add(g.deck.draw());
        g.cpuHand.add(g.deck.draw());
        g.cpuHand.add(g.deck.draw());

        g.pot = 0; g.playerBet = 0; g.cpuBet = 0;
        g.phase = Phase.PREFLOP;

        int sb = Math.min(g.blind / 2, g.playerChips);
        int bb = Math.min(g.blind, g.cpuChips);
        g.playerChips -= sb; g.cpuChips -= bb;
        g.playerBet = sb; g.cpuBet = bb;
        g.pot = sb + bb;
        g.currentBet = bb;

        listener.onStateChanged();
        listener.onLog(String.format(
                "Ronda %d. Small blind: %d | Big blind: %d. Para igualar necesitas: %d fichas.",
                g.round, sb, bb, Math.max(0, g.currentBet - g.playerBet)));
        listener.onAwaitingPlayerAction();
    }

    /** Cuánto necesita pagar el jugador para igualar la apuesta actual. */
    public int callAmount() {
        return Math.min(g.currentBet - g.playerBet, g.playerChips);
    }

    public boolean canCheck() {
        return g.playerBet >= g.currentBet;
    }

    /** Procesa fold / check / call / allin del jugador humano. */
    public void playerAct(ActionType action) {
        switch (action) {
            case FOLD:
                listener.onLog("Tiraste las cartas. La CPU toma el bote.");
                awardPot("cpu", "Fold", "", "");
                return;
            case CHECK:
                listener.onLog("Check: pasas sin apostar más.");
                cpuTurn();
                return;
            case CALL: {
                int amt = callAmount();
                g.playerChips -= amt; g.playerBet += amt; g.pot += amt;
                listener.onLog(String.format("Call de %d fichas. Bote total: %d", amt, g.pot));
                listener.onStateChanged();
                cpuTurn();
                return;
            }
            case ALLIN: {
                int amt = g.playerChips;
                g.pot += amt; g.playerBet += amt; g.playerChips = 0;
                listener.onLog(String.format("ALL-IN! Apostaste %d fichas. Bote: %d", amt, g.pot));
                listener.onStateChanged();
                cpuTurn();
                return;
            }
            default:
                // RAISE se maneja en playerRaise()
        }
    }

    /** Monto mínimo de raise permitido, para validar/inicializar el control en la GUI. */
    public int minRaise() {
        return Math.max(g.currentBet - g.playerBet + 10, 10);
    }

    /** Procesa un raise del jugador humano por el monto indicado. */
    public void playerRaise(int amt) {
        if (amt <= 0 || amt > g.playerChips) {
            listener.onLog("Cantidad inválida.");
            return;
        }
        g.playerChips -= amt; g.playerBet += amt; g.pot += amt;
        g.currentBet = g.playerBet;
        listener.onLog(String.format("Subiste (raise) a %d fichas. Bote: %d", g.playerBet, g.pot));
        listener.onStateChanged();
        cpuTurn();
    }

    /** Turno de la CPU, con una pequeña pausa para simular que "piensa". */
    private void cpuTurn() {
        Timer timer = new Timer(800, e -> resolveCpuTurn());
        timer.setRepeats(false);
        timer.start();
    }

    private void resolveCpuTurn() {
        int callAmt = Math.max(0, g.currentBet - g.cpuBet);
        CpuDecision d = cpu.decide(g);

        switch (d.type) {
            case FOLD:
                listener.onLog("La CPU tira sus cartas. ¡Ganaste el bote!");
                awardPot("player", "CPU fold", "", "");
                break;
            case CALL: {
                int pay = Math.min(callAmt, g.cpuChips);
                g.cpuChips -= pay; g.cpuBet += pay; g.pot += pay;
                listener.onLog(String.format("CPU hace call (%d). Bote: %d", pay, g.pot));
                listener.onStateChanged();
                advancePhase();
                break;
            }
            case CHECK:
                listener.onLog("CPU hace check.");
                listener.onStateChanged();
                advancePhase();
                break;
            default: { // RAISE
                int pay = Math.min(d.amount, g.cpuChips);
                g.cpuChips -= pay; g.cpuBet += pay; g.pot += pay;
                g.currentBet = g.cpuBet;
                listener.onLog(String.format("CPU sube (raise) a %d. Bote: %d", g.cpuBet, g.pot));
                listener.onStateChanged();
                listener.onAwaitingPlayerAction(); // el jugador debe responder al raise
            }
        }
    }

    /** Avanza preflop -> flop -> turn -> river -> showdown. */
    private void advancePhase() {
        switch (g.phase) {
            case PREFLOP: g.phase = Phase.FLOP; break;
            case FLOP: g.phase = Phase.TURN; break;
            case TURN: g.phase = Phase.RIVER; break;
            case RIVER: g.phase = Phase.SHOWDOWN; break;
            default: break;
        }
        if (g.phase != Phase.SHOWDOWN) {
            g.playerBet = 0; g.cpuBet = 0; g.currentBet = 0;
        }

        if (g.phase == Phase.FLOP) {
            g.community.add(g.deck.draw());
            g.community.add(g.deck.draw());
            g.community.add(g.deck.draw());
            listener.onLog("Flop revelado. ¿Cuál es tu movimiento?");
        } else if (g.phase == Phase.TURN) {
            g.community.add(g.deck.draw());
            listener.onLog("Turn en la mesa. Tu turno.");
        } else if (g.phase == Phase.RIVER) {
            g.community.add(g.deck.draw());
            listener.onLog("River. Última carta. ¡Decide bien!");
        } else if (g.phase == Phase.SHOWDOWN) {
            showdown();
            return;
        }
        listener.onStateChanged();
        listener.onAwaitingPlayerAction();
    }

    /** Compara las manos finales y declara ganador. */
    private void showdown() {
        listener.onStateChanged();
        List<Card> pCards = concat(g.playerHand, g.community);
        List<Card> cCards = concat(g.cpuHand, g.community);
        HandResult pEv = HandEvaluator.evalHand(pCards);
        HandResult cEv = HandEvaluator.evalHand(cCards);
        int cmp = pEv.compareTo(cEv);

        String winner;
        if (cmp > 0) {
            winner = "player";
            listener.onLog(String.format("Showdown: tu %s gana contra el %s de la CPU.", pEv.name, cEv.name));
        } else if (cmp < 0) {
            winner = "cpu";
            listener.onLog(String.format("Showdown: la CPU gana con %s vs tu %s.", cEv.name, pEv.name));
        } else {
            winner = "tie";
            listener.onLog(String.format("Empate: ambos tienen %s. El bote se divide.", pEv.name));
        }
        awardPot(winner, "Showdown", pEv.name, cEv.name);
    }

    private static List<Card> concat(List<Card> a, List<Card> b) {
        List<Card> out = new java.util.ArrayList<>(a);
        out.addAll(b);
        return out;
    }

    /** Entrega el bote, actualiza estadísticas y guarda la ronda en la base de datos. */
    private void awardPot(String winner, String reason, String pHandName, String cHandName) {
        int delta = 0;

        if (winner.equals("player")) {
            g.playerChips += g.pot; delta = g.pot; g.wins++;
            g.streak = g.streak > 0 ? g.streak + 1 : 1;
            listener.onBanner("¡Ganaste! +" + g.pot + " fichas");
        } else if (winner.equals("cpu")) {
            g.cpuChips += g.pot; delta = -g.pot; g.losses++;
            g.streak = g.streak < 0 ? g.streak - 1 : -1;
            listener.onBanner("CPU gana este bote");
        } else {
            int half = g.pot / 2;
            g.playerChips += half; g.cpuChips += half; delta = 0; g.ties++;
            g.streak = 0;
            listener.onBanner("Empate - bote dividido");
        }

        if (Math.abs(g.streak) > g.bestStreak) g.bestStreak = Math.abs(g.streak);
        g.profit += delta;

        StringBuilder pCardsStr = new StringBuilder();
        for (Card c : g.playerHand) pCardsStr.append(c).append(' ');
        StringBuilder commStr = new StringBuilder();
        for (Card c : g.community) commStr.append(c).append(' ');

        RoundRecord row = new RoundRecord(
                g.round,
                pCardsStr.toString().trim(),
                commStr.toString().trim(),
                pHandName.isEmpty() ? "Fold" : pHandName,
                cHandName.isEmpty() ? (winner.equals("player") ? "CPU Fold" : "—") : cHandName,
                winner.equals("player") ? "Gano" : (winner.equals("cpu") ? "Perdio" : "Empate"),
                delta,
                g.playerChips
        );
        db.saveRound(row);
        db.saveState(g);

        g.pot = 0;
        listener.onStateChanged();
        listener.onRoundEnded();

        if (g.playerChips <= 0 || g.cpuChips <= 0) {
            listener.onLog(g.playerChips <= 0
                    ? "Te quedaste sin fichas. Juego terminado."
                    : "La CPU se quedó sin fichas. ¡Ganaste el juego completo!");
        }
    }

    /** Reinicia fichas y estadísticas a los valores iniciales. */
    public void resetGame() {
        g.resetToDefaults();
        db.saveState(g);
        listener.onStateChanged();
        listener.onLog("Juego reiniciado. Ambos jugadores vuelven a 1000 fichas.");
        listener.onRoundEnded();
    }
}
