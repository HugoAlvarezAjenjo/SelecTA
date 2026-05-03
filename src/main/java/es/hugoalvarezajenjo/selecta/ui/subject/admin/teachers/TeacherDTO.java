package es.hugoalvarezajenjo.selecta.ui.subject.admin.teachers;

import es.hugoalvarezajenjo.selecta.services.user.User;
import lombok.Value;

@Value
public class TeacherDTO {
    private Long id;
    private String username;
    private String email;

    public static TeacherDTO fromUser(final User user) {
        return new TeacherDTO(user.getId(), user.getUsername(), user.getEmail());
    }
}
