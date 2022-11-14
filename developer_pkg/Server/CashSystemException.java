
public class CashSystemException extends Exception {
	CashSystemException(){
		
	}
	CashSystemException(CashSystemException e){
		super(e);
	}
	CashSystemException(String msg){
		super(msg);
	}
}
