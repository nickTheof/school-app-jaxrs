package gr.aueb.cf.schoolapp.mapper;

import gr.aueb.cf.schoolapp.dto.TeacherInsertDTO;
import gr.aueb.cf.schoolapp.dto.TeacherReadOnlyDTO;
import gr.aueb.cf.schoolapp.dto.TeacherUpdateDTO;
import gr.aueb.cf.schoolapp.model.Teacher;

import java.util.List;
import java.util.stream.Collectors;

public class Mapper {
    private Mapper() {

    }

    public static Teacher mapToTeacher(TeacherInsertDTO teacherInsertDTO) {
        return new Teacher(null, teacherInsertDTO.getFirstname(), teacherInsertDTO.getLastname(), teacherInsertDTO.getLastname());
    }

    public static Teacher mapToTeacher(TeacherUpdateDTO teacherUpdateDTO) {
        return new Teacher(teacherUpdateDTO.getId(), teacherUpdateDTO.getVat(), teacherUpdateDTO.getFirstname(), teacherUpdateDTO.getLastname());
    }

    public static TeacherReadOnlyDTO mapToReadOnlyDTO(Teacher teacher) {
        return new TeacherReadOnlyDTO(teacher.getId(), teacher.getFirstname(), teacher.getLastname(), teacher.getVat());
    }

    public static List<TeacherReadOnlyDTO> mapToTeacherReadOnlyDTOs(List<Teacher> teachers) {
        return teachers.stream().map(Mapper::mapToReadOnlyDTO).collect(Collectors.toList());
    }
}
