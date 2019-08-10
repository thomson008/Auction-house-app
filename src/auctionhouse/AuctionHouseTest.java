/**
 * 
 */
package auctionhouse;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * @author pbj
 *
 */
public class AuctionHouseTest {

    private static final double BUYER_PREMIUM = 10.0;
    private static final double COMMISSION = 15.0;
    private static final Money INCREMENT = new Money("10.00");
    private static final String HOUSE_ACCOUNT = "AH A/C";
    private static final String HOUSE_AUTH_CODE = "AH-auth";

    private AuctionHouse house;
    private MockMessagingService messagingService;
    private MockBankingService bankingService;

    /*
     * Utility methods to help shorten test text.
     */
    private static void assertOK(Status status) { 
        assertEquals(Status.Kind.OK, status.kind);
    }
    private static void assertError(Status status) { 
        assertEquals(Status.Kind.ERROR, status.kind);
    }
    private static void assertSale(Status status) { 
        assertEquals(Status.Kind.SALE, status.kind);
    }
    private static void assertNoSale(Status status) { 
        assertEquals(Status.Kind.NO_SALE, status.kind);
    }
    
    private static void assertSalePending(Status status) { 
        assertEquals(Status.Kind.SALE_PENDING_PAYMENT, status.kind);
    }
    
    /*
     * Logging functionality
     */

    // Convenience field.  Saves on getLogger() calls when logger object needed.
    private static Logger logger;

    // Update this field to limit logging.
    public static Level loggingLevel = Level.ALL;

    private static final String LS = System.lineSeparator();

    @BeforeClass
    public static void setupLogger() {

        logger = Logger.getLogger("auctionhouse"); 
        logger.setLevel(loggingLevel);

        // Ensure the root handler passes on all messages at loggingLevel and above (i.e. more severe)
        Logger rootLogger = Logger.getLogger("");
        Handler handler = rootLogger.getHandlers()[0];
        handler.setLevel(loggingLevel);
    }

    private String makeBanner(String testCaseName) {
        return  LS 
                + "#############################################################" + LS
                + "TESTCASE: " + testCaseName + LS
                + "#############################################################";
    }

