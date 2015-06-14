package ee.ttu.idu0080.raamatupood.types;

import java.io.Serializable;

public class TellimuseRida implements Serializable {
	private static final long serialVersionUID = 5231541610713209017L;
	Toode toode = null;
	long kogus;

	public TellimuseRida(Toode toode, long kogus) {
		this.toode = toode;
		this.kogus = kogus;
	}

	public Toode getToode() {
		return this.toode;
	}

	public long getKogus() {
		return this.kogus;
	}
}
