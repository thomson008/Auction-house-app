package auctionhouse;

public class Buyer {
	
    public String address;
    public String bankAccount;
    public String bankAuthCode;
    
    public Buyer(String address, String bankAccount, String bankAuthCode) {
    	this.address = address;
    	this.bankAccount = bankAccount;
    	this.bankAuthCode = bankAuthCode;
    }

}
