package info.goodline.boardrider.loader;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
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
import info.goodline.boardrider.fragment.NewsListFragment;
import info.goodline.boardrider.sqllite.SugarORM;
import valleyapp.BoardNewsApplication;

import static valleyapp.BoardNewsApplication.isOnline;

/**
 * Class for controlling data loading from database or internet
 * @author Sergey Baldin
 */
public class NewsLoader implements Response.Listener<String>,Response.ErrorListener{

    public static final String TAG=NewsLoader.class.getSimpleName();
    public static final int INTERNET_CONNECTION_ERROR=1;
    /**
     *  Current page index by default equal 1
     */
    private int mPageIndex=0;
    /**
     * additinal page index used for updating database and news list
     */
    private int mUpdatePageIndex=0;
    /**
     * Current context
     */
    private Context mContext;
    /**
     * Board url for fetching news topic
     */
    private String mBoardUrl;
    /**
     * News list which used for holding data from internet or database
     * before it will be requested by other classes
     */
    private ArrayList<BoardNews> mData;
    /**
     *  Message which will be received if data load failed
     */
    private String mErrorMessage;
    /**
     *  Error Code which will be received if data load failed
     */
    private int mErrorCode;
    /**
     *  flag to indicating successful loading of data
     */
    private boolean mIsDataLoadCorrectly;
    /**
     *  if device has slow internet connection and timeout of request is over, this flag used for
     *  request news once again
     */
    private boolean mIsRecursive=true;
    /**
     *  if device has slow internet connection and timeout of request is over, this flag used for
     *  request news once again
     */
    private boolean  mIsUpdateLoopBreak=false;
    /**
     *  Adapter of list view which contains news topic
     */
    private NewsRecordAdapter mAdapter;
    private Handler mHandler;
    private RequestType mRequestType;
    private boolean mIsNextPageNeeded;

    /**
     * Create new instance of NewsLoader
     * @param dataUrl url for downloading news
     * @param context current context
     */
    public NewsLoader(String dataUrl, Context context) {
        mBoardUrl=dataUrl;
        mContext=context;
        mData=new ArrayList<>();
    }

    @Override
    public void onResponse(String response) {
        switch (mRequestType){
            case REQUEST_FOR_DB:
                updateAllDB(response);
                break;
            case REQUEST_FOR_INTERNET:
                parseNewsFromInternet(response);
                break;
        }
    }
    @Override
    public void onErrorResponse(VolleyError error) {
        if(mContext!=null){
            Toast.makeText(mContext, R.string.error_load_data, Toast.LENGTH_SHORT).show();
        }
        Log.e(TAG, "error " + error.getMessage());
    }

    /**
     * Fetch news from Database with offset
     * @param offsetPages count of pages for offset
     * @param isNextPageNeeded flag that pageIndex incrementing is needed
     * @return true if data loaded successfully else otherwise
     */
    public boolean fetchFromDB(int offsetPages, boolean isNextPageNeeded){
        if(offsetPages == (mPageIndex+1)){
            throw new IllegalArgumentException(TAG+": startpage and mPageIndex must be different value: mPageIndex="+mPageIndex+" startindex"+offsetPages );
        }
          mData= new ArrayList<>();
          mData = SugarORM.getNewsFromPage(offsetPages);
        if(mData.size()>0){
            if(isNextPageNeeded){
                mPageIndex++;
            }
            return true;
        }
        return false;
    }

    /**
     * Fetch last news from Database
     */
    public boolean fetchFromDB() {
       return fetchFromDB(0, false);
    }
    /**
     *  Fetch news from startpage via internet using {@link RequestFuture}
     * @param startpage startpage index
     * @param isNextPageNeeded  flag that pageIndex incrementing is needed
     * @return true if data loaded successfully else otherwise
     * @throws ExecutionException when {@link StringRequest} is failed
     * @throws InterruptedException when {@link StringRequest} is failed
     */
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
            /**
             *  if device has slow internet connection and timeout of request is over, this flag used for
             *  request news once again
             */
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


