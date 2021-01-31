package com.nooul.apihelpers.springbootrest.specifications;

import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.criteria.*;
import javax.persistence.metamodel.*;
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

    public Map<String, Attribute> convertStringMapToAttrMap(Root root, Map<String, Object> filterMap) {
        Map<String, Attribute> attributeMap = new HashMap<>();
        for (Map.Entry<String,Object> entry: filterMap.entrySet()) {
            Attribute attribute = root.getModel().getAttribute(entry.getKey());
            attributeMap.put(entry.getKey(), attribute);
        }
        return attributeMap;
    }


    public Predicate customSpecificationBuilder(CriteriaBuilder cb, CriteriaQuery query, Root root, Map<String, Object> filterMap, List<String> includeOnlyFields) {
        query.distinct(true);

        List<Predicate> andPredicates = handleMap(cb, root, filterMap);

        return cb.and(andPredicates.toArray(new Predicate[andPredicates.size()]));
    }

    private List<Predicate> handleMap(CriteriaBuilder cb, Root root, Map<String, Object> filterMap) {
        Map<String, Attribute> attributeMap = convertStringMapToAttrMap(root, filterMap);
        Map<String, Attribute> singularAttrMap = filterSingularAttrs(attributeMap);
        Map<String, Attribute> listAttrMap = filterListAttr(attributeMap);

        Map<String, Attribute> associationAttributesMap = filterAssociationAttributes(attributeMap);


        List<Predicate> andPredicates = new ArrayList<>();

        Map<String, Object> primitiveMap = filterPrimitiveValues(filterMap);
        for (Map.Entry<String,Object> entry: primitiveMap.entrySet()) {
            String attributeName = entry.getKey();
            if (singularAttrMap.containsKey(attributeName)) {
                Object value = entry.getValue();
                if (value == null) {
                    Predicate predicate = cb.and(cb.isNull(root.get(attributeName)));
                    andPredicates.add(predicate);
                }
                else {
                    Predicate predicate = cb.and(cb.equal(root.get(attributeName), value));
                    andPredicates.add(predicate);
                }
            }
        }

        for (Map.Entry<String,Attribute> entry: associationAttributesMap.entrySet()) {
            String attributeName = entry.getKey();
            Object value = filterMap.get(entry.getKey());
                if (value == null) {
                    Predicate predicate = cb.and(cb.isNull(root.join(attributeName, JoinType.LEFT)));
                    andPredicates.add(predicate);
                } if (isPrimitiveValue(value)) {
                    Predicate predicate = cb.and(cb.equal(root.join(attributeName).get("id"), value));
                    andPredicates.add(predicate);
                } else if (isCollectionOfPrimitives(value)) {
                    Collection vals = (Collection) value;
                    List<Predicate> orPredicates = new ArrayList<>();
                    for (Object val: vals) {

                        Predicate predicate = cb.equal(root.join(attributeName).get("id"),val);
                        orPredicates.add(predicate);
                    }
                    andPredicates.add(cb.or(orPredicates.toArray(new Predicate[orPredicates.size()])));

                } else if(isMap(value)) {
                    Predicate predicate = cb.and(cb.equal(root.join(attributeName).get("id"), (((Map)value).get("id"))));
                    andPredicates.add(predicate);
                } else if(isCollectionOfMaps(value)) {
                    Collection<Map> vals = (Collection<Map>) value;
                    List<Predicate> orPredicates = new ArrayList<>();
                    for (Map val: vals) {
                        Predicate predicate = cb.equal(root.join(attributeName).get("id"),(val.get("id")));
                        orPredicates.add(predicate);
                    }
                    andPredicates.add(cb.or(orPredicates.toArray(new Predicate[orPredicates.size()])));
                }
//            }

        }
        return andPredicates;
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

    private static Map<String,Attribute> filterListAttr(Map<String,Attribute> map) {
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
