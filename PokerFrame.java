package com.pokergame.App;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

/**
 * Ventana principal. Traduce a Swing toda la parte visual que en el JS
 * original se manejaba con HTML/CSS (renderAll(), showActions(), loadHistory(), etc.).
 * Esta clase es la GameListener: reacciona a los eventos que dispara PokerEngine.
 */
public class PokerFrame extends JFrame implements GameListener {
    private final PokerEngine engine = new PokerEngine();

    // Paleta de colores inspirada en el HTML original
    private static final Color BG = new Color(0x0a0a0a);
    private static final Color PANEL_BG = new Color(0x111111);
    private static final Color FELT = new Color(0x1a5c2e);
    private static final Color GOLD = new Color(0xd4af37);
    private static final Color CREAM = new Color(0xe8dcc8);

    // ---- Cabecera ----
    private JLabel dbLabel;

    // ---- Barra de estadísticas ----
    private JLabel sRound, sWins, sLoss, sBest, sProfit;

    // ---- Mesa ----
    private JLabel cpuChipBadge, cpuBetBadge;
    private JLabel playerChipBadge, playerBetBadge;
    private JLabel potAmountLabel;
    private CardRow cpuCardsRow, communityCardsRow, playerCardsRow;
    private JPanel phaseStrip;
    private JLabel[] phaseSteps;

    // ---- Log y banner ----
    private JLabel logBox;
    private JLabel bannerLabel;
    private JPanel bannerOverlay;
    private Timer bannerTimer;

    // ---- Panel de acciones ----
    private JPanel actionPanel;
    private JPanel raisePanel;
    private JSpinner raiseSpinner;

    // ---- Historial ----
    private DefaultTableModel historyModel;
    private JTable historyTable;

