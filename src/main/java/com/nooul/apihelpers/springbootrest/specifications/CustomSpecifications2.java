package com.nooul.apihelpers.springbootrest.specifications;

import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.criteria.*;
import javax.persistence.metamodel.*;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

@Service
public class CustomSpecifications2<T> {

    @PersistenceContext
    private EntityManager em;

    public Specification<T> customSpecificationBuilder(Map<String, Object> map) {

        return (Specification<T>) (root, query, builder) -> {

            query.distinct(true);
            return builder.and();
        };
    }


    public Predicate customSpecificationBuilder(CriteriaBuilder cb, CriteriaQuery query, Root root, Map<String, Object> filterMap, List<String> includeOnlyFields) {
        query.distinct(true);

        List<Predicate> andPredicates = handleMap(cb, root, filterMap);

        return cb.and(andPredicates.toArray(new Predicate[andPredicates.size()]));
    }

    private List<Predicate> handleMap(CriteriaBuilder cb, Root root, Map<String, Object> filterMap) {

        List<Predicate> andPredicates = new ArrayList<>();
        andPredicates.addAll(handlePrimitiveEquality(cb, root, filterMap));
        andPredicates.addAll(handleAssociationEquality(cb, root, filterMap));
        andPredicates.addAll(handlePrimitiveComparison(cb, root, filterMap, "Gte"));
        andPredicates.addAll(handlePrimitiveComparison(cb, root, filterMap, "Lte"));
        andPredicates.addAll(handlePrimitiveComparison(cb, root, filterMap, "Lt"));
        andPredicates.addAll(handlePrimitiveComparison(cb, root, filterMap, "Gt"));
        return andPredicates;
    }

    private List<Predicate> handleAssociationEquality(CriteriaBuilder cb, Root root, Map<String, Object> filterMap) {
        Map<String, Attribute> attributeMap = convertStringMapToAttrMap(root, filterMap);
        Map<String, Attribute> associationAttributesMap = filterAssociationAttributes(attributeMap);
        List<Predicate> predicates = new ArrayList<>();
        for (Map.Entry<String,Attribute> entry: associationAttributesMap.entrySet()) {
            String attributeName = entry.getKey();
            Object value = filterMap.get(entry.getKey());
                if (value == null) {
                    Predicate predicate = cb.and(cb.isNull(root.join(attributeName, JoinType.LEFT)));
                    predicates.add(predicate);
                } if (isPrimitiveValue(value)) {
                    Join join = root.join(attributeName);
                    Predicate predicate = cb.and(cb.equal(join.get(getIdAttribute(join)), value));
                    predicates.add(predicate);
                } else if (isCollectionOfPrimitives(value)) {
                    Collection vals = (Collection) value;
                    List<Predicate> orPredicates = new ArrayList<>();
                    for (Object val: vals) {
                        Join join = root.join(attributeName);
                        Predicate predicate = cb.equal(join.get(getIdAttribute(join)),val);
                        orPredicates.add(predicate);
                    }
                    predicates.add(cb.or(orPredicates.toArray(new Predicate[orPredicates.size()])));

                } else if(isMap(value)) {
                    Join join = root.join(attributeName);
                    String primaryKeyName = getIdAttribute(join);
                    Predicate predicate = cb.and(cb.equal(join.get(primaryKeyName), (((Map)value).get(primaryKeyName))));
                    predicates.add(predicate);
                } else if(isCollectionOfMaps(value)) {
                    Collection<Map> vals = (Collection<Map>) value;
                    List<Predicate> orPredicates = new ArrayList<>();
                    for (Map val: vals) {
                        Join join = root.join(attributeName);
                        String primaryKeyName = getIdAttribute(join);
                        Predicate predicate = cb.equal(root.join(attributeName).get(getIdAttribute(join)),(val.get(primaryKeyName)));
                        orPredicates.add(predicate);
                    }
                    predicates.add(cb.or(orPredicates.toArray(new Predicate[orPredicates.size()])));
                }
//            }
        }
        return predicates;
    }

