package goodline.info.sqllite;

import android.content.Context;

import java.util.ArrayList;
import java.util.List;

import goodline.info.boardrider.BoardNews;

/**
 * Created by Балдин Сергей on 14.05.2015.
 */
public class SugarORM {

   public static ArrayList<BoardNews> getNews() {
        return new ArrayList<>(BoardNews.listAll(BoardNews.class));
    }
    public static ArrayList<BoardNews> getNewsFromPage(int startpage, int pageNumber) {
      //  List<BoardNews> oldNewsList = BoardNews.find(BoardNews.class,null,null,null,"ORDER BY m_time_stamp DESC", "LIMIT" + (10 * pageNumber), "OFFSET" + (10 * startpage));
        List<BoardNews> oldNewsList = BoardNews.findWithQuery(BoardNews.class,"Select * from BoardNews LIMIT ? OFFSET ?",""+(10 * pageNumber),""+ (10 * startpage));
        return new ArrayList<>(oldNewsList);
    }
    public static ArrayList<BoardNews> getNewsByDate(BoardNews firstBoardNews, BoardNews lastBoardNews) {
      List<BoardNews> oldNewsList =  BoardNews.find(BoardNews.class, "m_time_stamp between ? and ?", ""+firstBoardNews.getTimeStamp(), ""+lastBoardNews.getTimeStamp());
      return new ArrayList<>(oldNewsList);
    }
    public static void insertNews(ArrayList<BoardNews> news) {
        for(BoardNews boardNews: news){
           boardNews.save();
        }
    }
}
