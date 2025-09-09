import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.*;
import java.util.Timer;

public class CafeKanbanSimulator extends JFrame {
    private JPanel ordersPanel, preparingPanel, completedPanel;
    private JButton addButton, startButton, stopButton, resetButton, menuButton;
    private JComboBox<String> clientBox;
    private Timer timer;
    private String selectedDrink = "Espresso"; // Default

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

    // Emojis pentru băuturi
    private static final Map<String, String> DRINK_EMOJIS;
    static {
        Map<String, String> map = new HashMap<>();
        map.put("Espresso", "☕");
        map.put("Latte", "🥛");
        map.put("Cappuccino", "🍵");
        map.put("Raf", "🍶");
        map.put("Apă", "💧");
        DRINK_EMOJIS = Collections.unmodifiableMap(map);
    }

    // Căile către imagini la băuturi
    private static final Map<String, String> DRINK_IMAGE_PATHS;
    static {
        Map<String, String> map = new HashMap<>();
        map.put("Espresso", "/photo/espresso.png");
        map.put("Latte", "/photo/latte.png");
        map.put("Cappuccino", "/photo/cappuccino.png");
        map.put("Raf", "/photo/raf.png");
        map.put("Apă", "/photo/water.png");
        DRINK_IMAGE_PATHS = Collections.unmodifiableMap(map);
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
        menuButton = new JButton("Meniu");
        addButton = new JButton("➕ Adaugă în coadă");

        // Stilul butonului meniu
        menuButton.setBackground(new Color(139, 69, 19));
        menuButton.setForeground(Color.WHITE);
        menuButton.setFont(new Font("Arial", Font.BOLD, 12));

        gbc.gridx = 0; gbc.gridy = 0;
        topPanel.add(new JLabel("👤 Client:"), gbc);

        gbc.gridx = 1;
        topPanel.add(clientBox, gbc);

        gbc.gridx = 2;
        topPanel.add(menuButton, gbc);

        gbc.gridx = 3;
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
        menuButton.addActionListener(e -> showMenuDialog());
        addButton.addActionListener(e -> addOrder());
        startButton.addActionListener(e -> startSimulation());
        stopButton.addActionListener(e -> stopSimulation());
        resetButton.addActionListener(e -> resetSimulation());
    }

    private void showMenuDialog() {
        JDialog menuDialog = new JDialog(this, "Meniu Băuturi", true);
        menuDialog.setSize(400, 300);
        menuDialog.setLocationRelativeTo(this);
        menuDialog.setLayout(new BorderLayout());

        JPanel menuPanel = new JPanel(new GridLayout(0, 2, 10, 10));
        menuPanel.setBackground(UNIVERSAL_BG);
        menuPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Crearea butoanelor
        for (String drink : DRINK_EMOJIS.keySet()) {
            JButton drinkButton = createDrinkButton(drink, menuDialog);
            menuPanel.add(drinkButton);
        }

        JScrollPane scrollPane = new JScrollPane(menuPanel);
        scrollPane.setBackground(UNIVERSAL_BG);
        menuDialog.add(scrollPane, BorderLayout.CENTER);

        // Crearea button închidere
        JPanel buttonPanel = new JPanel();
        buttonPanel.setBackground(UNIVERSAL_BG);
        JButton closeButton = new JButton("Închide");
        closeButton.addActionListener(e -> menuDialog.dispose());
        buttonPanel.add(closeButton);
        menuDialog.add(buttonPanel, BorderLayout.SOUTH);

        menuDialog.setVisible(true);
    }

    private JButton createDrinkButton(String drink, JDialog parentDialog) {
        JButton button = new JButton();
        button.setLayout(new BorderLayout());
        button.setBackground(Color.WHITE);
        button.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(200, 200, 200), 2),
            BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));

        // Crearea imaginlor cu fotografia reală 
        JLabel imageLabel = new JLabel();
        imageLabel.setHorizontalAlignment(JLabel.CENTER);
        
        try {
            String imagePath = DRINK_IMAGE_PATHS.get(drink);
            java.net.URL imageURL = CafeKanbanSimulator.class.getResource(imagePath);
            
            if (imageURL != null) {
                ImageIcon originalIcon = new ImageIcon(imageURL);
                
                if (originalIcon.getIconWidth() > 0) {
                    // Scalarea imaginilor (80x80 pixels)
                    Image scaledImage = originalIcon.getImage().getScaledInstance(80, 80, Image.SCALE_SMOOTH);
                    ImageIcon scaledIcon = new ImageIcon(scaledImage);
                    imageLabel.setIcon(scaledIcon);
                } else {
                    // Trecerea la emoji în caz de eroare
                    imageLabel.setText(DRINK_EMOJIS.getOrDefault(drink, "☕"));
                    imageLabel.setFont(new Font("Arial", Font.PLAIN, 48));
                }
            } else {
                // Trecerea la emoji în caz de eroare
                System.out.println("Image not found for " + drink + " at path: " + imagePath);
                imageLabel.setText(DRINK_EMOJIS.getOrDefault(drink, "☕"));
                imageLabel.setFont(new Font("Arial", Font.PLAIN, 48));
            }
        } catch (Exception e) {
            // Trecerea la emoji în caz de eroare
            System.out.println("Exception loading image for " + drink + ": " + e.getMessage());
            imageLabel.setText(DRINK_EMOJIS.getOrDefault(drink, "☕"));
            imageLabel.setFont(new Font("Arial", Font.PLAIN, 48));
        }
        
        // Crearea etichetei de timp
        JLabel textLabel = new JLabel(drink, JLabel.CENTER);
        textLabel.setFont(new Font("Arial", Font.BOLD, 14));
        
        // Adăguarea time info
        int prepTime = PREP_TIMES.getOrDefault(drink, 3);
        JLabel timeLabel = new JLabel(prepTime + " sec", JLabel.CENTER);
        timeLabel.setFont(new Font("Arial", Font.ITALIC, 12));
        timeLabel.setForeground(Color.GRAY);

        JPanel textPanel = new JPanel(new BorderLayout());
        textPanel.setOpaque(false);
        textPanel.add(textLabel, BorderLayout.CENTER);
        textPanel.add(timeLabel, BorderLayout.SOUTH);

        button.add(imageLabel, BorderLayout.CENTER);
        button.add(textPanel, BorderLayout.SOUTH);

        button.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                selectedDrink = drink;
                parentDialog.dispose();
                addOrderWithSelectedDrink();
            }
        });

        // Hover effect
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                button.setBackground(new Color(240, 240, 240));
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                button.setBackground(Color.WHITE);
            }
        });

        return button;
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
        addOrderToQueue(client, selectedDrink);
    }

    private void addOrderWithSelectedDrink() {
        String client = (String) clientBox.getSelectedItem();
        addOrderToQueue(client, selectedDrink);
    }

    private void addOrderToQueue(String client, String drink) {
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