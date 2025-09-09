import javax.swing.*;
import java.awt.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

public class CafeKanbanSimulator extends JFrame {
    private JPanel ordersPanel, preparingPanel, completedPanel;
    private JButton addButton, startButton, stopButton, resetButton;
    private JComboBox<String> clientBox, drinkBox;
    private Timer timer;

    // 🎨 Paletă de culori
    private final Color UNIVERSAL_BG = new Color(250, 246, 240);
    private final Color QUEUE_COLOR = new Color(244, 234, 224);
    private final Color PREP_COLOR = new Color(244, 223, 200);
    private final Color FINISHED_COLOR = UNIVERSAL_BG;

    // Timpi de preparare (în secunde)
    private static final Map<String, Integer> PREP_TIMES = new HashMap<>();
    static {
        PREP_TIMES.put("Espresso", 2);
        PREP_TIMES.put("Latte", 4);
        PREP_TIMES.put("Cappuccino", 5);
        PREP_TIMES.put("Raf", 10);
        PREP_TIMES.put("Apă", 0);
    }

    public CafeKanbanSimulator() {
        setTitle("Cafe Kanban Simulator ☕");
        setSize(950, 600);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
        getContentPane().setBackground(UNIVERSAL_BG);

        // 🔝 Top panel cu layout modern
        JPanel topPanel = new JPanel(new GridBagLayout());
        topPanel.setBackground(UNIVERSAL_BG);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 10, 5, 10);

        clientBox = new JComboBox<>(new String[]{"Daniela", "Laurențiu", "Mărioara", "Ion"});
        drinkBox = new JComboBox<>(new String[]{"Espresso", "Latte", "Cappuccino", "Raf", "Apă"});
        addButton = new JButton("➕ Adaugă în coadă");

        gbc.gridx = 0; gbc.gridy = 0;
        topPanel.add(new JLabel("👤 Client:"), gbc);

        gbc.gridx = 1;
        topPanel.add(clientBox, gbc);

        gbc.gridx = 2;
        topPanel.add(new JLabel("Băutură:"), gbc);

        gbc.gridx = 3;
        topPanel.add(drinkBox, gbc);

        gbc.gridx = 4;
        topPanel.add(addButton, gbc);

        add(topPanel, BorderLayout.NORTH);

        // 🟫 Panouri Kanban
        ordersPanel = createColumn("📥 Coada", QUEUE_COLOR);
        preparingPanel = createColumn("⚙️ În pregătire", PREP_COLOR);
        completedPanel = createColumn("✅ Finalizat", FINISHED_COLOR);

        JPanel kanbanPanel = new JPanel(new GridLayout(1, 3, 10, 10));
        kanbanPanel.setBackground(UNIVERSAL_BG);
        kanbanPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        kanbanPanel.add(new JScrollPane(ordersPanel));
        kanbanPanel.add(new JScrollPane(preparingPanel));
        kanbanPanel.add(new JScrollPane(completedPanel));
        add(kanbanPanel, BorderLayout.CENTER);

        // 🔘 Panou de control jos
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        bottomPanel.setBackground(UNIVERSAL_BG);
        startButton = new JButton("▶️ Start");
        stopButton = new JButton("⏸️ Stop");
        resetButton = new JButton("🔄 Reset");
        bottomPanel.add(startButton);
        bottomPanel.add(stopButton);
        bottomPanel.add(resetButton);
        add(bottomPanel, BorderLayout.SOUTH);

        // 🎯 Acțiuni
        addButton.addActionListener(e -> addOrder());
        startButton.addActionListener(e -> startSimulation());
        stopButton.addActionListener(e -> stopSimulation());
        resetButton.addActionListener(e -> resetSimulation());
    }

    private JPanel createColumn(String title, Color bgColor) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(bgColor);
        panel.setBorder(BorderFactory.createTitledBorder(title));
        return panel;
    }

    private void addOrder() {
        String client = (String) clientBox.getSelectedItem();
        String drink = (String) drinkBox.getSelectedItem();

        JPanel card = new JPanel(new FlowLayout(FlowLayout.LEFT));
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.GRAY),
                BorderFactory.createEmptyBorder(5, 5, 5, 5)
        ));

        card.setBackground(new Color(
                Math.min(QUEUE_COLOR.getRed() + 10, 255),
                Math.min(QUEUE_COLOR.getGreen() + 10, 255),
                Math.min(QUEUE_COLOR.getBlue() + 10, 255)
        ));

        JLabel label = new JLabel(client + " - " + drink);
        card.putClientProperty("drink", drink); // stocăm băutura pentru timp de preparare
        card.add(label);

        ordersPanel.add(card);
        ordersPanel.revalidate();
        ordersPanel.repaint();
    }

    private void startSimulation() {
        if (timer != null) {
            timer.cancel();
        }
        timer = new Timer();

        // Verificăm coada la fiecare secundă
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                SwingUtilities.invokeLater(() -> processOrders());
            }
        }, 0, 1000);
    }

    private void stopSimulation() {
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
    }

    private void resetSimulation() {
        stopSimulation();
        ordersPanel.removeAll();
        preparingPanel.removeAll();
        completedPanel.removeAll();
        ordersPanel.revalidate();
        preparingPanel.revalidate();
        completedPanel.revalidate();
        ordersPanel.repaint();
        preparingPanel.repaint();
        completedPanel.repaint();
    }

    private void processOrders() {
        if (ordersPanel.getComponentCount() > 0) {
            // scoatem primul din coadă
            JPanel orderCard = (JPanel) ordersPanel.getComponent(0);
            ordersPanel.remove(orderCard);
            preparingPanel.add(orderCard);

            String drink = (String) orderCard.getClientProperty("drink");
            int prepTime = PREP_TIMES.getOrDefault(drink, 3);

            // programăm mutarea în Completed după timpul specific băuturii
            new Timer().schedule(new TimerTask() {
                @Override
                public void run() {
                    SwingUtilities.invokeLater(() -> {
                        preparingPanel.remove(orderCard);
                        completedPanel.add(orderCard);
                        revalidatePanels();
                    });
                }
            }, prepTime * 1000L);
        }

        revalidatePanels();
    }

    private void revalidatePanels() {
        ordersPanel.revalidate();
        preparingPanel.revalidate();
        completedPanel.revalidate();
        ordersPanel.repaint();
        preparingPanel.repaint();
        completedPanel.repaint();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new CafeKanbanSimulator().setVisible(true));
    }
}
