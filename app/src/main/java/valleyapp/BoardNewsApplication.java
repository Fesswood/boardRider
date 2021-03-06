package valleyapp;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.provider.Settings;
import android.support.v7.app.AlertDialog;
import android.widget.ImageView;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;
import com.nostra13.universalimageloader.cache.disc.naming.Md5FileNameGenerator;
import com.nostra13.universalimageloader.cache.memory.impl.WeakMemoryCache;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.assist.QueueProcessingType;
import com.nostra13.universalimageloader.core.display.SimpleBitmapDisplayer;

import goodline.info.boardrider.R;

/**
 * Application class for initialize {@link Volley}, {@link ImageLoader} and {@link info.goodline.boardrider.sqllite.SugarORM}
 * Also contain static method for load images, checking internet connection, showing no connection dialog
 * @author Sergey Baldin
 */
public class BoardNewsApplication extends com.orm.SugarApp {

    private static BoardNewsApplication sInstance;

    private RequestQueue mRequestQueue;

    @Override
    public void onCreate() {
        super.onCreate();
        mRequestQueue = Volley.newRequestQueue(this);
        sInstance = this;
        initImageLoader(this);
    }


    public synchronized static BoardNewsApplication getInstance() {
        return sInstance;
    }

    public RequestQueue getRequestQueue() {
        return mRequestQueue;
    }


    /**
     * Initialize {@link ImageLoader} with default options,
     * which further can be accessed by ImageLoader.getInstance()
     * @param context Current context
     */
    public static void initImageLoader(Context context) {

        DisplayImageOptions defaultOptions = new DisplayImageOptions.Builder()
                .cacheOnDisc(true).cacheInMemory(true)
                .imageScaleType(ImageScaleType.EXACTLY)
                .displayer(new SimpleBitmapDisplayer()).build();

        ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(
                context)
                .defaultDisplayImageOptions(defaultOptions)
                .memoryCache(new WeakMemoryCache())
                .discCacheSize(100 * 1024 * 1024).build();

        ImageLoader.getInstance().init(config);
    }

    /**
     * Load image from input url to input imageView
     * @param url image url
     * @param imageView view  placing image
     */
    public static void loadImage(String url, ImageView imageView) {
        ImageLoader imageLoader = ImageLoader.getInstance();
        imageLoader.displayImage(url, imageView);
    }

    /**
     * Load image from input url to bitmap
     * @param url image url
     * @return Bitmap of image
     */
    public static Bitmap loadImage(String url)
    {
        ImageLoader imageLoader = ImageLoader.getInstance();
        return  imageLoader.loadImageSync(url);
    }

    /**
     * Check internet connection
     * @param context current context
     * @return true if device has internet and false otherwise
     */
    public static boolean isOnline(Context context) {
        ConnectivityManager cm =
                (ConnectivityManager)  context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnectedOrConnecting();
    }

    /**
     * Show dialog with offer enable internet connection
     * @param context current context
     */
    public static void showNoConnectionDialog(final Context context){
        AlertDialog.Builder builder = new AlertDialog.Builder(context);builder.setCancelable(true);
        builder.setMessage(R.string.no_connection);
        builder.setTitle(R.string.no_connection_title);
        builder.setPositiveButton(R.string.settings_button_text, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which){
                context.startActivity(new Intent(Settings.ACTION_WIRELESS_SETTINGS));
            }
        });
        builder.setNegativeButton(R.string.cancel_button_text, null);
        builder.show();
    }
}
