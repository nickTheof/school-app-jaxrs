package gr.aueb.cf.schoolapp.dao;

import gr.aueb.cf.schoolapp.model.IdentifiableEntity;
import gr.aueb.cf.schoolapp.service.util.JPAHelper;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.*;

import java.util.*;

public abstract class AbstractDAO<T extends IdentifiableEntity> implements IGenericDAO<T> {
    private Class<T> persistentClass;

    public AbstractDAO() {
    }

    public Class<T> getPersistentClass() {
        return persistentClass;
    }

    public void setPersistentClass(Class<T> persistentClass) {
        this.persistentClass = persistentClass;
    }

    @Override
    public Optional<T> insert(T t) {
        EntityManager em = getEntityManager();
        em.persist(t);
        return Optional.of(t);
    }

    @Override
    public Optional<T> update(T t) {
        EntityManager em = getEntityManager();
        em.merge(t);
        return Optional.of(t);
    }

    @Override
    public void delete(Object id) {
        EntityManager em = getEntityManager();
        Optional<T> toDelete = getById(id);
        toDelete.ifPresent(em::remove);
    }

    @Override
    public Long count() {
        EntityManager em = getEntityManager();
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Long> selectQuery = cb.createQuery(Long.class);
        Root<T> rootEntity = selectQuery.from(getPersistentClass());
        selectQuery.select(cb.count(rootEntity));
        return em.createQuery(selectQuery).getSingleResult();
    }

    @Override
    public Long getCountByCriteria(Map<String, Object> criteria) {
        EntityManager em = getEntityManager();
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Long> selectQuery = cb.createQuery(Long.class);
        Root<T> rootEntity = selectQuery.from(getPersistentClass());
        List<Predicate> predicates = getPredicatesList(cb, rootEntity, criteria);
        selectQuery.select(cb.count(rootEntity)).where(predicates.toArray(new Predicate[0]));
        return em.createQuery(selectQuery).getSingleResult();
    }

    @Override
    public Optional<T> getById(Object id) {
        EntityManager em = getEntityManager();
        return Optional.ofNullable(em.find(persistentClass, id));

    }

    @Override
    public Optional<T> findByField(String fieldName, Object value) {
        EntityManager em = getEntityManager();
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<T> builder = cb.createQuery(persistentClass);
        Root<T> root = builder.from(persistentClass);
        ParameterExpression<Object> parameterExpression = cb.parameter(Object.class, buildParameterAlias(fieldName));
        builder.select(root).where(cb.equal(root.get(buildParameterAlias(fieldName)), parameterExpression));
        List<T> results = em.createQuery(builder).setParameter(fieldName, value).getResultList();
        if (results.isEmpty()) {
            return Optional.empty();
        } else {
            return Optional.of(results.get(0));
        }
    }

    @Override
    public List<T> getAll() {
        return getByCriteria(getPersistentClass(), Collections.<String, Object>emptyMap());
    }

    @Override
    public List<? extends T> getByCriteria(Map<String, Object> criteria) {
        return getByCriteria(getPersistentClass(), criteria);
    }

    @Override
    public <K extends T> List<K> getByCriteria(Class<K> clazz, Map<String, Object> criteria) {
        TypedQuery<K> query = getByCriteriaQuery(clazz, criteria);
        return query.getResultList();
    }

    @Override
    public <K extends T> List<K> getByCriteriaPaginated(Class<K> clazz, Map<String, Object> criteria, Integer page, Integer size) {
        TypedQuery<K> query = getByCriteriaQuery(clazz, criteria);
        query.setFirstResult((page - 1) * size);
        query.setMaxResults(size);
        return query.getResultList();
    }

    protected static EntityManager getEntityManager() {
        return JPAHelper.getEntityManager();
    }

    protected List<Predicate> getPredicatesList(CriteriaBuilder builder, Root<? extends T> entityRoot,  Map<String, Object> criteria) {
        List<Predicate> predicates = new ArrayList<>();
        for (Map.Entry<String, Object> entry: criteria.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();
            ParameterExpression<?> val = builder.parameter(value.getClass(), buildParameterAlias(key));
            Predicate predicateLike = builder.like((Expression<String>) resolvePath(entityRoot, key), (Expression<String>) val);
            predicates.add(predicateLike);
        }
        return predicates;
    }

    protected Path<?> resolvePath(Root<? extends T> root, String expression) {
        String[] fields = expression.split("\\.");
        Path<?> path = root.get(fields[0]);
        for (int i = 1; i < fields.length; i++) {
            path = path.get(fields[i]);
        }
        return path;
    }

    protected String buildParameterAlias(String malformedAlias) {
        return malformedAlias.replaceAll("\\.", "");
    }

    protected void addParametersToQuery(TypedQuery<?> query, Map<String, Object> criteria) {
        for (Map.Entry<String, Object> entry: criteria.entrySet()) {
            Object value = entry.getValue();
            query.setParameter(buildParameterAlias(entry.getKey()), value + "%");
        }
    }

    protected <K extends T> TypedQuery<K> getByCriteriaQuery(Class<K> clazz, Map<String, Object> criteria) {
        EntityManager em = getEntityManager();
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<K> selectQuery = cb.createQuery(clazz);
        Root<K> entityRoot = selectQuery.from(clazz);
        List<Predicate> predicates = getPredicatesList(cb, entityRoot, criteria);
        selectQuery.select(entityRoot).where(predicates.toArray(new Predicate[0]));
        TypedQuery<K> query = em.createQuery(selectQuery);
        addParametersToQuery(query, criteria);
        return query;
    }
}
