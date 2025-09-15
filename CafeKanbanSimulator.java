import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.Timer;
import java.util.TimerTask;

public class CafeKanbanSimulator extends JFrame {
    private JPanel ordersPanel, preparingPanel, completedPanel;
    private JButton startButton, stopButton, resetButton, menuButton;
    private JComboBox<String> clientBox;
    private Timer mainTimer;
    private Timer intervalTimer;
    private String selectedDrink = "Espresso"; // Default
    private String baseTitle = "Cafe Kanban Simulator ☕";
    private boolean isWorking = true;

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
        PREP_TIMES.put("Frappuccino", 2);
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
        map.put("Frappuccino", "☕");
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
        map.put("Frappuccino", "/photo/frappuccino.png");
        DRINK_IMAGE_PATHS = Collections.unmodifiableMap(map);
    }

    public CafeKanbanSimulator() {
        setTitle(baseTitle);
        setSize(950, 600);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
        getContentPane().setBackground(UNIVERSAL_BG);

        // Top panel 
        JPanel topPanel = new JPanel(new GridBagLayout());
        topPanel.setBackground(UNIVERSAL_BG);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 10, 5, 10);

        clientBox = new JComboBox<>(new String[]{"Daniela", "Laurențiu", "Mărioara", "Ion"});
        menuButton = new JButton("Meniu");

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

        add(topPanel, BorderLayout.NORTH);

        // Panouri Kanban
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

        // Panou de control jos
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        bottomPanel.setBackground(UNIVERSAL_BG);
        startButton = new JButton("▶️ Start");
        stopButton = new JButton("⏸️ Stop");
        resetButton = new JButton("🔄 Reset");
        bottomPanel.add(startButton);
        bottomPanel.add(stopButton);
        bottomPanel.add(resetButton);
        add(bottomPanel, BorderLayout.SOUTH);

        // Acțiuni
        menuButton.addActionListener(e -> showMenuDialog());
        startButton.addActionListener(e -> showSimulationModeDialog());
        stopButton.addActionListener(e -> stopSimulation());
        resetButton.addActionListener(e -> resetSimulation());
    }

    private void showSimulationModeDialog() {
        JDialog modeDialog = new JDialog(this, "Mod Simulare", true);
        modeDialog.setSize(400, 300);
        modeDialog.setLocationRelativeTo(this);
        modeDialog.setLayout(new BorderLayout());
        modeDialog.getContentPane().setBackground(UNIVERSAL_BG);

        JPanel modePanel = new JPanel(new GridLayout(3, 1, 10, 10));
        modePanel.setBackground(UNIVERSAL_BG);
        modePanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Normal mode button
        JButton normalButton = new JButton("Normal (procesare continuă)");
        normalButton.setFont(new Font("Arial", Font.BOLD, 14));
        normalButton.addActionListener(e -> {
            modeDialog.dispose();
            startNormalSimulation();
        });

        // Interval mode button
        JButton intervalButton = new JButton("Cu intervale (la fiecare 2 secunde)");
        intervalButton.setFont(new Font("Arial", Font.BOLD, 14));
        intervalButton.addActionListener(e -> {
            modeDialog.dispose();
            startIntervalSimulation();
        });

        // Scheduled time mode button
        JButton timeButton = new JButton("La o anumită oră");
        timeButton.setFont(new Font("Arial", Font.BOLD, 14));
        timeButton.addActionListener(e -> {
            modeDialog.dispose();
            showTimeSelectionDialog();
        });

        modePanel.add(normalButton);
        modePanel.add(intervalButton);
        modePanel.add(timeButton);

        JPanel buttonPanel = new JPanel();
        buttonPanel.setBackground(UNIVERSAL_BG);
        JButton cancelButton = new JButton("Anulează");
        cancelButton.addActionListener(e -> modeDialog.dispose());
        buttonPanel.add(cancelButton);

        modeDialog.add(modePanel, BorderLayout.CENTER);
        modeDialog.add(buttonPanel, BorderLayout.SOUTH);
        modeDialog.setVisible(true);
    }

    private void showTimeSelectionDialog() {
        JDialog timeDialog = new JDialog(this, "Selectează Ora", true);
        timeDialog.setSize(300, 200);
        timeDialog.setLocationRelativeTo(this);
        timeDialog.setLayout(new BorderLayout());
        timeDialog.getContentPane().setBackground(UNIVERSAL_BG);

        JPanel timePanel = new JPanel(new GridBagLayout());
        timePanel.setBackground(UNIVERSAL_BG);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);

        JLabel timeLabel = new JLabel("Ora (HH:mm):");
        JSpinner hourSpinner = new JSpinner(new SpinnerNumberModel(11, 0, 23, 1));
        JSpinner minuteSpinner = new JSpinner(new SpinnerNumberModel(0, 0, 59, 1));

        gbc.gridx = 0; gbc.gridy = 0;
        timePanel.add(timeLabel, gbc);
        gbc.gridx = 1;
        timePanel.add(hourSpinner, gbc);
        gbc.gridx = 2;
        timePanel.add(new JLabel(":"), gbc);
        gbc.gridx = 3;
        timePanel.add(minuteSpinner, gbc);

        JPanel buttonPanel = new JPanel();
        buttonPanel.setBackground(UNIVERSAL_BG);
        
        JButton okButton = new JButton("OK");
        JButton cancelButton = new JButton("Anulează");

        okButton.addActionListener(e -> {
            int hour = (Integer) hourSpinner.getValue();
            int minute = (Integer) minuteSpinner.getValue();
            timeDialog.dispose();
            startScheduledSimulation(hour, minute);
        });

        cancelButton.addActionListener(e -> timeDialog.dispose());

        buttonPanel.add(okButton);
        buttonPanel.add(cancelButton);

        timeDialog.add(timePanel, BorderLayout.CENTER);
        timeDialog.add(buttonPanel, BorderLayout.SOUTH);
        timeDialog.setVisible(true);
    }

    private void startNormalSimulation() {
        stopSimulation();
        setTitle(baseTitle);
        
        mainTimer = new Timer();
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                SwingUtilities.invokeLater(() -> processOrders());
            }
        };
        mainTimer.scheduleAtFixedRate(task, 0, 1000); // la fiecare secundă
        
        JOptionPane.showMessageDialog(this, "Simulare normală pornită!", "Info", JOptionPane.INFORMATION_MESSAGE);
    }

    private void startIntervalSimulation() {
        stopSimulation();
        isWorking = true;
        setTitle(baseTitle + " - Lucrează");
        
        mainTimer = new Timer();
        intervalTimer = new Timer();
        
        // Timer pentru procesarea comenzilor (doar când lucrează)
        TimerTask processTask = new TimerTask() {
            @Override
            public void run() {
                if (isWorking) {
                    SwingUtilities.invokeLater(() -> processOrders());
                }
            }
        };
        mainTimer.scheduleAtFixedRate(processTask, 0, 1000); // verifică la fiecare secundă
        
        // Timer pentru alternarea între lucru și pauză
        TimerTask intervalTask = new TimerTask() {
            @Override
            public void run() {
                SwingUtilities.invokeLater(() -> {
                    isWorking = !isWorking;
                    if (isWorking) {
                        setTitle(baseTitle + " - Lucrează");
                    } else {
                        setTitle(baseTitle + " - Pauză");
                    }
                });
            }
        };
        intervalTimer.scheduleAtFixedRate(intervalTask, 2000, 2000); // la fiecare 2 secunde
        
        JOptionPane.showMessageDialog(this, "Simulare cu intervale de 2 secunde pornită!", "Info", JOptionPane.INFORMATION_MESSAGE);
    }

    private void startScheduledSimulation(int hour, int minute) {
        stopSimulation();
        setTitle(baseTitle);
        
        LocalTime now = LocalTime.now();
        LocalTime targetTime = LocalTime.of(hour, minute);
        
        long delayMillis;
        if (targetTime.isAfter(now)) {
            delayMillis = java.time.Duration.between(now, targetTime).toMillis();
        } else {
            // Next day
            delayMillis = java.time.Duration.between(now, targetTime.plusHours(24)).toMillis();
        }
        
        mainTimer = new Timer();
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                SwingUtilities.invokeLater(() -> {
                    processOrders();
                    JOptionPane.showMessageDialog(CafeKanbanSimulator.this, 
                        "Simulare executată la " + targetTime.format(DateTimeFormatter.ofPattern("HH:mm")) + "!", 
                        "Simulare Programată", 
                        JOptionPane.INFORMATION_MESSAGE);
                });
            }
        };
        
        mainTimer.schedule(task, delayMillis);
        
        JOptionPane.showMessageDialog(this, 
            "Simulare programată pentru " + targetTime.format(DateTimeFormatter.ofPattern("HH:mm")) + 
            "\n(în " + (delayMillis / 1000 / 60) + " minute)", 
            "Info", 
            JOptionPane.INFORMATION_MESSAGE);
    }

    private void showMenuDialog() {
        JDialog menuDialog = new JDialog(this, "Meniu Băuturi", true);
        menuDialog.setSize(600, 600);
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

    private void stopSimulation() {
        if (mainTimer != null) {
            mainTimer.cancel();
            mainTimer = null;
        }
        if (intervalTimer != null) {
            intervalTimer.cancel();
            intervalTimer = null;
        }
        setTitle(baseTitle); // resetează titlul
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
            Timer completionTimer = new Timer();
            TimerTask completionTask = new TimerTask() {
                @Override
                public void run() {
                    SwingUtilities.invokeLater(() -> {
                        preparingPanel.remove(orderCard);
                        completedPanel.add(orderCard);
                        revalidatePanels();
                    });
                }
            };
            completionTimer.schedule(completionTask, prepTime * 1000);
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

    @Override
    public void dispose() {
        stopSimulation();
        super.dispose();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new CafeKanbanSimulator().setVisible(true));
    }
}