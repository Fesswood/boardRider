package info.goodline.boardrider.fragment;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import com.orangegangsters.github.swipyrefreshlayout.library.SwipyRefreshLayout;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.orangegangsters.github.swipyrefreshlayout.library.SwipyRefreshLayoutDirection;

import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

import info.goodline.boardrider.data.BoardNews;
import info.goodline.boardrider.loader.NewsLoader;
import info.goodline.boardrider.adapter.NewsRecordAdapter;
import goodline.info.boardrider.R;
import info.goodline.boardrider.activity.NewsListActivity;
import info.goodline.boardrider.activity.SplashScreenActivity;
import info.goodline.boardrider.activity.ViewPagerActivity;
import info.goodline.boardrider.data.BoardNewsLab;
import info.goodline.boardrider.servise.NotificationService;


/**
 * NewsListFragment main Fragment of app show news in ListView
 * @author  Sergey Baldin
 */
public class NewsListFragment extends Fragment implements ListView.OnItemClickListener, SwipyRefreshLayout.OnRefreshListener {

    public static final String TAG= NewsListFragment.class.getSimpleName();
    public static final String PAGE_INDEX = "NewsListFragment.pageindex";
    public static final String SELECTED_NEWS = "NewsListFragment.selected_news";

    public static final String PREFS_NAME = "BoardNewsPrefs";
    public static final int NEWS_ARE_LOADED = 100;
    /**
     *  flag for load extra in OnResume after user clicked to notification
     */
    private boolean mFlagSkipFirstOnResume = false;
    private boolean mPrefNotificationEnabled = true;

    private NewsRecordAdapter mAdapter;
    private NewsLoader mNewsLoader;
    private SwipyRefreshLayout mSwipyRefreshLayout;
    private ListView mListView;

    private AlarmManager  mNotificationAlarmMgr;
    private PendingIntent mStartNotificationServiceIntent;

    private String mBoardUrl;

    public NewsListFragment() {
        mBoardUrl="http://live.goodline.info/guest/page";
    }
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SharedPreferences settings = getActivity().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        mPrefNotificationEnabled = settings.getBoolean("Notification", true);
        if(mPrefNotificationEnabled){
            startService();
        }
        mNewsLoader= new NewsLoader(mBoardUrl,getActivity());
        if (savedInstanceState != null) {
            mNewsLoader.setPageIndex(savedInstanceState.getInt(PAGE_INDEX, 0));
        }else{
            mNewsLoader.setPageIndex(1);
        }
        getActivity().setTitle("Новости");
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View inflateView = inflater.inflate(R.layout.fragment_board_rider, container, false);

