package info.goodline.boardrider.data;

import android.content.Context;

import info.goodline.boardrider.adapter.NewsRecordAdapter;

/**
 * Singleton for retrieving {@link NewsRecordAdapter}
 * between {@link info.goodline.boardrider.fragment.NewsListFragment} and {@link info.goodline.boardrider.activity.ViewPagerActivity}
 *  @author  Sergey Baldin
 */
public class BoardNewsLab {

        private NewsRecordAdapter mNewsRecordAdapter;
        private static BoardNewsLab sBoardNewsLab;
        private Context mAppContext;

        private BoardNewsLab(Context appContext) {
            mAppContext = appContext;
            mNewsRecordAdapter = new NewsRecordAdapter(appContext);
        }

    /**
     * Constructor of  BoardNewsLab Singleton
     * @param c Current context
     * @return instanse of BoardNewsLab
     */
        public static BoardNewsLab get(Context c) {
            if (sBoardNewsLab == null) {
                sBoardNewsLab = new BoardNewsLab(c.getApplicationContext());
            }
            return sBoardNewsLab;
        }


        public NewsRecordAdapter getNewsRecordAdapter() {
            return mNewsRecordAdapter;
        }
}
