/**
 * 
 */
package auctionhouse;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import javax.tools.Diagnostic.Kind;
import java.util.Map;
import java.util.HashMap;
import java.util.Arrays;
/**
 * @author pbj
 *
 */
public class AuctionHouseImp implements AuctionHouse {

    private static Logger logger = Logger.getLogger("auctionhouse");
    private static final String LS = System.lineSeparator();
    private Map<String, Buyer> buyerMap = new HashMap<>();
    private Map<String, Seller> sellerMap = new HashMap<>();
    private Map<String, Auctioneer> auctioneerMap = new HashMap<>();
    private Map<Integer, Lot> lotMap = new HashMap<>();
    private Map<Integer, CatalogueEntry> entryMap = new HashMap<>();
    private Parameters parameters;
    
    
    private String startBanner(String messageName) {
        return  LS 
          + "-------------------------------------------------------------" + LS
          + "MESSAGE IN: " + messageName + LS
          + "-------------------------------------------------------------";
    }
   
    public AuctionHouseImp(Parameters parameters) {
    	this.parameters = parameters;
    }

    public Status registerBuyer(
            String name,
            String address,
            String bankAccount,
            String bankAuthCode) {
    	logger.fine(startBanner("registerBuyer " + name));
    	Buyer buyer = new Buyer(address, bankAccount, bankAuthCode);
    	
    	logger.fine("Checking if the name is already in database...");
    	if (buyerMap.keySet().contains(name)) {
    		return Status.error("Username already registered.");
    	}
    	
    	logger.fine("Registering new buyer...");
    	buyerMap.put(name, buyer);
        return Status.OK();
    }

    public Status registerSeller(
            String name,
            String address,
            String bankAccount) {
    	logger.fine(startBanner("registerSeller " + name));
    	Seller seller = new Seller(address, bankAccount);
    	
    	logger.fine("Checking if the name is already in database...");
    	if (sellerMap.keySet().contains(name)) {
    		return Status.error("Username already registered");
    	}
    	
    	logger.fine("Registering new seller...");
    	sellerMap.put(name, seller);
        return Status.OK();      
    }

    public Status addLot(
            String sellerName,
            int number,
            String description,
            Money reservePrice) {
    	logger.fine(startBanner("addLot " + sellerName + " " + number));
    	Lot lot = new Lot(sellerName, number, description, reservePrice);
    	
    	logger.fine("Checking if the seller is registered in the database...");
    	if (!sellerMap.keySet().contains(sellerName)) {
    		return Status.error("Unregistered seller");
    	}
    	
    	logger.fine("Seller authorised. Adding new lot...");
    	lotMap.put(number, lot);
    	
    	CatalogueEntry catEntry = new CatalogueEntry(number, description, LotStatus.UNSOLD);
    	entryMap.put(number, catEntry);
        return Status.OK();    
    }

    public List<CatalogueEntry> viewCatalogue() {
        logger.fine(startBanner("viewCatalogue"));
        List<CatalogueEntry> catalogue = new ArrayList<CatalogueEntry>();
        
        int[] keys = new int[entryMap.keySet().size()];
        int i = 0;
        
        for(Integer key : entryMap.keySet()) {
        	keys[i] = key;
        	i++;
        }
        
        Arrays.sort(keys);
        
        for (int key : keys) {
        	catalogue.add(entryMap.get(key));
        }
       
        logger.fine("Catalogue: " + catalogue.toString());
        return catalogue;
    }

    public Status noteInterest(
            String buyerName,
            int lotNumber) {
    	logger.fine(startBanner("noteInterest " + buyerName + " " + lotNumber));
    	logger.fine("Checking if lot exists...");
    	
    	
    	if (lotMap.keySet().contains(lotNumber)) {
    		logger.fine("Lot found, noting interest...");
   			lotMap.get(lotNumber).interestedBuyers.add(buyerName); 
   			return Status.OK();
    		}
    	
   
        return Status.error("Lot not found.");   
    }

    public Status openAuction(
            String auctioneerName,
            String auctioneerAddress,
            int lotNumber) {
    	logger.fine(startBanner("openAuction " + auctioneerName + " " + lotNumber));
    	
    	Auctioneer auctioneer = new Auctioneer(auctioneerAddress);
    	
    	if (!auctioneerMap.keySet().contains(auctioneerName)) {
    		auctioneerMap.put(auctioneerName, auctioneer);
    	}
    	
    	boolean checkLot = false;
    	
    	logger.fine("Looking for lot in database...");
    	
    	if (entryMap.keySet().contains(lotNumber) && entryMap.get(lotNumber).status == LotStatus.UNSOLD) {
    		entryMap.get(lotNumber).status = LotStatus.IN_AUCTION;
    		checkLot = true;
    		logger.fine("Lot found, auction in progress.");
    	}
    	
    	
    	if (!checkLot) return Status.error("Lot not found");
    	
    	Lot auctionLot = lotMap.get(lotNumber);
    	
    	logger.fine("Sending messages:");
    	
    	String sellerName = auctionLot.sellerName;
    	
        parameters.messagingService.auctionOpened(sellerMap.get(sellerName).address, lotNumber);
    			
    	auctionLot.auctioneerName = auctioneerName;
    	for (String name : auctionLot.interestedBuyers) {
    		String address = buyerMap.get(name).address;
    		parameters.messagingService.auctionOpened(address, lotNumber);
    	}
    			
    	return Status.OK();
    	
    }

