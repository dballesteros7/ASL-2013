package org.ftab.client.shell;

/**
 * Thrown to signify that the user cancelled the operation.
 * @author Jean-Pierre Smith
 */
@SuppressWarnings("serial")
class CancelOperationException extends Exception {

	CancelOperationException() { }

	CancelOperationException(String arg0) {
		super(arg0);
	}

	CancelOperationException(Throwable arg0) {
		super(arg0);
	}

	CancelOperationException(String arg0, Throwable arg1) {
		super(arg0, arg1);
	}
}
