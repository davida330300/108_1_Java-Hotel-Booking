public class Query implements java.io.Serializable, Cloneable {
	private static final long serialVersionUID = 1L;
	
	public QueryType type;
	public UserInfo userInfo;
	public OrderInfo orderInfo;
	public CreditCardInfo creditCardInfo;
	public String additionalInfo;
	/********* additionalInfo ********
	 * LIST_HOTEL: star condition
	 * CANCEL and PAY: bookID
	 *********************************/
	
	public Query(QueryType queryType) {
		switch (queryType) {
			case SIGN_UP: case LOG_IN:
				userInfo = new UserInfo();
				break;
			case LIST_HOTEL: case BOOK: case MODIFY:
				orderInfo = new OrderInfo();
				break;
			case PAY:
				creditCardInfo = new CreditCardInfo();
				break;
			default:
				break;
		}
	}
	
	public Query clone() {
		try {
			Query query = (Query)super.clone();
			query.userInfo = userInfo.clone();
			query.orderInfo = orderInfo.clone();
			query.creditCardInfo = creditCardInfo.clone();
			return query;
		} catch (CloneNotSupportedException e) {
			return null;
		}
	}
	
	public boolean quit() {
		return type == QueryType.QUIT;
	}
}
