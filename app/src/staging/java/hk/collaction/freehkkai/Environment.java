package hk.collaction.freehkkai;

public enum Environment {

	CONFIG(false, true);

	private final boolean debug;
	private final boolean beta;

	Environment(boolean debug, boolean beta) {
		this.debug = debug;
		this.beta = beta;
	}

	public boolean isBeta() {
		return beta;
	}

	public boolean isDebug() {
		return debug;
	}
}
