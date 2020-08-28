package cz.deepvision.iti.is.models;

import cz.deepvision.iti.is.graphql.PlaceDetailQuery;
import cz.deepvision.iti.is.models.victims.Document;
import io.realm.RealmList;
import io.realm.RealmObject;

public class Place extends RealmObject {
    private String label;
    private Location location;
    private String preview;
    private String description;
    private String full;
    private RealmList<Document> documentList;

    public Place() {
    }

    public Place(PlaceDetailQuery.PlaceDetail data) {
        if(data != null){
            documentList = new RealmList<>();
            if (data.label() != null) label = data.label();
            if (data.location() != null) location = new Location(data.location().lat(),data.location().lon());
            if (data.preview() != null) preview = data.preview();
            if (data.description() != null) description = data.description();
            if (data.full() != null) full = data.full();
            if (data.documents() != null) {
                for (PlaceDetailQuery.Document document : data.documents()) {
                    Document placeDocument = new Document(document.full(),document.name(),document.preview());
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

    public String getPreview() {
        return preview;
    }

    public void setPreview(String preview) {
        this.preview = preview;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getFull() {
        return full;
    }

    public void setFull(String full) {
        this.full = full;
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

