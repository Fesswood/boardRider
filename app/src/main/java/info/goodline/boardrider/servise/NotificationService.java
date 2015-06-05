package info.goodline.boardrider.servise;

import android.annotation.TargetApi;
import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;

import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.util.concurrent.ExecutionException;


import info.goodline.boardrider.data.BoardNews;
import info.goodline.boardrider.activity.NewsListActivity;
import info.goodline.boardrider.loader.NewsLoader;
import goodline.info.boardrider.R;

import static valleyapp.BoardNewsApplication.isOnline;

/**
 * Service check every 5 minute last news from database and news from internet
 * if Servise detects fresh news, it sends notification to user,
 * 2used in {@link info.goodline.boardrider.fragment.NewsListFragment}
 * @author Sergey Baldin
 */
public class NotificationService extends IntentService {
    private static final String TAG = NotificationService.class.getName() ;
    /**
     *  News for compare to fresh news from internet
     */
    private BoardNews mNewsFromBD;
    /**
     *  NewsLoader provides news from database and internet
     */
    private NewsLoader mDataLoader;
    private static final String DEBUG_TAG = NotificationService.class.getName();

    public static final String SERVICE_HAS_NEWS = "NotificationService.hasNews";

    public NotificationService() {
        super(DEBUG_TAG);

    }

    @Override
    protected void onHandleIntent(Intent intent) {
        try {
            mDataLoader=new NewsLoader("http://live.goodline.info/guest/page",getApplicationContext());
            //fetch news to compare
            mDataLoader.fetchFromDB();
            boolean isFetchingResultSucces;

             if(mDataLoader.getData().size()>0  && isOnline(getApplicationContext())){
                 mNewsFromBD = mDataLoader.getData().get(0);
                 //fetch fresh news
                 isFetchingResultSucces = mDataLoader.fetchLastNews();
                 if(isFetchingResultSucces){
                     BoardNews receivedNews = mDataLoader.getData().get(0);
                     int isFetchingNewsLater = mNewsFromBD.compareTo(receivedNews);
                     // if news from internet fresher then mNewsFromBD send notification to user
                     if(isFetchingNewsLater==1){
                         Log.d(DEBUG_TAG, "News received!");
                         sendNotification(receivedNews);
                    }
                 }
             }
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * Sends notification to user
     * @param receiveNews News to send in notification
     */
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    private void sendNotification(BoardNews receiveNews) {

        PendingIntent pIntent = prepareIntent(receiveNews);

        Notification notification  = new Notification.Builder(this)
                .setContentTitle(receiveNews.getTitle())
                .setContentText(receiveNews.getStringDate())
                .setSmallIcon(R.drawable.ic_stat_name)
                .setLargeIcon(loadImageBitmap(receiveNews.getImageUrl()))
                .setContentIntent(pIntent)
                .setAutoCancel(true).build();

        NotificationManager notificationManager =
                (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        notificationManager.notify(0, notification);
    }

    /**
     * Puts news to bundle, prepare intent with it
     * @param receiveNews News to send in notification
     */
    private PendingIntent prepareIntent(BoardNews receiveNews) {
        Bundle bundle = new Bundle();
        bundle.putSerializable(BoardNews.PACKAGE_CLASS, receiveNews);

        Intent intent = new Intent().setClass(this, NewsListActivity.class);
        intent.setAction(SERVICE_HAS_NEWS);
        intent.putExtra(BoardNews.PACKAGE_CLASS, bundle);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        return PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    /**
     * Load image from url and create bitmap
     * @param url url of image
     * @return bitmap of image
     */
    private Bitmap loadImageBitmap(String url){
        Bitmap contactPic = null;
        try {
            contactPic= Picasso.with(this).load(url)
                    .resize(100, 100)
                    .placeholder(R.drawable.image_polyfill)
                    .error(R.drawable.image_polyfill)
                    .get();
        } catch (IOException e) {
          Log.d(TAG, e.getMessage());
        }
        return  contactPic;
    }
}