    @Before
    public void setup() {
        messagingService = new MockMessagingService();
        bankingService = new MockBankingService();

        house = new AuctionHouseImp(
                    new Parameters(
                        BUYER_PREMIUM,
                        COMMISSION,
                        INCREMENT,
                        HOUSE_ACCOUNT,
                        HOUSE_AUTH_CODE,
                        messagingService,
                        bankingService));

    }
    /*
     * Setup story running through all the test cases.
     * 
     * Story end point is made controllable so that tests can check 
     * story prefixes and branch off in different ways. 
     */
    private void runStory(int endPoint) {
        assertOK(house.registerSeller("SellerY", "@SellerY", "SY A/C"));       
        assertOK(house.registerSeller("SellerZ", "@SellerZ", "SZ A/C")); 
        if (endPoint == 1) return;
        
        assertOK(house.addLot("SellerY", 2, "Painting", new Money("200.00")));
        assertOK(house.addLot("SellerY", 1, "Bicycle", new Money("80.00")));
        assertOK(house.addLot("SellerZ", 5, "Table", new Money("100.00")));
        if (endPoint == 2) return;
        
        assertOK(house.registerBuyer("BuyerA", "@BuyerA", "BA A/C", "BA-auth"));       
        assertOK(house.registerBuyer("BuyerB", "@BuyerB", "BB A/C", "BB-auth"));
        assertOK(house.registerBuyer("BuyerC", "@BuyerC", "BC A/C", "BC-auth"));
        if (endPoint == 3) return;
        
        assertOK(house.noteInterest("BuyerA", 1));
        assertOK(house.noteInterest("BuyerA", 5));
        assertOK(house.noteInterest("BuyerB", 1));
        assertOK(house.noteInterest("BuyerB", 2));
        assertOK(house.noteInterest("BuyerC", 2));
        if (endPoint == 4) return;
        
        assertOK(house.openAuction("Auctioneer1", "@Auctioneer1", 1));
        messagingService.expectAuctionOpened("@BuyerA", 1);
        messagingService.expectAuctionOpened("@BuyerB", 1);
        messagingService.expectAuctionOpened("@SellerY", 1);
        messagingService.verify();
        
        assertOK(house.openAuction("Auctioneer2", "@Auctioneer2", 5));
        messagingService.expectAuctionOpened("@BuyerA", 5);
        messagingService.expectAuctionOpened("@SellerZ", 5);
        messagingService.verify(); 
        
        assertOK(house.openAuction("Auctioneer3", "@Auctioneer3", 2));
        messagingService.expectAuctionOpened("@BuyerB", 2);
        messagingService.expectAuctionOpened("@BuyerC", 2);
        messagingService.expectAuctionOpened("@SellerY", 2);
        messagingService.verify(); 
        
        if (endPoint == 5) return;
        
        Money m70 = new Money("70.00");
        assertOK(house.makeBid("BuyerA", 1, m70));
        messagingService.expectBidReceived("@BuyerB", 1, m70);
        messagingService.expectBidReceived("@Auctioneer1", 1, m70);
        messagingService.expectBidReceived("@SellerY", 1, m70);
        messagingService.verify();
        
        assertOK(house.makeBid("BuyerA", 5, m70));
        messagingService.expectBidReceived("@Auctioneer2", 5, m70);
        messagingService.expectBidReceived("@SellerZ", 5, m70);
        messagingService.verify();
        
        Money m300 = new Money("300");
        assertOK(house.makeBid("BuyerC", 2, m300));
        messagingService.expectBidReceived("@BuyerB", 2, m300);
        messagingService.expectBidReceived("@Auctioneer3", 2, m300);
        messagingService.expectBidReceived("@SellerY", 2, m300);
        messagingService.verify();
        if (endPoint == 6) return;
        
        Money m100 = new Money("100.00");
        assertOK(house.makeBid("BuyerB", 1, m100));

        messagingService.expectBidReceived("@BuyerA", 1, m100);
        messagingService.expectBidReceived("@Auctioneer1", 1, m100);
        messagingService.expectBidReceived("@SellerY", 1, m100);
        messagingService.verify();
        if (endPoint == 7) return;
        
        
        assertSale(house.closeAuction("Auctioneer1",  1));
        messagingService.expectLotSold("@BuyerA", 1);
        messagingService.expectLotSold("@BuyerB", 1);
        messagingService.expectLotSold("@SellerY", 1);
        messagingService.verify();   

        bankingService.expectTransfer("BB A/C",  "BB-auth",  "AH A/C", new Money("110.00"));
        bankingService.expectTransfer("AH A/C",  "AH-auth",  "SY A/C", new Money("85.00"));
        bankingService.verify();
        
        if (endPoint == 8) return;
        
        assertNoSale(house.closeAuction("Auctioneer2", 5));
        messagingService.expectLotUnsold("@BuyerA", 5);
        messagingService.expectLotUnsold("@SellerZ", 5);
        messagingService.verify();
        
        if (endPoint == 9) return;
        
        assertSalePending(house.closeAuction("Auctioneer3", 2));
        return;
    }
    
    @Test
    public void testEmptyCatalogue() {
        logger.info(makeBanner("emptyLotStore"));

        List<CatalogueEntry> expectedCatalogue = new ArrayList<CatalogueEntry>();
        List<CatalogueEntry> actualCatalogue = house.viewCatalogue();

        assertEquals(expectedCatalogue, actualCatalogue);

    }

    @Test
    public void testRegisterSeller() {
        logger.info(makeBanner("testRegisterSeller"));
        runStory(1);
    }

    @Test
    public void testRegisterSellerDuplicateNames() {
        logger.info(makeBanner("testRegisterSellerDuplicateNames"));
        runStory(1);     
        assertError(house.registerSeller("SellerY", "@SellerZ", "SZ A/C"));       
    }

    @Test
    public void testAddLot() {
        logger.info(makeBanner("testAddLot"));
        runStory(2);
    }
    
    @Test
    public void testAddLotUnknownSeller() {
        logger.info(makeBanner("testAddLotUnknownSeller"));
        runStory(2);
        assertError(house.addLot("SellerA", 3, "Car", new Money("3000.00")));      //try adding a lot without being registered
    }
    
