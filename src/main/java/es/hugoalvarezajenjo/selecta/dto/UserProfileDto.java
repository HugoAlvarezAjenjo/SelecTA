package es.hugoalvarezajenjo.selecta.dto;

public class UserProfileDto {
    private String username;
    private String career = "Ingenieria del Software";

    public String getUsername() {
        return this.username;
    }

    public void setUsername(final String username) {
        this.username = username;
    }

    public String getCareer() {
        return this.career;
    }

    public void setCareer(final String career) {
        this.career = career;
    }
}
