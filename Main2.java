package com.pokergame.App;

public class Main2 {

    public static void main(String[] args) {

        Baraja baraja = new Baraja();
        baraja.mezclar();

        Jugador jugador = new Jugador("Jugador", 1000);
        Jugador cpu = new Jugador("CPU", 1000);

        jugador.recibirCarta(baraja.repartir());
        jugador.recibirCarta(baraja.repartir());

        cpu.recibirCarta(baraja.repartir());
        cpu.recibirCarta(baraja.repartir());

        System.out.println("Tus cartas:");
        for (Carta c : jugador.getMano()) {
            System.out.println(c);
        }
    }
}