package goodline.info.boardrider;

import android.content.Context;

import java.util.ArrayList;

/**
 * Created by Балдин Сергей on 06.05.2015.
 */
public class BoardNewsLab {
    private ArrayList<BoardNews> mNewsList;

    private static BoardNewsLab sBoardNewsLab;
    private Context mAppContext;

    private BoardNewsLab(Context appContext) {
        mAppContext = appContext;
        mNewsList = new ArrayList<>();

    }

    public static BoardNewsLab get(Context c) {
        if (sBoardNewsLab == null) {
            sBoardNewsLab = new BoardNewsLab(c.getApplicationContext());
        }
        return sBoardNewsLab;
    }

    public ArrayList<BoardNews> getCrimes() {
        return mNewsList;
    }
}
