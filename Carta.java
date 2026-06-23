package com.pokergame.App;

public class Carta {

    private String rango;
    private String palo;

    public Carta(String rango, String palo) {
        this.rango = rango;
        this.palo = palo;
    }

    public String getRango() {
        return rango;
    }

    public String getPalo() {
        return palo;
    }

    @Override
    public String toString() {
        return rango + palo;
    }
}