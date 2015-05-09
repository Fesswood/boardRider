package goodline.info.boardrider;

import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;


/**
 * A placeholder fragment containing a simple view.
 */
public class ViewPagerActivityFragment extends Fragment {

    ViewPager mViewPager;


    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        mViewPager =(ViewPager) getView().findViewById(R.id.view_pager);

        final NewsRecordAdapter newsRecordAdapter = BoardNewsLab.get(getActivity()).getNewsRecordAdapter();
        final List<BoardNews> newsList = newsRecordAdapter.getNewsList();
        FragmentManager fm = getFragmentManager();
        mViewPager.setAdapter(new FragmentStatePagerAdapter(fm) {
            @Override
            public int getCount() {
                return newsList.size();
            }
            @Override
            public Fragment getItem(int pos) {
                BoardNews item = newsList.get(pos);
                return boardNewsFragment.newInstance(item);
            }
        });

        BoardNews boardNews = getActivity().getIntent().getParcelableExtra(BoardRiderFragment.SELECTED_NEWS);
        for (int i = 0; i < newsList.size(); i++) {
            if (newsList.get(i).compareTo(boardNews)==0) {
                mViewPager.setCurrentItem(i);
                break;
            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_view_pager, container, false);
    }
}
