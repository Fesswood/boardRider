package info.goodline.boardrider.activity;




import android.content.AsyncTaskLoader;
import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.Bundle;;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;

import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

import info.goodline.boardrider.data.BoardNews;
import info.goodline.boardrider.loader.NewsLoader;
import goodline.info.boardrider.R;

import static valleyapp.BoardNewsApplication.isOnline;

/**
 *  Activity for retrieve news topics from database or load from url
 *  Do it in AsyncTaskLoader while SplashScreen is showing, after loading is completed starts {@link NewsListActivity}
 *  @see info.goodline.boardrider.fragment.NewsListFragment
 *  @author  Sergey Baldin
 */
public class SplashScreenActivity extends AppCompatActivity implements
        android.app.LoaderManager.LoaderCallbacks<ArrayList<BoardNews>>{

    private Button mReloadBtn;
    private TextView mStatusTextView;
    public static final  String NEWS_LIST="SplashScreenActivity.newsList";
    public static final  String SPLASH_HAS_NEWS="SplashScreenActivity.splashHasNews";

    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        Window window = getWindow();
        window.setFormat(PixelFormat.RGBA_8888);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                             WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_splash_creen);
        mReloadBtn = (Button) findViewById(R.id.reload_button);
        mStatusTextView = (TextView) findViewById(R.id.status_text);
        mReloadBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startLoading(getResources().getString(R.string.load_data_again));
            }
        });
        StartAnimations();
        startLoading(getResources().getString(R.string.load_data));
    }

    /**
     * Start Loading animation
     */
    private void StartAnimations() {
        Animation anim = AnimationUtils.loadAnimation(this, R.anim.alpha);
        anim.reset();
        LinearLayout l=(LinearLayout) findViewById(R.id.spl_src_lay);
        l.clearAnimation();
        l.startAnimation(anim);

        anim = AnimationUtils.loadAnimation(this, R.anim.translate);
        anim.reset();
        LinearLayout iv = (LinearLayout) findViewById(R.id.splash_content);
        iv.clearAnimation();
        iv.startAnimation(anim);
    }


    /**
     * Starts loading of news topics after 4 second
     * @param message message which user see while loading is not finished
     */
    public void startLoading(String message) {
        mStatusTextView.setText(message);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                getLoaderManager().initLoader(0, null, SplashScreenActivity.this);
            }
        }, 4000);
    }

    @Override
    public android.content.Loader<ArrayList<BoardNews>> onCreateLoader(int id, Bundle args) {
        return new BoardNewsLoader(this);
    }

    @Override
    public void onLoadFinished(android.content.Loader<ArrayList<BoardNews>> loader, ArrayList<BoardNews> data) {
        if(data.size()==0){
            mReloadBtn.setVisibility(View.VISIBLE);
        }else{
            showNewsListActivity(data);
        }
    }

    private void showNewsListActivity(ArrayList<BoardNews> data) {
        Intent i = new Intent(SplashScreenActivity.this, NewsListActivity.class);
        i.setAction(SPLASH_HAS_NEWS);
        i.putExtra(NEWS_LIST, data);
        startActivity(i);
        finish();
    }

    @Override
    public void onLoaderReset(android.content.Loader<ArrayList<BoardNews>> loader) {

    }


    /**
     * A custom Loader that loads news for subsequent retrieve it to NewsListActivity
     */
    public static class BoardNewsLoader extends AsyncTaskLoader<ArrayList<BoardNews>> {

        ArrayList<BoardNews> mBoardNews;

        public BoardNewsLoader(Context context) {
            super(context);
        }


        @Override
        public ArrayList<BoardNews> loadInBackground() {

            final Context context = getContext();
            NewsLoader mNewsLoader = new NewsLoader("http://live.goodline.info/guest/page",context);
            boolean isResultCorrect = false;

            try {
                // Try to load data from database
                mNewsLoader.fetchFromDB();
                if(mNewsLoader.getData().size()>0){
                    // if data from database is not empty
                    // mark flag that data is received
                    isResultCorrect=true;
                }else if(isOnline(context)){
                    isResultCorrect = mNewsLoader.fetchFromInternet(0, true);
                }

            } catch (ExecutionException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            // if data not empty return it
            // otherwise return empty list
            if(isResultCorrect){
               return mNewsLoader.getData();
            }else{
                return new ArrayList<>();
            }

        }

        @Override public void deliverResult(ArrayList<BoardNews> boardNews) {
            if (isReset()) {
                // An async query came in while the loader is stopped.  We
                // don't need the result.
                if (boardNews != null) {
                    onReleaseResources(boardNews);
                }
            }
            ArrayList<BoardNews> oldBoardNews = mBoardNews;
            mBoardNews = boardNews;

            if (isStarted()) {
                // If the Loader is currently started, we can immediately
                // deliver its results.
                super.deliverResult(boardNews);
            }

            // At this point we can release the resources associated with
            // 'oldApps' if needed; now that the new result is delivered we
            // know that it is no longer in use.
            if (oldBoardNews != null) {
                onReleaseResources(oldBoardNews);
            }
        }

        /**
         * Handles a request to start the Loader.
         */
        @Override protected void onStartLoading() {
            if (mBoardNews != null) {
                // If we currently have a result available, deliver it
                // immediately.
                deliverResult(mBoardNews);
            }

            if (takeContentChanged() || mBoardNews == null) {
                // If the data has changed since the last time it was loaded
                // or is not currently available, start a load.
                forceLoad();
            }
        }

        /**
         * Handles a request to stop the Loader.
         */
        @Override protected void onStopLoading() {
            // Attempt to cancel the current load task if possible.
            cancelLoad();
        }

        /**
         * Handles a request to cancel a load.
         */
        @Override public void onCanceled(ArrayList<BoardNews> apps) {
            super.onCanceled(apps);
            // At this point we can release the resources associated with 'apps'
            // if needed.
            onReleaseResources(apps);
        }

        /**
         * Handles a request to completely reset the Loader.
         */
        @Override protected void onReset() {
            super.onReset();

            // Ensure the loader is stopped
            onStopLoading();

            // At this point we can release the resources associated with 'apps'
            // if needed.
            if (mBoardNews != null) {
                onReleaseResources(mBoardNews);
                mBoardNews = null;
            }


        }

        /**
         * Helper function to take care of releasing resources associated
         * with an actively loaded data set.
         */
        protected void onReleaseResources(ArrayList<BoardNews> boardNews) {
            // For a simple List<> there is nothing to do.
            boardNews.clear();
        }
    }
}
