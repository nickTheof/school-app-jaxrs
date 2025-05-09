package gr.aueb.cf.schoolapp.service;


import gr.aueb.cf.schoolapp.core.exceptions.EntityAlreadyExistsException;
import gr.aueb.cf.schoolapp.core.exceptions.EntityInvalidArgumentException;
import gr.aueb.cf.schoolapp.core.exceptions.EntityNotFoundException;
import gr.aueb.cf.schoolapp.dao.ITeacherDAO;
import gr.aueb.cf.schoolapp.dto.TeacherInsertDTO;
import gr.aueb.cf.schoolapp.dto.TeacherReadOnlyDTO;
import gr.aueb.cf.schoolapp.dto.TeacherUpdateDTO;
import gr.aueb.cf.schoolapp.mapper.Mapper;
import gr.aueb.cf.schoolapp.model.Teacher;
import gr.aueb.cf.schoolapp.service.util.JPAHelper;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@ApplicationScoped
@RequiredArgsConstructor(onConstructor_ = @__(@Inject))
public class TeacherServiceImpl implements ITeacherService {
    private static final Logger LOGGER = LoggerFactory.getLogger(TeacherServiceImpl.class);

    private final ITeacherDAO teacherDAO;

    @Override
    public TeacherReadOnlyDTO insertTeacher(TeacherInsertDTO teacherInsertDTO) throws EntityAlreadyExistsException, EntityInvalidArgumentException {
        try {
            JPAHelper.beginTransaction();
            if (teacherDAO.getByVat(teacherInsertDTO.vat()).isPresent()) throw new EntityAlreadyExistsException("Teacher", "Teacher with vat " + teacherInsertDTO.vat() + " already exists");
            Teacher teacher = Mapper.mapToTeacher(teacherInsertDTO);
            TeacherReadOnlyDTO readOnlyDTO = teacherDAO.insert(teacher).map(Mapper::mapToReadOnlyDTO).orElseThrow(() -> new EntityInvalidArgumentException("Teacher", "Teacher with vat " + teacherInsertDTO.vat() + " not inserted"));
            JPAHelper.commitTransaction();
            LOGGER.info("Teacher with id={}, lastname={}, firstname={}, vat={} inserted.", readOnlyDTO.id(), readOnlyDTO.lastname(), readOnlyDTO.firstname(), readOnlyDTO.vat());
            return readOnlyDTO;
        } catch (EntityAlreadyExistsException | EntityInvalidArgumentException e) {
            JPAHelper.rollbackTransaction();
            LOGGER.error("Teacher with firstname={}, lastname={}, vat={} not inserted.", teacherInsertDTO.firstname(), teacherInsertDTO.lastname(), teacherInsertDTO.vat());
            throw e;
        } finally {
            JPAHelper.closeEntityManager();
        }
    }

    @Override
    public TeacherReadOnlyDTO updateTeacher(TeacherUpdateDTO teacherUpdateDTO) throws EntityNotFoundException, EntityInvalidArgumentException {
        try {
            JPAHelper.beginTransaction();
            teacherDAO.getByVat(teacherUpdateDTO.vat()).orElseThrow(() -> new EntityNotFoundException("Teacher", "Teacher with vat " + teacherUpdateDTO.vat() + " not found"));
            teacherDAO.getById(teacherUpdateDTO.id()).orElseThrow(() -> new EntityNotFoundException("Teacher", "Teacher with id " + teacherUpdateDTO.id() + " not found"));
            Teacher teacher = Mapper.mapToTeacher(teacherUpdateDTO);
            TeacherReadOnlyDTO teacherReadOnlyDTO = teacherDAO.update(teacher)
                    .map(Mapper::mapToReadOnlyDTO)
                    .orElseThrow(() -> new EntityInvalidArgumentException("Teacher", "Error during update"));
            JPAHelper.commitTransaction();
            LOGGER.info("Teacher with id={} updated successfully", teacherReadOnlyDTO.id());
            return teacherReadOnlyDTO;
        } catch (EntityNotFoundException | EntityInvalidArgumentException e) {
            JPAHelper.rollbackTransaction();
            LOGGER.error("Error. Teacher with vat={}, firstname={}, lastname={} not updated.", teacherUpdateDTO.vat(), teacherUpdateDTO.firstname(), teacherUpdateDTO.lastname());
            throw e;
        } finally {
            JPAHelper.closeEntityManager();
        }
    }

    @Override
    public void deleteTeacher(Long id) throws EntityNotFoundException {
        try {
            JPAHelper.beginTransaction();
            teacherDAO.getById(id).orElseThrow(() -> new EntityNotFoundException("Teacher", "Teacher with id " + id + " not found for delete."));
            teacherDAO.delete(id);
            JPAHelper.commitTransaction();
        } catch (EntityNotFoundException e) {
            JPAHelper.rollbackTransaction();
            LOGGER.error("Teacher with id={} was not found to delete", id);
            throw e;
        } finally {
            JPAHelper.closeEntityManager();
        }
    }

    @Override
    public TeacherReadOnlyDTO getTeacherById(Long id) throws EntityNotFoundException {
        try {
            JPAHelper.beginTransaction();
            TeacherReadOnlyDTO readOnlyDTO = teacherDAO.getById(id).map(Mapper::mapToReadOnlyDTO).orElseThrow(()-> new EntityNotFoundException("Teacher", "Teacher with id " + id + " not found."));
            JPAHelper.commitTransaction();
            return readOnlyDTO;
        } catch (EntityNotFoundException e) {
            JPAHelper.rollbackTransaction();
            LOGGER.error("Teacher with id={} not found.", id);
            throw e;
        } finally {
            JPAHelper.closeEntityManager();
        }
    }

    @Override
    public List<TeacherReadOnlyDTO> getTeachersByCriteria(Map<String, Object> criteria) {
        try {
            JPAHelper.beginTransaction();
            List<TeacherReadOnlyDTO> teacherReadOnlyDTOS = Mapper.mapToTeacherReadOnlyDTOs(teacherDAO.getByCriteria(Teacher.class, criteria));
            JPAHelper.commitTransaction();
            return teacherReadOnlyDTOS;
        } finally {
            JPAHelper.closeEntityManager();
        }
    }

    @Override
    public List<TeacherReadOnlyDTO> getAllTeachers() {
        try {
            JPAHelper.beginTransaction();
            List<TeacherReadOnlyDTO> readOnlyDTOS =  Mapper.mapToTeacherReadOnlyDTOs(teacherDAO.getAll());
            JPAHelper.commitTransaction();
            return readOnlyDTOS;
        } finally {
            JPAHelper.closeEntityManager();
        }

    }

    @Override
    public long getTeachersCountByCriteria(Map<String, Object> criteria) {
        try {
            JPAHelper.beginTransaction();
            long count = teacherDAO.getCountByCriteria(criteria);
            JPAHelper.commitTransaction();
            return count;
        } finally {
            JPAHelper.closeEntityManager();
        }
    }

    @Override
    public List<TeacherReadOnlyDTO> getTeachersByCriteriaPaginated(Map<String, Object> criteria, Integer page, Integer size) {
        try {
            JPAHelper.beginTransaction();
            List<TeacherReadOnlyDTO> readOnlyDTOS = Mapper.mapToTeacherReadOnlyDTOs(teacherDAO.getByCriteriaPaginated(Teacher.class, criteria, page, size));
            JPAHelper.commitTransaction();
            return readOnlyDTOS;
        } finally {
            JPAHelper.closeEntityManager();
        }
    }
}
