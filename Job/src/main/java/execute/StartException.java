package execute;

public class StartException extends RuntimeException{

	/**
	 * 
	 */
	private static final long serialVersionUID = 6418945313165980434L;
	public StartException(String msg) {
        super(msg);
    }
    public StartException(String msg, Exception e) {
        super(msg, e);
    }
}
