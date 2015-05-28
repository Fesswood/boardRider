package info.goodline.boardrider.data;


import android.os.Parcel;
import android.os.Parcelable;

import com.orm.SugarRecord;
import com.orm.dsl.Ignore;

import java.text.DateFormat;
import java.text.DateFormatSymbols;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Created by Балдин Сергей on 06.05.2015.
 */

public class BoardNews extends SugarRecord<BoardNews> implements Comparable<BoardNews>, Parcelable {
  private  String mTitle;
  private  String mSmallDesc;
  private  String mImageUrl;
  private  String mArticleUrl;
  private  String mArticleContent;
  private long mTimeStamp;
  public static final  String PACKAGE_CLASS="info.goodline.boardrider.SugarRecord";
  @Ignore
  public static DateFormat sJUD;

    public BoardNews(String title, String smallDesc, String imageUrl, String articleUrl, long timeStamp) {
        this.mSmallDesc=smallDesc;
        this.mTitle = title;
        this.mImageUrl = imageUrl;
        this.mArticleUrl=articleUrl;
        this.mTimeStamp=timeStamp;
        this.mArticleContent="";
        JUDInit();

    }
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

    public void setStringDate(String stringDate) {
        try {
            this.mTimeStamp=sJUD.parse(stringDate).getTime();
        } catch (ParseException e) {
            e.printStackTrace();
            mTimeStamp=0;
        }
    }

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
    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(mTitle);
        dest.writeString(mSmallDesc);
        dest.writeString(mImageUrl);
        dest.writeString(mArticleUrl);
        dest.writeLong(mTimeStamp);
        dest.writeString(mArticleContent);
    }
    // this is used to regenerate your object. All Parcelables must have a CREATOR that implements these two methods
    public static final Creator<BoardNews> CREATOR = new Creator<BoardNews>() {

        public BoardNews createFromParcel(Parcel in) {
         BoardNews createdParcel = new BoardNews(in.readString(), in.readString(), in.readString(),in.readString(),in.readLong());
            createdParcel.setArticleContent(in.readString());
            return createdParcel;
        }

        public BoardNews[] newArray(int size) {
            return new BoardNews[size];
        }
    };

    public String getSmallDesc() {
        return mSmallDesc;
    }

    public void setSmallDesc(String smallDesc) {
        mSmallDesc = smallDesc;
    }

    public long getTimeStamp() {
        return mTimeStamp;
    }
    public void setTimeStamp(long timeStamp) {
        mTimeStamp = timeStamp;
    }

    public String getArticleContent() {
        return mArticleContent;
    }

    public void setArticleContent(String articleContent) {
        mArticleContent = articleContent;
    }
}
