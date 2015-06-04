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
import info.goodline.boardrider.data.BoardNewsLab;
import info.goodline.boardrider.fragment.NewsTopicFragment;
import valleyapp.BoardNewsApplication;

import static valleyapp.BoardNewsApplication.*;

/**
 *  Adapter for displaying images in FullScreenImageGallery
 *  @see ImageGalleryActivity
 *  @author  Sergey Baldin
 */
public class FullScreenImageAdapter extends PagerAdapter {


    private Activity mActivity;
    private LayoutInflater inflater;
    /**
     * ImageView with touch zoom support
     */
    private TouchImageView imgDisplay;
    /**
     *  List with image urls from content of news topic
     */
    private ArrayList<String> mImageLinks;

    public FullScreenImageAdapter(ImageGalleryActivity imageGalleryActivity, ArrayList<String> imageLinksList) {
        this.mActivity = imageGalleryActivity;
        this.mImageLinks = imageLinksList;
    }

    @Override
    public int getCount() {
        return this.mImageLinks.size();
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        inflater = (LayoutInflater) mActivity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        // inflate gallery item
        View viewLayout = inflater.inflate(R.layout.image_gallery_item, container, false);
        imgDisplay = (TouchImageView) viewLayout.findViewById(R.id.imgDispImgGal);
        // load image to item's ImageView
        loadImage(mImageLinks.get(position), imgDisplay);
        container.addView(viewLayout);

        return viewLayout;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView((RelativeLayout) object);

    }



}