package info.goodline.boardrider.adapter;

import android.content.res.Resources;


import java.util.ArrayList;

import android.app.Activity;
import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;

import goodline.info.boardrider.R;
import info.goodline.boardrider.UI.TouchImageView;
import info.goodline.boardrider.activity.ImageGalleryActivity;
import valleyapp.BoardNewsApplication;

import static valleyapp.BoardNewsApplication.*;


public class FullScreenImageAdapter extends PagerAdapter {

    private final Resources resources;
    private Activity mActivity;
    private ArrayList<String> mImageLinks;
    private LayoutInflater inflater;
    private TouchImageView imgDisplay;

    public FullScreenImageAdapter(ImageGalleryActivity imageGalleryActivity, ArrayList<String> imageLinksList, Resources resources) {
        this.mActivity = imageGalleryActivity;
        this.mImageLinks = imageLinksList;
        this.resources = resources;
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

        inflater = (LayoutInflater) mActivity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View viewLayout = inflater.inflate(R.layout.image_gallery_item, container,
                false);

        imgDisplay = (TouchImageView) viewLayout.findViewById(R.id.imgDispImgGal);
        loadImage(mImageLinks.get(position), imgDisplay);
        ((ViewPager) container).addView(viewLayout);

        return viewLayout;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        ((ViewPager) container).removeView((RelativeLayout) object);

    }



}