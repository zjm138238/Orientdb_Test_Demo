package com.emc.poc.resources.person;

import com.emc.poc.models.person.Person;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Created by dev on 16/05/16.
 */
@RestController
@RequestMapping(value="/api/persons")
public class PersonController {

    @RequestMapping(value={"/",""}, method= RequestMethod.GET, produces = { MediaType.APPLICATION_JSON_VALUE })
    public List<Person> getAllPersons() {

        Person person1 = new Person();
        person1.setId(UUID.randomUUID().toString());
        person1.setName("Simon O'Brien");
        person1.setEmail("simon@the-obriens.net");

        Person person2 = new Person();
        person2.setId(UUID.randomUUID().toString());
        person2.setName("Darren Tarrant");
        person2.setEmail("darren.tarrant@emc.com");

        List<Person> persons = new ArrayList<Person>();
        persons.add(person1);
        persons.add(person2);

        return persons;
    }

}
