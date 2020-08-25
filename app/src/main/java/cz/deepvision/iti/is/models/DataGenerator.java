package cz.deepvision.iti.is.models;

import cz.deepvision.iti.is.models.victims.Person;
import io.realm.Realm;

public class DataGenerator {
    public DataGenerator() {
        generateData();
    }

    private void generateData(){
        Realm realm = Realm.getDefaultInstance();

        for (int i = 0; i < 20; i++) {
            realm.beginTransaction();

            Person person = realm.createObject(Person.class);
            person.setId(i);
            person.setName("Some name "+i);
            person.setBorn(i+".03.1920");
            person.setFate("Murdered");

            realm.commitTransaction();
        }


    }
}
