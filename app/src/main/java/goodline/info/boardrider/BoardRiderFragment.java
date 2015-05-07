package goodline.info.boardrider;

import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import com.orangegangsters.github.swipyrefreshlayout.library.SwipyRefreshLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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

import valleyapp.VolleyApplication;


/**
 * A placeholder fragment containing a simple view.
 */
public class BoardRiderFragment extends Fragment  {

    private static String TAG="BoardRiderFragment";
    private NewsRecordAdapter mAdapter;
    private String mBoardUrl;
    private int  mPageIndex;
    private SwipyRefreshLayout mSwipyRefreshLayout;
    private ListView mListView;

    public BoardRiderFragment() {
        mBoardUrl="http://live.goodline.info/guest/page";
        mPageIndex=1;
    }
    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

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

                            fetch();
                            mListView.smoothScrollToPosition(mAdapter.getCount()+5);
                        }
                        Toast.makeText(getActivity(), R.string.refresh_finished, Toast.LENGTH_SHORT).show();
                    }
                }, 2000);

            }
        });

        mAdapter = new NewsRecordAdapter(getActivity());

        mListView = (ListView) getView().findViewById(R.id.news_list);
        mListView.setAdapter(mAdapter);
        fetch();
    }
    

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_board_rider, container, false);
    }
    private void fetch(){
        StringRequest stringRequest = new StringRequest(Request.Method.GET, mBoardUrl+mPageIndex,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        // Display the first 500 characters of the response string.
                      //  mTextView.setText("Response is: "+ response.substring(0,500));
                        ArrayList<BoardNews> parsedNewsList = parse(response);
                        mAdapter.addNewsItems(parsedNewsList);
                        mPageIndex++;

                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(getActivity(), "Unable to fetch data: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
        VolleyApplication.getInstance().getRequestQueue().add(stringRequest);
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
        Elements articles = doc.select(".list-topic .topic.topic-type-topic.js-topic.out-topic");
        String imageUrl,articleTitle,articleUrl,articleDate;
        Elements titleElemet,imageElement, timeElement;
        for (Element article : articles) {
            titleElemet = article.select(".topic-title a");
            articleTitle=titleElemet.text();
            articleUrl=titleElemet.attr("href");
            imageElement = article.select(".preview img");
            imageUrl= imageElement.attr("src");
            timeElement = article.select(".topic-header time");
            articleDate=timeElement.text();
            BoardNews parsedNews= new BoardNews(articleTitle,imageUrl,articleUrl,articleDate);
            newsArrayList.add(parsedNews);
        }
        return newsArrayList;
    }


}
