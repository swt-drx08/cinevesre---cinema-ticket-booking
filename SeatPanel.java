// ============================================================
// SeatPanel.java
// A reusable panel that shows seat buttons (A1–E6).
// Green  = Available   Red = Booked   Blue = Selected by user
// OOP: Class, Constructor, Methods
// ============================================================

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.HashSet;

public class SeatPanel extends JPanel {

    // Rows A–E, columns 1–6  →  30 seats total
    private static final String[] ROWS = {"A", "B", "C", "D", "E"};
    private static final int      COLS = 6;

    // Tracks which seats are already booked (passed in from outside)
    private HashSet<String> bookedSeats;

    // Tracks which seats the current user has selected (toggled by clicking)
    private ArrayList<String> selectedSeats;

    // Store references to every button so we can update colours
    private JButton[][] buttons = new JButton[ROWS.length][COLS];

    // Colours
    private static final Color GREEN  = new Color(60, 179, 113);  // available
    private static final Color RED    = new Color(220, 50, 50);   // booked
    private static final Color BLUE   = new Color(30, 144, 255);  // selected

    // Constructor
    public SeatPanel(HashSet<String> bookedSeats) {
        this.bookedSeats   = bookedSeats;
        this.selectedSeats = new ArrayList<>();

        setLayout(new GridLayout(ROWS.length + 1, COLS + 1, 6, 6));
        setBackground(new Color(245, 245, 255));
        setBorder(BorderFactory.createTitledBorder("Select Seats  " +
                "[ 🟢 Available   🔴 Booked   🔵 Selected ]"));

        buildButtons();
    }

    // Creates all seat buttons and labels
    private void buildButtons() {
        // Top-left corner: blank cell
        add(new JLabel(""));

        // Column header: 1  2  3  4  5  6
        for (int col = 1; col <= COLS; col++) {
            JLabel lbl = new JLabel(String.valueOf(col), SwingConstants.CENTER);
            lbl.setFont(new Font("Arial", Font.BOLD, 12));
            add(lbl);
        }

        // Each row: row label + seat buttons
        for (int r = 0; r < ROWS.length; r++) {
            JLabel rowLbl = new JLabel(ROWS[r], SwingConstants.CENTER);
            rowLbl.setFont(new Font("Arial", Font.BOLD, 12));
            add(rowLbl);

            for (int c = 1; c <= COLS; c++) {
                String seatId = ROWS[r] + c;   // e.g. "A1", "B3"
                JButton btn = new JButton(seatId);
                btn.setFont(new Font("Arial", Font.PLAIN, 11));
                btn.setFocusPainted(false);
                btn.setOpaque(true);

                if (bookedSeats.contains(seatId)) {
                    // Seat already taken – disable it
                    btn.setBackground(RED);
                    btn.setForeground(Color.WHITE);
                    btn.setEnabled(false);
                } else {
                    btn.setBackground(GREEN);
                    btn.setForeground(Color.WHITE);
                    // Click toggles selection
                    btn.addActionListener(new SeatClickListener(seatId, btn));
                }

                buttons[r][c - 1] = btn;
                add(btn);
            }
        }
    }

    // Listener for a seat button click
    private class SeatClickListener implements ActionListener {
        private String seatId;
        private JButton button;

        public SeatClickListener(String seatId, JButton button) {
            this.seatId = seatId;
            this.button = button;
        }

        public void actionPerformed(ActionEvent e) {
            if (selectedSeats.contains(seatId)) {
                // Deselect
                selectedSeats.remove(seatId);
                button.setBackground(GREEN);
            } else {
                // Select
                selectedSeats.add(seatId);
                button.setBackground(BLUE);
            }
        }
    }

    // Returns the list of seats the user selected
    public ArrayList<String> getSelectedSeats() {
        return selectedSeats;
    }

    // Call this to reset selections (e.g. when switching shows)
    public void clearSelection() {
        for (int r = 0; r < ROWS.length; r++) {
            for (int c = 0; c < COLS; c++) {
                JButton btn = buttons[r][c];
                if (btn != null && btn.isEnabled()) {
                    String seatId = ROWS[r] + (c + 1);
                    if (selectedSeats.contains(seatId)) {
                        btn.setBackground(GREEN);
                    }
                }
            }
        }
        selectedSeats.clear();
    }
}
