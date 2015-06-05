package info.goodline.boardrider.adapter;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import goodline.info.boardrider.R;
import info.goodline.boardrider.data.BoardNews;

/**
 * NewsRecordAdapter displaying news in list item into {@link info.goodline.boardrider.fragment.NewsListFragment}
 *  @author  Sergey Baldin
 */
public class NewsRecordAdapter extends ArrayAdapter<BoardNews> {
    /**
     * List of news topics
     */
    private ArrayList<BoardNews> mNewslist;
    private final Context mContext;
    private LayoutInflater mInflater;

    public NewsRecordAdapter(Context context) {
        super(context, R.layout.news_list_item);
        mNewslist=new ArrayList<>();
        mContext=context;
        mInflater= (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        BoardNews newsItem = mNewslist.get(position);
        ViewHolder holder;
        if(convertView == null) {
            convertView = mInflater.inflate(R.layout.news_list_item, null, true);
            holder  = getViewHolder(convertView);
        } else{
            holder = (ViewHolder) convertView.getTag();
        }
        displayNewsImage(newsItem, holder.imageView);

        holder.titleView.setText(newsItem.getTitle());
        holder.dateView.setText(newsItem.getStringDate());

        if(mContext.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE ){
             holder.descView.setText(newsItem.getSmallDesc());
        }
        return convertView;
    }


    /**
     * Get viewHolder by tag or create new if it doesn't exist
     * @param convertView view of news topic
     * @return instance of viewHolder
     */
    private ViewHolder getViewHolder(View convertView) {
        ViewHolder holder;
        holder = new ViewHolder();
        holder.titleView = (TextView) convertView.findViewById(R.id.news_title);
        holder.dateView = (TextView) convertView.findViewById(R.id.news_date);
        if(mContext.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE ){
            holder.descView = (TextView) convertView.findViewById(R.id.news_desc);
        }
        holder.imageView = (ImageView) convertView.findViewById(R.id.news_image);
        convertView.setTag(holder);
        return holder;
    }

    /**
     * Load image to ImageView using {@link ImageLoader}
     * or set polyfill if there is no image
     * @param newsItem news topic contains image
     * @param imageView imageView to display image
     */
    private void displayNewsImage(BoardNews newsItem, ImageView imageView) {
        if(!newsItem.getImageUrl().isEmpty()){
            ImageLoader.getInstance().displayImage(newsItem.getImageUrl(), imageView);

        }else{
           imageView.setImageResource(R.drawable.image_polyfill);
        }
    }
    @Override
    public int getCount() {
        return mNewslist.size();
    }

    @Override
    public BoardNews getItem(int position) {
        return mNewslist.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }
    /**
     * Add list with news to current NewsList
     * @param parsedNewsList list to add
     */
    public void addNewslist(ArrayList<BoardNews> parsedNewsList) {
        parsedNewsList.removeAll(mNewslist);
        mNewslist.addAll(parsedNewsList);
        notifyDataSetChanged();
    }
    public ArrayList<BoardNews> getNewsList() {
        return mNewslist;
    }

    /**
     * prepend list with news to current NewsList
     * used when news received from "pull to update"
     * @param newsList list to prepend
     */
    public void prependNewsList(ArrayList<BoardNews> newsList) {
        for(BoardNews boardNews: newsList){
            mNewslist.add(0,boardNews);
        }
        notifyDataSetChanged();
    }

    /**
     * implementation of ViewHolder pattern for NewsRowAdapter
     */
    static class ViewHolder {
        public ImageView imageView;
        public TextView titleView;
        public TextView dateView;
        public TextView descView;
    }
}