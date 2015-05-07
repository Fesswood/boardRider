package goodline.info.boardrider;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.NetworkImageView;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import valleyapp.VolleyApplication;

/**
 * Created by Балдин Сергей on 06.05.2015.
 */
public class NewsRecordAdapter extends ArrayAdapter<BoardNews> {
   // private ImageLoader mImageLoader;

    public NewsRecordAdapter(Context context) {
        super(context, R.layout.news_list_item);
       // mImageLoader = new ImageLoader(VolleyApplication.getInstance().getRequestQueue(), new BitmapLruCache());
    }

    public void addNewsItems(ArrayList<BoardNews> parsedNewsList) {

        addAll(parsedNewsList);
        notifyDataSetChanged();
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
        BoardNews newsItem = getItem(position);
        Picasso.with(getContext())
                .load(newsItem.getImageUrl())
                .into(imageView);
        titleView.setText(newsItem.getTitle());
        dateView.setText(newsItem.getStringDate());
        return convertView;
    }
}