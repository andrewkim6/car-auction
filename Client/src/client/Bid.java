package client;

public class Bid {
    private double amount;
    private String bidder;
    private String model;
    private String brand;
    private String type;
    public Bid(double amount, String bidder, String item, String brand, String type) {
        this.amount = amount;
        this.bidder = bidder;
        this.model = item;
        this.brand = brand;
        this.type = type;
    }
    
    public double getAmount() {
        return amount;
    }
    
    public String getBidder() {
        return bidder;
    }

    public String getBrand(){
        return brand;
    }

    public String getType(){
        return type;
    }

    public String getModel(){
        return model;
    }
}
