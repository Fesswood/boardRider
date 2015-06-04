package info.goodline.boardrider.loader;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;
import android.widget.ListView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.RequestFuture;
import com.android.volley.toolbox.StringRequest;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import info.goodline.boardrider.adapter.NewsRecordAdapter;
import goodline.info.boardrider.R;
import info.goodline.boardrider.data.BoardNews;
import info.goodline.boardrider.sqllite.SugarORM;
import valleyapp.BoardNewsApplication;

import static valleyapp.BoardNewsApplication.isOnline;


public class NewsLoader {


    private int mPageIndex=1;
    private int mUpdatePageIndex=1;
    private Context mContext;
    private String mBoardUrl;
    private String mErrorMessage;
    private int mErrorCode;
    private ArrayList<BoardNews> mData;
    private boolean mIsDataLoadCorrectly;
    private boolean mIsRecursive=true;
    private boolean  mIsUpdateLoopBreak=false;



    public static final String TAG="NewsLoader";

    public static final int INTERNET_CONNECTION_ERROR=1;
    private NewsRecordAdapter mAdapter;



    public NewsLoader(String dataUri, Context context) {
        mBoardUrl=dataUri;
        mContext=context;
        mData=new ArrayList<>();
    }
    public boolean fechFromDB(int offsetPages, boolean isNextPageNeeded){
        if(offsetPages == (mPageIndex+1)){
            throw new IllegalArgumentException(TAG+": startpage and mPageIndex must be different value: mPageIndex="+mPageIndex+" startindex"+offsetPages );
        }
          mData= new ArrayList<BoardNews>();
          mData = SugarORM.getNewsFromPage(offsetPages, mPageIndex + 1);
        if(mData.size()>0){
            if(isNextPageNeeded || mPageIndex==1){
                mPageIndex++;
            }
            return true;
        }
        return false;
    }

    /**
     *  synchronized DB
     * @return false if  nothing changes true otherwise
     */
    public boolean syncDB() {
           BoardNews lastBoardNews = mData.get(0);
           BoardNews firstBoardNews = mData.get(mData.size() - 1);
           ArrayList<BoardNews> newsBetweenDates= SugarORM.getNewsByDate(firstBoardNews, lastBoardNews);
            // if database doesn't keep any rows between dates add all new rows to database
            if(newsBetweenDates.size()==0){
                SugarORM.insertNews(mData);
                return true;
            }else{
                boolean isCollectionModified = mData.removeAll(newsBetweenDates);
                if(isCollectionModified && mData.size()>0){
                    SugarORM.insertNews(mData);
                    mData.addAll(newsBetweenDates);
                    return true;
                }
            }
        return false;
    }

