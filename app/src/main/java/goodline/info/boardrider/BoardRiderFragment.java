package goodline.info.boardrider;

import android.content.Intent;
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

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.orangegangsters.github.swipyrefreshlayout.library.SwipyRefreshLayoutDirection;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

import goodline.info.sqllite.BoardNewsORM;
import valleyapp.VolleyApplication;


/**
 * A placeholder fragment containing a simple view.
 */
public class BoardRiderFragment extends Fragment implements ListView.OnItemClickListener {

    private static final String TAG= "BoardRiderFragment";
    private static final String PAGE_INDEX = "goodline.info.boardrider.index";
    public static final String SELECTED_NEWS = "goodline.info.boardrider.selected_news";
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
        mNewsLoader= new NewsLoader(mBoardUrl,getActivity());
        if (savedInstanceState != null) {
            mNewsLoader.setPageIndex(savedInstanceState.getInt(PAGE_INDEX, 0));
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
                           mNewsLoader.updateAllDB();
                           mAdapter.prependNewsList(mNewsLoader.getData());
                        }
                        Toast.makeText(getActivity(), R.string.refresh_finished, Toast.LENGTH_SHORT).show();
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
        }else{
          fetch(1, false, false);
        }
        mListView = (ListView) getView().findViewById(R.id.news_list);
        mListView.setAdapter(mAdapter);
        mListView.setOnItemClickListener(this);
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


    private void fetch(int startpage, boolean isScrollNeeded, boolean isNextPageNeeded){

        boolean isDataFromInetLoaded=false,
                isDataFromBDLoaded=mNewsLoader.fechFromDB(startpage, isNextPageNeeded);

        if(!isDataFromBDLoaded){
            try {
                isDataFromInetLoaded=  mNewsLoader.fetchFromInternet(startpage,isNextPageNeeded);
            } catch (ExecutionException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        if(!isDataFromBDLoaded && !isDataFromInetLoaded){
            Toast.makeText(getActivity(),  R.string.error_load_data, Toast.LENGTH_SHORT).show();
        }else{
            mAdapter.addAll(mNewsLoader.getData());
            if(isScrollNeeded){
                mListView.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        int nextViewPosition=getVisibleListItemsCount()+mListView.getLastVisiblePosition();
                        mListView.smoothScrollToPosition(nextViewPosition);
                    }
                },200);
            }
        }
    }

    private int getVisibleListItemsCount(){
        return (mListView.getLastVisiblePosition() - mListView.getFirstVisiblePosition()) + 1;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        BoardNews boardNews = mAdapter.getItem(position);
        Intent i = new Intent(getActivity(), ViewPagerActivity.class);
        i.putExtra(BoardRiderFragment.SELECTED_NEWS, boardNews);
        startActivity(i);
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

        return super.onOptionsItemSelected(item);
    }
}
