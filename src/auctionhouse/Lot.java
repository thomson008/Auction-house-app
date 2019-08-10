package auctionhouse;
import java.util.ArrayList;

public class Lot {
	
	public String sellerName;
    public int number;
    public String description;
    public Money reservePrice;
    public ArrayList<String> interestedBuyers = new ArrayList<>();
    public String auctioneerName;
    public Money currentHighestBid;
    public String currentHighestBidder;
    
    public Lot(String sellerName, int number, String description, Money reservePrice) {
    	this.sellerName = sellerName;
    	this.number = number;
    	this.description = description;
    	this.reservePrice = reservePrice;
    	currentHighestBid = new Money("0");
    }
    
}
