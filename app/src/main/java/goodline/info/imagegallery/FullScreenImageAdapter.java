package goodline.info.imagegallery;

import android.widget.ImageView;



import java.util.ArrayList;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.squareup.picasso.Picasso;

import goodline.info.boardrider.R;

/**
 * Created by Балдин Сергей on 26.05.2015.
 */
public class FullScreenImageAdapter extends PagerAdapter {

    private Activity _activity;
    private ArrayList<String> mImageLinks;
    private LayoutInflater inflater;

    // constructor
    public FullScreenImageAdapter(Activity activity,
                                  ArrayList<String> imageLinks) {
        this._activity = activity;
        this.mImageLinks = imageLinks;
    }

    @Override
    public int getCount() {
        return this.mImageLinks.size();
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == ((RelativeLayout) object);
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        ImageView imgDisplay;
        Button btnClose;

        inflater = (LayoutInflater) _activity
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View viewLayout = inflater.inflate(R.layout.image_gallery_item, container,
                false);

        imgDisplay = (ImageView) viewLayout.findViewById(R.id.imgDispImgGal);
        btnClose = (Button) viewLayout.findViewById(R.id.btnCloseImgGal);

        String currentImageLink = mImageLinks.get(position);

        if(!currentImageLink.isEmpty()){
            Picasso.with(_activity)
                    .load(currentImageLink)
                    .fit()
                    .centerCrop()
                    .into(imgDisplay);
        }else{
            Picasso.with(_activity)
                    .load(R.drawable.transparent_bg)
                    .fit()
                    .centerCrop()
                    .into(imgDisplay);
        }



        // close button click event
        btnClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                _activity.finish();
            }
        });

        ((ViewPager) container).addView(viewLayout);

        return viewLayout;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        ((ViewPager) container).removeView((RelativeLayout) object);

    }
}