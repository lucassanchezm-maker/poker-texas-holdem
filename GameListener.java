package com.pokergame.App;

/**
 * Eventos que el motor del juego (PokerEngine) notifica a quien lo esté
 * mostrando (la ventana Swing). Desacopla la lógica del juego de la GUI,
 * igual que en el JS original las funciones de lógica llamaban a setLog(),
 * renderAll(), showBanner(), etc.
 */
public interface GameListener {
    /** Actualiza el texto del cuadro de mensajes (equivalente a setLog()). */
    void onLog(String text);

    /** Muestra un banner grande temporal (equivalente a showBanner()). */
    void onBanner(String text);

    /** El estado del juego cambió: hay que repintar fichas, cartas, pot, stats. */
    void onStateChanged();

    /** Es el turno del jugador humano: hay que mostrar los botones de acción. */
    void onAwaitingPlayerAction();

    /** La ronda terminó: hay que mostrar "Nueva Ronda" / "Reiniciar" y refrescar historial. */
    void onRoundEnded();
}
