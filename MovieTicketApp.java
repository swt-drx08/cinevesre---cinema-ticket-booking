// ============================================================
// MovieTicketApp.java
// Main class – builds the entire GUI and connects all features.
// Panels: Home | Book Ticket | My Tickets
// OOP: uses Movie objects and Booking objects throughout
// ============================================================

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

public class MovieTicketApp extends JFrame {

    // -------------------------------------------------------
    // DATA – stored in memory while the app is running
    // -------------------------------------------------------

    // -------------------------------------------------------
    // MOOD → MOVIE SUGGESTION MAP
    // Each mood has a list of recommended movie names.
    // These names must match entries in the movies[] array.
    // -------------------------------------------------------
    private static final String[][] MOOD_MOVIES = {
        // { "Mood label (with emoji)", "movie1", "movie2", ... }
        { "😊  Happy",        "Avengers", "The Lion King", "Coco" },
        { "😢  Sad",          "Interstellar", "Coco", "The Lion King" },
        { "😎  Adventurous",  "Avatar", "Avengers", "Inception" },
        { "😌  Relaxed",      "Interstellar", "Inception", "The Dark Knight" },
        { "🤩  Excited",      "Avengers", "Avatar", "The Dark Knight" },
        { "😠  Angry",        "The Dark Knight", "Inception", "Avengers" }
    };

    // 8 Movie objects — covers all mood categories
    private Movie[] movies = {
        new Movie("Avengers",       200),   // action / superhero  → Happy, Adventurous, Excited, Angry
        new Movie("Avatar",         180),   // sci-fi / adventure  → Adventurous, Excited
        new Movie("Interstellar",   150),   // deep sci-fi / drama → Sad, Relaxed
        new Movie("The Dark Knight",220),   // thriller / action   → Relaxed, Excited, Angry
        new Movie("Inception",      190),   // mind-bending        → Adventurous, Relaxed, Angry
        new Movie("The Lion King",  160),   // emotional / feel-good → Happy, Sad
        new Movie("Coco",           140),   // heartwarming / family → Happy, Sad
        new Movie("Spider-Man",     170)    // fun / action        → Happy, Excited
    };

    // Available show timings
    private String[] timings = {"10:00 AM", "01:00 PM", "04:00 PM", "07:00 PM", "10:00 PM"};

    // All confirmed bookings (grows as user books)
    private ArrayList<Booking> bookings = new ArrayList<>();

    // Counter for unique booking IDs: BK001, BK002 ...
    private int bookingCounter = 1;

    // Booked seats per "movie+date+timing" key
    // Key  = "Avengers|2026-11-01|07:00 PM"
    // Value = set of booked seat IDs like {"A1","A2"}
    private HashMap<String, HashSet<String>> bookedSeatsMap = new HashMap<>();

    // -------------------------------------------------------
    // GUI COMPONENTS (shared across methods)
    // -------------------------------------------------------
    private JPanel mainPanel;        // holds all content panels
    private CardLayout cardLayout;   // switches between panels

    // Book Ticket panel widgets (kept as fields so buttons can read them)
    private JComboBox<Movie>   movieCombo;
    private JComboBox<String>  dateCombo;
    private JComboBox<String>  timingCombo;
    private JLabel             totalLabel;
    private SeatPanel          seatPanel;
    private JPanel             seatContainer; // holds seatPanel and refreshes

    // -------------------------------------------------------
    // ENTRY POINT
    // -------------------------------------------------------
    public static void main(String[] args) {
        // Run the GUI on the Event Dispatch Thread (standard Swing practice)
        SwingUtilities.invokeLater(() -> new MovieTicketApp());
    }

    // -------------------------------------------------------
    // CONSTRUCTOR – builds the window
    // -------------------------------------------------------
    public MovieTicketApp() {
        setTitle("🎬 Cineverse — Movie Ticket System");
        setSize(820, 640);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);

        // Card layout lets us swap panels like pages
        cardLayout = new CardLayout();
        mainPanel  = new JPanel(cardLayout);

        mainPanel.add(buildHomePanel(),    "HOME");
        mainPanel.add(buildBookPanel(),    "BOOK");
        mainPanel.add(buildHistoryPanel(), "HISTORY");

        add(mainPanel);
        setVisible(true);

