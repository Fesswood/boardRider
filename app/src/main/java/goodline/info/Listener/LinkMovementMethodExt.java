package goodline.info.Listener;

import android.os.Handler;
import android.os.Message;
import android.text.Layout;
import android.text.Spannable;
import android.text.method.LinkMovementMethod;
import android.text.method.MovementMethod;
import android.text.style.ImageSpan;
import android.view.MotionEvent;
import android.widget.TextView;



import java.util.ArrayList;
import java.util.List;

import goodline.info.boardrider.boardNewsFragment;


public class LinkMovementMethodExt extends LinkMovementMethod {
     private Handler mHandler			= null;
    private List<Class> mSpanClassList	= null;

    public LinkMovementMethodExt(Handler mHandler) {
        super();
        this.mHandler = mHandler;
        this.mSpanClassList = new ArrayList<>();
    }

    public void addClass(Class spanClass)
    {
        mSpanClassList.add(spanClass);
    }

    @Override
    public boolean onTouchEvent(TextView widget, Spannable buffer,
                                MotionEvent event) {
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
            /**
             * get you interest span, here get ImageSpan that you click
             */
            for(Class spanClass: mSpanClassList)
            {
                Object[] spans = buffer.getSpans(off, off, spanClass);
                if (spans.length != 0)
                {
                    if (action == MotionEvent.ACTION_UP)
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

        return super.onTouchEvent(widget, buffer, event);
    }

}