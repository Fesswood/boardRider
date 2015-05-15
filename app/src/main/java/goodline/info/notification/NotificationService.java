package goodline.info.notification;

import android.app.IntentService;
import android.content.Intent;
import android.content.Context;
import android.util.Log;

import java.util.List;
import java.util.concurrent.ExecutionException;

import goodline.info.boardrider.BoardNews;
import goodline.info.boardrider.BoardRiderFragment;
import goodline.info.boardrider.NewsLoader;

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

    public NotificationService() {
        super(DEBUG_TAG);

    }

    @Override
    protected void onHandleIntent(Intent intent) {
        mDataLoader=new NewsLoader("http://live.goodline.info/guest/page",getApplicationContext());
        mNewsFromBD = intent.getParcelableExtra(BoardRiderFragment.SERVICE_STANDARD);

        try {
            boolean isFetchingResultSucces = mDataLoader.fetchLastNews();
            if(isFetchingResultSucces){
                BoardNews boardNews = mDataLoader.getData().get(0);
                int isFetchingNewsLater = mNewsFromBD.compareTo(boardNews);
                if(isFetchingNewsLater==1){
                    Log.d(DEBUG_TAG, "News received!");
                }
            }
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }
}
