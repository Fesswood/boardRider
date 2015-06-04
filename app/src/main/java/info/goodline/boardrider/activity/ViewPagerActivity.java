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

/**
 *  Activity provide ViewPager to represent news topic to user
 *  Activity contains one element - viewPager,  news list are received from {@link BoardNewsLab},
 *  after that FragmentStateAdapter puts current news topic into {@link NewsTopicFragment} and show to user
 *  @author  Sergey Baldin
 */
public class ViewPagerActivity extends FragmentActivity implements  ViewPager.OnPageChangeListener{

    ViewPager mViewPager;
    public ArrayList<BoardNews> mNewsList;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mViewPager = new ViewPager(this);
        mViewPager.setId(R.id.viewPager);
        setContentView(mViewPager);

        NewsRecordAdapter newsRecordAdapter = BoardNewsLab.get(this).getNewsRecordAdapter();
        NewsFragmentStatePagerAdapter newsAdapter = new NewsFragmentStatePagerAdapter(getSupportFragmentManager());

        //get news list from RecordAdapter
        mNewsList = newsRecordAdapter.getNewsList();
        mViewPager.setAdapter(newsAdapter);

        // get selected news and show it
        BoardNews boardNews =(BoardNews) getIntent().getSerializableExtra(NewsListFragment.SELECTED_NEWS);
        if (boardNews != null){
            setCurrentItem(boardNews);
        }else{
            throw new IllegalArgumentException("Intent must contains selected by user news!");
        }
        mViewPager.setOnPageChangeListener(this);
    }

    /**
     * Set current news topic to mViewPager
     * @param boardNews news topic to show
     */
    private void setCurrentItem(BoardNews boardNews) {
        for (int i = 0; i < mNewsList.size(); i++) {
            if (mNewsList.get(i).equals(boardNews)) {
                mViewPager.setCurrentItem(i);
                break;
            }
        }
    }

    @Override
    public void onPageSelected(int position) {
        // looks like that the activity without anything
        // except for programmatically created View doesn't show actionbar
        // or there are some errors with activity theme
        BoardNews boardNews = mNewsList.get(position);
        if (boardNews.getTitle() != null) {
            setTitle(boardNews.getTitle());
        }
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
        // nothing to do here
    }

    @Override
    public void onPageScrollStateChanged(int state) {
        // nothing to do here
    }

    /**
     *  FragmentStatePagerAdapter works with {@link #mNewsList} and provide
     *  new instance of NewsTopicFragment for displaying news topic
     */
    public  class NewsFragmentStatePagerAdapter extends FragmentStatePagerAdapter{

        public NewsFragmentStatePagerAdapter(FragmentManager fm) {
            super(fm);
        }

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
    }

}
