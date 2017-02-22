package hk.collaction.freehkkai;

public enum Config {

	VERSION(false);

	private final boolean isBeta;

	Config(boolean isBeta) {
		this.isBeta = isBeta;
	}

	public boolean isBeta() {
		return isBeta;
	}
}
