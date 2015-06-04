package info.goodline.boardrider.activity;

import android.content.Intent;
import android.content.res.Resources;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import java.util.ArrayList;

import info.goodline.boardrider.adapter.FullScreenImageAdapter;
import info.goodline.boardrider.fragment.NewsTopicFragment;
import goodline.info.boardrider.R;

/**
 *  Activity show fullscreen Image Gallery to user
 *  Get List of image urls and show it via ViewPager used {@link FullScreenImageAdapter}
 *  Starts when user touches image into {@link NewsTopicFragment}
 *  @author  Sergey Baldin
 */
public class ImageGalleryActivity extends AppCompatActivity {

    private FullScreenImageAdapter adapter;
    private ViewPager viewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_gallery);
        // create viewPager for displaying images
        viewPager = (ViewPager) findViewById(R.id.pgrImgGal);
        Intent i = getIntent();
        // getting current position which will used in the beginning
        int position = i.getIntExtra(NewsTopicFragment.IMAGE_POSITION, 0);
        createFullscreenImageAdapter(i);

        // displaying selected image first
        viewPager.setCurrentItem(position);
    }

    /**
     * Get list of image urls, create {@link FullScreenImageAdapter} with them
     * and set it to viewPager
     * @param intent intent contained list with image urls
     */
    private void createFullscreenImageAdapter(Intent intent) {
        ArrayList<String> imageLinksList = intent.getStringArrayListExtra(NewsTopicFragment.IMAGE_LINKS_LIST_ENTRY);
        adapter = new FullScreenImageAdapter( ImageGalleryActivity.this, imageLinksList);
        viewPager.setAdapter(adapter);
    }
}
