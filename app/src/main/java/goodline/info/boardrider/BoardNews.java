package goodline.info.boardrider;


import android.os.Parcel;
import android.os.Parcelable;

import java.text.DateFormat;
import java.text.DateFormatSymbols;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Created by Балдин Сергей on 06.05.2015.
 */
public class BoardNews implements Comparable<BoardNews>, Parcelable {
  private  String mTitle;
  private  String mSmallDesc;
  private  String mImageUrl;
  private  String mArticleUrl;
  private  String mStringDate;
  public static DateFormat sJUD;


    public BoardNews(String title, String smallDesc, String imageUrl, String articleUrl, String stringDate) {
        this.mSmallDesc=smallDesc;
        this.mStringDate = stringDate;
        this.mTitle = title;
        this.mImageUrl = imageUrl;
        this.mArticleUrl=articleUrl;
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
        return mStringDate;
    }

    public void setStringDate(String stringDate) {
        mStringDate = stringDate;
    }

    @Override
    public int compareTo(BoardNews another) {



        try {
            Date currentDate =  sJUD.parse(mStringDate);
            Date anotherDate =  sJUD.parse(another.mStringDate);

            if(currentDate.before(anotherDate))
            {
                /* текущее меньше полученного */
                return -1;
            }
            else if(currentDate.after(anotherDate))
            {
                 /* текущее больше полученного */
                return 1;
            }

        } catch (ParseException e) {
            e.printStackTrace();
        }



        return 0;
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
        dest.writeString(mStringDate);
    }
    // this is used to regenerate your object. All Parcelables must have a CREATOR that implements these two methods
    public static final Parcelable.Creator<BoardNews> CREATOR = new Parcelable.Creator<BoardNews>() {

        public BoardNews createFromParcel(Parcel in) {
            return new BoardNews(in.readString(), in.readString(), in.readString(),in.readString(),in.readString());
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
}
