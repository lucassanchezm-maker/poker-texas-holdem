package com.pokergame.App;

import java.util.ArrayList;
import java.util.Collections;

public class Baraja {

    private ArrayList<Carta> cartas;

    public Baraja() {
        cartas = new ArrayList<>();

        String[] palos = {"♠", "♥", "♦", "♣"};
        String[] rangos = {
                "2","3","4","5","6","7","8","9",
                "10","J","Q","K","A"
        };

        for (String palo : palos) {
            for (String rango : rangos) {
                cartas.add(new Carta(rango, palo));
            }
        }
    }

    public void mezclar() {
        Collections.shuffle(cartas);
    }

    public Carta repartir() {
        if (cartas.isEmpty()) {
            return null;
        }
        return cartas.remove(cartas.size() - 1);
    }
}