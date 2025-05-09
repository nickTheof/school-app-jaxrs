package gr.aueb.cf.schoolapp.mapper;

import gr.aueb.cf.schoolapp.dto.TeacherFiltersDTO;
import gr.aueb.cf.schoolapp.dto.TeacherInsertDTO;
import gr.aueb.cf.schoolapp.dto.TeacherReadOnlyDTO;
import gr.aueb.cf.schoolapp.dto.TeacherUpdateDTO;
import gr.aueb.cf.schoolapp.model.Teacher;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class Mapper {
    private Mapper() {

    }

    public static Teacher mapToTeacher(TeacherInsertDTO teacherInsertDTO) {
        return new Teacher(null, teacherInsertDTO.vat(), teacherInsertDTO.firstname(), teacherInsertDTO.lastname());
    }

    public static Teacher mapToTeacher(TeacherUpdateDTO teacherUpdateDTO) {
        return new Teacher(teacherUpdateDTO.id(), teacherUpdateDTO.vat(), teacherUpdateDTO.firstname(), teacherUpdateDTO.lastname());
    }

    public static TeacherReadOnlyDTO mapToReadOnlyDTO(Teacher teacher) {
        return new TeacherReadOnlyDTO(teacher.getId(), teacher.getFirstname(), teacher.getLastname(), teacher.getVat());
    }

    public static List<TeacherReadOnlyDTO> mapToTeacherReadOnlyDTOs(List<Teacher> teachers) {
        return teachers.stream().map(Mapper::mapToReadOnlyDTO).collect(Collectors.toList());
    }

    public static Map<String, Object> mapToCriteria(TeacherFiltersDTO filtersDTO) {
        Map<String, Object> criteria = new HashMap<>();

        if (!(filtersDTO.firstname() == null) && !filtersDTO.firstname().isEmpty()) {
            criteria.put("firstname", filtersDTO.firstname());
        }
        if (!(filtersDTO.lastname() == null) && !filtersDTO.lastname().isEmpty()) {
            criteria.put("lastname", filtersDTO.lastname());
        }
        if (!(filtersDTO.vat() == null) && !filtersDTO.vat().isEmpty()) {
            criteria.put("vat", filtersDTO.vat());
        }
        return criteria;
    }
}
