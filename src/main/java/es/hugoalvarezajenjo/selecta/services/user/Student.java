package es.hugoalvarezajenjo.selecta.services.user;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

@Entity
@DiscriminatorValue("STUDENT")
public class Student extends User {
    private String titulation;

    public Student() {
        this.setRole(UserRole.STUDENT);
    }

    public String getTitulation() {
        return titulation;
    }

    public void setTitulation(String titulation) {
        this.titulation = titulation;
    }
}
