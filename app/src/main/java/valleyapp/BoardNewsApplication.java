package valleyapp;

import android.content.Context;
import android.graphics.Bitmap;
import android.widget.ImageView;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;
import com.nostra13.universalimageloader.cache.disc.naming.Md5FileNameGenerator;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.assist.QueueProcessingType;

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
        // This configuration tuning is custom. You can tune every option, you may tune some of them,
        // or you can create default configuration by
        //  ImageLoaderConfiguration.createDefault(this);
        // method.
        ImageLoaderConfiguration.Builder config = new ImageLoaderConfiguration.Builder(context);
        config.threadPriority(Thread.NORM_PRIORITY - 2);
        config.denyCacheImageMultipleSizesInMemory();
        config.diskCacheFileNameGenerator(new Md5FileNameGenerator());
        config.diskCacheSize(50 * 1024 * 1024); // 50 MiB
        config.tasksProcessingOrder(QueueProcessingType.LIFO);
        config.writeDebugLogs(); // Remove for release app

        // Initialize ImageLoader with configuration.
        ImageLoader.getInstance().init(config.build());
    }
    public static void loadImage(String url, ImageView imageView) {
        loadImage(url, imageView,false);
    }
    public static void loadImage(String url, ImageView imageView,boolean isFitNeeded)
    {
        ImageLoader imageLoader = ImageLoader.getInstance();
        DisplayImageOptions.Builder builder = new DisplayImageOptions.Builder()
                .cacheInMemory(true)
                .cacheOnDisc(true)
                .resetViewBeforeLoading(true);

        if(isFitNeeded){
            builder.imageScaleType(ImageScaleType.EXACTLY);
        }


        imageLoader.displayImage(url, imageView, builder.build());
    }
    public static Bitmap loadImageAsync(String url, boolean isFitNeeded)
    {
        ImageLoader imageLoader = ImageLoader.getInstance();
        DisplayImageOptions.Builder builder = new DisplayImageOptions.Builder()
                .cacheInMemory(true)
                .cacheOnDisc(true)
                .resetViewBeforeLoading(true);

        if(isFitNeeded){
            builder.imageScaleType(ImageScaleType.EXACTLY_STRETCHED);
        }


      return  imageLoader.loadImageSync(url,  builder.build());
    }
}