    public PokerFrame() {
        super("Texas Hold'em - Poker contra CPU");
        engine.setListener(this);
        buildUI();
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(640, 860));
        pack();
        setLocationRelativeTo(null);
        engine.start();
        loadHistory();
    }

    // ================================================================
    //  CONSTRUCCIÓN DE LA INTERFAZ
    // ================================================================
    private void buildUI() {
        getContentPane().setBackground(BG);
        setLayout(new BorderLayout(0, 10));

        JPanel root = new JPanel();
        root.setLayout(new BoxLayout(root, BoxLayout.Y_AXIS));
        root.setBackground(PANEL_BG);
        root.setBorder(new EmptyBorder(12, 14, 12, 14));

        root.add(buildHeader());
        root.add(Box.createVerticalStrut(8));
        root.add(buildStatsBar());
        root.add(Box.createVerticalStrut(8));
        root.add(buildTablePanel());
        root.add(Box.createVerticalStrut(8));
        root.add(buildActionArea());
        root.add(Box.createVerticalStrut(8));
        root.add(buildHistoryPanel());

        JScrollPane scroll = new JScrollPane(root);
        scroll.setBorder(null);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        add(scroll, BorderLayout.CENTER);
    }

    private JPanel buildHeader() {
        JPanel p = new JPanel(new BorderLayout());
        p.setOpaque(false);
        JLabel title = new JLabel("Texas Hold'em");
        title.setFont(new Font("Serif", Font.BOLD, 20));
        title.setForeground(GOLD);
        dbLabel = new JLabel("conectando...");
        dbLabel.setForeground(Color.GRAY);
        dbLabel.setFont(new Font("SansSerif", Font.PLAIN, 11));
        p.add(title, BorderLayout.WEST);
        p.add(dbLabel, BorderLayout.EAST);
        return p;
    }

    private JPanel buildStatsBar() {
        JPanel p = new JPanel(new GridLayout(1, 5, 6, 0));
        p.setOpaque(false);
        sRound = statValue("0");
        sWins = statValue("0");
        sLoss = statValue("0");
        sBest = statValue("0");
        sProfit = statValue("+0");
        p.add(statBox("Ronda", sRound));
        p.add(statBox("Ganadas", sWins));
        p.add(statBox("Perdidas", sLoss));
        p.add(statBox("Mejor Racha", sBest));
        p.add(statBox("Ganancia", sProfit));
        return p;
    }

    private JLabel statValue(String initial) {
        JLabel l = new JLabel(initial, SwingConstants.CENTER);
        l.setFont(new Font("Serif", Font.BOLD, 16));
        l.setForeground(GOLD);
        return l;
    }

    private JPanel statBox(String label, JLabel valueLabel) {
        JPanel box = new JPanel();
        box.setLayout(new BoxLayout(box, BoxLayout.Y_AXIS));
        box.setBackground(new Color(0x0d0d0d));
        box.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(0x4a4020)),
                new EmptyBorder(5, 4, 5, 4)));
        valueLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        JLabel l = new JLabel(label, SwingConstants.CENTER);
        l.setAlignmentX(Component.CENTER_ALIGNMENT);
        l.setFont(new Font("SansSerif", Font.PLAIN, 10));
        l.setForeground(new Color(180, 180, 180));
        box.add(valueLabel);
        box.add(l);
        return box;
    }

    private JPanel buildTablePanel() {
        JPanel felt = new JPanel();
        felt.setLayout(new BoxLayout(felt, BoxLayout.Y_AXIS));
        felt.setBackground(FELT);
        felt.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(0x7a5215), 6),
                new EmptyBorder(14, 14, 14, 14)));

        // -- Fila CPU --
        JPanel cpuSeat = new JPanel(new BorderLayout());
        cpuSeat.setOpaque(false);
        JLabel cpuName = seatName("CPU");
        cpuChipBadge = chipBadge();
        cpuBetBadge = betBadge();
        JPanel cpuRight = new JPanel();
        cpuRight.setOpaque(false);
        cpuRight.add(cpuChipBadge);
        cpuRight.add(cpuBetBadge);
        cpuSeat.add(cpuName, BorderLayout.WEST);
        cpuSeat.add(cpuRight, BorderLayout.EAST);

        cpuCardsRow = new CardRow(2);
        felt.add(cpuSeat);
        felt.add(centered(cpuCardsRow));
        felt.add(Box.createVerticalStrut(10));

        // -- Comunitarias + Pot --
        communityCardsRow = new CardRow(5);
        felt.add(centered(communityCardsRow));
        felt.add(Box.createVerticalStrut(6));

        JPanel potPanel = new JPanel();
        potPanel.setOpaque(false);
        potPanel.setLayout(new BoxLayout(potPanel, BoxLayout.Y_AXIS));
        JLabel potLabel = new JLabel("BOTE", SwingConstants.CENTER);
        potLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        potLabel.setForeground(new Color(255, 255, 255, 160));
        potLabel.setFont(new Font("SansSerif", Font.PLAIN, 11));
        potAmountLabel = new JLabel("0", SwingConstants.CENTER);
        potAmountLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        potAmountLabel.setFont(new Font("Serif", Font.ITALIC | Font.BOLD, 18));
        potAmountLabel.setForeground(GOLD);
        potPanel.add(potLabel);
        potPanel.add(potAmountLabel);
        felt.add(potPanel);
        felt.add(Box.createVerticalStrut(10));

        // -- Fila Jugador --
        playerCardsRow = new CardRow(2);
        felt.add(centered(playerCardsRow));
        felt.add(Box.createVerticalStrut(6));

        JPanel playerSeat = new JPanel(new BorderLayout());
        playerSeat.setOpaque(false);
        JLabel playerName = seatName("TÚ");
        playerChipBadge = chipBadge();
        playerBetBadge = betBadge();
        JPanel playerRight = new JPanel();
        playerRight.setOpaque(false);
        playerRight.add(playerChipBadge);
        playerRight.add(playerBetBadge);
        playerSeat.add(playerName, BorderLayout.WEST);
        playerSeat.add(playerRight, BorderLayout.EAST);
        felt.add(playerSeat);

        felt.add(Box.createVerticalStrut(10));
        felt.add(buildPhaseStrip());

        // Overlay del banner de ganador, encima de la mesa
        JLayeredPane layered = new JLayeredPane();
        layered.setLayout(new OverlayLayout(layered));
        bannerOverlay = new JPanel(new GridBagLayout());
        bannerOverlay.setOpaque(false);
        bannerOverlay.setVisible(false);
        bannerLabel = new JLabel("");
        bannerLabel.setFont(new Font("Serif", Font.BOLD, 22));
        bannerLabel.setForeground(GOLD);
        bannerLabel.setOpaque(true);
        bannerLabel.setBackground(new Color(0, 0, 0, 200));
        bannerLabel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(GOLD), new EmptyBorder(10, 20, 10, 20)));
        bannerOverlay.add(bannerLabel);
        layered.add(felt);
        layered.add(bannerOverlay);

        JPanel wrap = new JPanel(new BorderLayout());
        wrap.setOpaque(false);
        wrap.add(layered, BorderLayout.CENTER);
        return wrap;
    }

    private JLabel seatName(String text) {
        JLabel l = new JLabel(text);
        l.setForeground(GOLD);
        l.setFont(new Font("SansSerif", Font.BOLD, 12));
        return l;
    }

    private JLabel chipBadge() {
        JLabel l = new JLabel("1000");
        l.setForeground(GOLD);
        l.setFont(new Font("SansSerif", Font.BOLD, 12));
        l.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(212, 175, 55, 90)),
                new EmptyBorder(2, 8, 2, 8)));
        l.setOpaque(true);
        l.setBackground(new Color(0, 0, 0, 130));
        return l;
    }

    private JLabel betBadge() {
        JLabel l = new JLabel("");
        l.setForeground(GOLD);
        l.setFont(new Font("SansSerif", Font.PLAIN, 11));
        l.setVisible(false);
        return l;
    }

    private JComponent centered(JComponent c) {
        JPanel p = new JPanel();
        p.setOpaque(false);
        p.add(c);
        return p;
    }

    private JPanel buildPhaseStrip() {
        phaseStrip = new JPanel(new GridLayout(1, 5, 4, 0));
        phaseStrip.setOpaque(false);
        String[] labels = {"Pre-Flop", "Flop", "Turn", "River", "Showdown"};
        phaseSteps = new JLabel[5];
        for (int i = 0; i < 5; i++) {
            JLabel l = new JLabel(labels[i], SwingConstants.CENTER);
            l.setFont(new Font("SansSerif", Font.PLAIN, 9));
            l.setForeground(new Color(255, 255, 255, 90));
            l.setBorder(BorderFactory.createLineBorder(new Color(255, 255, 255, 40)));
            phaseSteps[i] = l;
            phaseStrip.add(l);
        }
        return phaseStrip;
    }

    private JPanel buildActionArea() {
        JPanel container = new JPanel();
        container.setLayout(new BoxLayout(container, BoxLayout.Y_AXIS));
        container.setBackground(new Color(0x0d0d0d));
        container.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(255, 255, 255, 20)),
                new EmptyBorder(10, 10, 10, 10)));

        logBox = new JLabel("Cargando...");
        logBox.setForeground(CREAM);
        logBox.setFont(new Font("SansSerif", Font.ITALIC, 12));
        logBox.setBorder(new EmptyBorder(4, 4, 8, 4));
        container.add(logBox);

        actionPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 6, 4));
        actionPanel.setOpaque(false);
        container.add(actionPanel);

        raisePanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 6, 4));
        raisePanel.setOpaque(false);
        raisePanel.setVisible(false);
        raiseSpinner = new JSpinner(new SpinnerNumberModel(50, 10, 1000, 10));
        JButton confirmRaise = new JButton("Confirmar Raise");
        styleButton(confirmRaise, new Color(0x3d2f00), new Color(0xffd060));
        confirmRaise.addActionListener(e -> {
            int amt = (Integer) raiseSpinner.getValue();
            raisePanel.setVisible(false);
            engine.playerRaise(amt);
        });
        JLabel montoLabel = new JLabel("Monto:");
        montoLabel.setForeground(CREAM);
        raisePanel.add(montoLabel);
        raisePanel.add(raiseSpinner);
        raisePanel.add(confirmRaise);
        container.add(raisePanel);

        return container;
    }

    private JPanel buildHistoryPanel() {
        JPanel p = new JPanel(new BorderLayout(0, 6));
        p.setBackground(new Color(0x0d0d0d));
        p.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(255, 255, 255, 20)),
                new EmptyBorder(10, 10, 10, 10)));

        JPanel headerRow = new JPanel(new BorderLayout());
        headerRow.setOpaque(false);
        JLabel title = new JLabel("HISTORIAL DE PARTIDAS");
        title.setForeground(new Color(212, 175, 55, 180));
        title.setFont(new Font("SansSerif", Font.BOLD, 11));
        JPanel histButtons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 4, 0));
        histButtons.setOpaque(false);
        JButton exportBtn = new JButton("Exportar CSV");
        JButton clearBtn = new JButton("Borrar Historial");
        styleButton(exportBtn, new Color(0x0f2a50), new Color(0x7db8ff));
        styleButton(clearBtn, new Color(0x1a1a1a), new Color(0x999999));
        exportBtn.addActionListener(e -> exportCsv());
        clearBtn.addActionListener(e -> clearHistory());
        histButtons.add(exportBtn);
        histButtons.add(clearBtn);
        headerRow.add(title, BorderLayout.WEST);
        headerRow.add(histButtons, BorderLayout.EAST);
        p.add(headerRow, BorderLayout.NORTH);

        String[] cols = {"Ronda", "Tus Cartas", "Tu Mano", "Mano CPU", "Comunitarias", "Resultado", "Δ Fichas", "Fichas"};
        historyModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        historyTable = new JTable(historyModel);
        historyTable.setBackground(new Color(0x0d0d0d));
        historyTable.setForeground(new Color(0xc8bfb0));
        historyTable.setGridColor(new Color(255, 255, 255, 20));
        historyTable.getTableHeader().setForeground(GOLD);
        historyTable.getTableHeader().setBackground(new Color(0x0d0d0d));
        JScrollPane sp = new JScrollPane(historyTable);
        sp.setPreferredSize(new Dimension(600, 200));
        p.add(sp, BorderLayout.CENTER);
        return p;
    }

    private void styleButton(JButton b, Color bg, Color fg) {
        b.setBackground(bg);
        b.setForeground(fg);
        b.setFocusPainted(false);
        b.setFont(new Font("SansSerif", Font.BOLD, 11));
        b.setBorder(new EmptyBorder(6, 12, 6, 12));
    }

    // ================================================================
    //  IMPLEMENTACIÓN DE GameListener
    // ================================================================
    @Override
    public void onLog(String text) {
        logBox.setText("<html><div style='width:520px'>" + escapeHtml(text) + "</div></html>");
    }

    @Override
    public void onBanner(String text) {
        bannerLabel.setText(text);
        bannerOverlay.setVisible(true);
        if (bannerTimer != null) bannerTimer.stop();
        bannerTimer = new Timer(2200, e -> bannerOverlay.setVisible(false));
        bannerTimer.setRepeats(false);
        bannerTimer.start();
    }

    @Override
    public void onStateChanged() {
        GameState g = engine.g;
        dbLabel.setText(engine.getDatabase().isAvailable()
                ? "DB conectada (archivo local)" : "Sin almacenamiento disponible");
        dbLabel.setForeground(engine.getDatabase().isAvailable() ? new Color(0x4dcc7a) : new Color(0xe05555));

        sRound.setText(String.valueOf(g.round));
        sWins.setText(String.valueOf(g.wins));
        sLoss.setText(String.valueOf(g.losses));
        sBest.setText(String.valueOf(g.bestStreak));
        sProfit.setText((g.profit >= 0 ? "+" : "") + g.profit);
        sProfit.setForeground(g.profit >= 0 ? new Color(0x4dcc7a) : new Color(0xe05555));

        playerChipBadge.setText(String.valueOf(g.playerChips));
        cpuChipBadge.setText(String.valueOf(g.cpuChips));
        potAmountLabel.setText(String.valueOf(g.pot));

        if (g.playerBet > 0) {
            playerBetBadge.setText("Apuesta: " + g.playerBet);
            playerBetBadge.setVisible(true);
        } else playerBetBadge.setVisible(false);

        if (g.cpuBet > 0) {
            cpuBetBadge.setText("Apuesta: " + g.cpuBet);
            cpuBetBadge.setVisible(true);
        } else cpuBetBadge.setVisible(false);

        playerCardsRow.show(g.playerHand, false);
        communityCardsRow.show(g.community, false);
        cpuCardsRow.show(g.cpuHand, g.phase != Phase.SHOWDOWN);

        updatePhaseStrip(g.phase);
    }

    private void updatePhaseStrip(Phase phase) {
        int cur;
        switch (phase) {
            case PREFLOP: cur = 0; break;
            case FLOP: cur = 1; break;
            case TURN: cur = 2; break;
            case RIVER: cur = 3; break;
            case SHOWDOWN: cur = 4; break;
            default: cur = -1;
        }
        for (int i = 0; i < 5; i++) {
            JLabel l = phaseSteps[i];
            if (i < cur) {
                l.setForeground(new Color(212, 175, 55, 150));
                l.setBorder(BorderFactory.createLineBorder(new Color(212, 175, 55, 80)));
            } else if (i == cur) {
                l.setForeground(GOLD);
                l.setBorder(BorderFactory.createLineBorder(GOLD));
            } else {
                l.setForeground(new Color(255, 255, 255, 90));
                l.setBorder(BorderFactory.createLineBorder(new Color(255, 255, 255, 40)));
            }
        }
    }

    @Override
    public void onAwaitingPlayerAction() {
        raisePanel.setVisible(false);
        actionPanel.removeAll();

        JButton fold = new JButton("Fold");
        styleButton(fold, new Color(0x6b1515), new Color(0xffb5b5));
        fold.addActionListener(e -> engine.playerAct(ActionType.FOLD));
        actionPanel.add(fold);

        if (engine.canCheck()) {
            JButton check = new JButton("Check");
            styleButton(check, new Color(0x0f3d1f), new Color(0x7dffaa));
            check.addActionListener(e -> engine.playerAct(ActionType.CHECK));
            actionPanel.add(check);
        } else {
            int callAmt = engine.callAmount();
            JButton call = new JButton("Call " + callAmt);
            styleButton(call, new Color(0x0f2a50), new Color(0x7db8ff));
            call.addActionListener(e -> engine.playerAct(ActionType.CALL));
            actionPanel.add(call);
        }

        JButton raise = new JButton("Raise");
        styleButton(raise, new Color(0x3d2f00), new Color(0xffd060));
        raise.addActionListener(e -> openRaise());
        actionPanel.add(raise);

        if (engine.g.playerChips > 0) {
            JButton allin = new JButton("All-In (" + engine.g.playerChips + ")");
            styleButton(allin, new Color(0x4a0a4a), new Color(0xffaaff));
            allin.addActionListener(e -> engine.playerAct(ActionType.ALLIN));
            actionPanel.add(allin);
        }

        actionPanel.revalidate();
        actionPanel.repaint();
    }

    private void openRaise() {
        int min = engine.minRaise();
        int max = Math.max(min, engine.g.playerChips);
        int def = Math.min(min + 40, engine.g.playerChips);
        raiseSpinner.setModel(new SpinnerNumberModel(def, min, max, 10));
        raisePanel.setVisible(true);
    }

    @Override
    public void onRoundEnded() {
        raisePanel.setVisible(false);
        actionPanel.removeAll();

        JButton newRound = new JButton("Nueva Ronda");
        styleButton(newRound, new Color(0xc9a84c), new Color(0x1a0e00));
        newRound.addActionListener(e -> engine.newRound());
        actionPanel.add(newRound);

        JButton reset = new JButton("Reiniciar");
        styleButton(reset, new Color(0x1a1a1a), new Color(0x999999));
        reset.addActionListener(e -> engine.resetGame());
        actionPanel.add(reset);

        actionPanel.revalidate();
        actionPanel.repaint();
        loadHistory();
    }

    // ================================================================
    //  HISTORIAL
    // ================================================================
    private void loadHistory() {
        historyModel.setRowCount(0);
        List<RoundRecord> rows = engine.getDatabase().getRounds();
        for (int i = rows.size() - 1; i >= Math.max(0, rows.size() - 40); i--) {
            RoundRecord r = rows.get(i);
            String delta = r.delta > 0 ? "+" + r.delta : String.valueOf(r.delta);
            historyModel.addRow(new Object[]{
                    r.round, r.playerCards, r.playerHandName, r.cpuHandName,
                    r.community, r.resultado, delta, r.fichas
            });
        }
    }

    private void clearHistory() {
        int confirm = JOptionPane.showConfirmDialog(this,
                "Esto borrará todo el historial guardado. ¿Continuar?",
                "Confirmar", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            engine.getDatabase().clearRounds();
            loadHistory();
            onLog("Historial borrado de la base de datos.");
        }
    }

    private void exportCsv() {
        if (engine.getDatabase().getRounds().isEmpty()) {
            JOptionPane.showMessageDialog(this, "No hay datos para exportar.");
            return;
        }
        JFileChooser chooser = new JFileChooser();
        chooser.setSelectedFile(new java.io.File("poker_historial.csv"));
        if (chooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            try {
                Path dest = chooser.getSelectedFile().toPath();
                engine.getDatabase().exportCSV(dest);
                JOptionPane.showMessageDialog(this, "Historial exportado a: " + dest);
            } catch (IOException e) {
                JOptionPane.showMessageDialog(this, "Error exportando: " + e.getMessage());
            }
        }
    }

    private static String escapeHtml(String s) {
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }

    // ================================================================
    //  Fila de cartas reutilizable (jugador / CPU / comunitarias)
    // ================================================================
    private static class CardRow extends JPanel {
        private final CardView[] slots;

        CardRow(int totalSlots) {
            super(new FlowLayout(FlowLayout.CENTER, 6, 4));
            setOpaque(false);
            slots = new CardView[totalSlots];
            for (int i = 0; i < totalSlots; i++) {
                slots[i] = new CardView();
                add(slots[i]);
            }
        }

        void show(List<Card> cards, boolean hidden) {
            for (int i = 0; i < slots.length; i++) {
                if (i < cards.size()) {
                    slots[i].setCard(cards.get(i), hidden);
                } else {
                    slots[i].setCard(null, false);
                }
            }
        }
    }
}
