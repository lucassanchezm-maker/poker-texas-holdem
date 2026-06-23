package com.pokergame.App;

import java.io.*;
import java.nio.file.*;
import java.util.*;


public class Database {
    private static final int MAX_ROUNDS = 200;
    private final Path baseDir;
    private final Path stateFile;
    private final Path roundsFile;
    private boolean available = true;

    public Database() {
        baseDir = Paths.get(System.getProperty("user.home"), ".poker_java");
        stateFile = baseDir.resolve("poker_state.csv");
        roundsFile = baseDir.resolve("poker_rounds.csv");
        init();
    }

    public boolean isAvailable() {
        return available;
    }

    private void init() {
        try {
            Files.createDirectories(baseDir);
            if (!Files.exists(stateFile)) {
                saveState(new GameState());
            }
            if (!Files.exists(roundsFile)) {
                Files.write(roundsFile, new byte[0]);
            }
            available = true;
        } catch (IOException e) {
            available = false;
        }
    }

    public void saveRound(RoundRecord row) {
        if (!available) return;
        try {
            List<String> lines = new ArrayList<>(Files.exists(roundsFile)
                    ? Files.readAllLines(roundsFile) : List.of());
            lines.add(row.toCsvLine());
            if (lines.size() > MAX_ROUNDS) {
                lines = lines.subList(lines.size() - MAX_ROUNDS, lines.size());
            }
            Files.write(roundsFile, lines);
        } catch (IOException e) {
            System.err.println("Error guardando ronda: " + e.getMessage());
        }
    }

    public List<RoundRecord> getRounds() {
        List<RoundRecord> rows = new ArrayList<>();
        if (!available || !Files.exists(roundsFile)) return rows;
        try {
            for (String line : Files.readAllLines(roundsFile)) {
                if (!line.isBlank()) rows.add(RoundRecord.fromCsvLine(line));
            }
        } catch (IOException ignored) { }
        return rows;
    }


    public void clearRounds() {
        try {
            Files.write(roundsFile, new byte[0]);
        } catch (IOException ignored) { }
    }

    public void saveState(GameState g) {
        if (!available) return;
        Properties p = new Properties();
        p.setProperty("playerChips", String.valueOf(g.playerChips));
        p.setProperty("cpuChips", String.valueOf(g.cpuChips));
        p.setProperty("wins", String.valueOf(g.wins));
        p.setProperty("losses", String.valueOf(g.losses));
        p.setProperty("ties", String.valueOf(g.ties));
        p.setProperty("streak", String.valueOf(g.streak));
        p.setProperty("bestStreak", String.valueOf(g.bestStreak));
        p.setProperty("profit", String.valueOf(g.profit));
        p.setProperty("round", String.valueOf(g.round));
        try (OutputStream out = Files.newOutputStream(stateFile)) {
            p.store(out, "Estado del juego Texas Hold'em");
        } catch (IOException e) {
            System.err.println("Error guardando estado: " + e.getMessage());
        }
    }

    public void loadStateInto(GameState g) {
        if (!available || !Files.exists(stateFile)) return;
        Properties p = new Properties();
        try (InputStream in = Files.newInputStream(stateFile)) {
            p.load(in);
            g.playerChips = Integer.parseInt(p.getProperty("playerChips", "1000"));
            g.cpuChips = Integer.parseInt(p.getProperty("cpuChips", "1000"));
            g.wins = Integer.parseInt(p.getProperty("wins", "0"));
            g.losses = Integer.parseInt(p.getProperty("losses", "0"));
            g.ties = Integer.parseInt(p.getProperty("ties", "0"));
            g.streak = Integer.parseInt(p.getProperty("streak", "0"));
            g.bestStreak = Integer.parseInt(p.getProperty("bestStreak", "0"));
            g.profit = Integer.parseInt(p.getProperty("profit", "0"));
            g.round = Integer.parseInt(p.getProperty("round", "0"));
        } catch (IOException | NumberFormatException e) {
            System.err.println("Error cargando estado: " + e.getMessage());
        }
    }

    public void exportCSV(Path destination) throws IOException {
        List<RoundRecord> rows = getRounds();
        StringBuilder sb = new StringBuilder("ronda,cartas,comunitarias,mano,mano_cpu,resultado,delta,fichas,fecha\n");
        for (RoundRecord r : rows) {
            sb.append(r.round).append(",")
              .append('"').append(r.playerCards).append("\",")
              .append('"').append(r.community).append("\",")
              .append('"').append(r.playerHandName).append("\",")
              .append('"').append(r.cpuHandName).append("\",")
              .append(r.resultado).append(",")
              .append(r.delta).append(",")
              .append(r.fichas).append(",")
              .append('"').append(r.timestamp).append("\"\n");
        }
        Files.writeString(destination, sb.toString());
    }
}
