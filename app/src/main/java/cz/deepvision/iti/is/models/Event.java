package cz.deepvision.iti.is.models;

import cz.deepvision.iti.is.graphql.EventDetailQuery;
import cz.deepvision.iti.is.models.victims.Document;
import io.realm.RealmList;
import io.realm.RealmObject;

public class Event extends RealmObject {
    private String label;
    private String entity;
    private String entityID;
    private String place;
    private Location location;
    private String date;
    private RealmList<Document> documentList;

    public Event() {
    }

    public Event(EventDetailQuery.EventDetail data) {
        if (data != null) {
            documentList = new RealmList<>();
            if (data.label() != null) label = data.label();
            if (data.place() != null) place = data.place();
            if (data.entity() != null) entity = data.entity();
            if (data.entity_id() != null) entityID = data.entity_id();
            if (data.place() != null) place = data.place();
            if (data.location() != null) location = new Location(data.location().lat(),data.location().lon());
            if (data.date() != null) date = data.date();
            if (data.documents() != null) {
                for (EventDetailQuery.Document document : data.documents()) {
                    Document placeDocument = new Document(document.full(), document.name(), document.preview());
                    if (document.name() != null && document.preview() != null || document.full() != null)
                        documentList.add(placeDocument);

                }
            }
        }
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getEntity() {
        return entity;
    }

    public void setEntity(String entity) {
        this.entity = entity;
    }

    public String getEntityID() {
        return entityID;
    }

    public void setEntityID(String entityID) {
        this.entityID = entityID;
    }

    public String getPlace() {
        return place;
    }

    public void setPlace(String place) {
        this.place = place;
    }



    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    public RealmList<Document> getDocumentList() {
        return documentList;
    }

    public void setDocumentList(RealmList<Document> documentList) {
        this.documentList = documentList;
    }
}
