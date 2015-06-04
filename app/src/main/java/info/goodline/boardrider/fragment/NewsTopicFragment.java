package info.goodline.boardrider.fragment;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Point;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.text.Html;
import android.text.style.ImageSpan;
import android.text.style.URLSpan;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.util.ArrayList;


import goodline.info.boardrider.R;
import info.goodline.boardrider.activity.ImageGalleryActivity;
import info.goodline.boardrider.data.BoardNews;
import info.goodline.boardrider.listener.UniversalTouchListener;
import info.goodline.boardrider.loader.NewsLoader;
import info.goodline.boardrider.sqllite.SugarORM;
import valleyapp.BoardNewsApplication;

import static valleyapp.BoardNewsApplication.isOnline;
import static valleyapp.BoardNewsApplication.loadImage;
import static valleyapp.BoardNewsApplication.loadImageAsync;
import static valleyapp.BoardNewsApplication.showNoConnectionDialog;

/**
 *  NewsTopicFragment show selected news and news before/after selected  into ViewPager
 *  @see info.goodline.boardrider.activity.ViewPagerActivity
 *  @author  Sergey Baldin
 */
public class NewsTopicFragment extends Fragment implements TextView.OnClickListener {

    public static final String IMAGE_POSITION = "NewsTopicFragment.IMAGE_POSITION";
    public static final String IMAGE_LINKS_LIST_ENTRY = "NewsTopicFragment.IMAGE_LINKS_LIST_ENTRY";
    public static final String BOARD_NEWS_ENTRY = "NewsTopicFragment.BOARD_NEWS_ENTRY";
    public static final int IMAGE_CLICK = 100;
    private static final String TAG = NewsTopicFragment.class.getSimpleName();

    private ScrollView mScrollView;
    private ImageView mTitleImageView;
    private TextView mTitleView;
    private TextView mArticleView;
    private TextView mWatchersView;
    private TextView mLinkToSiteView;

    private BoardNews mBoardNews;
    /**
     *  Handler for catching onImageClick and showing fullscreen gallery
     */
    private Handler mOnSpanClickHandler;
    private final ArrayList<String> mImageUrlsLinks =new ArrayList<>();
    private NewsTopicImageGetter mNewsTopicImageGetter;

    /**
     * Create new instance of NewsTopicFragment
     * @param boardNews news for displaying in tne fragment
     * @return instance of NewsTopicFragment
     */
    public static NewsTopicFragment newInstance(BoardNews boardNews) {
        Bundle args = new Bundle();
        args.putSerializable(BOARD_NEWS_ENTRY, boardNews);
        NewsTopicFragment fragment = new NewsTopicFragment();
        fragment.setArguments(args);
        Log.d(TAG,"newInstance "+boardNews.getTitle());
        return fragment;
    }

