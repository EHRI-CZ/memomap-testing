package cz.deepvision.iti.is.models.victims;

public class ListViewItem {

    private String key;
    private String label;
    private String type;

    public ListViewItem(String key, String label, String type) {

        this.label = label;

        if(type.equals("entity")){
            String name = label.split("\\(")[0];
            String[] birthDay = label.split("\\(")[1].split("\\)");
            String customLabel = name + "\n" + birthDay[0];
            this.label = customLabel;
        }
        this.key = key;
        this.type = type;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
