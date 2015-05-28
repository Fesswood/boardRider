package info.goodline.boardrider.fragment;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
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
import com.squareup.picasso.Picasso;

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
import valleyapp.VolleyApplication;


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

    PicassoImageGetter mPicassoImageGetter;
    private Handler mOnObjectClickHandler;
    private final ArrayList<String> mImageUrlsLinks =new ArrayList<>();

    public static NewsTopicFragment newInstance(BoardNews boardNews) {
        Bundle args = new Bundle();
        args.putParcelable(BOARD_NEWS_ENTRY, boardNews);

        NewsTopicFragment fragment = new NewsTopicFragment();
        fragment.setArguments(args);
        Log.d(TAG,"newInstance "+boardNews.getTitle());
        return fragment;
    }

    public NewsTopicFragment() {
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);



    }


    private boolean onUrlClick(String url)
    {
        Log.d(TAG, "onUrlClick url = " + url);
        return false;
    }

    private boolean onImageClick(String imageSourceUrl)
    {
        for (int imageIndex = 0; imageIndex < mImageUrlsLinks.size(); imageIndex++)
        {
            if (mImageUrlsLinks.get(imageIndex).equals(imageSourceUrl))
            {
                // найден индекс изображения в массиве, передать в активити
                showImageGallery(imageIndex, mImageUrlsLinks);
                Log.d(TAG, "onImageClick imageIndex = " + imageIndex );
                return true;
            }
        }
        return false;
    }

    private void showImageGallery(int imageIndex, ArrayList<String> mImageUrlsLinks) {
        Intent i = new Intent(getActivity(), ImageGalleryActivity.class);
        i.putExtra(IMAGE_POSITION, imageIndex);
        i.putExtra(IMAGE_LINKS_LIST_ENTRY, mImageUrlsLinks);
        startActivity(i);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View fragmentView= inflater.inflate(R.layout.fragment_board_news, container, false);
        setHasOptionsMenu(true);
        mBoardNews      =  getArguments().getParcelable(BOARD_NEWS_ENTRY);
        mScrollView     = (ScrollView)fragmentView.findViewById(R.id.scroll_view);
        mTitleImageView = (ImageView) fragmentView.findViewById(R.id.title_image);
        mTitleView      = (TextView)  fragmentView.findViewById(R.id.title_view);
        mArticleView    = (TextView)  fragmentView.findViewById(R.id.article_content);
        mWatchersView   = (TextView)  fragmentView.findViewById(R.id.watchers_view);
        mLinkToSiteView = (TextView)  fragmentView.findViewById(R.id.link);
        mTitleView.setText(mBoardNews.getTitle());



        if(!mBoardNews.getImageUrl().isEmpty()){
            Picasso.with(getActivity())
                    .load(mBoardNews.getImageUrl())
                    .fit()
                    .centerCrop()
                    .into(mTitleImageView);
        }else{
            Picasso.with(getActivity())
                    .load(R.drawable.transparent_bg)
                    .fit()
                    .centerCrop()
                    .into(mTitleImageView);
        }

        mLinkToSiteView.setOnClickListener(this);

        mPicassoImageGetter = new PicassoImageGetter(
                mArticleView,
                Resources.getSystem(),
                Picasso.with(getActivity())
        );

        getActivity().setTitle(mBoardNews.getTitle());
       // fetchArticle();

        // событие при клике на ссылку или изображение в статье
        mOnObjectClickHandler = new Handler()
        {
            public void handleMessage(Message msg)
            {
                if (msg.what == IMAGE_CLICK)
                {
                    Object span = msg.obj;
                    if (span instanceof URLSpan)
                    {
                        onUrlClick(((URLSpan) span).getURL());
                    }
                    if (span instanceof ImageSpan)
                    {
                        onImageClick(((ImageSpan) span).getSource());
                    }
                }
            }
        };

        fetchArticle();
        attachTouchListener();

        return fragmentView;
    }

    private void attachTouchListener() {
        UniversalTouchListener universalTouchListener = new UniversalTouchListener(mOnObjectClickHandler);
        universalTouchListener.addClass(ImageSpan.class);
        universalTouchListener.addClass(URLSpan.class);
        mArticleView.setOnTouchListener(universalTouchListener);
    }

    private void fetchArticle(){
        Context context=getActivity();
        if(!NewsLoader.isOnline(context) && mBoardNews.getArticleContent().length()==0){
            showNoConnectionDialog(context);
            mArticleView.setText(mBoardNews.getSmallDesc());
        }else if(NewsLoader.isOnline(context)){

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
                    Log.e(TAG, "News Topic:"+mBoardNews.getTitle()+" : Error "+error.getMessage());
                }
            });
            VolleyApplication.getInstance().getRequestQueue().add(stringRequest);

        }else if(mBoardNews.getArticleContent().length()>0){
            mArticleView.setText(Html.fromHtml(mBoardNews.getArticleContent(),
                    mPicassoImageGetter   , null));

        }

    }

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
        mArticleView.setText(Html.fromHtml(articleHTML,
                new PicassoImageGetter(
                        mArticleView,
                        Resources.getSystem(),
                        Picasso.with(getActivity())
                ), null));
        if(mBoardNews.getArticleContent().length()==0){
            mBoardNews.setArticleContent("" + articleHTML);

            new SaveOperation().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,null);
        }
    }


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBoardNews      =  getArguments().getParcelable(BOARD_NEWS_ENTRY);

        Log.d(TAG, "onCreate " + this.mBoardNews.getTitle());
    }

    @Override
    public void onClick(View v) {
        Intent i = new Intent(Intent.ACTION_VIEW);
        i.setData(Uri.parse(mBoardNews.getArticleUrl()));
        startActivity(i);
    }
    @Override
    public void onResume() {
        super.onResume();
       Log.d(TAG, "onResume " + this.mBoardNews.getTitle());
        // fetchArticle();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mOnObjectClickHandler=null;
        mPicassoImageGetter=null;
    }

    public class PicassoImageGetter implements Html.ImageGetter {

        final Resources resources;

        final Picasso pablo;

        final TextView textView;

        public PicassoImageGetter(final TextView textView, final Resources resources, final Picasso pablo) {
            this.textView = textView;
            this.resources = resources;
            this.pablo = pablo;
        }

        @Override
        public Drawable getDrawable(final String source) {
            final BitmapDrawablePlaceHolder result = new BitmapDrawablePlaceHolder();

            new AsyncTask<Void, Void, Bitmap>() {

                @Override
                protected Bitmap doInBackground(final Void... meh) {
                    try {
                        mImageUrlsLinks.add(source);
                        return pablo.load(source).resize(300,300).onlyScaleDown().centerCrop().get();
                    } catch (Exception e) {
                        Log.e(TAG, "doInBackground " + e.getMessage());
                        return null;
                    }
                }

                @Override
                protected void onPostExecute(final Bitmap bitmap) {
                    try {
                        final BitmapDrawable drawable = new BitmapDrawable(resources, bitmap);

                        drawable.setBounds(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());

                        result.setDrawable(drawable);
                        result.setBounds(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());

                        textView.setText(textView.getText()); // invalidate() doesn't work correctly...
                    } catch (Exception e) {
                        Log.e(TAG, "onPostExecute " + e.getMessage());
                    }
                }

            }.execute((Void) null);

            return result;
        }


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
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_board_rider, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_to_top) {
            mScrollView.fullScroll(View.FOCUS_DOWN);
        }
        if (id == R.id.action_to_bottom) {
            mScrollView.fullScroll(View.FOCUS_UP);
        }

        return super.onOptionsItemSelected(item);
    }
    public static void showNoConnectionDialog(Context ctx1)
    {
        final Context ctx = ctx1;
        AlertDialog.Builder builder = new AlertDialog.Builder(ctx);
        builder.setCancelable(true);
        builder.setMessage(R.string.no_connection);
        builder.setTitle(R.string.no_connection_title);
        builder.setPositiveButton(R.string.settings_button_text, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which)
            {
                ctx.startActivity(new Intent(Settings.ACTION_WIRELESS_SETTINGS));
            }
        });

        builder.setNegativeButton(R.string.cancel_button_text, new DialogInterface.OnClickListener()
        {
            public void onClick(DialogInterface dialog, int which)
            {
                return;
            }
        });

        builder.setOnCancelListener(new DialogInterface.OnCancelListener()
        {
            public void onCancel(DialogInterface dialog) {
                return;
            }
        });

        builder.show();
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

}