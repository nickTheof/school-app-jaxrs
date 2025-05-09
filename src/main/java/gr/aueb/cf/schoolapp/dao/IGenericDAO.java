package gr.aueb.cf.schoolapp.dao;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface IGenericDAO<T> {
    Optional<T> insert(T t);
    Optional<T> update(T t);
    void delete(Object id);
    Long count();
    Long getCountByCriteria(Map<String, Object> criteria);
    Optional<T> getById(Object id);
    Optional<T> findByField(String fieldName, Object value);
    List<T> getAll();
    List<? extends T> getByCriteria(Map<String, Object> criteria);
    <K extends T> List<K> getByCriteria(Class<K> clazz, Map<String, Object> criteria);

    <K extends T> List<K> getByCriteriaPaginated(Class<K> clazz, Map<String, Object> criteria, Integer page, Integer size);
}
