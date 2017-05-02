

public class LamportRequest {

	private final int id;

	private final int clockValue;

	public LamportRequest(int id, int clockValue) {
		this.id = id;
		this.clockValue = clockValue;
	}

	public int getId() {
		return this.id;
	}

	public int getClockValue() {
		return this.clockValue;
	}

	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (!(o instanceof LamportRequest)) {
			return false;
		}

		LamportRequest lr = (LamportRequest) o;

		return this.id == lr.getId()
			&& this.clockValue == lr.getClockValue();
	}

	public String toString() {
		StringBuilder sb = new StringBuilder();

		sb.append("LamportRequest:<");
		sb.append(this.id);
		sb.append(",");
		sb.append(this.clockValue);
		sb.append(">");

		return sb.toString();
	}
}

