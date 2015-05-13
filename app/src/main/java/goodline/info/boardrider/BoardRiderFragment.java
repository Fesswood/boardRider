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
    private String mBoardUrl;
    private int  mPageIndex;
    private SwipyRefreshLayout mSwipyRefreshLayout;
    private ListView mListView;

    public BoardRiderFragment() {
        mBoardUrl="http://live.goodline.info/guest/page";
    }
    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        if (savedInstanceState != null) {
            mPageIndex = savedInstanceState.getInt(PAGE_INDEX, 0);
        }else{
            mPageIndex=1;
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
                            fetch(mPageIndex,true,true);
                        } else {
                            update();
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
                mPageIndex++;
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
        savedInstanceState.putInt(PAGE_INDEX, mPageIndex);
      //  BoardNewsORM.insertPost(getActivity(),  mAdapter.getNewsList());

    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_board_rider, container, false);
    }

    private void update() {
        StringRequest stringRequest = new StringRequest(Request.Method.GET, mBoardUrl+mPageIndex,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {

                        ArrayList<BoardNews> parsedNewsList = parse(response);
                        ArrayList<BoardNews> swaplist= new ArrayList<>();
                        BoardNews firstNews =  mAdapter.getNewsList().get(0);
                        for (BoardNews loadedBoardNews : parsedNewsList)  {
                            if (firstNews.compareTo(loadedBoardNews) == -1){
                                swaplist.add(loadedBoardNews);
                            }
                        }
                        mAdapter.prependNewsList(swaplist);


                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(getActivity(),  R.string.error_load_data, Toast.LENGTH_SHORT).show();
                Log.e(TAG,error.getMessage());
            }
        });
        VolleyApplication.getInstance().getRequestQueue().add(stringRequest);
    }

    private void fetch(int startpage, boolean isScrollNeeded, boolean isNextPageNeeded){
        final boolean isScrollneed=isScrollNeeded;
        StringRequest stringRequest;
        for (int i=startpage; i<mPageIndex+1; i++){
                 stringRequest = new StringRequest(Request.Method.GET, mBoardUrl+i,
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            ArrayList<BoardNews> parsedNewsList = parse(response);
                            mAdapter.addNewslist(parsedNewsList);

                            if(isScrollneed){
                                mListView.postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        int nextViewPosition=getVisibleListItemsCount()+mListView.getLastVisiblePosition();
                                        mListView.smoothScrollToPosition(nextViewPosition);
                                    }
                                },200);
                            }

                        }
                    }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Toast.makeText(getActivity(),  R.string.error_load_data, Toast.LENGTH_SHORT).show();
                    Log.e(TAG,"error "+error.getMessage());
                }
            });
            VolleyApplication.getInstance().getRequestQueue().add(stringRequest);
        }
      if(isNextPageNeeded || mPageIndex==1){
          mPageIndex++;
      }
    }

    private ArrayList<BoardNews> parse(String HTML){
        ArrayList<BoardNews> newsArrayList = new ArrayList<>();
        Document doc =null;
        try {
            doc = Jsoup.parse(HTML);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            Log.e(null,e.getMessage(),e);
        }
        Elements articles = doc.select(".list-topic article.topic");
        String imageUrl,
               articleTitle,
               articleUrl,
               articleDate;
        StringBuffer smallDesc=new StringBuffer(150);
        Elements pageElement;
        for (Element article : articles) {
            pageElement = article.select(".topic-title a");
            articleTitle=pageElement.text();
            articleUrl=pageElement.attr("href");
            pageElement = article.select(".preview img");
            if(!pageElement.isEmpty()){
                imageUrl= pageElement.attr("src");
            }else{
                imageUrl="";
            }
            pageElement = article.select(".topic-header time");
            articleDate=pageElement.text();
            pageElement = article.select(".topic-content.text");
            smallDesc.append(pageElement.text());
            if(smallDesc.length()>150){
                smallDesc.setLength(150);
                smallDesc.append("...");
            }
            BoardNews parsedNews= new BoardNews(articleTitle,smallDesc.toString(), imageUrl,articleUrl,articleDate);
            newsArrayList.add(parsedNews);
            smallDesc.setLength(0);
        }
        return newsArrayList;
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
