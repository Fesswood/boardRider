package info.goodline.boardrider.sqllite;

import android.content.Context;

import java.util.ArrayList;
import java.util.List;

import info.goodline.boardrider.data.BoardNews;


/**
 *  Class for operate with database via {@link com.orm.SugarDb}
 */
public class SugarORM {

   public static ArrayList<BoardNews> getNews() {
        return new ArrayList<>(BoardNews.listAll(BoardNews.class));
    }

    /**
     * Select news starts with offsets 10 * startPage, count of selected news limited by 10
     * @param startPage index of start page
     * @return
     */
    public static ArrayList<BoardNews> getNewsFromPage(int startPage) {
        List<BoardNews> oldNewsList = BoardNews.find(BoardNews.class, null, null, null,"m_time_stamp desc", "" + (10 * startPage) + ", 10");
        return new ArrayList<>(oldNewsList);
    }
    /**
     * Select news between dates of inputs news
     * @param firstBoardNews news with date of the range start
     * @param lastBoardNews news with date of the range end
     * @return
     */
    public static ArrayList<BoardNews> getNewsByDate(BoardNews firstBoardNews, BoardNews lastBoardNews) {
        // it's strange but calling this method with "between" instead of two comparisons return empty list
        List<BoardNews> oldNewsList =  BoardNews.find(BoardNews.class, "m_time_stamp >= ? and m_time_stamp <= ?"
                                                                              ,""+firstBoardNews.getTimeStamp()
                                                                              ,""+lastBoardNews.getTimeStamp());
      return new ArrayList<>(oldNewsList);
    }
    /**
     * save and update news in database
     * @param news list with news to save in database
     */
    public static void insertNews(ArrayList<BoardNews> news) {
        for(BoardNews boardNews: news){
           boardNews.save();
        }
    }
    /**
     * Update news etry in database add content of news topic to it
     * @param boardNews news for update
     */
    public static void updateNews(BoardNews boardNews) {
        List<BoardNews> news = BoardNews.findWithQuery(BoardNews.class, "Select * from BOARD_NEWS where m_time_stamp = ?", ""+boardNews.getTimeStamp());
        if(news.size()>0){
            BoardNews selectedBoardNews = news.get(0);
            selectedBoardNews.setArticleContent(boardNews.getArticleContent());
            selectedBoardNews.save();
        }
    }
}
