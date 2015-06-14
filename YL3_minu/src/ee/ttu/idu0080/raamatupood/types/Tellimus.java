package ee.ttu.idu0080.raamatupood.types;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class Tellimus implements Serializable {
	private static final long serialVersionUID = 1L;
	List<TellimuseRida> tellimuseRead;

	public Tellimus() {
		tellimuseRead = new ArrayList<TellimuseRida>();
	}

	public void addTellimuseRida(TellimuseRida tellimuseRida) {
		this.tellimuseRead.add(tellimuseRida);
	}

	public List<TellimuseRida> getTellimuseRead() {
		return tellimuseRead;
	}

	public String toString() {
		String str = null;
		for (int i = 0; i < tellimuseRead.size(); i++) {
			Toode toode = tellimuseRead.get(i).getToode();
			long kogus = tellimuseRead.get(i).getKogus();
			String nimetus = toode.getNimetus();
			int kood = toode.getKood();
			BigDecimal hind = toode.getHind();

			str += "\n Toode: " + nimetus + "\n Kogus: " + kogus + "\n id: "
					+ kood + "\n hind: " + hind;
		}
		return str;
	}
}
