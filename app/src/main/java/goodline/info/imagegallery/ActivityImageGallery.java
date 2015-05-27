package goodline.info.imagegallery;

import android.content.Intent;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;

import java.util.ArrayList;

import goodline.info.boardrider.R;
import goodline.info.boardrider.boardNewsFragment;
import goodline.info.util.ImageGalleryUtil;

public class ActivityImageGallery extends AppCompatActivity {

    private ImageGalleryUtil utils;
    private FullScreenImageAdapter adapter;
    private ViewPager viewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_gallery);

        viewPager = (ViewPager) findViewById(R.id.pgrImgGal);

        utils = new ImageGalleryUtil(getApplicationContext());

        Intent i = getIntent();
        int position = i.getIntExtra(boardNewsFragment.IMAGE_POSITION, 0);
        ArrayList<String> imageLinksList = i.getStringArrayListExtra(boardNewsFragment.IMAGE_LINKS_LIST_ENTRY);

        adapter = new FullScreenImageAdapter(ActivityImageGallery.this,
                imageLinksList);

        viewPager.setAdapter(adapter);

        // displaying selected image first
        viewPager.setCurrentItem(position);
    }
}
