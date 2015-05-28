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
import info.goodline.boardrider.fragment.NewsListFragment;
import info.goodline.boardrider.activity.NewsListActivity;
import info.goodline.boardrider.loader.NewsLoader;
import goodline.info.boardrider.R;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p/>
 * TODO: Customize class - update intent actions, extra parameters and static
 * helper methods.
 */
public class NotificationService extends IntentService {
    private BoardNews mNewsFromBD;
    private NewsLoader mDataLoader;
    private static final String DEBUG_TAG = "NotificationService";

    public static final String PARAM_RECEIVE_NEWS = "receiveNews";
    public static final String NOTI_HAS_NEWS = "hasNewsNOTIPLEASEHELPME";

    public NotificationService() {
        super(DEBUG_TAG);

    }

    @Override
    protected void onHandleIntent(Intent intent) {
        mDataLoader=new NewsLoader("http://live.goodline.info/guest/page",getApplicationContext());
        mNewsFromBD = intent.getParcelableExtra(NewsListFragment.NEWS_TO_COMPARE);

        try {
            boolean isFetchingResultSucces = mDataLoader.fetchLastNews();
            if(isFetchingResultSucces){
                BoardNews reseivedNews = mDataLoader.getData().get(0);
                int isFetchingNewsLater = mNewsFromBD.compareTo(reseivedNews);
                isFetchingNewsLater=1;
                if(isFetchingNewsLater==1){
                    Log.d(DEBUG_TAG, "News received!");
                    sendNotif(reseivedNews);
                }
            }
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    private void sendNotif(BoardNews receiveNews) {


        Bundle bundle = new Bundle();
        bundle.putParcelable(BoardNews.PACKAGE_CLASS, receiveNews);



        Intent intent = new Intent().setClass(this, NewsListActivity.class);
        intent.setAction(NOTI_HAS_NEWS);
        intent.putExtra(BoardNews.PACKAGE_CLASS, bundle);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pIntent =  PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        Notification notification  = new Notification.Builder(this)
                .setContentTitle(receiveNews.getTitle())
                .setContentText(receiveNews.getStringDate())
                .setSmallIcon(R.drawable.ic_stat_name)
                .setLargeIcon(getNewsImage(receiveNews.getImageUrl()))
                .setContentIntent(pIntent)
                .setAutoCancel(true).build();

        NotificationManager notificationManager =
                (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        notificationManager.notify(0, notification);
    }
    private Bitmap getNewsImage(String url){
        Bitmap contactPic = null;

        final String getOnlinePic = url;

        try {
            contactPic= Picasso.with(this).load(getOnlinePic)
                    .resize(100, 100)
                    .placeholder(R.drawable.image_polyfill)
                    .error(R.drawable.image_polyfill)
                    .get();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return  contactPic;
    }
}
