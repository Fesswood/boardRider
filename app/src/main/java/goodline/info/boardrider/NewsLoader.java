package goodline.info.boardrider;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

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

import goodline.info.sqllite.BoardNewsORM;
import goodline.info.sqllite.SugarORM;
import valleyapp.VolleyApplication;

/**
 * Created by Крабов on 11.05.2015.
 */
public class NewsLoader {


    private int mPageIndex=1;
    private Context mContext;
    private String mBoardUrl;
    private String mErrorMessage;
    private int mErrorCode;
    private ArrayList<BoardNews> mData;
    private boolean mIsDataLoadCorrectly;
    private boolean mIsRecursive=true;

    private static final String TAG="NewsLoader";

    private static final int INTERNET_CONNECTION_ERROR=1;


    public NewsLoader(String dataUri) {
        mBoardUrl=dataUri;
    }
    public NewsLoader(String dataUri, Context context) {
        mBoardUrl=dataUri;
        mContext=context;
    }
    public boolean fechFromDB(int offsetPages, boolean isNextPageNeeded){
        if(offsetPages == (mPageIndex+1)){
            throw new IllegalArgumentException(TAG+": startpage and mPageIndex must be different value: mPageIndex="+mPageIndex+" startindex"+offsetPages );
        }

       // mData = BoardNewsORM.getPostsFromPage(mContext, offset, mPageIndex);
          mData = SugarORM.getNewsFromPage(offsetPages, mPageIndex+1);

        if(isNextPageNeeded || mPageIndex==1){
            mPageIndex++;
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
        //   ArrayList<BoardNews> newsBetweenDates= BoardNewsORM.getPostsByDate(mContext, firstBoardNews, lastBoardNews);
            ArrayList<BoardNews> newsBetweenDates= SugarORM.getNewsByDate(firstBoardNews, lastBoardNews);
            // if database doesn't keep any rows between dates add all new rows to database
            if(newsBetweenDates.size()==0){
                SugarORM.insertNews(mData);
                return true;
            }else{
                boolean isCollectionModified = mData.removeAll(newsBetweenDates);
                if(isCollectionModified){
                    SugarORM.insertNews(mData);
                    mData.addAll(newsBetweenDates);
                    return true;
                }
            }
        return false;
    }
    public boolean updateAllDB(){
        int iterator=1;
        StringRequest stringRequest;
        while(iterator<100){
                RequestFuture<String> future = RequestFuture.newFuture();
                stringRequest = new StringRequest(Request.Method.GET, mBoardUrl + iterator, future, future);
                VolleyApplication.getInstance().getRequestQueue().add(stringRequest);

                String response = null;

                try {
                    response = future.get(20, TimeUnit.SECONDS);

                } catch (TimeoutException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (ExecutionException e) {
                    e.printStackTrace();
                }
                ArrayList<BoardNews> temp=parse(response);
                temp.removeAll(mData);
                for(BoardNews boardNews: temp){
                    mData.add(0,boardNews);
                }
                boolean isDataBaseChanges=syncDB();
                if(isDataBaseChanges){
                    break;
                }
             iterator++;
            }
        return true;
    }

    public boolean fetchFromInternet(int startpage, boolean isNextPageNeeded) throws ExecutionException, InterruptedException {
        StringRequest stringRequest;
        for (int i=startpage; i<mPageIndex+1; i++){
            RequestFuture<String> future = RequestFuture.newFuture();
            stringRequest = new StringRequest(Request.Method.GET, mBoardUrl+i, future, future);

            VolleyApplication.getInstance().getRequestQueue().add(stringRequest);
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
            // TODO Auto-generated catch block
            Log.e(TAG,""+e.getMessage(),e);
        }
        Elements articles = doc.select(".list-topic .topic.topic-type-topic.js-topic.out-topic");
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
            imageUrl= pageElement.attr("src");
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

    public static boolean isOnline(Context context) {
        ConnectivityManager cm =
                (ConnectivityManager)  context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnectedOrConnecting();
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
        mData=SugarORM.getNews();
    }
}
