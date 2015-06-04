package info.goodline.boardrider.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import java.util.Stack;
import goodline.info.boardrider.R;


/**
 *  Main Activity of application
 *  Activity contains one fragment which provide the user interaction with the news list
 *  Also works with Stack of Intents when user interacts with notification or new intent received from loader in {@link SplashScreenActivity}
 *  @see info.goodline.boardrider.fragment.NewsListFragment
 *  @author  Sergey Baldin
 */
public class NewsListActivity extends AppCompatActivity {
    /**
     * Stack of intents
     * contains intent with action equal SPLASH_HAS_NEWS from SplashScreenActivity
     * or NOTI_HAS_NEWS from NotificationService
     */
    private Stack<Intent> mIntentStack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mIntentStack=new Stack<>();
        mIntentStack.push(getIntent());
        setContentView(R.layout.activity_board_rider);
    }

    /**
     * Push new Intent to stack if it's action equals SPLASH_HAS_NEWS from SplashScreenActivity
     * or NOTI_HAS_NEWS from NotificationService
     * @param intent any intent
     */
    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        if(intent.getAction()== SplashScreenActivity.SPLASH_HAS_NEWS || intent.getAction()== info.goodline.boardrider.servise.NotificationService.NOTI_HAS_NEWS){
            mIntentStack.push(intent);
        }

    }

    /**
     * Pop last intent form intent stack
     * @return upper intent from stack or intent which start the activity
     */
    public Intent popLastIntent(){
        if(mIntentStack.size()==0){
            return getIntent();
        }else{
            return mIntentStack.pop();
        }
    }
}
