package valleyapp;

import android.content.Context;
import android.graphics.Bitmap;
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
 * Created by Балдин Сергей on 06.05.2015.
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
    public static void loadImage(String url, ImageView imageView) {
        ImageLoader imageLoader = ImageLoader.getInstance();
        imageLoader.displayImage(url, imageView);
    }

    public static Bitmap loadImageAsync(String url)
    {
        ImageLoader imageLoader = ImageLoader.getInstance();
        return  imageLoader.loadImageSync(url);
    }
}
