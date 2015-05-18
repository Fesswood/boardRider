package goodline.info.boardrider;

import android.content.Intent;
import android.content.SharedPreferences;
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

import goodline.info.notification.NotificationService;


/**
 * A placeholder fragment containing a simple view.
 */
public class BoardRiderFragment extends Fragment implements ListView.OnItemClickListener {

    public static final String TAG= "BoardRiderFragment";
    public static final String PAGE_INDEX = "goodline.info.boardrider.index";
    public static final String SELECTED_NEWS = "goodline.info.boardrider.selected_news";
    public static final String NEWS_TO_COMPARE = "goodline.info.boardrider.service_standard";

    public static final String PREFS_NAME = "MyPrefsFile";
    private boolean mPrefsisNotificationEnabled = true;

    private NewsRecordAdapter mAdapter;
    private NewsLoader mNewsLoader;
    private String mBoardUrl;
    private SwipyRefreshLayout mSwipyRefreshLayout;
    private ListView mListView;

    public BoardRiderFragment() {
        mBoardUrl="http://live.goodline.info/guest/page";
    }
    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        SharedPreferences settings = getActivity().getSharedPreferences(PREFS_NAME, 0);
        mPrefsisNotificationEnabled = settings.getBoolean("Notification", true);


        mNewsLoader= new NewsLoader(mBoardUrl,getActivity());

        if (savedInstanceState != null) {
            mNewsLoader.setPageIndex(savedInstanceState.getInt(PAGE_INDEX, 0));
        }else{
            mNewsLoader.setPageIndex(2);
        }
        getActivity().setTitle("Новости");
        setHasOptionsMenu(true);

        mSwipyRefreshLayout = (SwipyRefreshLayout) getView().findViewById(R.id.refresh);
        mSwipyRefreshLayout.setDirection(SwipyRefreshLayoutDirection.BOTH);
        mSwipyRefreshLayout.setOnRefreshListener(new SwipyRefreshLayout.OnRefreshListener() {
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
                }, 2000);
            }
        });

        mAdapter =  BoardNewsLab.get(getActivity()).getNewsRecordAdapter();

        if (getActivity().getIntent().getExtras() != null) {
          ArrayList<BoardNews> loadedNews = getActivity().getIntent().getExtras().getParcelableArrayList(SplashScreenActivity.NEWS_LIST);
            if(loadedNews.size()!=0){
                mAdapter.addNewslist(loadedNews);
            }else{
                fetch(1, false, false);
            }
            BoardNews notiNews = (BoardNews) getActivity().getIntent().getExtras().get(NotificationService.PARAM_RECEIVE_NEWS);
            if(notiNews!=null){
                if(mAdapter.getNewsList().get(0).compareTo(notiNews)==1){
                    mAdapter.getNewsList().add(0,notiNews);
                    startViewPagerActivity(notiNews);
                }

            }

        }else{
            fetch(1, false, false);
        }
        if(mPrefsisNotificationEnabled){
            startService();
        }
        mListView = (ListView) getView().findViewById(R.id.news_list);
        mListView.setAdapter(mAdapter);
        mListView.setOnItemClickListener(this);
    }

    private void update() {
        boolean isUpdated = mNewsLoader.updateAllDB();
        if(!isUpdated && mNewsLoader.getErrorCode()== NewsLoader.INTERNET_CONNECTION_ERROR){
            Toast.makeText(getActivity(), R.string.error_load_data, Toast.LENGTH_SHORT).show();
        }else{
            Toast.makeText(getActivity(), R.string.refresh_finished, Toast.LENGTH_SHORT).show();
            mAdapter.prependNewsList(mNewsLoader.getData());
        }
    }
    private void fetch(int startpage, boolean isScrollNeeded, boolean isNextPageNeeded){

        boolean isDataFromInetLoaded=false,
                isDataFromBDLoaded=mNewsLoader.fechFromDB(startpage, isNextPageNeeded);
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
                mNewsLoader.fetchFromInternetWithListener(startpage,isNextPageNeeded,isScrollNeeded,mAdapter,mListView);
            } catch (ExecutionException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }


    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
        Log.i(TAG, "onSaveInstanceState");
        savedInstanceState.putInt(PAGE_INDEX, mNewsLoader.getPageIndex());

    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_board_rider, container, false);
    }

    @Override
    public void onStop() {
        super.onStop();
        SharedPreferences settings = getActivity().getSharedPreferences(PREFS_NAME, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putBoolean("Notification", mPrefsisNotificationEnabled);
        editor.commit();
    }




    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        BoardNews boardNews = mAdapter.getItem(position);
        startViewPagerActivity(boardNews);
    }

    private void startViewPagerActivity(BoardNews boardNews) {
        Intent i = new Intent(getActivity(), ViewPagerActivity.class);
        i.putExtra(BoardRiderFragment.SELECTED_NEWS, boardNews);
        startActivity(i);
    }


    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        MenuItem checkable = menu.findItem(R.id.notification_state);
        checkable.setChecked(mPrefsisNotificationEnabled);
    }

    @Override
        public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_board_rider, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
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
           mPrefsisNotificationEnabled=!mPrefsisNotificationEnabled;
            if(mPrefsisNotificationEnabled){
                startService();
            }else{
                stopService();
            }
        }

        return super.onOptionsItemSelected(item);
    }

    private void stopService() {
        getActivity().stopService(new Intent(getActivity(),NotificationService.class));
    }

    private void startService() {
        Intent serviceIntent = new Intent(getActivity(),
               NotificationService.class);
        BoardNews newsToCompare = mAdapter.getNewsList().get(0);
        serviceIntent.putExtra(NEWS_TO_COMPARE, newsToCompare);
        getActivity().startService(serviceIntent);
    }
}
