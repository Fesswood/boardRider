package goodline.info.boardrider;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by Балдин Сергей on 06.05.2015.
 */
public class NewsRecordAdapter extends ArrayAdapter<BoardNews> {

    private ArrayList<BoardNews> mNewslist;

    public NewsRecordAdapter(Context context) {
        super(context, R.layout.news_list_item);
        mNewslist=new ArrayList<>();
    }

    public void addNewslist(ArrayList<BoardNews> parsedNewsList) {
        mNewslist.addAll(parsedNewsList);
        notifyDataSetChanged();
    }
    public List<BoardNews> getNewsList() {
       return mNewslist;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if(convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.news_list_item, parent, false);
        }

        // NOTE: You would normally use the ViewHolder pattern here
       ImageView imageView = (ImageView) convertView.findViewById(R.id.news_image);
        TextView titleView = (TextView) convertView.findViewById(R.id.news_title);
        TextView dateView = (TextView) convertView.findViewById(R.id.news_date);
        BoardNews newsItem = mNewslist.get(position);
        Picasso.with(getContext())
                .load(newsItem.getImageUrl())
                .into(imageView);
        titleView.setText(newsItem.getTitle());
        dateView.setText(newsItem.getStringDate());
        return convertView;
    }
    @Override
    public int getCount() {
        return mNewslist.size();
    }

    // getItem(int) in Adapter returns Object but we can override
    // it to BananaPhone thanks to Java return type covariance
    @Override
    public BoardNews getItem(int position) {
        return mNewslist.get(position);
    }

    // getItemId() is often useless, I think this should be the default
    // implementation in BaseAdapter
    @Override
    public long getItemId(int position) {
        return position;
    }

    public void prependNewsList(ArrayList<BoardNews> newsList) {
        for(BoardNews boardNews: newsList){
            mNewslist.add(0,boardNews);
        }
        notifyDataSetChanged();
    }
}