    private List<Predicate>  handlePrimitiveEquality(CriteriaBuilder cb, Root root, Map<String, Object> filterMap) {
        List<Predicate> predicates = new ArrayList<>() ;
        Map<String, Object> primitiveMap = filterPrimitiveValues(filterMap);
        Map<String, Attribute> attributeMap = convertStringMapToAttrMap(root, filterMap);
        Map<String, Attribute> singularAttrMap = filterSingularAttrs(attributeMap);
        Map<String, Object> attributesWithCollectionValuesMap = filterAttributesWithCollectionValues(filterMap);
        for (Map.Entry<String,Object> entry: primitiveMap.entrySet()) {
            String attributeName = entry.getKey();
            if (singularAttrMap.containsKey(attributeName)) {
                Object value = entry.getValue();
                if (value == null) {
                    Predicate predicate = cb.and(cb.isNull(root.get(attributeName)));
                    predicates.add(predicate);
                } else {
                    Predicate predicate = cb.and(cb.equal(root.get(attributeName), value));
                    predicates.add(predicate);
                }
            }
        }
        for (Map.Entry<String,Object> entry: attributesWithCollectionValuesMap.entrySet()) {
            Object value = entry.getValue();
            if(!convertStringToAttribute(root, entry.getKey()).isAssociation()) {
                Collection vals = (Collection) value;
                List<Predicate> orPredicates = new ArrayList<>();
                for (Object val: vals) {
                    Predicate predicate = cb.equal(root.get(entry.getKey()), val);
                    orPredicates.add(predicate);
                }
                predicates.add(cb.or(orPredicates.toArray(new Predicate[orPredicates.size()])));
            }
        }

        return predicates;
    }

    private List<Predicate> handlePrimitiveComparison(CriteriaBuilder cb, Root root, Map<String, Object> filterMap, String comparisonPostFix) {
        List<Predicate> predicates = new ArrayList<>() ;
        Map<String, Object> primitiveMap = filterPrimitiveValues(filterMap);
        for (Map.Entry<String,Object> entry: primitiveMap.entrySet()) {
            String attributeName = entry.getKey();
            if (attributeName.endsWith(comparisonPostFix)) {
                Object value = entry.getValue();
                if (value != null) {

                    switch (comparisonPostFix) {
                        case "Gte": { predicates.add(createGtePredicate(cb, root, attributeName, value)); break; }
                        case "Lte": { predicates.add(createLtePredicate(cb, root, attributeName, value)); break; }
                        case "Lt": { predicates.add(createLtPredicate(cb, root, attributeName, value)); break; }
                        case "Gt": { predicates.add(createGtPredicate(cb, root, attributeName, value)); break; }
                    }
                }

            }
        }
        return predicates;
    }

    private boolean isDirtyKey(String key) {
        return !key.equals(cleanUpKey(key));
    }

    private String cleanUpKey(String key) {

        List<String> postfixes = Arrays.asList("Gte", "Gt", "Lte", "Lt", "Not", "And");
        for (String postfix : postfixes) {
            if (key.endsWith(postfix)) {
                return key.substring(0, key.length() - postfix.length());
            }
        }
        return key;
    }


    private Predicate createLtPredicate(CriteriaBuilder builder, Root root, String attributeName, Object val) {
        String cleanKey = cleanUpKey(attributeName);
        if (val instanceof String) {
            Timestamp timestamp = timeStamp((String)val);
            if (timestamp != null) {
                return builder.lessThan(builder.lower(root.get(cleanKey)), timestamp);
            }
            return builder.lessThan(builder.lower(root.get(cleanKey)), ((String) val).toLowerCase());
        } else if (val instanceof Integer) {
            return builder.lessThan(root.get(cleanKey), (Integer) val);
        }
        throw new IllegalArgumentException("val type not supported yet");
    }

    private Predicate createLtePredicate(CriteriaBuilder builder, Root root, String attributeName, Object val) {
        String cleanKey = cleanUpKey(attributeName);
        if (val instanceof String) {
            Timestamp timestamp = timeStamp((String)val);
            if (timestamp != null) {
                return builder.lessThanOrEqualTo(builder.lower(root.get(cleanKey)), timestamp);
            }
            return builder.lessThanOrEqualTo(builder.lower(root.get(cleanKey)), ((String) val).toLowerCase());
        } else if (val instanceof Integer) {
            return builder.lessThanOrEqualTo(root.get(cleanKey), (Integer) val);
        }
        throw new IllegalArgumentException("val type not supported yet");
    }