    /**
     *  synchronized Database entry with entry from downloaded data list
     * @return false if nothing changes true otherwise
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

    /**
     *  register {@link StringRequest} for updating database  news entries
     */
    public void registerRequestForUpdateAllDB(){
        if(!isOnline(mContext)){
            mErrorCode=INTERNET_CONNECTION_ERROR;
            return;
        }
        mData=new ArrayList<>(mAdapter.getNewsList());
        mRequestType=RequestType.REQUEST_FOR_DB;
        StringRequest stringRequest = new StringRequest(Request.Method.GET, mBoardUrl+mUpdatePageIndex,this,this);
            BoardNewsApplication.getInstance().getRequestQueue().add(stringRequest);
    }
    /**
     *   Compare all database news entries with news from newsListAdapter
     *   recursively call registerRequestForUpdateAllDB if fresh news have detected
     */
    private void updateAllDB(String response){
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
                registerRequestForUpdateAllDB();
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

    /**
     * Register Request for fetching news from url via internet
     * @param startpage index of startpage
     * @param handler handler for sending message back to UI tread
     * @param isNextPageNeeded flag that pageIndex incrementing is needed
     * @param adapter Adapter with news
     * @throws ExecutionException when {@link StringRequest} is failed
     * @throws InterruptedException when {@link StringRequest} is failed
     */
    public void registerInternetRequestWithListener(int startpage, Handler handler, boolean isNextPageNeeded, NewsRecordAdapter adapter)
            throws ExecutionException, InterruptedException {
        StringRequest stringRequest;
        mAdapter=adapter;
        mIsNextPageNeeded =isNextPageNeeded;
        mRequestType=RequestType.REQUEST_FOR_INTERNET;
        mHandler=handler;
        for (int i=startpage; i<mPageIndex+1; i++){
            stringRequest = new StringRequest(Request.Method.GET, mBoardUrl+i,this,this);
            BoardNewsApplication.getInstance().getRequestQueue().add(stringRequest);
        }

    }
    /**
     * Parse fetched html to news instance and put it to Adapter
     * @param response html content of news preview
     */
    private void parseNewsFromInternet(String response)  {
        mData=parse(response);
        syncDB();
        mAdapter.addNewslist(mData);
        if(mContext!=null){
            Toast.makeText(mContext, R.string.refresh_finished, Toast.LENGTH_SHORT).show();
        }
        if(mIsNextPageNeeded || mPageIndex==1){
            mPageIndex++;
        }
        Message message = mHandler.obtainMessage();
        message.what = NewsListFragment.NEWS_ARE_LOADED;
        message.sendToTarget();
        mAdapter=null;
    }
    /**
     * Register {@link StringRequest} for fetching one last news
     * Used in non-UI tread
     * @return true if data load correctly
     */
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

    /**
     * Parsed HTML received from url  and return instance of news
     * @param HTML HTML contains news
     * @return instance of news
     */
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

        return parseHtmlElements(article);
    }

    /**
     * Parsed HTML received from url and return news list
     * @param HTML HTML contains news
     * @return list with news
     */
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
            BoardNews parsedNews = parseHtmlElements(article);
            newsArrayList.add(parsedNews);
        }
        return newsArrayList;
    }

    /**
     * Parsed HTML element and create instance of {@link BoardNews}
     * @param newsTopic {@link Element} with news topic
     * @return instance of BoardNews
     */
    private BoardNews parseHtmlElements(Element newsTopic) {
        StringBuilder smallDesc = new StringBuilder();
        Elements pageElement;
        String articleTitle;
        String articleUrl;
        String imageUrl;
        String articleDate;
        pageElement = newsTopic.select(".topic-title a");
        articleTitle=pageElement.text();
        articleUrl=pageElement.attr("href");
        pageElement = newsTopic.select(".preview img");
        imageUrl= pageElement.attr("src");
        pageElement = newsTopic.select(".topic-header time");
        articleDate=pageElement.text();
        pageElement = newsTopic.select(".topic-content.text");
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

    public void clearDataBase() {
        BoardNews.deleteAll(BoardNews.class);
    }

    public void setAdapter(NewsRecordAdapter adapter) {
        this.mAdapter = adapter;
    }
    private enum RequestType{
        REQUEST_FOR_DB,
        REQUEST_FOR_INTERNET
    }

}
