package fr.pederobien.mumble.server.exceptions;

public class MumbleServerTypeDismatchException extends RuntimeException {
	private static final long serialVersionUID = 1L;
	private String expected, actual;

	public MumbleServerTypeDismatchException(String expected, String actual) {
		super(String.format("The server type found in the configuration (%s) file does not correspond to the running server type (%s)", actual, expected));
		this.expected = expected;
		this.actual = actual;
	}

	/**
	 * @return The expected server type.
	 */
	public String getExpected() {
		return expected;
	}

	/**
	 * @return The actual server type.
	 */
	public String getActual() {
		return actual;
	}
}
