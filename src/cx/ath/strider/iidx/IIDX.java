package cx.ath.strider.iidx;

import cx.ath.strider.iidx.model.IIDXModel;

/*
 * Global application objects
 */
public class IIDX {
	private IIDX() { }
	
	public static final String DATABASE_NAME = "iidxData.db";
	public static IIDXModel model;
	public static GeoScore geoScore;
}
