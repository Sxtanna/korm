package com.sxtanna.korm;

import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.TestInstance;

import java.util.Arrays;
import java.util.List;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
final class JavaTests {

    private final Korm korm = new Korm();


    @RepeatedTest(10)
    void testPush() {
        final String text = korm.push(new People("Sxtanna", "ViolentDelightz"));
        System.out.println(text);
    }

    @RepeatedTest(10)
    void testPull() {
        final People people = korm.pull("people: [\"Sxtanna\", \"ViolentDelightz\"]").to(People.class);
        System.out.println(people);
    }


    private static final class People {

        private final List<String> people;


        People(String... people) {
            this.people = Arrays.asList(people);
        }


        public List<String> getPeople() {
            return people;
        }


        @Override
        public String toString() {
            return "People{" + "people=" + people + '}';
        }

    }

}
