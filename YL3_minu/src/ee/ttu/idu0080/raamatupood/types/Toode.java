package ee.ttu.idu0080.raamatupood.types;

import java.io.Serializable;
import java.math.BigDecimal;

public class Toode implements Serializable{
	private static final long serialVersionUID = 2L;
	private int kood;
	  private String nimetus;
	  private BigDecimal hind;
	  
	  public Toode(int kood, String nimetus,BigDecimal hind) {
			this.kood = kood;
			this.nimetus=nimetus;
			this.hind=hind;
		}
	  public void setKood(int kood)
	  {
	    this.kood = kood;
	  }
	  
	  public void setNimetus(String nimetus)
	  {
	    this.nimetus = nimetus;
	  }
	  
	  public void setHind(BigDecimal hind)
	  {
	    this.hind = hind;
	  }

	  public int getKood()
	  {
	    return this.kood;
	  }
	  
	  public String getNimetus()
	  {
	    return this.nimetus;
	  }
	  
	  public BigDecimal getHind()
	  {
	    return this.hind;
	  }
	  
	  @Override
	  public String toString() {
	       return ("Toote kood:"+this.getKood()+
	                   ", Nimetus: "+ this.getNimetus() +
	                   ", Hind: "+ this.getHind());
	  }
	}
