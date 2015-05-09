package goodline.info.boardrider;

import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
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

import valleyapp.VolleyApplication;


public class boardNewsFragment extends Fragment implements TextView.OnClickListener {

    public static final String BOARD_NEWS_ENTRY = "boardNewsFragment.BOARD_NEWS_ENTRY";
    private static final String TAG= "boardNewsFragment";
    private ImageView mTitleImageView;
    private TextView mTitleView;
    private TextView mArticleView;
    private TextView mWatchersView;
    private TextView mLinkToSiteView;
    private BoardNews mBoardNews;


    public static boardNewsFragment newInstance(BoardNews boardNews) {
        Bundle args = new Bundle();
        args.putParcelable(BOARD_NEWS_ENTRY, boardNews);

        boardNewsFragment fragment = new boardNewsFragment();
        fragment.setArguments(args);

        return fragment;
    }
    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        mBoardNews      =  getActivity().getIntent().getParcelableExtra(BOARD_NEWS_ENTRY);
        mTitleImageView = (ImageView)getView().findViewById(R.id.title_image);
        mTitleView      = (TextView) getView().findViewById(R.id.title_view);
        mArticleView    = (TextView) getView().findViewById(R.id.article_content);
        mWatchersView   = (TextView) getView().findViewById(R.id.watchers_view);
        mLinkToSiteView = (TextView) getView().findViewById(R.id.link);
        mTitleView.setText(mBoardNews.getTitle());


        Picasso.with(getActivity())
                .load(mBoardNews.getImageUrl())
                .fit()
                .centerCrop()
                .into(mTitleImageView);


        mLinkToSiteView.setOnClickListener(this);

        getActivity().setTitle(mBoardNews.getTitle());
        fetchArticle();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_board_news, container, false);
    }
    private void fetchArticle(){

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
                    Log.e(TAG, error.getMessage());
                }
            });
            VolleyApplication.getInstance().getRequestQueue().add(stringRequest);
        }

    private void parseAndSetArticle(String HTML){
         Document doc =null;
        try {
            doc = Jsoup.parse(HTML);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            Log.e(null,e.getMessage(),e);
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
    }

    @Override
    public void onClick(View v) {
        Intent i = new Intent(Intent.ACTION_VIEW);
        i.setData(Uri.parse(mBoardNews.getArticleUrl()));
        startActivity(i);
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
                        return pablo.load(source).get();
                    } catch (Exception e) {
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
                /* nom nom nom*/
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
}
