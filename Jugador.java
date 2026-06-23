package com.pokergame.App;

import java.util.ArrayList;

public class Jugador {

    private String nombre;
    private int fichas;
    private ArrayList<Carta> mano;

    public Jugador(String nombre, int fichas) {
        this.nombre = nombre;
        this.fichas = fichas;
        this.mano = new ArrayList<>();
    }

    public void recibirCarta(Carta carta) {
        mano.add(carta);
    }

    public void limpiarMano() {
        mano.clear();
    }

    public ArrayList<Carta> getMano() {
        return mano;
    }

    public String getNombre() {
        return nombre;
    }

    public int getFichas() {
        return fichas;
    }

    public void agregarFichas(int cantidad) {
        fichas += cantidad;
    }

    public void quitarFichas(int cantidad) {
        fichas -= cantidad;
    }
}