    public NewsTopicFragment() {
        // empty constructor for newInstance
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mNewsTopicImageGetter =  new NewsTopicImageGetter(
                Resources.getSystem(),
                getActivity().getWindowManager().getDefaultDisplay()
        );
        if (getArguments() != null) {
            mBoardNews = (BoardNews) getArguments().getSerializable(BOARD_NEWS_ENTRY);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View fragmentView= inflater.inflate(R.layout.fragment_board_news, container, false);
        setHasOptionsMenu(true);
        mScrollView     = (ScrollView)fragmentView.findViewById(R.id.scroll_view);
        mTitleImageView = (ImageView) fragmentView.findViewById(R.id.title_image);
        mTitleView      = (TextView)  fragmentView.findViewById(R.id.title_view);
        mArticleView    = (TextView)  fragmentView.findViewById(R.id.article_content);
        mWatchersView   = (TextView)  fragmentView.findViewById(R.id.watchers_view);
        mLinkToSiteView = (TextView)  fragmentView.findViewById(R.id.link);
        mLinkToSiteView.setOnClickListener(this);
        mTitleView.setText(mBoardNews.getTitle());

        setTitleImage();
        initializeHandler();
        fetchArticle();
        attachTouchListener();

        return fragmentView;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mOnSpanClickHandler = null;
        mNewsTopicImageGetter = null;
    }

    @Override
    public void onClick(View v) {
        showBrowserForFullNews();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_board_rider, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_to_top) {
            mScrollView.fullScroll(View.FOCUS_DOWN);
        }
        if (id == R.id.action_to_bottom) {
            mScrollView.fullScroll(View.FOCUS_UP);
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * check if input url image is contained in {@link #mImageUrlsLinks}
     * @param imageSourceUrl url of clicked image
     * @return true if mImageUrlsLinks contains url image and false otherwise
     */
   private boolean onImageClick(String imageSourceUrl){
        for (int imageIndex = 0; imageIndex < mImageUrlsLinks.size(); imageIndex++){
            if (mImageUrlsLinks.get(imageIndex).equals(imageSourceUrl)){
                showImageGallery(imageIndex, mImageUrlsLinks);
                return true;
            }
        }
        return false;
    }

    /**
     * Show image gallery starts with imageIndex
     * @param imageIndex index of first image
     * @param mImageUrlsLinks list of all urls
     */
    private void showImageGallery(int imageIndex, ArrayList<String> mImageUrlsLinks) {
        Intent i = new Intent(getActivity(), ImageGalleryActivity.class);
        i.putExtra(IMAGE_POSITION, imageIndex);
        i.putExtra(IMAGE_LINKS_LIST_ENTRY, mImageUrlsLinks);
        startActivity(i);
    }
    /**
     *  Set image from url for title block of news or set transparent image
     */
    private void setTitleImage() {
        if(!mBoardNews.getImageUrl().isEmpty()){
            loadImage(mBoardNews.getImageUrl(), mTitleImageView);

        }else{
            mTitleImageView.setImageResource(R.drawable.transparent_bg);
        }
    }

    /**
     *  initialize handle for handle click messages of span in TextView
     */
    private void initializeHandler() {
        mOnSpanClickHandler = new Handler()
        {
            public void handleMessage(Message msg)
            {
                if (msg.what == IMAGE_CLICK)
                {
                    Object span = msg.obj;
                    if (span instanceof ImageSpan)
                    {
                        onImageClick(((ImageSpan) span).getSource());
                    }
                }
            }
        };
    }
    private void attachTouchListener() {
        UniversalTouchListener universalTouchListener = new UniversalTouchListener(mOnSpanClickHandler);
        universalTouchListener.addClass(ImageSpan.class);
        universalTouchListener.addClass(URLSpan.class);
        mArticleView.setOnTouchListener(universalTouchListener);
    }

    /**
     *   Fetch  content of news topic ({@link #mBoardNews})
     *   if there are no internet and connection no content of news in database show smallDesc of news topic
     *   if there is no only internet connection show content of news from database
     *   if there is internet connection content will be fetched from url
     */
    private void fetchArticle(){
        Context context=getActivity();
        if(!isOnline(context) && mBoardNews.getArticleContent().length()==0){

            showNoConnectionDialog(context);
            mArticleView.setText(mBoardNews.getSmallDesc());

        }else if(isOnline(context)){

            sendContentNewsRequest();

        }else if(mBoardNews.getArticleContent().length()>0){

            mArticleView.setText(Html.fromHtml(mBoardNews.getArticleContent()
                                               ,mNewsTopicImageGetter
                                               ,null));
        }

    }

    /**
     *  Send StringRequest for getting content of news otherwise show toast with error message
     */
    private void sendContentNewsRequest() {
        StringRequest stringRequest;
        stringRequest = new StringRequest(Request.Method.GET, mBoardNews.getArticleUrl(),
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        parseAndSetArticle(response);
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(getActivity(), R.string.error_load_data, Toast.LENGTH_SHORT).show();
                Log.e(TAG, "News Topic:" + mBoardNews.getTitle() + " : Error " + error.getMessage());
            }
        });
        BoardNewsApplication.getInstance().getRequestQueue().add(stringRequest);
    }

    /**
     * Parse html and set it to textView (@link #mArticleView) and count of viewer
     * after that if news topic doesn't  contain content in database, save it to database
     * @param HTML string with html content of news
     */
    private void parseAndSetArticle(String HTML){
         Document doc =null;
        try {
            doc = Jsoup.parse(HTML);
        } catch (Exception e) {
            Log.e(TAG,e.getMessage(),e);
        }
        Elements article = doc.select(".topic-content.text");
        String articleHTML=article.html();
        Elements watchers =  doc.select(".topic-info-viewers");
        mWatchersView.setText(" " + watchers.text());
        mArticleView.setText(Html.fromHtml(articleHTML, mNewsTopicImageGetter, null));
        if(mBoardNews.getArticleContent().length()==0){
            mBoardNews.setArticleContent("" + articleHTML);
            new SaveOperation().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, null);
        }
    }

