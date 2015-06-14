package ee.ttu.idu0080.raamatupood.types;

import java.io.Serializable;

public class Car implements Serializable {
	private static final long serialVersionUID = 1L;
	public int doors;

	public Car(int doors) {
		this.doors = doors;
	}

	public String toString() {
		return "car has " + doors + " doors";
	}
}
