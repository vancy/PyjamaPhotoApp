package application.flickr;

import java.awt.Image;
import java.util.*;

import com.flickr4java.flickr.*;
import com.flickr4java.flickr.Flickr;
import com.flickr4java.flickr.FlickrException;
import com.flickr4java.flickr.photos.Photo;
import com.flickr4java.flickr.photos.PhotoList;
import com.flickr4java.flickr.photos.PhotosInterface;
import com.flickr4java.flickr.photos.SearchParameters;
import com.flickr4java.flickr.photos.Size;

public class Search {
	
	public final static String API_KEY = "465dee203d07dbc90a62f0ba776006b1";
	
	public final static String sharedSecret = "51ec27365c21fa24";

	private static Flickr flickr = new Flickr(API_KEY, sharedSecret, new REST());
	private static PhotosInterface photoInterface = flickr.getPhotosInterface();
//	private static int picsPerPage = 2;
		
	public static Photo getPhoto(String id) {
		Photo p = null;
		try {
			p = photoInterface.getPhoto(id);
		} catch (FlickrException e) {
			e.printStackTrace();
		}
		return p;
	}
		
	public static Image getSquareImage(Photo p) {
		Image image = null;
		try {
			image = photoInterface.getImage(p, Size.SQUARE);
		} catch (FlickrException e) {
			e.printStackTrace();
		}
		return image;
	}
	
	public static Image getThumbnailImage(Photo p) {
		Image image = null;
		try {
			image = photoInterface.getImage(p, Size.THUMB);
		} catch (FlickrException e) {
			e.printStackTrace();
		}
		return image;
	}
	
	public static Image getMediumImage(Photo p) {
		Image image = null;
		try {
			image = photoInterface.getImage(p, Size.MEDIUM);
		} catch (FlickrException e) {
			e.printStackTrace();
		}
		return image;
	}
	
	public static Image getSmallImage(Photo p) {
		Image image = null;
		try {
			image = photoInterface.getImage(p, Size.SMALL);
		} catch (FlickrException e) {
			e.printStackTrace();
		}
		return image;
	}
	public static Image getLargeImage(Photo p) {
		Image image = null;
		try {
			image = photoInterface.getImage(p, Size.LARGE);
		} catch (FlickrException e) {
			e.printStackTrace();
		}
		return image;
	}
		
    public static List<PhotoWithImage> search(String str, int picsPerPage, int pageOffset) {
		try {
			SearchParameters sp = new SearchParameters();
			sp.setText(str);
			PhotoList<?> pl = photoInterface.search(sp, picsPerPage, pageOffset);
			List<PhotoWithImage> list = new ArrayList<PhotoWithImage>();
			for (int i = 0; i < pl.size(); i++) {
				Photo p = (Photo) pl.get(i);
				Image image = Search.getSquareImage(p);
				PhotoWithImage pi = new PhotoWithImage(p, image);
				list.add(pi);
			}
			return list;
		} catch (FlickrException e) {
			e.printStackTrace();
		}
		return null;
	}
	
}
