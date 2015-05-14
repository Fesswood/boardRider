package goodline.info.sqllite;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.util.ArrayList;

import goodline.info.boardrider.BoardNews;

/**
 * Created by ������ ������ on 12.05.2015.
 */
public class BoardNewsORM {

    private static final String TAG = "BoardNewsORM";

    private static final String TABLE_NAME = "BoardNews";

    private static final String COMMA_SEP = ", ";

    private static final String COLUMN_TITLE_TYPE = "TEXT";
    private static final String COLUMN_TITLE = "title";

    private static final String COLUMN_DESC_TYPE = "TEXT";
    private static final String COLUMN_DESC = "description";

    private static final String COLUMN_IMAGE_URL_TYPE = "TEXT";
    private static final String COLUMN_IMAGE_URL = "imageUrl";

    private static final String COLUMN_ARTICLE_URL_TYPE = "TEXT";
    private static final String COLUMN_ARTICLE_URL = "articleUrl";

    private static final String COLUMN_DATE_TYPE = "TEXT";
    private static final String COLUMN_DATE = "StringDate";


    public static final String SQL_CREATE_TABLE =
            "CREATE TABLE " + TABLE_NAME + " (" +
                    COLUMN_TITLE  + " " + COLUMN_TITLE_TYPE + COMMA_SEP +
                    COLUMN_DESC + " " + COLUMN_DESC_TYPE + COMMA_SEP +
                    COLUMN_IMAGE_URL + " " + COLUMN_IMAGE_URL_TYPE + COMMA_SEP +
                    COLUMN_ARTICLE_URL + " " + COLUMN_ARTICLE_URL_TYPE + COMMA_SEP +
                    COLUMN_DATE + " " + COLUMN_DATE_TYPE +
                    ")";

    public static final String SQL_DROP_TABLE =
            "DROP TABLE IF EXISTS " + TABLE_NAME;


    public static void insertPost(Context context, ArrayList<BoardNews> news) {
        DatabaseWrapper databaseWrapper = new DatabaseWrapper(context);
        SQLiteDatabase database = databaseWrapper.getWritableDatabase();
        ContentValues values;  long postId;
        for(BoardNews boardNews: news){
            values = postToContentValues(boardNews);
            postId = database.insert(BoardNewsORM.TABLE_NAME, "null", values);
            Log.i(TAG, "Inserted new Post with ID: " + postId);
        }
        database.close();
    }

    /**
     * Packs a Post object into a ContentValues map for use with SQL inserts.
     */
    private static ContentValues postToContentValues(BoardNews news) {
        ContentValues values = new ContentValues();
        values.put(BoardNewsORM.COLUMN_TITLE, news.getTitle());
        values.put(BoardNewsORM.COLUMN_DESC, news.getSmallDesc());
        values.put(BoardNewsORM.COLUMN_IMAGE_URL, news.getImageUrl());
        values.put(BoardNewsORM.COLUMN_ARTICLE_URL, news.getArticleUrl());
        values.put(BoardNewsORM.COLUMN_DATE, news.getStringDate());

        return values;
    }

    public static ArrayList<BoardNews> getPosts(Context context) {
        DatabaseWrapper databaseWrapper = new DatabaseWrapper(context);
        SQLiteDatabase database = databaseWrapper.getReadableDatabase();

        Cursor cursor = database.rawQuery("SELECT * FROM " + BoardNewsORM.TABLE_NAME, null);

        Log.i(TAG, "Loaded " + cursor.getCount() + " BoardNewss...");
        ArrayList<BoardNews> BoardNewsList = new ArrayList<BoardNews>();

        if(cursor.getCount() > 0) {
            cursor.moveToFirst();
            while (!cursor.isAfterLast()) {
                BoardNews boardNews = cursorToBoardNews(cursor);
                BoardNewsList.add(boardNews);
                cursor.moveToNext();
            }
            Log.i(TAG, "BoardNews loaded successfully.");
        }

        database.close();

        return BoardNewsList;
    }
    public static ArrayList<BoardNews> getPostsFromPage(Context context, int startpage, int pageNumber) {
        DatabaseWrapper databaseWrapper = new DatabaseWrapper(context);
        SQLiteDatabase database = databaseWrapper.getReadableDatabase();

        Cursor cursor = database.rawQuery("SELECT * FROM " + BoardNewsORM.TABLE_NAME
                                          + " LIMIT " +10 * pageNumber
                                          + " OFFSET "+ 10 * (startpage), null);

        Log.i(TAG, "Loaded " + cursor.getCount() + " BoardNewss...");
        ArrayList<BoardNews> BoardNewsList = new ArrayList<BoardNews>();

        if(cursor.getCount() > 0) {
            cursor.moveToFirst();
            while (!cursor.isAfterLast()) {
                BoardNews boardNews = cursorToBoardNews(cursor);
                BoardNewsList.add(boardNews);
                cursor.moveToNext();
            }
            Log.i(TAG, "BoardNews loaded successfully.");
        }

        database.close();

        return BoardNewsList;
    }

    /**
     * Populates a Post object with data from a Cursor
     * @param cursor
     * @return
     */
    private static BoardNews cursorToBoardNews(Cursor cursor) {
        BoardNews news = new BoardNews();
        news.setTitle(cursor.getString(cursor.getColumnIndex(COLUMN_TITLE)));
        news.setSmallDesc(cursor.getString(cursor.getColumnIndex(COLUMN_DESC)));
        news.setImageUrl(cursor.getString(cursor.getColumnIndex(COLUMN_IMAGE_URL)));
        news.setArticleUrl(cursor.getString(cursor.getColumnIndex(COLUMN_ARTICLE_URL)));
        news.setStringDate(cursor.getString(cursor.getColumnIndex(COLUMN_DATE)));

        return news;
    }

    public static ArrayList<BoardNews> getPostsByDate(Context context, BoardNews firstBoardNews, BoardNews lastBoardNews) {
        DatabaseWrapper databaseWrapper = new DatabaseWrapper(context);
        SQLiteDatabase database = databaseWrapper.getReadableDatabase();

        Cursor cursor = database.rawQuery("SELECT * FROM " + BoardNewsORM.TABLE_NAME
                + " WHERE "+COLUMN_DATE + " BETWEEN \'"
                + firstBoardNews.getTimeStamp()  + "\' AND \'" +11/8/2011+ "\'", null);

        Log.i(TAG, "Loaded " + cursor.getCount() + " BoardNewss...");
        ArrayList<BoardNews> BoardNewsList = new ArrayList<BoardNews>();

        if(cursor.getCount() > 0) {
            cursor.moveToFirst();
            while (!cursor.isAfterLast()) {
                BoardNews boardNews = cursorToBoardNews(cursor);
                BoardNewsList.add(boardNews);
                cursor.moveToNext();
            }
            Log.i(TAG, "BoardNews loaded successfully.");
        }

        database.close();

        return BoardNewsList;
    }
}