    @Test
    public void testViewCatalogue() {
        logger.info(makeBanner("testViewCatalogue"));
        runStory(2);
        
        List<CatalogueEntry> expectedCatalogue = new ArrayList<CatalogueEntry>();
        expectedCatalogue.add(new CatalogueEntry(1, "Bicycle", LotStatus.UNSOLD)); 
        expectedCatalogue.add(new CatalogueEntry(2, "Painting", LotStatus.UNSOLD));
        expectedCatalogue.add(new CatalogueEntry(5, "Table", LotStatus.UNSOLD));

        List<CatalogueEntry> actualCatalogue = house.viewCatalogue();

        assertEquals(expectedCatalogue, actualCatalogue);
    }

    @Test
    public void testRegisterBuyer() {
        logger.info(makeBanner("testRegisterBuyer"));
        runStory(3);       
    }
    
    @Test
    public void testRegisterBuyerDuplicateNames() {
        logger.info(makeBanner("testRegisterBuyerDuplicateNames"));
        runStory(3);     
        assertError(house.registerBuyer("BuyerB", "@BuyerB", "bank", "auth"));       
    }


    @Test
    public void testNoteInterest() {
        logger.info(makeBanner("testNoteInterest"));
        runStory(4);
    }
    
    @Test
    public void testNoteInterestUnknownLot() {
        logger.info(makeBanner("testNoteInterestUnknownLot"));
        runStory(4);
        assertError(house.noteInterest("BuyerA", 4));                              //try noting interest in a lot that hasn't been registered
    }
      
    @Test
    public void testOpenAuction() {
        logger.info(makeBanner("testOpenAuction"));
        runStory(5);       
    }
    
    @Test
    public void testOpenAuctionUnknownLot() {
        logger.info(makeBanner("testOpenAuctionUnknownLot"));
        runStory(5);       
        assertError(house.openAuction("Auctioneer2", "@Auctioneer2", 69));         //try opening auction of a non-existent lot
    }
      
      
    @Test
    public void testMakeBid() {
        logger.info(makeBanner("testMakeBid"));
        runStory(7);
        assertError(house.makeBid("BuyerC", 1, new Money("500")));                 //try making a bid in a lot you are not interested in
    }
    
    @Test
    public void testMakeBidTooLowOrJustEnough() {                                  //try making a bid that is below or just equal to current bid + minimum increment
        logger.info(makeBanner("testMakeBidTooLowOrJustEnough"));
        runStory(7);
        Money m100 = new Money("100");
        Money m109 = new Money("109");
        Money m110 = new Money("110");
        assertError(house.makeBid("BuyerB", 1, m100));
        assertError(house.makeBid("BuyerB", 1, m109));
        assertOK(house.makeBid("BuyerA", 1, m110));
        messagingService.expectBidReceived("@BuyerA", 1, m110);
    }
    
    
  
    @Test
    public void testCloseAuctionWithSale() {
        logger.info(makeBanner("testCloseAuctionWithSale"));
        runStory(8);
    }
    
    @Test
    public void testCloseAuctionNoSale() {                                         //closing the auction when the reserve price was not met
        logger.info(makeBanner("testCloseAuctionNoSale"));
        runStory(9);
    }
    
    @Test
    public void testCloseAuctionPending() {                                        //closing the auction but failing to transfer money because of a bad account
        logger.info(makeBanner("testCloseAuctionPending"));
        bankingService.setBadAccount("BC A/C");
        runStory(10);
    }
    
    @Test
    public void testCatalogueAfterSale() {                                         //viewing the catalogue after auctions to see if the statuses of catalogue entries have been updated
    	logger.info(makeBanner("testCatalogueAfterSale"));
    	bankingService.setBadAccount("BC A/C");
    	runStory(11);
    	List<CatalogueEntry> expectedCatalogue = new ArrayList<CatalogueEntry>();
        expectedCatalogue.add(new CatalogueEntry(1, "Bicycle", LotStatus.SOLD)); 
        expectedCatalogue.add(new CatalogueEntry(2, "Painting", LotStatus.SOLD_PENDING_PAYMENT));
        expectedCatalogue.add(new CatalogueEntry(5, "Table", LotStatus.UNSOLD));

        List<CatalogueEntry> actualCatalogue = house.viewCatalogue();

        assertEquals(expectedCatalogue, actualCatalogue);
      
    }
}
