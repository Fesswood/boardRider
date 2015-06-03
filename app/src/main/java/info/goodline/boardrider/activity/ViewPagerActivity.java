package info.goodline.boardrider.activity;

import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.os.Bundle;

import java.util.ArrayList;

import info.goodline.boardrider.data.BoardNews;
import info.goodline.boardrider.fragment.NewsListFragment;
import info.goodline.boardrider.data.BoardNewsLab;
import info.goodline.boardrider.adapter.NewsRecordAdapter;
import info.goodline.boardrider.fragment.NewsTopicFragment;
import goodline.info.boardrider.R;


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
            public CharSequence getPageTitle(int position) {
                return  mNewsList.get(position).getTitle();
            }

            @Override
            public int getCount() {
                return mNewsList.size();
            }
            @Override
            public Fragment getItem(int pos) {
                BoardNews item = mNewsList.get(pos);
                return NewsTopicFragment.newInstance(item);
            }
        });

        BoardNews boardNews =(BoardNews) getIntent().getSerializableExtra(NewsListFragment.SELECTED_NEWS);
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


}
