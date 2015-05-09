package goodline.info.util;

/**
 * Created by Балдин Сергей on 08.05.2015.
 */
import android.view.View;

public interface TextLinkClickListener{

    //  This method is called when the TextLink is clicked from LinkEnabledTextView

    public void onTextLinkClick(View textView, String clickedString);
}