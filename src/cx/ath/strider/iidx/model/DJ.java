package cx.ath.strider.iidx.model;

import java.io.Serializable;

public class DJ implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = -4947698014437899115L;
	public int DJID;
	public String Name;
	public String Password;
	public String Info;
	
	@Override
	public String toString() {
		return Name;
	}
}
