import java.awt.*;
import java.awt.event.ActionEvent;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicInteger;
import javax.swing.*;
import javax.swing.border.TitledBorder;


public class TimerThreeInOne extends JFrame {
    enum Mode { AT_TIME, AFTER_DELAY, PERIODIC }
    enum Status { READY, RUNNING, EXPIRED, CANCELED }

    static final DateTimeFormatter HHMM = DateTimeFormatter.ofPattern("HH:mm");
    static final DateTimeFormatter TS = DateTimeFormatter.ofPattern("HH:mm:ss");

    static class TimerEntry {
        String name = "Timer";
        Mode mode = Mode.AFTER_DELAY;
        Status status = Status.READY;
        Integer h, m;
        Long delayMs, initDelayMs, periodMs;
        Integer count;
        Timer timer; TimerTask task; AtomicInteger remaining;
        void cancel() { if (timer != null) try { timer.cancel(); } catch (Exception ignored) {} status = Status.CANCELED; }
    }

    private final JPanel rowsPanel = new JPanel();
    private final JScrollPane rowsScroll;
    private final JTextArea console = new JTextArea();

    private final JButton bAdd = new JButton("Add Timer");
    private final JButton bCancelAll = new JButton("Cancel All");
    private final JButton bRemoveFinished = new JButton("Remove Finished");
    private final JButton bClearConsole = new JButton("Clear Console");

    public TimerThreeInOne() {
        super("Timer");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(980, 720);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(8,8));

