package hk.collaction.freehkkai;

public enum Config {

	VERSION(true);

	private final boolean isBeta;

	Config(boolean isBeta) {
		this.isBeta = isBeta;
	}


	public boolean isBeta() {
		return isBeta;
	}
}