    public void updateAllDB(){
        if(!isOnline(mContext)){
            mErrorCode=INTERNET_CONNECTION_ERROR;
            return;
        }
        mData=new ArrayList<>(mAdapter.getNewsList());
        StringRequest stringRequest;
            stringRequest = new StringRequest(Request.Method.GET, mBoardUrl+mUpdatePageIndex,
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            if(!mIsUpdateLoopBreak){
                                ArrayList<BoardNews> temp=parse(response);
                                temp.removeAll(mData);
                                for(BoardNews boardNews: temp){
                                    mData.add(0,boardNews);
                                }
                                boolean isDataBaseChanges=syncDB();

                                if(isDataBaseChanges){
                                    if(mContext!=null){
                                        Toast.makeText(mContext, R.string.refresh_finished_for_all_pages, Toast.LENGTH_SHORT).show();
                                    }
                                    mUpdatePageIndex++;
                                    NewsLoader.this.updateAllDB();
                                }else{
                                    if(mContext!=null){
                                        Toast.makeText(mContext, R.string.no_fresh_news, Toast.LENGTH_SHORT).show();
                                    }
                                    mAdapter.prependNewsList(mData);
                                    mPageIndex=++mUpdatePageIndex;
                                    mIsUpdateLoopBreak=true;
                                }

                            }
                        }
                    }
                    ,new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    if(mContext!=null){
                        Toast.makeText(mContext, R.string.error_load_data, Toast.LENGTH_SHORT).show();
                    }
                    Log.e(TAG,"error "+error.getMessage());
                }
            });
            BoardNewsApplication.getInstance().getRequestQueue().add(stringRequest);
    }

    public boolean fetchFromInternet(int startpage, boolean isNextPageNeeded) throws ExecutionException, InterruptedException {
        StringRequest stringRequest;
        for (int i=startpage; i<mPageIndex+1; i++){
            RequestFuture<String> future = RequestFuture.newFuture();
            stringRequest = new StringRequest(Request.Method.GET, mBoardUrl+i, future, future);

            BoardNewsApplication.getInstance().getRequestQueue().add(stringRequest);
            String response = null;

            try {
                response = future.get(20, TimeUnit.SECONDS);
            } catch (TimeoutException e) {
                e.printStackTrace();
            }
            if(response==null && mIsRecursive){
                mIsRecursive=false;
                fetchFromInternet(startpage,isNextPageNeeded);
            }else{
                mData=parse(response);
                syncDB();
                mIsDataLoadCorrectly =true;
            }

        }
        if(mIsDataLoadCorrectly && (isNextPageNeeded || mPageIndex==1)){
            mPageIndex++;
        }

        return mIsDataLoadCorrectly;
    }

    public void fetchFromInternetWithListener(int startpage, final boolean isNextPageNeeded, final boolean isScrollNeeded,NewsRecordAdapter adapter, final ListView listView) throws ExecutionException, InterruptedException {
        StringRequest stringRequest;
        mAdapter=adapter;
        for (int i=startpage; i<mPageIndex+1; i++){
            stringRequest = new StringRequest(Request.Method.GET, mBoardUrl+i,
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            mData=parse(response);
                            syncDB();
                            mAdapter.addNewslist(mData);
                            if(mContext!=null){
                                Toast.makeText(mContext, R.string.refresh_finished, Toast.LENGTH_SHORT).show();
                            }
                            if(isNextPageNeeded || mPageIndex==1){
                                mPageIndex++;
                            }
                            if(isScrollNeeded){
                                listView.postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        int nextViewPosition= (listView.getLastVisiblePosition() - listView.getFirstVisiblePosition())+1+listView.getLastVisiblePosition();
                                        listView.smoothScrollToPosition(nextViewPosition);
                                    }
                                },200);
                            }
                            mAdapter=null;
                        }
                    }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    if(mContext!=null){
                        Toast.makeText(mContext, R.string.error_load_data, Toast.LENGTH_SHORT).show();
                    }
                    Log.e(TAG,"error "+error.getMessage());
                }
            });
            BoardNewsApplication.getInstance().getRequestQueue().add(stringRequest);
        }

    }
    public boolean fetchLastNews() throws ExecutionException, InterruptedException {
        StringRequest stringRequest;
        mIsDataLoadCorrectly=false;

        RequestFuture<String> future = RequestFuture.newFuture();
        stringRequest = new StringRequest(Request.Method.GET, mBoardUrl+1, future, future);

        BoardNewsApplication.getInstance().getRequestQueue().add(stringRequest);
        String response = null;

        try {
            response = future.get(20, TimeUnit.SECONDS);
            BoardNews boardNews = parseOne(response);
            mData=new ArrayList<>();
            mData.add(boardNews);
            mIsDataLoadCorrectly =true;
        } catch (TimeoutException e) {
            e.printStackTrace();
        }
        return mIsDataLoadCorrectly;
    }

    private BoardNews parseOne(String HTML){
        Document doc =null;
        if(HTML==null){
            mErrorCode=1;
            return  new BoardNews();
        }
        try {
            doc = Jsoup.parse(HTML);
        } catch (Exception e) {
            Log.e(TAG, "" + e.getMessage(), e);
        }
        Element article = doc.select(".list-topic .topic.topic-type-topic.js-topic.out-topic").first();

        return parseArticleRaw(article);
    }
    private ArrayList<BoardNews> parse(String HTML){
        ArrayList<BoardNews> newsArrayList = new ArrayList<>();
        Document doc =null;
        if(HTML==null){
            mErrorCode=1;
            return  newsArrayList;
        }
        try {
            doc = Jsoup.parse(HTML);
        } catch (Exception e) {
            Log.e(TAG,""+e.getMessage(),e);
        }
        Elements articles = doc.select(".list-topic .topic.topic-type-topic.js-topic.out-topic");


        for (Element article : articles) {
            BoardNews parsedNews = parseArticleRaw(article);
            newsArrayList.add(parsedNews);
        }
        return newsArrayList;
    }

    private BoardNews parseArticleRaw(Element article) {
        StringBuffer smallDesc = new StringBuffer();
        Elements pageElement;
        String articleTitle;
        String articleUrl;
        String imageUrl;
        String articleDate;
        pageElement = article.select(".topic-title a");
        articleTitle=pageElement.text();
        articleUrl=pageElement.attr("href");
        pageElement = article.select(".preview img");
        imageUrl= pageElement.attr("src");
        pageElement = article.select(".topic-header time");
        articleDate=pageElement.text();
        pageElement = article.select(".topic-content.text");
        smallDesc.append(pageElement.text());
        if(smallDesc.length()>150){
            smallDesc.setLength(150);
            smallDesc.append("...");
        }
        return new BoardNews(articleTitle,smallDesc.toString(), imageUrl,articleUrl,articleDate);
    }

    public int getErrorCode() {
        int errorCode=mErrorCode;
        mErrorCode=0;
        return errorCode;
    }
    public int getPageIndex() {
        return mPageIndex;
    }

    public void setPageIndex(int pageIndex) {
        mPageIndex = pageIndex;
    }

    public String getBoardUrl() {
        return mBoardUrl;
    }

    public void setBoardUrl(String boardUrl) {
        mBoardUrl = boardUrl;
    }

    public String getErrorMessage() {
        return mErrorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        mErrorMessage = errorMessage;
    }

    public ArrayList<BoardNews> getData() {
        return mData;
    }

    public void setData(ArrayList<BoardNews> data) {
        mData = data;
    }

    public void fetchNewsOffline() {
        mData.clear();
        mData=SugarORM.getNewsFromPage(0, mPageIndex);
    }

    public void clearDataBase() {
        BoardNews.deleteAll(BoardNews.class);
    }

    public void setAdapter(NewsRecordAdapter adapter) {
        this.mAdapter = adapter;
    }
}
