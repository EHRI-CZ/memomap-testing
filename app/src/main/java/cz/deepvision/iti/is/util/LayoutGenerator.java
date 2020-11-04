package cz.deepvision.iti.is.util;

import android.content.Context;
import android.os.Build;
import android.widget.LinearLayout;
import android.widget.TextView;
import cz.deepvision.iti.is.models.victims.Event;
import cz.deepvision.iti.is.models.victims.Person;
import io.realm.RealmList;

import java.util.ArrayList;
import java.util.List;

public class LayoutGenerator {
    private static Context ctx;

    public static void init(Context context) {
        ctx = context;
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
