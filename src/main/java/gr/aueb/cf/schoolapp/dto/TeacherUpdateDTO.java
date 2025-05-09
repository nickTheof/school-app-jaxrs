package gr.aueb.cf.schoolapp.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TeacherUpdateDTO {

    @NotNull
    private Long id;

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
