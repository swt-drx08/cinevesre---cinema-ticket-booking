// ============================================================
// Movie.java
// Represents one movie with its name and ticket price.
// OOP: Class, Constructor, Methods, Encapsulation
// ============================================================

public class Movie {

    private String name;   // Movie title
    private int price;     // Price per ticket in rupees

    // Constructor – runs when we do: new Movie("Avengers", 200)
    public Movie(String name, int price) {
        this.name  = name;
        this.price = price;
    }

    // Getter for name
    public String getName() {
        return name;
    }

    // Getter for price
    public int getPrice() {
        return price;
    }

    // Used by JComboBox to show "Avengers — ₹200"
    @Override
    public String toString() {
        return name + "  —  ₹" + price + " per ticket";
    }
}
