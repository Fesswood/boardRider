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

public class ImageGalleryActivity extends AppCompatActivity {

    private FullScreenImageAdapter adapter;
    private ViewPager viewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_gallery);

        viewPager = (ViewPager) findViewById(R.id.pgrImgGal);

        Intent i = getIntent();
        int position = i.getIntExtra(NewsTopicFragment.IMAGE_POSITION, 0);
        ArrayList<String> imageLinksList = i.getStringArrayListExtra(NewsTopicFragment.IMAGE_LINKS_LIST_ENTRY);
        Resources resources = getResources();
        adapter = new FullScreenImageAdapter( ImageGalleryActivity.this, imageLinksList,resources);

        viewPager.setAdapter(adapter);

        // displaying selected image first
        viewPager.setCurrentItem(position);
    }
}
