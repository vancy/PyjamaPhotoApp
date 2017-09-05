//Pyjama compiler version:v2.3.0
package application.flickr;

import java.awt.Image;
import java.util.*;
import javax.swing.JProgressBar;
import com.flickr4java.flickr.*;
import com.flickr4java.flickr.Flickr;
import com.flickr4java.flickr.FlickrException;
import com.flickr4java.flickr.photos.Photo;
import com.flickr4java.flickr.photos.PhotoList;
import com.flickr4java.flickr.photos.PhotosInterface;
import com.flickr4java.flickr.photos.SearchParameters;
import com.flickr4java.flickr.photos.Size;
import application.SearchProjectPanel;
import pj.Pyjama;

import pj.pr.*;
import pj.PjRuntime;
import pj.Pyjama;
import pi.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.ReentrantLock;
import java.lang.reflect.InvocationTargetException;
import pj.pr.exceptions.*;

public class Search {

    public static final String API_KEY = "465dee203d07dbc90a62f0ba776006b1";

    public static final String sharedSecret = "51ec27365c21fa24";

    private static Flickr flickr = new Flickr(API_KEY, sharedSecret, new REST());

    private static PhotosInterface photoInterface = flickr.getPhotosInterface();

    public static Photo getPhoto(String id) {{
        Photo p = null;
        try {
            p = photoInterface.getPhoto(id);
        } catch (FlickrException e) {
            e.printStackTrace();
        }
        return p;
    }
    }


    public static Image getSquareImage(Photo p) {{
        Image image = null;
        try {
            image = photoInterface.getImage(p, Size.SQUARE);
        } catch (FlickrException e) {
            e.printStackTrace();
        }
        return image;
    }
    }


    public static Image getThumbnailImage(Photo p) {{
        Image image = null;
        try {
            image = photoInterface.getImage(p, Size.THUMB);
        } catch (FlickrException e) {
            e.printStackTrace();
        }
        return image;
    }
    }


    public static Image getMediumImage(Photo p) {{
        Image image = null;
        try {
            image = photoInterface.getImage(p, Size.MEDIUM);
        } catch (FlickrException e) {
            e.printStackTrace();
        }
        return image;
    }
    }


    public static Image getSmallImage(Photo p) {{
        Image image = null;
        try {
            image = photoInterface.getImage(p, Size.SMALL);
        } catch (FlickrException e) {
            e.printStackTrace();
        }
        return image;
    }
    }


    public static Image getLargeImage(Photo p) {{
        Image image = null;
        try {
            image = photoInterface.getImage(p, Size.LARGE);
        } catch (FlickrException e) {
            e.printStackTrace();
        }
        return image;
    }
    }


    public static List<PhotoWithImage> search(String str, int picsPerPage, int pageOffset, SearchProjectPanel panel) {{
        try {
            SearchParameters sp = new SearchParameters();
            sp.setText(str);
            PjRuntime.checkTaskCancellation();

            PhotoList<?> pl = photoInterface.search(sp, picsPerPage, pageOffset);
            PjRuntime.checkTaskCancellation();

            List<PhotoWithImage> list = new ArrayList<PhotoWithImage>();
            int i = 0;
            for (i = 0; i < pl.size(); i++) {
                Photo p = (Photo) pl.get(i);
                Image image = Search.getSquareImage(p);
                PhotoWithImage pi = new PhotoWithImage(p, image);
                list.add(pi);
                /*OpenMP Target region (#16) -- START */
                _OMP_TargetTaskRegion_16 _OMP_TargetTaskRegion_16_in = new _OMP_TargetTaskRegion_16();
                _OMP_TargetTaskRegion_16_in.pl = pl;
                _OMP_TargetTaskRegion_16_in.panel = panel;
                _OMP_TargetTaskRegion_16_in.i = i;
                _OMP_TargetTaskRegion_16_in.pi = pi;
                if (PjRuntime.currentThreadIsTheTarget("edt")) {
                    _OMP_TargetTaskRegion_16_in.run();
                    pl = _OMP_TargetTaskRegion_16_in.pl;
                    panel = _OMP_TargetTaskRegion_16_in.panel;
                    i = _OMP_TargetTaskRegion_16_in.i;
                    pi = _OMP_TargetTaskRegion_16_in.pi;
                } else {
                    PjRuntime.submitTargetTask(Thread.currentThread(), "edt", _OMP_TargetTaskRegion_16_in);
                }
                /*OpenMP Target region (#16) -- END */

                PjRuntime.checkTaskCancellation();

            }
            return list;
        } catch (FlickrException e) {
            e.printStackTrace();
        }
        return null;
    }
    }
static class _OMP_TargetTaskRegion_16 extends pj.pr.task.TargetTask<Void>{

    //#BEGIN shared, private variables defined here
    public int i;
    public PhotoWithImage pi;
    public PhotoList<?> pl;
    public SearchProjectPanel panel;
    //#END shared, private variables defined here

    private int OMP_state = 0;
    @Override
    public Void call() {
        try {
            /****User Code BEGIN***/
            {
                panel.progressBar.setValue((int) ((i + 1.0) / pl.size() * 100));
                panel.addToDisplay(pi);
                panel.updateUI();
                System.err.println("update progress bar " + (i + 1) + "/" + pl.size() + "from thread " + Thread.currentThread());
            }
            /****User Code END***/
        } catch(pj.pr.exceptions.OmpCancelCurrentTaskException e) {
            ;
        }
        this.setFinish();
        return null;
    }
    
}




}