        // Show welcome + mood dialog AFTER the main window is visible
        showWelcomeDialog();
    }

    // ===========================================================
    // WELCOME + MOOD DIALOG
    // Shows a greeting, asks for mood, gives suggestions or skips.
    // ===========================================================
    private void showWelcomeDialog() {

        // ---- Get a time-based greeting ----
        int hour = LocalTime.now().getHour();
        String greeting;
        if (hour < 12)       greeting = "Good Morning ☀️";
        else if (hour < 17)  greeting = "Good Afternoon 🌤️";
        else if (hour < 21)  greeting = "Good Evening 🌆";
        else                 greeting = "Good Night 🌙";

        // ---- Build the dialog ----
        JDialog dialog = new JDialog(this, "Welcome to Cineverse!", true);
        dialog.setSize(460, 420);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout());
        dialog.setResizable(false);
        dialog.getContentPane().setBackground(new Color(18, 18, 45));

        // --- Top banner ---
        JPanel banner = new JPanel();
        banner.setLayout(new BoxLayout(banner, BoxLayout.Y_AXIS));
        banner.setBackground(new Color(18, 18, 45));
        banner.setBorder(new EmptyBorder(28, 20, 10, 20));

        JLabel cineverseLabel = new JLabel("🎬  CINEVERSE", SwingConstants.CENTER);
        cineverseLabel.setFont(new Font("Arial", Font.BOLD, 26));
        cineverseLabel.setForeground(new Color(255, 200, 50));
        cineverseLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        banner.add(cineverseLabel);

        banner.add(Box.createVerticalStrut(8));

        JLabel greetLabel = new JLabel(greeting + "  Welcome!", SwingConstants.CENTER);
        greetLabel.setFont(new Font("Arial", Font.PLAIN, 15));
        greetLabel.setForeground(new Color(200, 200, 255));
        greetLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        banner.add(greetLabel);

        banner.add(Box.createVerticalStrut(6));

        JLabel tagline = new JLabel("Your perfect movie is just one click away 🍿", SwingConstants.CENTER);
        tagline.setFont(new Font("Arial", Font.ITALIC, 13));
        tagline.setForeground(new Color(150, 150, 200));
        tagline.setAlignmentX(Component.CENTER_ALIGNMENT);
        banner.add(tagline);

        dialog.add(banner, BorderLayout.NORTH);

        // --- Mood selection area ---
        JPanel centre = new JPanel();
        centre.setLayout(new BoxLayout(centre, BoxLayout.Y_AXIS));
        centre.setBackground(new Color(28, 28, 60));
        centre.setBorder(new EmptyBorder(18, 30, 10, 30));

        JLabel moodLabel = new JLabel("How are you feeling today?", SwingConstants.CENTER);
        moodLabel.setFont(new Font("Arial", Font.BOLD, 14));
        moodLabel.setForeground(Color.WHITE);
        moodLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        centre.add(moodLabel);

        centre.add(Box.createVerticalStrut(14));

        // Mood buttons in a grid (2 per row)
        JPanel moodGrid = new JPanel(new GridLayout(3, 2, 10, 10));
        moodGrid.setBackground(new Color(28, 28, 60));
        moodGrid.setMaximumSize(new Dimension(360, 130));

        // We'll remember which mood was clicked using a single-element array
        // (trick to modify variable inside anonymous class)
        final String[] chosenMood = { null };
        final JButton[] moodButtons = new JButton[MOOD_MOVIES.length];

        for (int i = 0; i < MOOD_MOVIES.length; i++) {
            String moodLabel2 = MOOD_MOVIES[i][0];
            JButton mb = new JButton(moodLabel2);
            mb.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 13));
            mb.setBackground(new Color(50, 50, 100));
            mb.setForeground(Color.WHITE);
            mb.setFocusPainted(false);
            mb.setBorderPainted(false);
            mb.setOpaque(true);

            mb.addActionListener(e -> {
                // Highlight selected, reset others
                for (JButton b : moodButtons) {
                    b.setBackground(new Color(50, 50, 100));
                }
                mb.setBackground(new Color(255, 160, 20));
                chosenMood[0] = moodLabel2;
            });

            moodButtons[i] = mb;
            moodGrid.add(mb);
        }

        centre.add(moodGrid);
        dialog.add(centre, BorderLayout.CENTER);

        // --- Bottom buttons: Get Suggestions / Skip ---
        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 14));
        bottom.setBackground(new Color(18, 18, 45));

        JButton suggestBtn = new JButton("✨  Get Suggestions");
        suggestBtn.setBackground(new Color(255, 160, 20));
        suggestBtn.setForeground(Color.WHITE);
        suggestBtn.setFont(new Font("Arial", Font.BOLD, 13));
        suggestBtn.setFocusPainted(false);
        suggestBtn.setBorderPainted(false);
        suggestBtn.setOpaque(true);

        JButton skipBtn = new JButton("Skip →");
        skipBtn.setBackground(new Color(70, 70, 120));
        skipBtn.setForeground(new Color(180, 180, 255));
        skipBtn.setFont(new Font("Arial", Font.PLAIN, 13));
        skipBtn.setFocusPainted(false);
        skipBtn.setBorderPainted(false);
        skipBtn.setOpaque(true);

        // "Get Suggestions" click
        suggestBtn.addActionListener(e -> {
            if (chosenMood[0] == null) {
                JOptionPane.showMessageDialog(dialog,
                    "Please select a mood first! 😊",
                    "No Mood Selected", JOptionPane.WARNING_MESSAGE);
                return;
            }
            dialog.dispose();
            showMoodSuggestions(chosenMood[0]);
        });

        // "Skip" click — close dialog, go straight to Home
        skipBtn.addActionListener(e -> dialog.dispose());

        bottom.add(suggestBtn);
        bottom.add(skipBtn);
        dialog.add(bottom, BorderLayout.SOUTH);

        dialog.setVisible(true);
    }

    // ===========================================================
    // MOOD SUGGESTION DIALOG
    // Shows movies matching the selected mood. User can:
    //   Book Now  →  goes to Book Ticket with that movie pre-selected
    //   Browse All →  closes and shows Home with full movie list
    // ===========================================================
    private void showMoodSuggestions(String mood) {

        // Find the movie names for this mood
        String[] suggestedNames = null;
        for (String[] row : MOOD_MOVIES) {
            if (row[0].equals(mood)) {
                // row[0] is the mood label, rest are movie names
                suggestedNames = new String[row.length - 1];
                System.arraycopy(row, 1, suggestedNames, 0, suggestedNames.length);
                break;
            }
        }
        if (suggestedNames == null) return;

        // Find the actual Movie objects for those names
        ArrayList<Movie> suggested = new ArrayList<>();
        for (String name : suggestedNames) {
            for (Movie m : movies) {
                if (m.getName().equalsIgnoreCase(name)) {
                    suggested.add(m);
                    break;
                }
            }
        }

        // ---- Build suggestion dialog ----
        JDialog sDialog = new JDialog(this, "Movie Suggestions for You 🎬", true);
        sDialog.setSize(440, 400);
        sDialog.setLocationRelativeTo(this);
        sDialog.setLayout(new BorderLayout());
        sDialog.setResizable(false);
        sDialog.getContentPane().setBackground(new Color(245, 245, 255));

        // Header
        JPanel header = new JPanel();
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));
        header.setBackground(new Color(30, 30, 80));
        header.setBorder(new EmptyBorder(18, 20, 14, 20));

        JLabel title = new JLabel("Since you're feeling  " + mood, SwingConstants.CENTER);
        title.setFont(new Font("Segoe UI Emoji", Font.BOLD, 15));
        title.setForeground(new Color(255, 200, 50));
        title.setAlignmentX(Component.CENTER_ALIGNMENT);
        header.add(title);

        header.add(Box.createVerticalStrut(4));

        JLabel sub2 = new JLabel("We think you'll love these movies!", SwingConstants.CENTER);
        sub2.setFont(new Font("Arial", Font.ITALIC, 13));
        sub2.setForeground(new Color(180, 180, 255));
        sub2.setAlignmentX(Component.CENTER_ALIGNMENT);
        header.add(sub2);

        sDialog.add(header, BorderLayout.NORTH);

        // Movie suggestion cards
        JPanel cardsPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 18, 18));
        cardsPanel.setBackground(new Color(245, 245, 255));

        // Track which movie the user clicks (for "Book Now")
        final Movie[] pickedMovie = { suggested.get(0) }; // default first

        for (Movie m : suggested) {
            JPanel card = new JPanel();
            card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
            card.setBackground(Color.WHITE);
            card.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(255, 160, 20), 2, true),
                new EmptyBorder(14, 18, 14, 18)
            ));
            card.setPreferredSize(new Dimension(160, 120));
            card.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

            JLabel emojiL = new JLabel("🎬", SwingConstants.CENTER);
            emojiL.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 30));
            emojiL.setAlignmentX(Component.CENTER_ALIGNMENT);
            card.add(emojiL);

            card.add(Box.createVerticalStrut(6));

            JLabel nameL = new JLabel(m.getName(), SwingConstants.CENTER);
            nameL.setFont(new Font("Arial", Font.BOLD, 13));
            nameL.setAlignmentX(Component.CENTER_ALIGNMENT);
            card.add(nameL);

            JLabel priceL = new JLabel("₹" + m.getPrice() + " / ticket", SwingConstants.CENTER);
            priceL.setFont(new Font("Arial", Font.PLAIN, 12));
            priceL.setForeground(new Color(34, 139, 34));
            priceL.setAlignmentX(Component.CENTER_ALIGNMENT);
            card.add(priceL);

            // Clicking a card highlights it and sets it as the picked movie
            card.addMouseListener(new MouseAdapter() {
                public void mouseClicked(MouseEvent e) {
                    pickedMovie[0] = m;
                    // highlight: reset all cards then highlight this one
                    for (Component comp : cardsPanel.getComponents()) {
                        comp.setBackground(Color.WHITE);
                        if (comp instanceof JPanel) {
                            ((JPanel) comp).setBorder(BorderFactory.createCompoundBorder(
                                new LineBorder(new Color(255, 160, 20), 2, true),
                                new EmptyBorder(14, 18, 14, 18)
                            ));
                        }
                    }
                    card.setBackground(new Color(255, 245, 220));
                    card.setBorder(BorderFactory.createCompoundBorder(
                        new LineBorder(new Color(255, 100, 0), 3, true),
                        new EmptyBorder(14, 18, 14, 18)
                    ));
                }
            });

            cardsPanel.add(card);
        }

        sDialog.add(new JScrollPane(cardsPanel), BorderLayout.CENTER);

        // Bottom buttons
        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 12));
        btnRow.setBackground(new Color(245, 245, 255));

        JButton bookNowBtn = styledButton("🎟  Book Now", new Color(34, 139, 34));
        bookNowBtn.addActionListener(e -> {
            sDialog.dispose();
            // Pre-select the picked movie in the Book Ticket panel
            refreshSeatPanel();
            for (int i = 0; i < movieCombo.getItemCount(); i++) {
                if (movieCombo.getItemAt(i).getName()
                        .equalsIgnoreCase(pickedMovie[0].getName())) {
                    movieCombo.setSelectedIndex(i);
                    break;
                }
            }
            cardLayout.show(mainPanel, "BOOK");
        });

        JButton browseBtn = styledButton("🎬  Browse All Movies", new Color(70, 130, 220));
        browseBtn.addActionListener(e -> sDialog.dispose()); // just close → lands on Home

        btnRow.add(bookNowBtn);
        btnRow.add(browseBtn);
        sDialog.add(btnRow, BorderLayout.SOUTH);

        sDialog.setVisible(true);
    }

    // ===========================================================
    // NAV BAR – shown at the top of every panel
    // ===========================================================
    private JPanel buildNavBar(String activePage) {
        JPanel nav = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        nav.setBackground(new Color(30, 30, 60));
        nav.setPreferredSize(new Dimension(820, 48));

        // App title on the left
        JLabel title = new JLabel("  🎬 Cineverse  ");
        title.setFont(new Font("Arial", Font.BOLD, 18));
        title.setForeground(Color.WHITE);
        nav.add(title);

        // Nav buttons
        nav.add(navButton("Home",       "HOME",    activePage));
        nav.add(navButton("Book Ticket","BOOK",    activePage));
        nav.add(navButton("My Tickets", "HISTORY", activePage));

        return nav;
    }

    private JButton navButton(String label, String card, String activePage) {
        JButton btn = new JButton(label);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setFont(new Font("Arial", Font.BOLD, 13));
        btn.setPreferredSize(new Dimension(120, 48));

        if (card.equals(activePage)) {
            btn.setBackground(new Color(70, 130, 220));
            btn.setForeground(Color.WHITE);
        } else {
            btn.setBackground(new Color(30, 30, 60));
            btn.setForeground(new Color(180, 180, 210));
        }

        btn.addActionListener(e -> {
            if (card.equals("HISTORY")) refreshHistoryPanel();
            if (card.equals("BOOK"))    refreshSeatPanel();
            cardLayout.show(mainPanel, card);
        });
        return btn;
    }

    // ===========================================================
    // PANEL 1 – HOME
    // ===========================================================
    private JPanel buildHomePanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(new Color(245, 245, 255));

        panel.add(buildNavBar("HOME"), BorderLayout.NORTH);

        // Centre content
        JPanel centre = new JPanel();
        centre.setLayout(new BoxLayout(centre, BoxLayout.Y_AXIS));
        centre.setBackground(new Color(245, 245, 255));
        centre.setBorder(new EmptyBorder(30, 50, 20, 50));

        JLabel welcome = new JLabel("Welcome to Cineverse! 🎬�", SwingConstants.CENTER);
        welcome.setFont(new Font("Arial", Font.BOLD, 22));
        welcome.setForeground(new Color(30, 30, 100));
        welcome.setAlignmentX(Component.CENTER_ALIGNMENT);
        centre.add(welcome);

        centre.add(Box.createVerticalStrut(10));

        JLabel sub = new JLabel("Book your favourite movie tickets instantly.", SwingConstants.CENTER);
        sub.setFont(new Font("Arial", Font.PLAIN, 14));
        sub.setForeground(Color.GRAY);
        sub.setAlignmentX(Component.CENTER_ALIGNMENT);
        centre.add(sub);

        centre.add(Box.createVerticalStrut(30));

        // Movie cards row
        JPanel movieRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 0));
        movieRow.setBackground(new Color(245, 245, 255));
        for (Movie m : movies) {
            movieRow.add(buildMovieCard(m));
        }
        centre.add(movieRow);

        centre.add(Box.createVerticalStrut(35));

        // Big "Book a Ticket" button
        JButton bookBtn = styledButton("  🎟  Book a Ticket  ", new Color(70, 130, 220));
        bookBtn.setFont(new Font("Arial", Font.BOLD, 16));
        bookBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        bookBtn.addActionListener(e -> {
            refreshSeatPanel();
            cardLayout.show(mainPanel, "BOOK");
        });
        centre.add(bookBtn);

        panel.add(centre, BorderLayout.CENTER);

        // Footer
        JLabel footer = new JLabel("© 2026 Cineverse — College Project", SwingConstants.CENTER);
        footer.setFont(new Font("Arial", Font.ITALIC, 11));
        footer.setForeground(Color.LIGHT_GRAY);
        footer.setBorder(new EmptyBorder(6, 0, 6, 0));
        panel.add(footer, BorderLayout.SOUTH);

        return panel;
    }

    // Small coloured card for each movie
    private JPanel buildMovieCard(Movie movie) {
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(new Color(255, 255, 255));
        card.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(new Color(180, 180, 220), 1, true),
            new EmptyBorder(16, 20, 16, 20)
        ));
        card.setPreferredSize(new Dimension(185, 130));

        // Movie emoji placeholder
        JLabel emoji = new JLabel("🎬", SwingConstants.CENTER);
        emoji.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 36));
        emoji.setAlignmentX(Component.CENTER_ALIGNMENT);
        card.add(emoji);

        card.add(Box.createVerticalStrut(8));

        JLabel name = new JLabel(movie.getName(), SwingConstants.CENTER);
        name.setFont(new Font("Arial", Font.BOLD, 13));
        name.setAlignmentX(Component.CENTER_ALIGNMENT);
        card.add(name);

        JLabel price = new JLabel("₹" + movie.getPrice() + " / ticket", SwingConstants.CENTER);
        price.setFont(new Font("Arial", Font.PLAIN, 12));
        price.setForeground(new Color(70, 130, 50));
        price.setAlignmentX(Component.CENTER_ALIGNMENT);
        card.add(price);

        return card;
    }

    // ===========================================================
    // PANEL 2 – BOOK TICKET
    // ===========================================================
    private JPanel buildBookPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(new Color(245, 245, 255));
        panel.add(buildNavBar("BOOK"), BorderLayout.NORTH);

        // ---- Form area (left of seat panel) ----
        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(new Color(245, 245, 255));
        form.setBorder(new EmptyBorder(20, 30, 10, 10));

        GridBagConstraints gc = new GridBagConstraints();
        gc.insets  = new Insets(8, 4, 8, 4);
        gc.anchor  = GridBagConstraints.WEST;
        gc.fill    = GridBagConstraints.HORIZONTAL;

        // Heading
        gc.gridx = 0; gc.gridy = 0; gc.gridwidth = 2;
        JLabel heading = new JLabel("Book Your Ticket");
        heading.setFont(new Font("Arial", Font.BOLD, 17));
        heading.setForeground(new Color(30, 30, 100));
        form.add(heading, gc);

        // Movie dropdown
        gc.gridy = 1; gc.gridwidth = 1;
        form.add(formLabel("Movie:"), gc);
        gc.gridx = 1;
        movieCombo = new JComboBox<>(movies);
        movieCombo.setFont(new Font("Arial", Font.PLAIN, 13));
        form.add(movieCombo, gc);

        // Date dropdown (next 7 days from today)
        gc.gridx = 0; gc.gridy = 2;
        form.add(formLabel("Date:"), gc);
        gc.gridx = 1;
        dateCombo = new JComboBox<>(generateDates());
        dateCombo.setFont(new Font("Arial", Font.PLAIN, 13));
        form.add(dateCombo, gc);

        // Timing dropdown
        gc.gridx = 0; gc.gridy = 3;
        form.add(formLabel("Show Time:"), gc);
        gc.gridx = 1;
        timingCombo = new JComboBox<>(timings);
        timingCombo.setFont(new Font("Arial", Font.PLAIN, 13));
        form.add(timingCombo, gc);

        // Refresh seats when movie/date/timing changes
        ActionListener refreshAction = e -> refreshSeatPanel();
        movieCombo.addActionListener(refreshAction);
        dateCombo.addActionListener(refreshAction);
        timingCombo.addActionListener(refreshAction);

        // Seat panel container (refreshed dynamically)
        seatContainer = new JPanel(new BorderLayout());
        seatContainer.setBackground(new Color(245, 245, 255));
        seatContainer.setBorder(new EmptyBorder(10, 0, 0, 0));
        gc.gridx = 0; gc.gridy = 4; gc.gridwidth = 2;
        form.add(seatContainer, gc);

        // Total price label
        gc.gridy = 5; gc.gridwidth = 2;
        totalLabel = new JLabel("Total: ₹0   (select seats above)");
        totalLabel.setFont(new Font("Arial", Font.BOLD, 14));
        totalLabel.setForeground(new Color(34, 139, 34));
        form.add(totalLabel, gc);

        // Confirm button
        gc.gridy = 6;
        JButton confirmBtn = styledButton("  ✅  Confirm Booking  ", new Color(34, 139, 34));
        confirmBtn.addActionListener(e -> handleBooking());
        form.add(confirmBtn, gc);

        panel.add(new JScrollPane(form), BorderLayout.CENTER);

        // Build the initial seat panel
        refreshSeatPanel();

        return panel;
    }

    // Generates dates for the next 7 days (e.g. "2026-10-25")
    private String[] generateDates() {
        String[] dates = new String[7];
        LocalDate today = LocalDate.now();
        for (int i = 0; i < 7; i++) {
            dates[i] = today.plusDays(i).toString();
        }
        return dates;
    }

    // Rebuild the seat panel for the current movie+date+timing selection
    private void refreshSeatPanel() {
        if (seatContainer == null) return;

        String showKey = getShowKey();
        HashSet<String> booked = bookedSeatsMap.getOrDefault(showKey, new HashSet<>());
        seatPanel = new SeatPanel(booked);

        // When any seat is clicked, update the total price label
        seatPanel.addMouseListener(new MouseAdapter() {
            public void mouseReleased(MouseEvent e) { updateTotalLabel(); }
        });

        // Wrap seat panel so total updates after each click
        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setBackground(new Color(245, 245, 255));
        wrapper.add(seatPanel, BorderLayout.CENTER);

        // Override paintComponent to refresh total every repaint (simple trick)
        seatContainer.removeAll();
        seatContainer.add(seatPanel, BorderLayout.CENTER);
        seatContainer.revalidate();
        seatContainer.repaint();

        updateTotalLabel();
    }

    // Returns a string key for the current show selection
    private String getShowKey() {
        if (movieCombo == null || dateCombo == null || timingCombo == null) return "";
        Movie  m = (Movie)  movieCombo.getSelectedItem();
        String d = (String) dateCombo.getSelectedItem();
        String t = (String) timingCombo.getSelectedItem();
        return m.getName() + "|" + d + "|" + t;
    }

    // Recalculates and updates the total label
    private void updateTotalLabel() {
        if (seatPanel == null || movieCombo == null) return;
        Movie m = (Movie) movieCombo.getSelectedItem();
        int count = seatPanel.getSelectedSeats().size();
        int total = m.getPrice() * count;
        totalLabel.setText("Total: ₹" + total + "   (" + count + " seat" + (count == 1 ? "" : "s") + " selected)");
    }

    // ---- Handle the Confirm Booking button click ----
    private void handleBooking() {
        Movie  movie  = (Movie)  movieCombo.getSelectedItem();
        String date   = (String) dateCombo.getSelectedItem();
        String timing = (String) timingCombo.getSelectedItem();

        ArrayList<String> selected = seatPanel.getSelectedSeats();

        if (selected.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                "Please select at least one seat before booking.",
                "No Seats Selected", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Build booking ID: BK001, BK002 ...
        String bookingId = String.format("BK%03d", bookingCounter++);

        // Convert selected list to array
        String[] seatArray = selected.toArray(new String[0]);

        // Create a Booking object (OOP in action!)
        Booking booking = new Booking(bookingId, movie.getName(),
                                      movie.getPrice(), date, timing, seatArray);

        // Save booking
        bookings.add(booking);

        // Mark seats as booked in the map
        String showKey = getShowKey();
        bookedSeatsMap.computeIfAbsent(showKey, k -> new HashSet<>()).addAll(selected);

        // Show ticket popup
        showTicketPopup(booking);

        // Refresh the seat panel so booked seats turn red
        refreshSeatPanel();
    }

    // ===========================================================
    // TICKET POPUP
    // ===========================================================
    private void showTicketPopup(Booking b) {
        JDialog dialog = new JDialog(this, "🎟 Booking Confirmed!", true);
        dialog.setSize(400, 420);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout());
        dialog.getContentPane().setBackground(new Color(245, 245, 255));

        // Ticket text area
        JTextArea area = new JTextArea(b.getTicketText());
        area.setFont(new Font("Monospaced", Font.PLAIN, 13));
        area.setEditable(false);
        area.setBackground(new Color(245, 245, 255));
        area.setBorder(new EmptyBorder(20, 25, 10, 25));
        dialog.add(new JScrollPane(area), BorderLayout.CENTER);

        // Buttons row
        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10));
        btnRow.setBackground(new Color(245, 245, 255));

        JButton homeBtn = styledButton("← Back to Home", new Color(70, 130, 220));
        homeBtn.addActionListener(e -> {
            dialog.dispose();
            cardLayout.show(mainPanel, "HOME");
        });

        JButton myTicketsBtn = styledButton("📋 My Tickets", new Color(120, 80, 180));
        myTicketsBtn.addActionListener(e -> {
            dialog.dispose();
            refreshHistoryPanel();
            cardLayout.show(mainPanel, "HISTORY");
        });

        btnRow.add(homeBtn);
        btnRow.add(myTicketsBtn);
        dialog.add(btnRow, BorderLayout.SOUTH);

        dialog.setVisible(true);
    }

    // ===========================================================
    // PANEL 3 – MY TICKETS / BOOKING HISTORY
    // ===========================================================
    private JPanel historyPanel; // keep reference to rebuild table

    private JPanel buildHistoryPanel() {
        historyPanel = new JPanel(new BorderLayout());
        historyPanel.setBackground(new Color(245, 245, 255));
        historyPanel.add(buildNavBar("HISTORY"), BorderLayout.NORTH);
        populateHistoryContent();
        return historyPanel;
    }

    private void refreshHistoryPanel() {
        if (historyPanel == null) return;
        // Remove everything except nav bar (which is NORTH)
        historyPanel.remove(historyPanel.getComponentCount() > 1
            ? historyPanel.getComponent(1) : historyPanel.getComponent(0));
        populateHistoryContent();
        historyPanel.revalidate();
        historyPanel.repaint();
    }

    private void populateHistoryContent() {
        JPanel content = new JPanel(new BorderLayout());
        content.setBackground(new Color(245, 245, 255));
        content.setBorder(new EmptyBorder(15, 20, 15, 20));

        JLabel heading = new JLabel("My Tickets");
        heading.setFont(new Font("Arial", Font.BOLD, 18));
        heading.setForeground(new Color(30, 30, 100));
        content.add(heading, BorderLayout.NORTH);

        if (bookings.isEmpty()) {
            JLabel empty = new JLabel("No bookings yet. Go to Book Ticket to get started!",
                    SwingConstants.CENTER);
            empty.setFont(new Font("Arial", Font.ITALIC, 14));
            empty.setForeground(Color.GRAY);
            content.add(empty, BorderLayout.CENTER);
        } else {
            // Refresh each booking status before displaying
            for (Booking b : bookings) b.refreshStatus();

            // Table columns
            String[] cols = {"Booking ID", "Movie", "Date", "Time", "Seats", "Total", "Status"};
            Object[][] rows = new Object[bookings.size()][7];

            for (int i = 0; i < bookings.size(); i++) {
                Booking b = bookings.get(i);
                rows[i][0] = b.getBookingId();
                rows[i][1] = b.getMovieName();
                rows[i][2] = b.getDate();
                rows[i][3] = b.getTiming();
                rows[i][4] = b.getSeatsAsString();
                rows[i][5] = "₹" + b.getTotalPrice();
                rows[i][6] = b.getStatus();
            }

            // Non-editable table
            DefaultTableModel tableModel = new DefaultTableModel(rows, cols) {
                public boolean isCellEditable(int r, int c) { return false; }
            };
            JTable table = new JTable(tableModel);
            table.setFont(new Font("Arial", Font.PLAIN, 13));
            table.setRowHeight(26);
            table.getTableHeader().setFont(new Font("Arial", Font.BOLD, 13));
            table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

            // Colour rows by status
            table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
                public Component getTableCellRendererComponent(
                        JTable tbl, Object val, boolean sel, boolean foc, int row, int col) {
                    super.getTableCellRendererComponent(tbl, val, sel, foc, row, col);
                    String status = (String) tbl.getValueAt(row, 6);
                    if (!sel) {
                        switch (status) {
                            case "Cancelled":  setBackground(new Color(255, 220, 220)); break;
                            case "Completed":  setBackground(new Color(220, 240, 220)); break;
                            default:           setBackground(new Color(220, 230, 255)); break;
                        }
                    }
                    return this;
                }
            });

            JScrollPane scroll = new JScrollPane(table);
            scroll.setPreferredSize(new Dimension(760, 320));
            content.add(scroll, BorderLayout.CENTER);

            // Action buttons below table
            JPanel actions = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 8));
            actions.setBackground(new Color(245, 245, 255));

            JButton viewBtn = styledButton("👁  View Ticket", new Color(70, 130, 220));
            viewBtn.addActionListener(e -> {
                int row = table.getSelectedRow();
                if (row < 0) {
                    JOptionPane.showMessageDialog(this, "Please click a row first.",
                            "Nothing Selected", JOptionPane.INFORMATION_MESSAGE);
                    return;
                }
                showTicketPopup(bookings.get(row));
            });

            JButton cancelBtn = styledButton("❌  Cancel Ticket", new Color(200, 60, 60));
            cancelBtn.addActionListener(e -> {
                int row = table.getSelectedRow();
                if (row < 0) {
                    JOptionPane.showMessageDialog(this, "Please click a row first.",
                            "Nothing Selected", JOptionPane.INFORMATION_MESSAGE);
                    return;
                }
                Booking b = bookings.get(row);
                if (!b.canCancel()) {
                    JOptionPane.showMessageDialog(this,
                        "This ticket cannot be cancelled.\n"
                        + "(Show already started or ticket already cancelled/completed.)",
                        "Cannot Cancel", JOptionPane.WARNING_MESSAGE);
                    return;
                }
                int confirm = JOptionPane.showConfirmDialog(this,
                    "Cancel booking " + b.getBookingId() + " for " + b.getMovieName() + "?",
                    "Confirm Cancellation", JOptionPane.YES_NO_OPTION);
                if (confirm == JOptionPane.YES_OPTION) {
                    b.setStatus("Cancelled");
                    // Free up the seats so others can book them
                    String showKey = b.getMovieName() + "|" + b.getDate() + "|" + b.getTiming();
                    HashSet<String> booked = bookedSeatsMap.get(showKey);
                    if (booked != null) {
                        for (String s : b.getSeats()) booked.remove(s);
                    }
                    refreshHistoryPanel();
                    JOptionPane.showMessageDialog(this,
                        "Booking " + b.getBookingId() + " cancelled successfully.\nSeats are now available again.",
                        "Cancelled", JOptionPane.INFORMATION_MESSAGE);
                }
            });

            actions.add(viewBtn);
            actions.add(cancelBtn);

            // Legend
            JLabel legend = new JLabel(
                "   🔵 Upcoming    🟢 Completed    🔴 Cancelled");
            legend.setFont(new Font("Arial", Font.ITALIC, 12));
            legend.setForeground(Color.GRAY);
            actions.add(legend);

            content.add(actions, BorderLayout.SOUTH);
        }

        historyPanel.add(content, BorderLayout.CENTER);
    }

    // ===========================================================
    // HELPERS
    // ===========================================================

    // Consistent styled button factory
    private JButton styledButton(String label, Color bg) {
        JButton btn = new JButton(label);
        btn.setBackground(bg);
        btn.setForeground(Color.WHITE);
        btn.setFont(new Font("Arial", Font.BOLD, 13));
        btn.setFocusPainted(false);
        btn.setBorder(new EmptyBorder(8, 16, 8, 16));
        btn.setOpaque(true);
        return btn;
    }

    // Consistent label for form rows
    private JLabel formLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(new Font("Arial", Font.BOLD, 13));
        return lbl;
    }
}
