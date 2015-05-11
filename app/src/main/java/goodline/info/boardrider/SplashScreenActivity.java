package goodline.info.boardrider;



import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.concurrent.ExecutionException;


public class SplashScreenActivity extends Activity {

    public LoadingTask mTask;
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
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_splash_creen);

        mNewsLoader = new NewsLoader("http://live.goodline.info/guest/page");
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
        mTask = new LoadingTask();


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {

            mTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        } else {
            mTask.execute();
        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (mTask != null && mTask.getStatus() != LoadingTask.Status.FINISHED) {
            mTask.cancel(true);
            mTask = null;
        }
    }

    private class LoadingTask extends AsyncTask<Void, Integer, ArrayList<BoardNews>> {

        @Override
        protected ArrayList<BoardNews> doInBackground(Void... params) {

            boolean result = false;
            try {
                result = mNewsLoader.fetch(1, true);
            } catch (ExecutionException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            try {
                    Thread.sleep(3000);
                    this.publishProgress(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            if(result){
                return mNewsLoader.getData();
            }else{
                String errorMessage = mNewsLoader.getErrorMessage();
            }
            return new ArrayList<>();
        }

        @Override
        protected void onPostExecute(ArrayList<BoardNews> result) {
            mTask = null;
            if(result.size()==0||result == null){
                mReloadBtn.setVisibility(View.VISIBLE);
                finish();
            }else{
                Intent i = new Intent(SplashScreenActivity.this, BoardRiderActivity.class);
                i.putExtra(NEWS_LIST, result);
                startActivity(i);
                finish();
            }

        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            if (mProgressBar != null) {
                mProgressBar.setProgress(values[0]);
            }
        }
    }

}
