package cz.deepvision.iti.is.models.victims;

import io.realm.RealmObject;

public class Document extends RealmObject {
    String fullImage;
    String name;
    String previewImage;

    public Document() {
    }

    public Document(String full,String label, String preview) {
        if(full != null)
            this.fullImage = full;
        if(label !=null)
            this.name = label;
        if(preview != null)
            this.previewImage = preview;
    }

    public String getFullImage() {
        return fullImage;
    }

    public void setFullImage(String fullImage) {
        this.fullImage = fullImage;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPreviewImage() {
        return previewImage;
    }

    public void setPreviewImage(String previewImage) {
        this.previewImage = previewImage;
    }
}
