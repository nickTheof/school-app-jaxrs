package gr.aueb.cf.schoolapp.dao;

import gr.aueb.cf.schoolapp.model.Teacher;
import jakarta.enterprise.context.ApplicationScoped;


import java.util.Optional;

@ApplicationScoped
public class TeacherDAOImpl extends AbstractDAO<Teacher> implements ITeacherDAO {
    public TeacherDAOImpl() {
        this.setPersistentClass(Teacher.class);
    }

    @Override
    public Optional<Teacher> getByVat(String vat) {
        return this.findByField("vat", vat);
    }
}
