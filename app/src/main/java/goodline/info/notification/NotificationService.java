package goodline.info.notification;

import android.annotation.TargetApi;
import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Build;
import android.util.Log;

import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.util.concurrent.ExecutionException;

import goodline.info.boardrider.BoardNews;
import goodline.info.boardrider.BoardRiderFragment;
import goodline.info.boardrider.NewsLoader;
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

    public static final String ACTION_NOTIFY = "com.androidhotel.simplenotification.SimpleIntentService.DATA_RETRIEVED";
    public static final String PARAM_RECEIVE_NEWS = "receiveNews";
    public static final String PARAM_NEWS_FLAG = "toBoardNewsFragment";

    public NotificationService() {
        super(DEBUG_TAG);

    }

    @Override
    protected void onHandleIntent(Intent intent) {
        mDataLoader=new NewsLoader("http://live.goodline.info/guest/page",getApplicationContext());
        mNewsFromBD = intent.getParcelableExtra(BoardRiderFragment.NEWS_TO_COMPARE);

        try {
            boolean isFetchingResultSucces = mDataLoader.fetchLastNews();
            if(isFetchingResultSucces){
                BoardNews boardNews = mDataLoader.getData().get(0);
                int isFetchingNewsLater = mNewsFromBD.compareTo(boardNews);
                isFetchingNewsLater=1;
                if(isFetchingNewsLater==1){
                    Log.d(DEBUG_TAG, "News received!");
                    sendNotif(boardNews);
                }
            }
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
    private void sendBroadcast(BoardNews receiveNews)
    {
        Intent broadcastIntent = new Intent();
        broadcastIntent.setAction(NotificationService.ACTION_NOTIFY);
        broadcastIntent.addCategory(Intent.CATEGORY_DEFAULT);
        broadcastIntent.putExtra(PARAM_RECEIVE_NEWS, receiveNews);
        sendBroadcast(broadcastIntent, Permissions.SEND_NOTIFICATIONS);
        sendBroadcast(broadcastIntent, Permissions.SEND_NOTIFICATIONS);
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    private void sendNotif(BoardNews receiveNews) {
        Intent intent = new Intent(this, BoardRiderFragment.class);
        intent.putExtra(PARAM_RECEIVE_NEWS,receiveNews);
        intent.putExtra(PARAM_NEWS_FLAG,true);
        PendingIntent pIntent = PendingIntent.getActivity(this, 0, intent, 0);

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
