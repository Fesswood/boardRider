package info.goodline.boardrider.data;

import android.content.Context;

import info.goodline.boardrider.adapter.NewsRecordAdapter;

/**
 * Created by Балдин Сергей on 08.05.2015.
 */
public class BoardNewsLab {

        private NewsRecordAdapter mNewsRecordAdapter;
        private static BoardNewsLab sBoardNewsLab;
        private Context mAppContext;

        private BoardNewsLab(Context appContext) {
            mAppContext = appContext;
            mNewsRecordAdapter = new NewsRecordAdapter(appContext);
        }

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
