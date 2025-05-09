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
            LOGGER.info("In INSERT after Begin Transaction");
            if (teacherDAO.getByVat(teacherInsertDTO.getVat()).isPresent()) throw new EntityAlreadyExistsException("Teacher", "Teacher with vat " + teacherInsertDTO.getVat() + " already exists");
            LOGGER.info("In INSERT after get by vat");
            Teacher teacher = Mapper.mapToTeacher(teacherInsertDTO);
            TeacherReadOnlyDTO readOnlyDTO = teacherDAO.insert(teacher).map(Mapper::mapToReadOnlyDTO).orElseThrow(() -> new EntityInvalidArgumentException("Teacher", "Teacher with vat " + teacherInsertDTO.getVat() + " not inserted"));
            JPAHelper.commitTransaction();
            LOGGER.info("Teacher with id, lastname, firstname, vat: {} {} {} {} inserted.", readOnlyDTO.getId(), readOnlyDTO.getLastname(), readOnlyDTO.getFirstname(), readOnlyDTO.getVat());
            return readOnlyDTO;
        } catch (EntityAlreadyExistsException | EntityInvalidArgumentException e) {
            JPAHelper.rollbackTransaction();
            LOGGER.error("Teacher with firstname, lastname, vat: {} {} {} not inserted.", teacherInsertDTO.getFirstname(), teacherInsertDTO.getLastname(), teacherInsertDTO.getVat());
            throw e;
        } finally {
            JPAHelper.closeEntityManager();
        }
    }

    @Override
    public TeacherReadOnlyDTO updateTeacher(TeacherUpdateDTO teacherUpdateDTO) throws EntityNotFoundException, EntityInvalidArgumentException {
        try {
            JPAHelper.beginTransaction();
            if (teacherDAO.getByVat(teacherUpdateDTO.getVat()).isEmpty() || teacherDAO.getById(teacherUpdateDTO.getId()).isEmpty()) {
                throw new EntityNotFoundException("Teacher", "Teacher with vat " + teacherUpdateDTO.getVat() + " not found for update.");
            }
            Teacher teacher = Mapper.mapToTeacher(teacherUpdateDTO);
            TeacherReadOnlyDTO teacherReadOnlyDTO = teacherDAO.update(teacher)
                    .map(Mapper::mapToReadOnlyDTO)
                    .orElseThrow(() -> new EntityInvalidArgumentException("Teacher", "Error during update"));
            JPAHelper.commitTransaction();
            LOGGER.info("Teacher with id {} updated successfully", teacherReadOnlyDTO.getId());
            return teacherReadOnlyDTO;
        } catch (EntityNotFoundException | EntityInvalidArgumentException e) {
            JPAHelper.rollbackTransaction();
            LOGGER.error("Error. Teacher {} {} {} not updated.", teacherUpdateDTO.getVat(), teacherUpdateDTO.getFirstname(), teacherUpdateDTO.getLastname());
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
            LOGGER.error("Teacher with id {} was not found to delete", id);
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
            LOGGER.error("Teacher with id: {} not found.", id);
            throw e;
        } finally {
            JPAHelper.closeEntityManager();
        }
    }

    @Override
    public List<TeacherReadOnlyDTO> getTeachersByCriteria(Map<String, Object> criteria) {
        try {
            JPAHelper.beginTransaction();
            List<TeacherReadOnlyDTO> teacherReadOnlyDTOS = teacherDAO.getByCriteria(criteria).stream().map(Mapper::mapToReadOnlyDTO).collect(Collectors.toList());
            JPAHelper.commitTransaction();
            return teacherReadOnlyDTOS;
        } finally {
            JPAHelper.closeEntityManager();
        }
    }
}
