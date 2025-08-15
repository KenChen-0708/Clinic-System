package entity;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class Medicine implements Serializable {

    private String medId;
    private String name;
    private int stock;
    private double price;
    private LocalDate expiryDate;

    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MMM-yyyy");

    public Medicine(String medId, String name, int stock, double price, LocalDate expiryDate) {
        this.medId = medId;
        this.name = name;
        this.stock = stock;
        this.price = price;
        this.expiryDate = expiryDate;
    }

    public String getMedId() {
        return medId;
    }

    public String getName() {
        return name;
    }

    public int getStock() {
        return stock;
    }

    public double getPrice() {
        return price;
    }

    public LocalDate getExpiryDate() {
        return expiryDate;
    }

    public void setStock(int stock) {
        this.stock = stock;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public void setExpiryDate(LocalDate expiryDate) {
        this.expiryDate = expiryDate;
    }

    @Override
    public String toString() {
        return String.format(
                "Medicine ID  : %-5s\nName         : %s\nQuantity     : %d\nPrice/unit   : RM %.2f\nExpiry Date  : %s\n",
                medId, name, stock, price, expiryDate.format(formatter)
        );
    }
}
