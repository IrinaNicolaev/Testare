import javax.swing.table.AbstractTableModel;

import java.util.ArrayList;
import java.util.List;

public class AlertTableModel extends AbstractTableModel {

    private final List<Alert> alerts = new ArrayList<Alert>();

    private final String[] columns = {
            "Название",
            "Тип",
            "Параметр",
            "Повтор",
            "Звук",
            "Действие",
            "Включено"
    };

    @Override
    public int getRowCount() {
        return alerts.size();
    }

    @Override
    public int getColumnCount() {
        return columns.length;
    }

    @Override
    public String getColumnName(int column) {
        return columns[column];
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        Alert a = alerts.get(rowIndex);
        switch (columnIndex) {
            case 0:
                return a.getName();
            case 1:
                return a.getType().name();
            case 2:
                switch (a.getType()) {
                    case TIME:
                        return (a.getTime() != null) ? a.getTime().toString() : "";
                    case INTERVAL:
                        return "каждые " + a.getIntervalSeconds() + " сек";
                    case COUNTDOWN:
                        return "таймер " + a.getCountdownTotalSeconds() +
                               " сек, осталось " + a.getRemainingSeconds() + " сек";
                    default:
                        return "";
                }
            case 3:
                switch (a.getType()) {
                    case TIME:
                        return a.isRepeatDaily() ? "Да" : "Нет";
                    case INTERVAL:
                        int rt = a.getRepeatTimes();
                        return rt < 0 ? "∞" : rt;
                    case COUNTDOWN:
                        return "-";
                    default:
                        return "";
                }
            case 4:
                return a.getSoundPath();
            case 5:
                return a.getActionPath();
            case 6:
                return a.isEnabled() ? "Да" : "Нет";
            default:
                return "";
        }
    }

    public void addAlert(Alert alert) {
        alerts.add(alert);
        int row = alerts.size() - 1;
        fireTableRowsInserted(row, row);
    }

    public void removeAlert(int row) {
        if (row >= 0 && row < alerts.size()) {
            alerts.remove(row);
            fireTableRowsDeleted(row, row);
        }
    }

    public Alert getAlertAt(int row) {
        return alerts.get(row);
    }

    public List<Alert> getAlerts() {
        return alerts;
    }

    public void refresh() {
        if (!alerts.isEmpty()) {
            fireTableRowsUpdated(0, alerts.size() - 1);
        }
    }
}
