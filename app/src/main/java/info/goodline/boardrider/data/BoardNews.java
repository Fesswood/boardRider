package info.goodline.boardrider.data;


import android.os.Parcel;
import android.os.Parcelable;

import com.orm.SugarRecord;
import com.orm.dsl.Ignore;

import java.io.Serializable;
import java.text.DateFormat;
import java.text.DateFormatSymbols;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 *  Class for represent entity of News
 *  used in {@link info.goodline.boardrider.fragment.NewsListFragment} for displaying list of news
 *  used in {@link info.goodline.boardrider.fragment.NewsTopicFragment} for displaying content of news
 *  @author  Sergey Baldin
 */
public class BoardNews extends SugarRecord<BoardNews> implements Comparable<BoardNews>, Serializable {
  private  String mTitle;
  private  String mSmallDesc;
  private  String mImageUrl;
  private  String mArticleUrl;
  private  String mArticleContent;
  private long mTimeStamp;
  public static final  String PACKAGE_CLASS="info.goodline.boardrider.SugarRecord";
    /**
     * Date formater for output human readable date in the news list
     */
  @Ignore
  public static DateFormat sJUD;

    /**
     * Create new instance of news topic
     * @param title Title of news
     * @param smallDesc Small Description, used in the landscape orientation
     * @param imageUrl Url of news thumbnail
     * @param articleUrl Url of article
     * @param stringDate string contains date, parsed into long mTimeStamp
     */
    public BoardNews(String title, String smallDesc, String imageUrl, String articleUrl, String stringDate) {
        this.mSmallDesc=smallDesc;
        this.mTitle = title;
        this.mImageUrl = imageUrl;
        this.mArticleUrl=articleUrl;
        this.mArticleContent="";
        JUDInit();
        try {
            this.mTimeStamp=sJUD.parse(stringDate).getTime();
        } catch (ParseException e) {
            e.printStackTrace();
            mTimeStamp=0;
        }
    }



    public BoardNews() {
        mTitle = "";
        mSmallDesc = "";
        mImageUrl = "";
        mArticleUrl = "";
        mTimeStamp=0;
        setArticleContent("");
        JUDInit();
    }

    /**
     * Initialize static SimpleDateFormater for displaying dates
     */
    private void JUDInit() {
        if(sJUD==null){
            Locale russian = new Locale("ru");
            String[] newMonths = {
                    "января", "февраля", "марта", "апреля", "мая", "июня",
                    "июля", "августа", "сентября", "октября", "ноября", "декабря"};
            DateFormatSymbols dfs = DateFormatSymbols.getInstance(russian);
            dfs.setMonths(newMonths);
            DateFormat df = DateFormat.getDateInstance(DateFormat.LONG, russian);
            SimpleDateFormat sdf = (SimpleDateFormat) df;
            sdf.setDateFormatSymbols(dfs);
            sJUD  =  new SimpleDateFormat("d MMMM yyyy, HH:mm", new Locale("ru"));
        }
    }

    /**
     * Compare dates of current news and another
     * @param another news to compare
     * @return like a compareTo() {@link Date}
     */
    @Override
    public int compareTo(BoardNews another) {
        int result=-1;
        if(another==null){
            throw new NullPointerException("another entry is null!");
        }
            Date currentDate =  new Date(mTimeStamp);
            Date anotherDate =  new Date(another.mTimeStamp);
            result = currentDate.compareTo(anotherDate);

        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        if (this.compareTo((BoardNews)obj) != 0)
            return false;
        return true;
    }
    public String getSmallDesc() {
        return mSmallDesc;
    }

    public long getTimeStamp() {
        return mTimeStamp;
    }

    public String getArticleContent() {
        return mArticleContent;
    }

    public void setArticleContent(String articleContent) {
        mArticleContent = articleContent;
    }

    public String getTitle() {
        return mTitle;
    }

    public void setTitle(String title) {
        mTitle = title;
    }

    public String getImageUrl() {
        return mImageUrl;
    }

    public void setImageUrl(String imageUrl) {
        mImageUrl = imageUrl;
    }

    public String getArticleUrl() {
        return mArticleUrl;
    }

    public void setArticleUrl(String articleUrl) {
        mArticleUrl = articleUrl;
    }

    public String getStringDate() {
        Date d= new Date(mTimeStamp);

        return sJUD.format(d);
    }

}
