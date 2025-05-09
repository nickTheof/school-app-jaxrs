package gr.aueb.cf.schoolapp.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TeacherInsertDTO {

    @NotNull(message = "Το όνομα δεν μπορεί να είναι κενό")
    @Size(min = 2, max = 255, message = "Το όνομα πρέπει να είναι μεταξύ 2 - 255 χαρακτήρων")
    private String firstname;

    @NotNull(message = "Το επώνυμο δεν μπορεί να είναι κενό")
    @Size(min = 2, max = 255, message = "Το επώνυμο πρέπει να είναι μεταξύ 2 - 255 χαρακτήρων")
    private String lastname;

    @NotNull(message = "Το ΑΦΜ δεν μπορεί να είναι κενό")
    @Size(min = 9, max = 9, message = "Το ΑΦΜ πρέπει να είναι 9 χαρακτήρες")
    private String vat;
}
