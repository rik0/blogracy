package net.blogracy.errors;

public class BlogracyItemNotFound extends BlogracyError {

	public BlogracyItemNotFound() {
	}

	public BlogracyItemNotFound(String s) {
		super(s);
	}

	public BlogracyItemNotFound(String s, Throwable throwable) {
		super(s, throwable);
	}

	public BlogracyItemNotFound(Throwable throwable) {
		super(throwable);
	}
}
