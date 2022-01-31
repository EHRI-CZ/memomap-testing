package cz.deepvision.iti.is.util;

import android.content.Context;
import android.os.Build;
import android.text.Html;
import android.text.Spanned;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import cz.deepvision.iti.is.R;
import cz.deepvision.iti.is.models.victims.Event;
import cz.deepvision.iti.is.models.victims.Person;
import io.realm.RealmList;

public class LayoutGenerator {
    private static Context ctx;

    public static void init(Context context) {
        ctx = context;
    }

    private static Spanned getSpannedText(String text) {
        return Html.fromHtml("<b>" + text + "</b>");
    }


    public static void addInfo(LinearLayout infoContainer, String s) {
        TextView info = new TextView(ctx);
        info.setText(s);
        info.setTextSize(12);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            info.setLineHeight(50);
        }
        infoContainer.addView(info);
    }

    public static void addTransportInfo(LinearLayout infoContainer, String s,boolean last) {
        TextView info = new TextView(ctx);
        info.setText(s);
        info.setTextSize(12);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            info.setLineHeight(50);
        }
        infoContainer.addView(info);
        if(!last){
            ImageView imageView = new ImageView(ctx);
            imageView.setImageDrawable(ctx.getDrawable(R.drawable.ic_action_name));
            infoContainer.addView(imageView);
        }
    }

    public static Event filterEvents(String type, RealmList<Event> eventRealmList) {
        Event event = null;
        for (cz.deepvision.iti.is.models.victims.Event el : eventRealmList) {
            if (el.getType().equals(type)) {
                event = el;
            }
        }
        return event;
    }

    public static List<Event> getTransports(RealmList<Event> eventRealmList) {
        List<Event> events = new ArrayList<>();
        for (cz.deepvision.iti.is.models.victims.Event el : eventRealmList) {
            if (el.getType().equals("transport")) {
                events.add(el);
            }
        }
        return events;
    }

    public static String getEndingChar(String info, Person data) {
        if (data.getSex() == null) {
            info += "/a";
        } else if (data.getSex().equals("female")) {
            info += "a";
        }
        return info;
    }

    public LayoutGenerator() {
    }
}