    private Predicate createGtPredicate(CriteriaBuilder builder, Root root, String attributeName, Object val) {
        String cleanKey = cleanUpKey(attributeName);
        if (val instanceof String) {
            Timestamp timestamp = timeStamp((String)val);
            if (timestamp != null) {
                return builder.greaterThan(builder.lower(root.get(cleanKey)), timestamp);
            }

            return builder.greaterThan(builder.lower(root.get(cleanKey)), ((String) val).toLowerCase());
        } else if (val instanceof Integer) {
            return builder.greaterThan(root.get(cleanKey), (Integer) val);
        }
        throw new IllegalArgumentException("val type not supported yet");
    }


    private Predicate createGtePredicate(CriteriaBuilder builder, Root root, String attributeName, Object val) {
        String cleanKey = cleanUpKey(attributeName);
        if (val instanceof String) {
            Timestamp timestamp = timeStamp((String)val);
            if (timestamp != null) {
                return builder.greaterThanOrEqualTo(builder.lower(root.get(cleanKey)), timestamp);
            }
            return builder.greaterThanOrEqualTo(builder.lower(root.get(cleanKey)), ((String) val).toLowerCase());
        } else if (val instanceof Integer) {
            return builder.greaterThanOrEqualTo(root.get(cleanKey), (Integer) val);
        }
        throw new IllegalArgumentException("val type not supported yet");
    }

    private static Timestamp timeStamp(String dateStr) {
        DateFormat dateFormat;
        if (dateStr.contains("T")) {
            dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
        }
        else {
            dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        }
        Date date;
        try {
            date = dateFormat.parse(dateStr);
        } catch (ParseException e) {
            return null;
        }
        long time = date.getTime();
        return new Timestamp(time);
    }


    public Map<String, Attribute> convertStringMapToAttrMap(Root root, Map<String, Object> filterMap) {
        Map<String, Attribute> attributeMap = new HashMap<>();
        for (Map.Entry<String,Object> entry: filterMap.entrySet()) {
            if (isDirtyKey(entry.getKey())) {
                continue;
            }
            Attribute attribute = root.getModel().getAttribute(entry.getKey());
            attributeMap.put(entry.getKey(), attribute);
        }
        return attributeMap;
    }

    public static Attribute convertStringToAttribute(Root root, String name) {
        return root.getModel().getAttribute(name);
    }



    private String getIdAttribute(Join join) {
        String primaryKey =  getIdAttribute(em, join.getJavaType()).getName();
        return primaryKey;
    }

    //https://stackoverflow.com/a/16911313/986160
    //https://stackoverflow.com/a/47793003/986160
    private Attribute getIdAttribute(EntityManager em, Class<T> clazz) {
        Metamodel m = em.getMetamodel();
        IdentifiableType<T> of = (IdentifiableType<T>) m.managedType(clazz);
        return of.getId(of.getIdType().getJavaType());
    }
//    private Class getJavaTypeOfClassContainingAttribute(Root root, String attributeName) {
//        Attribute a = root.getModel().getAttribute(attributeName);
//        if (a.isAssociation()) {
//            return addJoinIfNotExists(root, a, false, false).getJavaType();
//        }
//        return null;
//    }


    private static Map<String,Attribute> filterSingularAttrs(Map<String,Attribute> map) {
        Map<String, Attribute> singularAttrMap = new HashMap<>();
        for (Map.Entry<String,Attribute> entry: map.entrySet()) {
            Attribute attribute = entry.getValue();
            if (attribute instanceof SingularAttribute) {
                singularAttrMap.put(entry.getKey(), entry.getValue());
            }
        }
        return singularAttrMap;
    }

