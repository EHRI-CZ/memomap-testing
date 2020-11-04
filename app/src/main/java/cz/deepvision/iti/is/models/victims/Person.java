package cz.deepvision.iti.is.models.victims;

import cz.deepvision.iti.is.graphql.EntityDetailQuery;
import cz.deepvision.iti.is.models.Location;
import io.realm.RealmList;
import io.realm.RealmObject;

public class Person extends RealmObject {
    private String name;
    private String born;
    private String fate;
    private String death;
    private String deathPlace;
    private String sex;
    private String preview;
    private String full;
    private Location location;
    private RealmList<Event> eventList;
    private RealmList<Document> documentList;

    public Person() {
    }

    public Person(EntityDetailQuery.EntityDetail data) {
        if (data != null) {
            eventList = new RealmList<>();
            documentList = new RealmList<>();
            if (data.firstname() != null)
                name = data.firstname();
            if (data.surname() != null)
                name += " " + data.surname();

            if (data.sex() != null) {
                sex = data.sex();
            } else
                sex = "Narozen/a";
            if (data.fate() != null) {
                fate = data.fate();
            }
            if (data.events() != null) {
                for (EntityDetailQuery.Event event : data.events()) {
                    if (event.type().equals("birth") ||
                            event.type().equals("death") ||
                            event.type().equals("transport") ||
                            event.type().equals("residence_before_deportation") ||
                            event.type().equals("residence_registration")) {
                        Event personEvent = new Event(event);

                        if (personEvent.type.equals("birth"))
                            born = personEvent.date;
                        else if (personEvent.type.equals("death")){
                            deathPlace += personEvent.getDate() != null ? personEvent.getDate() : "";
                            deathPlace = personEvent.getPlace() != null ? personEvent.getPlace() : "";
                        }
                        else
                            eventList.add(personEvent);
                    }
                }
            }
            if (data.preview() != null)
                preview = data.preview();
            if (data.full() != null)
                full = data.full();
            if (data.location() != null)
                location = new Location(data.location().lat(), data.location().lon());

            if (data.documents() != null) {
                for (EntityDetailQuery.Document document : data.documents()) {
                    Document personDocument = new Document(document.full(), document.name(), document.preview());
                    if (document.name() != null && document.preview() != null || document.full() != null)
                        documentList.add(personDocument);

                }
            }
        }

    }

    public String getPreview() {
        return preview;
    }

    public void setPreview(String preview) {
        this.preview = preview;
    }

    public String getFull() {
        return full;
    }

    public void setFull(String full) {
        this.full = full;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getBorn() {
        return born;
    }

    public void setBorn(String born) {
        this.born = born;
    }

    public String getFate() {
        return fate;
    }

    public void setFate(String fate) {
        this.fate = fate;
    }

    public String getSex() {
        return sex;
    }

    public void setSex(String sex) {
        this.sex = sex;
    }

    public String getDeath() {
        return death;
    }

    public void setDeath(String death) {
        this.death = death;
    }

    public String getDeathPlace() {
        return deathPlace;
    }

    public void setDeathPlace(String deathPlace) {
        this.deathPlace = deathPlace;
    }

    public RealmList<Event> getEventList() {
        return eventList;
    }

    public void setEventList(RealmList<Event> eventList) {
        this.eventList = eventList;
    }

    public RealmList<Document> getDocumentList() {
        return documentList;
    }

    public void setDocumentList(RealmList<Document> documentList) {
        this.documentList = documentList;
    }

    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }
}
