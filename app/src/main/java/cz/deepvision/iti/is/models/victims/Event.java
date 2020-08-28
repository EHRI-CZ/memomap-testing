package cz.deepvision.iti.is.models.victims;

import cz.deepvision.iti.is.graphql.EntityDetailQuery;
import io.realm.RealmObject;

public class Event extends RealmObject {
    String type;
    String date;
    String name;
    String place;
    String transport_nm;

    public Event() {
    }
    public Event(EntityDetailQuery.Event event) {
        if(event.type() != null){
            type = event.type();
        }
        if(event.date() != null){
            date = event.date();
        }
        if(event.name() != null){
            name = event.name();
        }
        if(event.place() != null){
            place = event.place();
        }
        if(event.transport_nr() != null){
            transport_nm = event.transport_nr();
        }
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPlace() {
        return place;
    }

    public void setPlace(String place) {
        this.place = place;
    }

    public String getTransport_nm() {
        return transport_nm;
    }

    public void setTransport_nm(String transport_nm) {
        this.transport_nm = transport_nm;
    }
}
