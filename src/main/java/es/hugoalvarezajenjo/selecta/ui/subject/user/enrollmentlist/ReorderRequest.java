package es.hugoalvarezajenjo.selecta.ui.subject.user.enrollmentlist;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
public class ReorderRequest {
    private List<Long> orderedSubjectIds;
    private boolean reserve;
}
