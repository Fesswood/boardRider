package goodline.info.boardrider;


/**
 * Created by Балдин Сергей on 06.05.2015.
 */
public class BoardNews {
  private  String mTitle;
  private  String mImageUrl;
  private  String mArticleUrl;
  private  String mStringDate;

    public BoardNews(String title,String imageUrl,String articleUrl,String stringDate) {
        this.mStringDate = stringDate;
        this.mTitle = title;
        this.mImageUrl = imageUrl;
        this.mArticleUrl=articleUrl;
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
}
