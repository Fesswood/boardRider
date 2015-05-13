package goodline.info.boardrider;




import android.app.ActionBar;
import android.content.AsyncTaskLoader;
import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.AsyncTask;
import android.os.Bundle;;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.AppCompatActivity;

import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.concurrent.ExecutionException;


public class SplashScreenActivity extends AppCompatActivity implements
        android.app.LoaderManager.LoaderCallbacks<ArrayList<BoardNews>>{

    private ProgressBar mProgressBar;
    private Button mReloadBtn;
    private TextView mStatusTextView;
    private NewsLoader mNewsLoader;
    public static final  String NEWS_LIST="goodline.info.boardrider.news_list";

    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        Window window = getWindow();
        window.setFormat(PixelFormat.RGBA_8888);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
       // requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);
      //  getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
       //         WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_splash_creen);


        mProgressBar = (ProgressBar) findViewById(R.id.data_progressBar);
        mReloadBtn = (Button) findViewById(R.id.reload_button);
        mStatusTextView = (TextView) findViewById(R.id.status_text);
        mReloadBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startLoading("Загружаем новости еще раз...");
            }
        });
        StartAnimations();
        startLoading("Загружаем Новости...");
    }
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
     * Starts loading the data
     */
    public void startLoading(String message) {
        mStatusTextView.setText(message);
        getLoaderManager().initLoader(0, null, this);
 /*       mTask = new LoadingTask();


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {

            mTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        } else {
            mTask.execute();
        }*/

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
     /*   if (mTask != null && mTask.getStatus() != LoadingTask.Status.FINISHED) {
            mTask.cancel(true);
            mTask = null;
        }*/
    }


    @Override
    public android.content.Loader<ArrayList<BoardNews>> onCreateLoader(int id, Bundle args) {
        return new BoardNewsLoader(this);
    }

    @Override
    public void onLoadFinished(android.content.Loader<ArrayList<BoardNews>> loader, ArrayList<BoardNews> data) {
        if(data.size()==0){
            mReloadBtn.setVisibility(View.VISIBLE);
            finish();
        }else{
            Intent i = new Intent(SplashScreenActivity.this, BoardRiderActivity.class);
            i.putExtra(NEWS_LIST, data);
            startActivity(i);
            finish();
        }
    }

    @Override
    public void onLoaderReset(android.content.Loader<ArrayList<BoardNews>> loader) {

    }


    /**
     * A custom Loader that loads all of the installed applications.
     */
    public static class BoardNewsLoader extends AsyncTaskLoader<ArrayList<BoardNews>> {

        ArrayList<BoardNews> mBoardNews;
        private NewsLoader mNewsLoader;

        public BoardNewsLoader(Context context) {
            super(context);
        }

        /**
         * This is where the bulk of our work is done.  This function is
         * called in a background thread and should generate a new set of
         * data to be published by the loader.
         */
        @Override public ArrayList<BoardNews> loadInBackground() {
            // Retrieve all known applications.

            final Context context = getContext();
            mNewsLoader = new NewsLoader("http://live.goodline.info/guest/page");
            boolean isResultCorrect = false;
            try {
                isResultCorrect = mNewsLoader.fetch(1, true);
            } catch (ExecutionException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            if(isResultCorrect){
               return mNewsLoader.getData();
            }else{
                return new ArrayList<>();
            }

        }

        /**
         * Called when there is new data to deliver to the client.  The
         * super class will take care of delivering it; the implementation
         * here just adds a little more logic.
         */
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
            // For a simple List<> there is nothing to do.  For something
            // like a Cursor, we would close it here.
        }
    }
}
