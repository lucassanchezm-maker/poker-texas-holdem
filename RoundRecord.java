package com.pokergame.App;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Una fila del historial de rondas jugadas. Equivalente al objeto `row`
 * que se guardaba con DB.saveRound() en el JS original.
 */
public class RoundRecord {
    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

    public int round;
    public String playerCards;
    public String community;
    public String playerHandName;
    public String cpuHandName;
    public String resultado; // "Gano", "Perdio", "Empate"
    public int delta;
    public int fichas;
    public String timestamp;

    public RoundRecord(int round, String playerCards, String community,
                        String playerHandName, String cpuHandName,
                        String resultado, int delta, int fichas) {
        this.round = round;
        this.playerCards = playerCards;
        this.community = community;
        this.playerHandName = playerHandName;
        this.cpuHandName = cpuHandName;
        this.resultado = resultado;
        this.delta = delta;
        this.fichas = fichas;
        this.timestamp = LocalDateTime.now().format(FMT);
    }

    /** Constructor usado al leer una fila ya guardada desde el archivo CSV. */
    public RoundRecord(int round, String playerCards, String community,
                        String playerHandName, String cpuHandName,
                        String resultado, int delta, int fichas, String timestamp) {
        this(round, playerCards, community, playerHandName, cpuHandName, resultado, delta, fichas);
        this.timestamp = timestamp;
    }

    /** Convierte la fila a una línea CSV (escapando comas dentro de campos). */
    public String toCsvLine() {
        return round + "," +
                csvEscape(playerCards) + "," +
                csvEscape(community) + "," +
                csvEscape(playerHandName) + "," +
                csvEscape(cpuHandName) + "," +
                resultado + "," +
                delta + "," +
                fichas + "," +
                csvEscape(timestamp);
    }

    private static String csvEscape(String s) {
        return "\"" + (s == null ? "" : s.replace("\"", "'")) + "\"";
    }

    /** Reconstruye un RoundRecord a partir de una línea CSV previamente guardada. */
    public static RoundRecord fromCsvLine(String line) {
        List<String> f = splitCsv(line);
        return new RoundRecord(
                Integer.parseInt(f.get(0)),
                f.get(1), f.get(2), f.get(3), f.get(4),
                f.get(5), Integer.parseInt(f.get(6)), Integer.parseInt(f.get(7)),
                f.get(8)
        );
    }

    // Parser CSV simple que respeta campos entre comillas
    private static List<String> splitCsv(String line) {
        List<String> out = new ArrayList<>();
        StringBuilder cur = new StringBuilder();
        boolean inQuotes = false;
        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            if (c == '"') {
                inQuotes = !inQuotes;
            } else if (c == ',' && !inQuotes) {
                out.add(cur.toString());
                cur.setLength(0);
            } else {
                cur.append(c);
            }
        }
        out.add(cur.toString());
        return out;
    }
}