   /**
    *  if user clicked to link_article show browser with full news
    */
   private void showBrowserForFullNews() {
       Intent i = new Intent(Intent.ACTION_VIEW);
       i.setData(Uri.parse(mBoardNews.getArticleUrl()));
       startActivity(i);
   }

   private class SaveOperation extends AsyncTask<Void, Void, Void> {
          @Override
       protected Void doInBackground(Void... params) {
           SugarORM.updateNews(mBoardNews);
         return null;
       }
          @Override
       protected void onProgressUpdate(Void... values) {}
   }

    /**
     * ImageGetter for loading images in news topic
     *
     * NewsTopicImageGetter used for downloading images from news html to canvas of news TextView.
     * @see info.goodline.boardrider.fragment.NewsTopicFragment
     * Also while the AsyncTask working add every image url to ArrayList for subsequent creating fullscreen gallery.
     * @see info.goodline.boardrider.activity.ImageGalleryActivity
     * @author  Sergey Baldin
     *
     */
    public class NewsTopicImageGetter implements Html.ImageGetter {


        private String DEBUG_TAG = NewsTopicImageGetter.class.getSimpleName();
        private Display mDisplay;
        private Resources mResources;

        /**
         *  Create instance of NewsTopicImageGetter
         * @param resources reference to app resources
         * @param display  reference to display for getting size of screen
         */
        public NewsTopicImageGetter(final Resources resources,  Display display ) {
            this.mResources = resources;
            this.mDisplay = display;
        }

        @Override
        public Drawable getDrawable(final String source) {
            final BitmapDrawablePlaceHolder result = new BitmapDrawablePlaceHolder();

            new AsyncTask<Void, Void, Bitmap>() {

                @Override
                protected Bitmap doInBackground(final Void... meh) {
                    try {
                        mImageUrlsLinks.add(source);
                        return  loadImageAsync(source);
                    } catch (Exception e) {
                        Log.e(DEBUG_TAG, "doInBackground " + e.getMessage());
                        return null;
                    }
                }

                @Override
                protected void onPostExecute(final Bitmap bitmap) {
                    try {
                        final BitmapDrawable drawable = new BitmapDrawable(mResources, bitmap);
                        Point size = getRelativeImageSize(drawable);
                        drawable.setBounds(0, 0, size.x, size.y);
                        result.setDrawable(drawable);
                        result.setBounds(0, 0, size.x, size.y);
                        mArticleView.setText(mArticleView.getText());
                    } catch (Exception e) {
                        Log.e(DEBUG_TAG, "onPostExecute " + e.getMessage());
                    }
                }

            }.execute((Void) null);
            return result;
        }

        private Point getRelativeImageSize(BitmapDrawable drawable) {

            Point size = new Point();
            mDisplay.getSize(size);

            // delete padding from screen size
            float scale = mResources.getDisplayMetrics().density;
            int paddingDpAsPixels = (int) (40*scale + 0.5f);

            size.x=size.x - paddingDpAsPixels;
            double ratio = ((float) size.x) / (float) drawable.getIntrinsicWidth();
            size.y = (int) (ratio * drawable.getIntrinsicHeight());
            return size;
        }

        /**
         *  BitmapDrawable for drawing images from news in textView
         */
        class BitmapDrawablePlaceHolder extends BitmapDrawable {

            protected Drawable drawable;

            @Override
            public void draw(final Canvas canvas) {
                if (drawable != null) {
                    drawable.draw(canvas);
                }
            }
            public void setDrawable(Drawable drawable) {
                this.drawable = drawable;
            }
        }
    }

}
