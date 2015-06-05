package info.goodline.boardrider.utils;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Point;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.text.Html;
import android.util.Log;
import android.view.Display;
import android.widget.TextView;

import java.util.ArrayList;

import static valleyapp.BoardNewsApplication.loadImage;

/**
 * ImageGetter for loading images in news topic
 * NewsTopicImageGetter used for downloading images from news html to canvas of news TextView.
 * @see info.goodline.boardrider.fragment.NewsTopicFragment
 * Also while the AsyncTask working add every image url to ArrayList for subsequent creating fullscreen gallery.
 * @see info.goodline.boardrider.activity.ImageGalleryActivity
 * @author  Sergey Baldin
 *
 */
public class NewsTopicImageGetter implements Html.ImageGetter {


    private String DEBUG_TAG = NewsTopicImageGetter.class.getSimpleName();
    private Display mDisplay;
    private Resources mResources;
    private ArrayList<String> mImageUrlsList;
    private TextView mNewsTopicView;

    /**
     *  Create instance of NewsTopicImageGetter
     * @param resources reference to app resources
     * @param display  reference to display for getting size of screen
     */
    public NewsTopicImageGetter(final Resources resources,  Display display,ArrayList<String> ImageLinks,TextView textView) {
        mResources = resources;
        mDisplay = display;
        mImageUrlsList = ImageLinks;
        mNewsTopicView = textView;
    }

    @Override
    public Drawable getDrawable(final String source) {
        final BitmapDrawablePlaceHolder result = new BitmapDrawablePlaceHolder();

        new AsyncTask<Void, Void, Bitmap>() {

            @Override
            protected Bitmap doInBackground(final Void... meh) {
                try {
                    mImageUrlsList.add(source);
                    return  loadImage(source);
                } catch (Exception e) {
                    Log.e(DEBUG_TAG, "doInBackground " + e.getMessage());
                    return null;
                }
            }

            @Override
            protected void onPostExecute(final Bitmap bitmap) {
                try {
                    final BitmapDrawable drawable = new BitmapDrawable(mResources, bitmap);
                    Point size = getRelativeImageSize(drawable);
                    drawable.setBounds(0, 0, size.x, size.y);
                    result.setDrawable(drawable);
                    result.setBounds(0, 0, size.x, size.y);
                    mNewsTopicView.setText(mNewsTopicView.getText());
                } catch (Exception e) {
                    Log.e(DEBUG_TAG, "onPostExecute " + e.getMessage());
                }
            }

        }.execute((Void) null);
        return result;
    }

    private Point getRelativeImageSize(BitmapDrawable drawable) {

        Point size = new Point();
        mDisplay.getSize(size);

        // delete padding from screen size
        float scale = mResources.getDisplayMetrics().density;
        int paddingDpAsPixels = (int) (40*scale + 0.5f);

        size.x=size.x - paddingDpAsPixels;
        double ratio = ((float) size.x) / (float) drawable.getIntrinsicWidth();
        size.y = (int) (ratio * drawable.getIntrinsicHeight());
        return size;
    }

    /**
     *  BitmapDrawable for drawing images from news in textView
     */
    class BitmapDrawablePlaceHolder extends BitmapDrawable {

        protected Drawable drawable;

        @Override
        public void draw(final Canvas canvas) {
            if (drawable != null) {
                drawable.draw(canvas);
            }
        }
        public void setDrawable(Drawable drawable) {
            this.drawable = drawable;
        }
    }
}