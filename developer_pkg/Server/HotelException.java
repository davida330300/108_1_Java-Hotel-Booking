
public class HotelException extends Exception {
	HotelException(){
		
	}
	HotelException(HotelException e){
		super(e);
	}
	HotelException(String msg){
		super(msg);
	}
}
