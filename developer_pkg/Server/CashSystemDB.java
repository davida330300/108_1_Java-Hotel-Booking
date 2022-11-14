

public interface CashSystemDB {
	
	/**
	 * Check is the bank in the "Bank Table".
	 * @param bankName banks name
	 * @return if the bankID in the "Bank Table"
	 */
	public boolean isBankExist(String bankName);
	
	/**
	 * Check if the the credit card is in the "Credit_Card" table.
	 * @param cardID input credit card id or hotel id
	 * @return if the name id exist
	 */
	public boolean isCardExist(String cardID);

	/**
	 * Check if the the client or hotel is in the "Credit_Card" table.
	 * @param nameID input credit card id or hotel id
	 * @return if the name id exist
	 */
	public boolean isClientExist(String clientID);

	public boolean checkCreditInfo(String cardNumber, String expirationDate, int secureCode, String name);
		
	public void pay(String bookID, int bankID, String creditcardID, String expirationDate, String securityCode, int amount, int hotelID) throws CashSystemException;

	public String getBankName(int bankID) throws CashSystemException;

}
