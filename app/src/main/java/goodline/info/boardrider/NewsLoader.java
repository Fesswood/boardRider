package goodline.info.boardrider;

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

import java.net.ConnectException;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import valleyapp.VolleyApplication;

/**
 * Created by Крабов on 11.05.2015.
 */
public class NewsLoader {


    private int mPageIndex=1;
    private String mBoardUrl;
    private String mErrorMessage;
    private ArrayList<BoardNews> mData;
    private boolean mDataLoadResult;

    private static final String TAG="NewsLoader";


    public NewsLoader( String dataUri) {
        mBoardUrl=dataUri;
    }

    public boolean fetch(int startpage, boolean isNextPageNeeded) throws ExecutionException, InterruptedException {
        StringRequest stringRequest;
        for (int i=startpage; i<mPageIndex+1; i++){
            RequestFuture<String> future = RequestFuture.newFuture();
            stringRequest = new StringRequest(Request.Method.POST, mBoardUrl+i, future, future);

            VolleyApplication.getInstance().getRequestQueue().add(stringRequest);
            String response = null;

            try {
                response = future.get(20, TimeUnit.SECONDS);
            } catch (TimeoutException e) {
                e.printStackTrace();
            }
            mData=parse(response);
            mDataLoadResult =true;
        }
        if(isNextPageNeeded || mPageIndex==1){
            mPageIndex++;
        }


        return mDataLoadResult;
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
}