        mSwipyRefreshLayout = (SwipyRefreshLayout) inflateView.findViewById(R.id.refresh);
        mSwipyRefreshLayout.setDirection(SwipyRefreshLayoutDirection.BOTH);
        mSwipyRefreshLayout.setOnRefreshListener(this);
        mAdapter =  BoardNewsLab.get(getActivity()).getNewsRecordAdapter();
        checkExtras();
        mListView = (ListView) inflateView.findViewById(R.id.news_list);
        mListView.setAdapter(mAdapter);
        mListView.setOnItemClickListener(this);
        return inflateView;
    }

    /**
     * onRefresh listener for SwipeRefreshLayout ({@link #mSwipyRefreshLayout})
     * if user make swipe to top call method {@link #update}  for current news
     * if user make swipe to bottom call method {@link #fetch}  for news from the next page
     * @param direction  direction of swipe, can be Buttom or Top
     */
    @Override
    public void onRefresh(SwipyRefreshLayoutDirection direction) {
        Toast.makeText(getActivity(), R.string.refresh_started, Toast.LENGTH_SHORT).show();
        mSwipyRefreshLayout.setRefreshing(true);
        final SwipyRefreshLayoutDirection dir = direction;
        mSwipyRefreshLayout.postDelayed(new Runnable() {
            @Override
            public void run() {
                mSwipyRefreshLayout.setRefreshing(false);
                if (dir == SwipyRefreshLayoutDirection.BOTTOM) {
                    fetch(mNewsLoader.getPageIndex(),true, true);
                } else {
                    update();
                }
            }
        }, 1500);
    }

    /**
     *  Pop last intent from activity stack get extra from it
     *  if action of intent equal SplashScreenActivity.SPLASH_HAS_NEWS get data from splashScreen
     *  if action of intent equal SplashScreenActivity.SPLASH_HAS_NEWS show news topic from notification
     *  @see info.goodline.boardrider.activity.NewsListActivity
     */
    private void checkExtras() {
        Intent intent = ((NewsListActivity) getActivity()).popLastIntent();
        Bundle extras =intent.getExtras();
        if (extras != null) {
            String action = intent.getAction();
            if (action.equals(SplashScreenActivity.SPLASH_HAS_NEWS)) {
                getLoadedNewsFromSplashScreen(extras);
            }else if(action.equals(NotificationService.SERVICE_HAS_NEWS)){
                showNewsFromNotification(intent);
            }
        }
    }

    private void showNewsFromNotification(Intent intent) {
        Bundle oldBundle = intent.getBundleExtra(BoardNews.PACKAGE_CLASS);
        BoardNews notiNews = (BoardNews) oldBundle.getSerializable(BoardNews.PACKAGE_CLASS);
        if (notiNews !=null){
            if(mAdapter.getNewsList().get(0).compareTo(notiNews)==1){
                mAdapter.getNewsList().add(0, notiNews);
            }
            startViewPagerActivity(notiNews);
        }
    }

    private void getLoadedNewsFromSplashScreen(Bundle extras) {
        ArrayList<BoardNews> loadedNews =(ArrayList<BoardNews>) extras.getSerializable(SplashScreenActivity.NEWS_LIST);
        if (loadedNews != null) {
            mAdapter.addNewslist(loadedNews);
        } else {
            fetch(1, false, false);
        }
    }

    /**
     * register the newsRecordAdapter in newsLoader
     * and update database
     * @see info.goodline.boardrider.loader.NewsLoader
     */
    private void update() {
        mNewsLoader.setAdapter(mAdapter);
        mNewsLoader.registerRequestForUpdateAllDB();
    }

    /**
     * Try to fetch news from database if it cannot be done try to load news from internet
     * @param startpage start page index
     * @param isScrollNeeded flag for auto scrolling down after the next page was loaded
     * @param isNextPageNeeded if true increase pageIndex
     */
    private void fetch(int startpage, boolean isScrollNeeded, boolean isNextPageNeeded){

        boolean isDataFromBDLoaded=mNewsLoader.fetchFromDB(startpage, isNextPageNeeded);

        if(isDataFromBDLoaded){
            mAdapter.addNewslist(mNewsLoader.getData());
            if(isScrollNeeded){
                mListView.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        int nextViewPosition= (mListView.getLastVisiblePosition() - mListView.getFirstVisiblePosition())+1+mListView.getLastVisiblePosition();
                        mListView.smoothScrollToPosition(nextViewPosition);
                    }
                },200);
            }
        }
        else{
            try {
                Handler scrollHandler = initializeHandler();
                mNewsLoader.registerInternetRequestWithListener(startpage, scrollHandler, isNextPageNeeded, mAdapter);
            } catch (ExecutionException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Initialize handler with handle message from {@link NewsLoader} request listener
     * @return handler with override handleMessage method for scrolling after downloading news
     */
    private Handler initializeHandler() {
        return new Handler()  {
            public void handleMessage(Message msg)
            {
                if (msg.what == NEWS_ARE_LOADED)
                {
                    int nextViewPosition= (mListView.getLastVisiblePosition() - mListView.getFirstVisiblePosition())+1+mListView.getLastVisiblePosition();
                    mListView.smoothScrollToPosition(nextViewPosition);
                }
            }
        };
    }


    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
        Log.i(TAG, "onSaveInstanceState");
        savedInstanceState.putInt(PAGE_INDEX, mNewsLoader.getPageIndex());
    }

    @Override
    public void onStop() {
        super.onStop();
        SharedPreferences settings = getActivity().getSharedPreferences(PREFS_NAME, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putBoolean("Notification", mPrefNotificationEnabled);
        editor.commit();
    }

    @Override
    public void onResume() {
        super.onResume();
        if(mFlagSkipFirstOnResume){
            checkExtras();
        }
        mFlagSkipFirstOnResume =true;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        BoardNews boardNews = mAdapter.getItem(position);
        startViewPagerActivity(boardNews);
    }

    private void startViewPagerActivity(BoardNews boardNews) {
        Intent i = new Intent(getActivity(), ViewPagerActivity.class);
        i.putExtra(NewsListFragment.SELECTED_NEWS, boardNews);
        startActivity(i);
    }


    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        MenuItem checkable = menu.findItem(R.id.notification_state);
        checkable.setChecked(mPrefNotificationEnabled);
    }

    @Override
        public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_board_rider, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_to_top) {
            mListView.smoothScrollToPosition(0);
        }
        if (id == R.id.action_to_bottom) {
            mListView.smoothScrollToPosition(mAdapter.getCount()-1);
        }
        if (id == R.id.clear_cache) {
            mNewsLoader.clearDataBase();
            mAdapter.getNewsList().clear();
            mNewsLoader.setPageIndex(1);
            fetch(1, false, true);
        }
        if (id == R.id.notification_state) {
           mPrefNotificationEnabled =!mPrefNotificationEnabled;
            if(mPrefNotificationEnabled){
                startService();
            }else{
                stopService();
            }
        }
        return super.onOptionsItemSelected(item);
    }

    private void stopService() {
        if (mNotificationAlarmMgr != null) {
            mNotificationAlarmMgr.cancel(mStartNotificationServiceIntent);
        }
    }

    private void startService() {
        Intent serviceIntent = new Intent(getActivity(),
               NotificationService.class);
        Context context = getActivity();
        mStartNotificationServiceIntent = PendingIntent.getService(context, 0, serviceIntent, 0);
        mNotificationAlarmMgr =(AlarmManager) context.getSystemService(context.ALARM_SERVICE);
        mNotificationAlarmMgr.setInexactRepeating(AlarmManager.RTC_WAKEUP,
                System.currentTimeMillis(), 300 * 60 * 1000, mStartNotificationServiceIntent);
    }


}
