package info.goodline.boardrider.listener;

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

import info.goodline.boardrider.fragment.NewsTopicFragment;

/**
 *  Listener for touching events on snap of news topic content used in {@link NewsTopicFragment}
 *  Interacts with {@link ImageSpan} after touching it sends message to Handler with message IMAGE_CLICK
 */
public class UniversalTouchListener implements View.OnTouchListener{

    private static final String TAG = UniversalTouchListener.class.getSimpleName();

    private List<Class> mSpanClassList	= null;
    private Handler mHandler;

    public UniversalTouchListener(Handler mHandler) {
        this.mHandler = mHandler;
        mSpanClassList=new ArrayList<>();
    }

    /**
     * add class only interested to touch span
     * @param spanClass
     */
    public void addClass(Class spanClass) {
        mSpanClassList.add(spanClass);
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        TextView touchedView = (TextView) v;
        int action = event.getAction();
        /**
         *  react on touch down and up events
         */
        if (action == MotionEvent.ACTION_UP ||
                action == MotionEvent.ACTION_DOWN) {
            int x = (int) event.getX();
            int y = (int) event.getY();

            x -= touchedView.getTotalPaddingLeft();
            y -= touchedView.getTotalPaddingTop();

            x += touchedView.getScrollX();
            y += touchedView.getScrollY();

            Layout layout = touchedView.getLayout();
            int line = layout.getLineForVertical(y);
            int off = layout.getOffsetForHorizontal(line, x);

            CharSequence text = touchedView.getText();
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
                        message.what = NewsTopicFragment.IMAGE_CLICK;
                        message.sendToTarget();
                        return true;
                    }
                }
            }
        }
        return true;
    }
}