    private static Map<String,Attribute> filterCollectionAttrs(Map<String,Attribute> map) {
        Map<String, Attribute> singularAttrMap = new HashMap<>();
        for (Map.Entry<String,Attribute> entry: map.entrySet()) {
            Attribute attribute = entry.getValue();
            if (attribute instanceof ListAttribute ||
                    attribute instanceof CollectionAttribute ||
                    attribute instanceof SetAttribute) {
                singularAttrMap.put(entry.getKey(), entry.getValue());
            }
        }
        return singularAttrMap;
    }


    private static Map<String,Object> filterPrimitiveValues(Map<String,Object> map) {
        Map<String, Object> primitiveMap = new HashMap<>();
        for (Map.Entry<String,Object> entry: map.entrySet()) {
            if (isPrimitiveValue(entry.getValue()) || entry.getValue() == null) {
                primitiveMap.put(entry.getKey(), entry.getValue());
            }
        }
        return primitiveMap;
    }

    private static Map<String,Object> filterAttributesWithCollectionValues(Map<String,Object> map) {
        Map<String, Object> collectionValuesMap = new HashMap<>();
        for (Map.Entry<String,Object> entry: map.entrySet()) {
            if (entry.getValue() instanceof Collection) {
                collectionValuesMap.put(entry.getKey(), entry.getValue());
            }
        }
        return collectionValuesMap;
    }

    private static Map<String, Attribute> filterAssociationAttributes(Map<String, Attribute> map) {
        Map<String, Attribute> associationsMap = new HashMap<>();
        for (Map.Entry<String,Attribute> entry: map.entrySet()) {
            Attribute attribute = entry.getValue();
            if (attribute.isAssociation()) {
                associationsMap.put(entry.getKey(), entry.getValue());
            }
        }
        return associationsMap;
    }
//
//    private static Map<String, Object> filterJoinEntities(Map<String, Object> map) {
//        Map<String, Object> joinEntitiesMap = new HashMap<>();
//        for (Map.Entry<String,Object> entry: map.entrySet()) {
//            if (!isPrimitiveValue(entry.getValue()) &&
//                    !isCollectionOfPrimitives(entry.getValue())) {
//                joinEntitiesMap.put(entry.getKey(), entry.getValue());
//            }
//        }
//        return joinEntitiesMap;
//    }

    private static boolean isCollectionOfPrimitives(Object value) {
        if (value instanceof Collection) {
            Collection collection = (Collection) value;
            Object object = collection.iterator().next();
            if (isPrimitiveValue(object)) {
                return true;
            }
        }
        return false;
    }

    private static boolean isCollectionOfMaps(Object value) {
        if (value instanceof Collection) {
            Collection collection = (Collection) value;
            Object object = collection.iterator().next();
            if (object instanceof Map) {
                return true;
            }
        }
        return false;
    }

    private static boolean isMap(Object value) {

        return value != null && value instanceof Map;
    }
//
//    private static boolean isCollectionOfObjects(Object value) {
//        if (value instanceof Collection) {
//            Collection collection = (Collection) value;
//            Object object = collection.iterator().next();
//            if (!isPrimitiveValue(object)) {
//                return true;
//            }
//        }
//        return false;
//    }


    private static boolean isPrimitiveValue(Object obj) {
        if (obj == null) {
            return false;
        }
        String javaClass = obj.getClass().getSimpleName().toLowerCase();
        return javaClass.startsWith("int") ||
                javaClass.startsWith("long") ||
                javaClass.equals("boolean") ||
                javaClass.equals("string") ||
                javaClass.equals("float") ||
                javaClass.equals("double");
    }


    @Deprecated
    public Predicate customSpecificationBuilder(CriteriaBuilder builder, CriteriaQuery query, Root root, Map<String, Object> filterMap) {
        query.distinct(true);
        return builder.and();
    }




    @Deprecated
    public Predicate customSpecificationBuilder(CriteriaBuilder builder, CriteriaQuery query, Root root, List<Map<String, Object>> filterList) {
        query.distinct(true);
//        List<Predicate> orPredicates = new ArrayList<>();
//        for (Map<String, Object> map: list) {
//            List<Predicate> predicates = handleMap(builder, root, null, query, map, new ArrayList<>());
//            Predicate orPred =  builder.and(predicates.toArray(new Predicate[predicates.size()]));
//            orPredicates.add(orPred);
//        }
        return builder.or();
    }
}
