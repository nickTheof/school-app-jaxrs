package gr.aueb.cf.schoolapp.dto;

import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
public class TeacherReadOnlyDTO {
    private Long id;
    private String firstname;
    private String lastname;
    private String vat;
}
