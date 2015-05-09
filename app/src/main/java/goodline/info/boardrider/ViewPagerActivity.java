package goodline.info.boardrider;

import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;


public class ViewPagerActivity extends FragmentActivity {

    ViewPager mViewPager;
    public ArrayList<BoardNews> mNewsList;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mViewPager = new ViewPager(this);
        mViewPager.setId(R.id.viewPager);
        setContentView(mViewPager);

        final NewsRecordAdapter newsRecordAdapter = BoardNewsLab.get(this).getNewsRecordAdapter();
        mNewsList = newsRecordAdapter.getNewsList();
        FragmentManager fm = getSupportFragmentManager();
        mViewPager.setAdapter(new FragmentStatePagerAdapter(fm) {
            @Override
            public int getCount() {
                return mNewsList.size();
            }
            @Override
            public Fragment getItem(int pos) {
                BoardNews item = mNewsList.get(pos);
                return boardNewsFragment.newInstance(item);
            }
        });

        BoardNews boardNews = getIntent().getParcelableExtra(BoardRiderFragment.SELECTED_NEWS);
        for (int i = 0; i < mNewsList.size(); i++) {
            if (mNewsList.get(i).equals(boardNews)) {
                mViewPager.setCurrentItem(i);
                break;
            }
        }
        mViewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            public void onPageScrollStateChanged(int state) { }
            public void onPageScrolled(int pos, float posOffset, int posOffsetPixels) {
            }
            public void onPageSelected(int pos) {
                BoardNews boardNews = mNewsList.get(pos);
                if (boardNews.getTitle() != null) {
                    setTitle(boardNews.getTitle());
                }
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_view_pager, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
