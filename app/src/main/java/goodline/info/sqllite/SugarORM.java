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
        List<BoardNews> oldNewsList = BoardNews.find(BoardNews.class, null, null, null,"m_time_stamp desc", "" + (10 * startpage) + "," + (10 * pageNumber));
        return new ArrayList<>(oldNewsList);
    }
    public static ArrayList<BoardNews> getNewsByDate(BoardNews firstBoardNews, BoardNews lastBoardNews) {
        String WhereStatemnt= "m_time_stamp between "+firstBoardNews.getTimeStamp()+" and "+lastBoardNews.getTimeStamp();
        List<BoardNews> temp =  BoardNews.find(BoardNews.class,  "m_time_stamp=?",""+firstBoardNews.getTimeStamp());
        List<BoardNews> temp1 =  BoardNews.find(BoardNews.class,  "m_time_stamp=?",""+lastBoardNews.getTimeStamp());
      List<BoardNews> oldNewsList =  BoardNews.find(BoardNews.class, "m_time_stamp >= ? and m_time_stamp <= ?"
                                                                              ,""+firstBoardNews.getTimeStamp()
                                                                              ,""+lastBoardNews.getTimeStamp());
      return new ArrayList<>(oldNewsList);
    }
    public static void insertNews(ArrayList<BoardNews> news) {
        for(BoardNews boardNews: news){
           boardNews.save();
        }
    }

    public static void updateNews(BoardNews boardNews) {
        List<BoardNews> news = BoardNews.findWithQuery(BoardNews.class, "Select * from BOARD_NEWS where m_time_stamp = ?", ""+boardNews.getTimeStamp());
        if(news.size()>0){
            BoardNews selectedBoardNews = news.get(0);
            selectedBoardNews.setArticleContent(boardNews.getArticleContent());
            selectedBoardNews.save();
        }
    }
}
