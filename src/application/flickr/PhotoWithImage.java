package application.flickr;

import java.awt.Image;

import com.flickr4java.flickr.photos.Photo;

public class PhotoWithImage {

	private Photo photo;
	private Image image;

	public PhotoWithImage(Photo photo, Image image) {
		this.photo = photo;
		this.image = image;
	}

	public Photo getPhoto() {
		return photo;
	}

	public Image getImage() {
		return image;
	}

	@Override
	public int hashCode() {
		return photo.getId().hashCode();
	}
}
