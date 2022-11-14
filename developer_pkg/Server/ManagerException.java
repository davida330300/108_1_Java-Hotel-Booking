
public class ManagerException extends Exception {
	ManagerException(){
		
	}
	ManagerException(ManagerException e){
		super(e);
	}
	ManagerException(String msg){
		super(msg);
	}
}
