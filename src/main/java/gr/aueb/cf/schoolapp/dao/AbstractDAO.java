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
        Optional<T> toUpdate = getById(t.getId());
        if (toUpdate.isEmpty()) {
            return Optional.empty();
        } else {
            getEntityManager().merge(t);
            return Optional.of(t);
        }
    }

    @Override
    public void delete(Object id) {
        Optional<T> toDelete = getById(id);
        toDelete.ifPresent(getEntityManager()::remove);
    }

    @Override
    public Long count() {
        return getEntityManager().createQuery("SELECT COUNT(e) FROM " + persistentClass.getSimpleName() + " e", Long.class).getSingleResult();
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
        String sql = "SELECT e FROM " + persistentClass.getSimpleName() + " e WHERE e." + fieldName + ":= value";
        TypedQuery<T> query = getEntityManager().createQuery(sql, persistentClass);
        query.setParameter("value", value);
        return query.getResultList().stream().findFirst();
    }

    @Override
    public List<T> getAll() {
        return getByCriteria(getPersistentClass(), Collections.emptyMap());
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
        if (page != null && size != null) {
            query.setFirstResult(page * size);      // skip
            query.setMaxResults(size);
        }
        return query.getResultList();
    }

    protected static EntityManager getEntityManager() {
        return JPAHelper.getEntityManager();
    }

    @SuppressWarnings("unchecked")
    protected <K extends T> List<Predicate> getPredicatesList(CriteriaBuilder builder, Root<K> entityRoot, Map<String, Object> criteria) {
        List<Predicate> predicates = new ArrayList<>();

        for (Map.Entry<String, Object> entry : criteria.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();

            // Handling the cases where the value is a List, Map or a "isNull" condition
            if (value instanceof List) {
                Path<?> path = resolvePath(entityRoot, key);
                CriteriaBuilder.In<Object> inClause = builder.in(path);
                for (Object v : (List<?>) value) {
                    inClause.value(v);
                }
                predicates.add(inClause);
            } else if (value instanceof Map) {
                // For 'BETWEEN' condition
                Map<String, Object> mapValue = (Map<String, Object>) value;
                if (mapValue.containsKey("from") && mapValue.containsKey("to")) {
                    Object from = mapValue.get("from");
                    Object to = mapValue.get("to");

                    if (from instanceof Comparable && to instanceof Comparable) {
                        Expression<? extends Comparable<Object>> path =
                                (Expression<? extends Comparable<Object>>) resolvePath(entityRoot, key);

                        predicates.add(builder.between(path, (Comparable<Object>) from, (Comparable<Object>) to));
                    }
                }
            } else if ("isNull".equals(value)) {
                // For 'IS NULL' condition
                predicates.add(builder.isNull(resolvePath(entityRoot, key)));
            } else if ("isNotNull".equals(value)) {
                // For 'IS NOT NULL' condition
                predicates.add(builder.isNotNull(resolvePath(entityRoot, key)));
            } else if (value instanceof String && ((String) value).contains("%")) {
                // Treat as LIKE pattern (e.g., "Jo%")
                predicates.add(
                        builder.like(
                                builder.lower((Expression<String>) resolvePath(entityRoot, key)),
                                ((String) value).toLowerCase()
                        ));
            } else {
                // For '=' condition (default case)
                predicates.add(builder.equal(resolvePath(entityRoot, key), value));
            }
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

    protected void addParametersToQuery(TypedQuery<? extends T> query, Map<String, Object> criteria) {
        for (Map.Entry<String, Object> entry : criteria.entrySet()) {
            Object value = entry.getValue();
            if (value instanceof List || value instanceof Map) {
                // Handle complex cases like IN or BETWEEN that need special parameter setting
                // (    Do not add % for LIKE here)
                query.setParameter(buildParameterAlias(entry.getKey()), value);
            } else {
                // Adding '%' for LIKE operations if needed
                query.setParameter(buildParameterAlias(entry.getKey()) , value + "%");
            }
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
