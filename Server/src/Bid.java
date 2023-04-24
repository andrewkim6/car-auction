import java.io.Serializable;

public class Bid implements Serializable{
    private double amount;
    private String bidder;
    private String model;
    private String brand;
    private String type;
    private Boolean highBidder;
    public Bid(double amount, String bidder, String item, String brand, String type, boolean highBidder) {
        this.amount = amount;
        this.bidder = bidder;
        this.model = item;
        this.brand = brand;
        this.type = type;
        this.highBidder = highBidder;
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

    public boolean getHighbidder(){
        return highBidder;
    }
}
