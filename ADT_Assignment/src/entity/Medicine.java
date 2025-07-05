/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package entity;

public class Medicine {
    private String medId;
    private String name;
    private int stock;

    public Medicine(String medId, String name, int stock) {
        this.medId = medId;
        this.name = name;
        this.stock = stock;
    }

    public String getMedId() { return medId; }
    public String getName() { return name; }
    public int getStock() { return stock; }

    public void setStock(int stock) { this.stock = stock; }

    public String toString() {
        return medId + "," + name + "," + stock;
    }

    public static Medicine fromString(String line) {
        String[] parts = line.split(",");
        return new Medicine(parts[0], parts[1], Integer.parseInt(parts[2]));
    }

    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Medicine)) return false;
        Medicine m = (Medicine) o;
        return medId.equals(m.medId);
    }
}
