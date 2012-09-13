package net.blogracy.errors;

public class PhotoAlbumDuplicated extends BlogracyError {

	public PhotoAlbumDuplicated() {
	}

	public PhotoAlbumDuplicated(String s) {
		super(s);
	}

	public PhotoAlbumDuplicated(String s, Throwable throwable) {
		super(s, throwable);
	}

	public PhotoAlbumDuplicated(Throwable throwable) {
		super(throwable);
	}

}