    public Status makeBid(
            String buyerName,
            int lotNumber,
            Money bid) {
        logger.fine(startBanner("makeBid " + buyerName + " " + lotNumber + " " + bid));
        
        Lot auctionLot = lotMap.get(lotNumber);
        Money currentBid = auctionLot.currentHighestBid;
        String auctioneerName = auctionLot.auctioneerName;
        String sellerName = auctionLot.sellerName;
        
        
        logger.fine("Checking if the buyer is interested in a lot...");
        
        if (!auctionLot.interestedBuyers.contains(buyerName)) {
        	return Status.error("You can't bid on a lot you haven't noted interest in");
        }
        
        logger.fine("Checking if the bid is above the minimum increment...");
        
        if (bid.lessEqual(currentBid.add(parameters.increment))) {
        	if (bid.compareTo(currentBid.add(parameters.increment)) != 0) {
        		return Status.error("Bid is not above the minimum increment.");
        	}
        }
        
        logger.fine("Sending messages:");
        
        auctionLot.number = lotNumber;
        auctionLot.currentHighestBid = bid;
        auctionLot.currentHighestBidder = buyerName;
        
    	String auctioneerAddress = auctioneerMap.get(auctioneerName).auctioneerAddress;   
        parameters.messagingService.bidAccepted(auctioneerAddress, lotNumber, bid);
        
        String sellerAddress = sellerMap.get(sellerName).address;
        parameters.messagingService.bidAccepted(sellerAddress, lotNumber, bid);
        
        for (String buyer : auctionLot.interestedBuyers) {
        	String address = buyerMap.get(buyer).address;
        	if (!buyer.equals(buyerName)) {
        		parameters.messagingService.bidAccepted(address, lotNumber, bid);
        	}
        }
        
        return Status.OK();    
    }

    public Status closeAuction(
            String auctioneerName,
            int lotNumber) {
        logger.fine(startBanner("closeAuction " + auctioneerName + " " + lotNumber));
        
        logger.fine("Find lot, and retrieve information from said lot.");
        Lot closingLot = lotMap.get(lotNumber);   
        CatalogueEntry c = entryMap.get(lotNumber);
        String sellerName = closingLot.sellerName;
        Money currentBid = closingLot.currentHighestBid;
        String openerName = closingLot.auctioneerName;
        Money reservePrice = closingLot.reservePrice;
        String highestBidder = closingLot.currentHighestBidder;
        
        logger.fine("Getting addresses of interested buyers of lot...");
        ArrayList<String>intBuyersAddress = new ArrayList<>();
        for (String name : closingLot.interestedBuyers) {
        	intBuyersAddress.add(buyerMap.get(name).address);
        }
        String winnerAddress = buyerMap.get(highestBidder).address;
        
        logger.fine("Getting address of the seller...");
        String sellerAddress = sellerMap.get(sellerName).address;
       
        logger.fine("Checking if auctioneer is authorized to close it...");
        if (!openerName.equals(auctioneerName)) {
        	return Status.error("You are not authorised to close this auction.");
        }
        logger.fine("Auctioneer is authorized.");
        
        logger.fine("Retrieving winner's banking details...");
        String account = buyerMap.get(highestBidder).bankAccount;
        String authCode = buyerMap.get(highestBidder).bankAuthCode;
        
        logger.fine("Retrieving seller's banking details...");
        String sellerAccount = sellerMap.get(sellerName).bankAccount;
        
        
        logger.fine("Checking if reserve price was met...");
        if (currentBid.compareTo(reservePrice) < 0) {
            logger.fine("Update catalogue entry to unsold.");
            c.status = LotStatus.UNSOLD;
            parameters.messagingService.lotUnsold(sellerAddress, lotNumber);
            for (String address : intBuyersAddress) {
              parameters.messagingService.lotUnsold(address, lotNumber);
            }
        	return new Status(Status.Kind.NO_SALE, "Reserve price not met.");
        }
        logger.fine("Reserve price met. Executing transactions...");
        
    
        String houseAccount = parameters.houseBankAccount;
        String houseAuthCode = parameters.houseBankAuthCode;
        logger.fine("Getting money from buyer...");
        Status buyerToHouse = parameters.bankingService.transfer(account,
        		authCode,
        		houseAccount,
        		currentBid.addPercent(parameters.buyerPremium));
        
        Status.Kind bTH = buyerToHouse.kind;
        logger.fine("Calculating seller cut...");
        Money sellerCut = currentBid.addPercent(100 - parameters.commission).subtract(currentBid);
        
        Status houseToSeller = parameters.bankingService.transfer(houseAccount,
        		houseAuthCode,
        		sellerAccount,
        		sellerCut);
        
        Status.Kind hTS = houseToSeller.kind;
        logger.fine("Checking if both transactions were successful...");
        if (hTS == Status.Kind.OK && bTH == Status.Kind.OK	) {
        	logger.fine("Update catalogue entry to sold");
        	c.status = LotStatus.SOLD;
        	parameters.messagingService.lotSold(winnerAddress, lotNumber);
			parameters.messagingService.lotSold(sellerAddress, lotNumber);
			for (String address : intBuyersAddress) {
				parameters.messagingService.lotSold(address, lotNumber);
			}
			
			return new Status(Status.Kind.SALE, "Transaction successful.");
        }
        
        logger.fine("Update catalogue entry to sold pending payment.");
        c.status = LotStatus.SOLD_PENDING_PAYMENT;
		return new Status(Status.Kind.SALE_PENDING_PAYMENT, "Transaction unsuccessful.");
       
        
    }
}