        rowsPanel.setLayout(new BoxLayout(rowsPanel, BoxLayout.Y_AXIS));
        rowsScroll = new JScrollPane(rowsPanel);
        rowsScroll.getVerticalScrollBar().setUnitIncrement(16);
        add(rowsScroll, BorderLayout.CENTER);

        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 8));
        top.add(bAdd); top.add(bCancelAll); top.add(bRemoveFinished);
        add(top, BorderLayout.NORTH);

        console.setEditable(false); console.setLineWrap(true); console.setWrapStyleWord(true);
        JScrollPane sp = new JScrollPane(console);
        sp.setPreferredSize(new Dimension(960, 220));
        JPanel bottom = new JPanel(new BorderLayout(6,6));
        JPanel bottomBtns = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bottomBtns.add(bClearConsole);
        bottom.add(bottomBtns, BorderLayout.NORTH);
        bottom.add(sp, BorderLayout.CENTER);
        add(bottom, BorderLayout.SOUTH);

        bAdd.addActionListener(e -> addRow(null));
        bCancelAll.addActionListener(this::onCancelAll);
        bRemoveFinished.addActionListener(e -> removeFinishedRows());
        bClearConsole.addActionListener(e -> console.setText(""));

        addRow(null);
    }

    private void addRow(TimerEntry preset) {
        TimerRowPanel row = new TimerRowPanel(preset);
        rowsPanel.add(row);
        rowsPanel.revalidate(); rowsPanel.repaint();
    }

    private void onCancelAll(ActionEvent e) {
        for (Component c : rowsPanel.getComponents()) if (c instanceof TimerRowPanel) ((TimerRowPanel)c).cancelTimer();
        log("[CANCEL ALL] requested");
    }

    private void removeFinishedRows() {
        for (int i = rowsPanel.getComponentCount()-1; i>=0; i--) {
            Component c = rowsPanel.getComponent(i);
            if (c instanceof TimerRowPanel) {
                TimerRowPanel r = (TimerRowPanel)c;
                if (r.entry.status==Status.EXPIRED || r.entry.status==Status.CANCELED) rowsPanel.remove(i);
            }
        }
        rowsPanel.revalidate(); rowsPanel.repaint();
    }

    private void err(String m){ JOptionPane.showMessageDialog(this,m,"Input error",JOptionPane.ERROR_MESSAGE); }
    private void log(String m){ console.append("["+TS.format(LocalDateTime.now())+"] "+m+"\n"); console.setCaretPosition(console.getDocument().getLength()); }

    @Override public void dispose(){ for (Component c:rowsPanel.getComponents()) if (c instanceof TimerRowPanel) ((TimerRowPanel)c).entry.cancel(); super.dispose(); }

    class TimerRowPanel extends JPanel {
        final TimerEntry entry; final JTextField tfName;
        final JRadioButton rbAt=new JRadioButton("At time"), rbDelay=new JRadioButton("After delay"), rbPer=new JRadioButton("Periodic");
        final JTextField tfHour=new JTextField(2), tfMin=new JTextField(2), tfDelay=new JTextField("4000",6), tfInit=new JTextField("1000",6), tfPeriod=new JTextField("800",6), tfCount=new JTextField("5",4);
        final JButton bStart=new JButton("Start"), bCancel=new JButton("Cancel"), bDelete=new JButton("Delete");
        final JLabel statusLabel=new JLabel(Status.READY.toString());

        TimerRowPanel(TimerEntry preset) {
            setLayout(new GridBagLayout());
            this.entry=(preset==null?new TimerEntry():preset);
            this.tfName=new JTextField(this.entry.name,12);
            setBorder(new TitledBorder("Timer"));

            ButtonGroup g=new ButtonGroup(); g.add(rbAt); g.add(rbDelay); g.add(rbPer); rbDelay.setSelected(true);

            GridBagConstraints gc=new GridBagConstraints();
            gc.insets=new Insets(4,4,4,4); gc.fill=GridBagConstraints.HORIZONTAL; gc.weighty=0;

            LocalDateTime now=LocalDateTime.now(); tfHour.setText(String.format("%02d",now.getHour())); tfMin.setText(String.format("%02d",now.getMinute()));

            int x=0,y=0;
            gc.gridx=x++;gc.gridy=y;gc.weightx=0;add(new JLabel("Name:"),gc);
            gc.gridx=x++;gc.weightx=0.4;add(tfName,gc);

            JPanel modeP=new JPanel(new FlowLayout(FlowLayout.LEFT,6,0)); modeP.add(new JLabel("Mode:")); modeP.add(rbAt); modeP.add(rbDelay); modeP.add(rbPer);
            gc.gridx=x++;gc.weightx=0.6;add(modeP,gc);

            gc.gridx=x++;gc.weightx=0;add(new JLabel("Status:"),gc);
            gc.gridx=x++;gc.weightx=0;add(statusLabel,gc);

            y++;x=0;
            JPanel atP=new JPanel(new FlowLayout(FlowLayout.LEFT,6,0)); atP.add(new JLabel("At (HH:MM):")); atP.add(tfHour); atP.add(new JLabel(":")); atP.add(tfMin);
            gc.gridx=x++;gc.gridy=y;gc.gridwidth=2;gc.weightx=0.6;add(atP,gc);gc.gridwidth=1;

            JPanel delayP=new JPanel(new FlowLayout(FlowLayout.LEFT,6,0)); delayP.add(new JLabel("Delay ms:")); delayP.add(tfDelay);
            gc.gridx=x++;gc.weightx=0.2;add(delayP,gc);

            JPanel perP=new JPanel(new FlowLayout(FlowLayout.LEFT,6,0)); perP.add(new JLabel("Init ms:")); perP.add(tfInit); perP.add(new JLabel("Period ms:")); perP.add(tfPeriod); perP.add(new JLabel("Count:")); perP.add(tfCount);
            gc.gridx=x++;gc.weightx=0.8;add(perP,gc);

            JPanel btns=new JPanel(new FlowLayout(FlowLayout.RIGHT,6,0)); btns.add(bStart); btns.add(bCancel); btns.add(bDelete);
            gc.gridx=x++;gc.weightx=0.2;add(btns,gc);

            rbAt.addActionListener(e->entry.mode=Mode.AT_TIME); rbDelay.addActionListener(e->entry.mode=Mode.AFTER_DELAY); rbPer.addActionListener(e->entry.mode=Mode.PERIODIC);
            bStart.addActionListener(this::onStart); bCancel.addActionListener(e->cancelTimer()); bDelete.addActionListener(e->deleteRow());
            updateState(Status.READY);
        }

        void onStart(ActionEvent e){ if(entry.status==Status.RUNNING)return; entry.name=safe(tfName.getText(),"Timer"); try{ switch(entry.mode){ case AT_TIME->{ entry.h=parseInt(tfHour.getText(),0,23,"hour"); entry.m=parseInt(tfMin.getText(),0,59,"minute"); scheduleAtTime(); } case AFTER_DELAY->{ entry.delayMs=parseLong(tfDelay.getText(),0,Integer.MAX_VALUE,"delay (ms)"); scheduleAfterDelay(entry.delayMs); } case PERIODIC->{ entry.initDelayMs=parseLong(tfInit.getText(),0,Integer.MAX_VALUE,"init (ms)"); entry.periodMs=parseLong(tfPeriod.getText(),1,Integer.MAX_VALUE,"period (ms)"); entry.count=parseInt(tfCount.getText(),1,Integer.MAX_VALUE,"count"); schedulePeriodic(entry.initDelayMs,entry.periodMs,entry.count); } } updateState(Status.RUNNING); log("[START] "+entry.name+" ("+entry.mode+")"); }catch(Exception ex){ err(ex.getMessage()); } }

        void scheduleAtTime(){ LocalDateTime now=LocalDateTime.now(); LocalDateTime tgt=now.withHour(entry.h).withMinute(entry.m).withSecond(0).withNano(0); if(!tgt.isAfter(now))tgt=tgt.plusDays(1); long delay=tgt.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()-System.currentTimeMillis(); scheduleOnce(delay,()->log("[AT_TIME] "+entry.name+" fired at "+HHMM.format(LocalDateTime.now()))); }
        void scheduleAfterDelay(long d){ scheduleOnce(d,()->log("[AFTER_DELAY] "+entry.name+" finished")); }
        void scheduleOnce(long d,Runnable onFire){ cancelTimer(); entry.timer=new Timer(true); entry.task=new TimerTask(){ public void run(){ SwingUtilities.invokeLater(()->{ entry.status=Status.EXPIRED; updateState(entry.status); onFire.run(); }); }}; entry.timer.schedule(entry.task,Math.max(d,1)); }
        void schedulePeriodic(long init,long per,int count){ cancelTimer(); entry.timer=new Timer(true); entry.remaining=new AtomicInteger(count); entry.task=new TimerTask(){ public void run(){ int left=entry.remaining.decrementAndGet(); SwingUtilities.invokeLater(()->{ if(left<=0){ try{entry.timer.cancel();}catch(Exception ignored){} entry.status=Status.EXPIRED; updateState(entry.status); log("[PERIODIC] "+entry.name+" expired"); } else { statusLabel.setText(Status.RUNNING+" ("+left+" left)"); log("[PERIODIC] "+entry.name+" tick (remaining="+left+")"); } }); }}; entry.timer.schedule(entry.task,Math.max(init,0),Math.max(per,1)); }

        void cancelTimer(){ if(entry.status==Status.RUNNING)log("[CANCEL] "+entry.name); entry.cancel(); updateState(entry.status); }
        void deleteRow(){ cancelTimer(); rowsPanel.remove(this); rowsPanel.revalidate(); rowsPanel.repaint(); }
        void updateState(Status s){ entry.status=s; boolean running=s==Status.RUNNING; tfName.setEnabled(!running); rbAt.setEnabled(!running); rbDelay.setEnabled(!running); rbPer.setEnabled(!running); tfHour.setEnabled(!running); tfMin.setEnabled(!running); tfDelay.setEnabled(!running); tfInit.setEnabled(!running); tfPeriod.setEnabled(!running); tfCount.setEnabled(!running); bStart.setEnabled(!running); bCancel.setEnabled(running); statusLabel.setText(s.toString()); }
        int parseInt(String s,int min,int max,String n){ int v=Integer.parseInt(s.trim()); if(v<min||v>max) throw new IllegalArgumentException(n+" out of range"); return v; }
        long parseLong(String s,long min,long max,String n){ long v=Long.parseLong(s.trim()); if(v<min||v>max) throw new IllegalArgumentException(n+" out of range"); return v; }
        String safe(String s,String def){ s=s==null?"":s.trim(); return s.isEmpty()?def:s; }
    }

    public static void main(String[] args){ try{ UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); }catch(Exception ignored){} SwingUtilities.invokeLater(()->new TimerThreeInOne().setVisible(true)); }
}