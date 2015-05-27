package goodline.info.Listener;

import android.os.Handler;
import android.os.Message;
import android.text.Layout;
import android.text.Spannable;
import android.text.style.ImageSpan;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import goodline.info.boardrider.boardNewsFragment;

/**
 * Created by Балдин Сергей on 27.05.2015.
 */
public class LinkAndImageLister implements View.OnTouchListener{

    private static final String TAG = LinkAndImageLister.class.getSimpleName();

    private List<Class> mSpanClassList	= null;
    private Handler mHandler;

    public LinkAndImageLister(Handler mHandler) {

        this.mHandler = mHandler;
        mSpanClassList=new ArrayList<>();
    }

    public void addClass(Class spanClass)
    {
        mSpanClassList.add(spanClass);
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        TextView widget = (TextView) v;
        int action = event.getAction();
        if (action == MotionEvent.ACTION_UP ||
                action == MotionEvent.ACTION_DOWN) {
            int x = (int) event.getX();
            int y = (int) event.getY();

            x -= widget.getTotalPaddingLeft();
            y -= widget.getTotalPaddingTop();

            x += widget.getScrollX();
            y += widget.getScrollY();

            Layout layout = widget.getLayout();
            int line = layout.getLineForVertical(y);
            int off = layout.getOffsetForHorizontal(line, x);

            CharSequence text = widget.getText();
            Spannable stext = Spannable.Factory.getInstance().newSpannable(text);


            /**
             * get you interest span, here get ImageSpan that you click
             */
            for(Class spanClass: mSpanClassList)
            {
                Object[] spans = stext.getSpans(off, off, spanClass);
                if (spans.length != 0)
                {
                    Log.d(TAG," действие"+action);
                    if ((action == MotionEvent.ACTION_UP )
                            && spans[0] instanceof ImageSpan)
                    {
                        Message message = mHandler.obtainMessage();
                        message.obj = spans[0];
                        message.what = boardNewsFragment.IMAGE_CLICK;
                        message.sendToTarget();
                        return true;
                    }
                }
            }
        }
        return true;
    }
}
