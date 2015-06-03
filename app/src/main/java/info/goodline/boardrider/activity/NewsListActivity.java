package info.goodline.boardrider.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import java.util.Stack;
import goodline.info.boardrider.R;



public class NewsListActivity extends AppCompatActivity {

    private Stack<Intent> mIntentStack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_board_rider);
        mIntentStack=new Stack<>();
        mIntentStack.push(getIntent());
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        if(intent.getAction()== SplashScreenActivity.SPLASH_HAS_NEWS || intent.getAction()== info.goodline.boardrider.servise.NotificationService.NOTI_HAS_NEWS){
            mIntentStack.push(intent);
        }

    }
    public Intent popLastIntent(){
        if(mIntentStack.size()==0){
            return getIntent();
        }else{
            return mIntentStack.pop();
        }
    }
}
