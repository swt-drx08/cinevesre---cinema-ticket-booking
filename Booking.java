// ============================================================
// Booking.java
// Stores all details of one ticket booking.
// OOP: Class, Constructor, Methods, Encapsulation
// ============================================================

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class Booking {

    // ---- Fields ----
    private String   bookingId;      // Unique ID like BK001
    private String   movieName;      // Which movie
    private int      pricePerTicket; // Price of one ticket
    private String   date;           // Show date  e.g. "2026-11-01"
    private String   timing;         // Show time  e.g. "06:00 PM"
    private String[] seats;          // Selected seat numbers e.g. ["A1","A2"]
    private int      totalPrice;     // Total cost
    private String   status;         // "Upcoming", "Completed", or "Cancelled"

    // Constructor – called when user confirms booking
    public Booking(String bookingId, String movieName, int pricePerTicket,
                   String date, String timing, String[] seats) {
        this.bookingId      = bookingId;
        this.movieName      = movieName;
        this.pricePerTicket = pricePerTicket;
        this.date           = date;
        this.timing         = timing;
        this.seats          = seats;
        this.totalPrice     = pricePerTicket * seats.length;
        this.status         = "Upcoming"; // default status
    }

    // ---- Getters ----
    public String   getBookingId()      { return bookingId; }
    public String   getMovieName()      { return movieName; }
    public int      getPricePerTicket() { return pricePerTicket; }
    public String   getDate()           { return date; }
    public String   getTiming()         { return timing; }
    public String[] getSeats()          { return seats; }
    public int      getTotalPrice()     { return totalPrice; }
    public String   getStatus()         { return status; }

    // Allow status to be changed (for cancel)
    public void setStatus(String status) {
        this.status = status;
    }

    // ---- Calculate current status based on show date & time ----
    // Compares today's date and current time against the booked show.
    public void refreshStatus() {
        if (status.equals("Cancelled")) return; // don't override cancel

        try {
            LocalDate  showDate = LocalDate.parse(date);
            LocalDate  today    = LocalDate.now();

            // Parse timing like "06:00 PM" → 24-hour LocalTime
            DateTimeFormatter fmt = DateTimeFormatter.ofPattern("hh:mm a");
            LocalTime  showTime   = LocalTime.parse(timing, fmt);
            LocalTime  nowTime    = LocalTime.now();

            if (showDate.isBefore(today)) {
                status = "Completed";
            } else if (showDate.isEqual(today) && showTime.isBefore(nowTime)) {
                status = "Completed";
            } else {
                status = "Upcoming";
            }
        } catch (Exception e) {
            // If parsing fails, keep existing status
        }
    }

    // ---- Check if cancellation is still allowed ----
    public boolean canCancel() {
        if (status.equals("Cancelled") || status.equals("Completed")) return false;
        try {
            LocalDate  showDate = LocalDate.parse(date);
            LocalDate  today    = LocalDate.now();
            DateTimeFormatter fmt = DateTimeFormatter.ofPattern("hh:mm a");
            LocalTime  showTime   = LocalTime.parse(timing, fmt);
            LocalTime  nowTime    = LocalTime.now();
            // Can cancel only if show hasn't started yet
            if (showDate.isAfter(today)) return true;
            if (showDate.isEqual(today) && showTime.isAfter(nowTime)) return true;
        } catch (Exception e) { /* ignore */ }
        return false;
    }

    // ---- Seats as a single display string ----
    public String getSeatsAsString() {
        return String.join(", ", seats);
    }

    // ---- Ticket receipt text (shown in popup) ----
    public String getTicketText() {
        return "╔══════════════════════════════╗\n"
             + "║   🎬  MOVIE TICKET            ║\n"
             + "╠══════════════════════════════╣\n"
             + "  Booking ID  : " + bookingId      + "\n"
             + "  Movie       : " + movieName      + "\n"
             + "  Date        : " + date           + "\n"
             + "  Timing      : " + timing         + "\n"
             + "  Seats       : " + getSeatsAsString() + "\n"
             + "  Tickets     : " + seats.length   + "\n"
             + "  Price/Ticket: ₹" + pricePerTicket + "\n"
             + "  Total       : ₹" + totalPrice    + "\n"
             + "  Status      : " + status         + "\n"
             + "╚══════════════════════════════╝";
    }
}
