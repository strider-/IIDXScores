package cx.ath.strider.iidx;

public class Style {
	public int StyleID;
	public int StyleOrder;
	public String StyleName;
	public String Theme;
	public int ParentID = -1;
	
	@Override
	public String toString(){
		return this.StyleName.replace("beatmaniaIIDX ", "");
	}
}
