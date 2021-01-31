package com.nooul.apihelpers.springbootrest.specifications;

import org.apache.commons.lang3.StringUtils;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.w3c.dom.Attr;

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
            addRoot(root, query);
            query.distinct(true);
            return builder.and();
        };
    }


    @Deprecated
    public Predicate customSpecificationBuilder(CriteriaBuilder cb, CriteriaQuery query, Path path, Map<String, Object> filterMap) {
        return customSpecificationBuilder(cb, query, path, filterMap, new ArrayList<>());
    }



    public Predicate customSpecificationBuilder(CriteriaBuilder cb, CriteriaQuery query, Path path, Map<String, Object> filterMap, List<String> searchOnlyInFields) {
        query.distinct(true);

        List<Predicate> andPredicates = handleMap(cb, query, path, filterMap, searchOnlyInFields);

        return cb.and(andPredicates.toArray(new Predicate[andPredicates.size()]));
    }

    private List<Predicate> handleMap(CriteriaBuilder cb, CriteriaQuery query, Path path, Map<String, Object> filterMap, List<String> searchOnlyInFields) {
        if (path instanceof Root) {
            addRoot(path, query);
        }
        List<Predicate> andPredicates = new ArrayList<>();
        if (filterMap.containsKey("q") && filterMap.get("q") instanceof String) {

            andPredicates.add(searchInAllAttributesPredicate(cb, query, path, (String) filterMap.get("q"), searchOnlyInFields));
            filterMap.remove("q");
        }



        andPredicates.addAll(handlePrimitiveEquality(cb, query, path, filterMap));
        andPredicates.addAll(handleAssociationEquality(cb, query, path, filterMap));
        andPredicates.addAll(handlePrimitiveComparison(cb, path, filterMap, "Gte"));
        andPredicates.addAll(handlePrimitiveComparison(cb, path, filterMap, "Lte"));
        andPredicates.addAll(handlePrimitiveComparison(cb, path, filterMap, "Lt"));
        andPredicates.addAll(handlePrimitiveComparison(cb, path, filterMap, "Gt"));
        return andPredicates;
    }
    private Root addRoot(Path path,  CriteriaQuery query) {
        if (path instanceof Root) {
            return (Root)path;
            //return rootsMap.put(path.getJavaType(), (Root)path);
        }
        else if(path instanceof Join) {
            Class key = path.getModel().getBindableJavaType();
//            if (rootsMap.containsKey(key)) {
//                return rootsMap.get(key);
//            }
//            else {
//                rootsMap.put(key, query.from(key));
//                return rootsMap.get(key);
//            }
            return query.from(key);
        }
        else {
            Class key = path.getJavaType();
//            if (rootsMap.containsKey(key)) {
//                return  rootsMap.get(key);
//            }
//            else {
//                Root root = rootsMap.put(key, query.from(key));
//                return root;
//            }
            return query.from(key);
        }
    }

    private List<Predicate> handleAssociationEquality(CriteriaBuilder cb, CriteriaQuery query, Path path, Map<String, Object> filterMap) {
        Root root = addRoot(path, query);
        Map<String, Attribute> attributeMap = convertStringMapToAttrMap(root, filterMap);
        Map<String, Attribute> associationAttributesMap = filterAssociationAttributes(attributeMap);
        List<Predicate> predicates = new ArrayList<>();

        for (Map.Entry<String,Attribute> entry: associationAttributesMap.entrySet()) {
            String attributeName = entry.getKey();
            Object value = filterMap.get(entry.getKey());
            if (value == null) {
                Predicate predicate = cb.and(cb.isNull(root.join(attributeName, JoinType.LEFT)));
                predicates.add(predicate);
            } else {
                Join join = addJoinIfNotExists(root, attributeName);
                String primaryKeyName = getPrimaryKey(join);
                if (isPrimitiveValue(value)) {

                    Predicate predicate = cb.and(cb.equal(join.get(getPrimaryKey(join)), value));
                    predicates.add(predicate);
                } else if (isCollectionOfPrimitives(value)) {
                    Collection vals = (Collection) value;
                    List<Predicate> orPredicates = new ArrayList<>();
                    for (Object val : vals) {
                        Predicate predicate = cb.equal(join.get(getPrimaryKey(join)), val);
                        orPredicates.add(predicate);
                    }
                    predicates.add(cb.or(orPredicates.toArray(new Predicate[orPredicates.size()])));

                } else if (isMap(value)) {


                    if (((Map)value).containsKey(primaryKeyName)) {
                        Predicate predicate = cb.and(cb.equal(join.get(primaryKeyName), (((Map) value).get(primaryKeyName))));
                        predicates.add(predicate);
                    }
                    else {
//                        Path expanded = path.get(attributeName);
                        predicates.addAll(handleMap(cb, query, path.get(attributeName), (Map)value, new ArrayList<>()));
                        //do something for non ids in actors: { name:  }
                    }
                } else if (isCollectionOfMaps(value)) {
                    Collection<Map> vals = (Collection<Map>) value;
                    List<Predicate> orPredicates = new ArrayList<>();
                    for (Map val : vals) {
                        if (val.containsKey(primaryKeyName)) {
                            Predicate predicate = cb.equal(root.join(attributeName).get(getPrimaryKey(join)), (val.get(primaryKeyName)));
                            orPredicates.add(predicate);
                        }
                    }
                    predicates.add(cb.or(orPredicates.toArray(new Predicate[orPredicates.size()])));
                }
            }
        }
        return predicates;
    }


    private Predicate searchInAllAttributesPredicate(CriteriaBuilder builder, CriteriaQuery query, Path path, String text, List<String> includeOnlyFields) {

        if (!text.contains("%")) {
            text = "%" + text + "%";
        }
        final String finalText = text;
        Root root = addRoot(path, query);
        Set<Attribute> attributes = root.getModel().getAttributes();
        List<Predicate> orPredicates = new ArrayList<>();
        for (Attribute a : attributes) {
            boolean javaTypeIsString = a.getJavaType().getSimpleName().equalsIgnoreCase("string");
            boolean shouldSearch = includeOnlyFields.isEmpty() || includeOnlyFields.contains(a.getName());
            if (javaTypeIsString && shouldSearch) {
                Predicate orPred = builder.like(root.get(a.getName()), finalText);
                orPredicates.add(orPred);
            }

        }

        return builder.or(orPredicates.toArray(new Predicate[orPredicates.size()]));

    }

    private Join addJoinIfNotExists(Root root, String attributeName) {
        Set<Join> joins = root.getJoins();
        Join toReturn = null;
        for (Join join: joins) {
            if (attributeName.equals(join.getAttribute().getName())){
                toReturn =  join;
                break;
            }
        }
        if (toReturn == null) {
            toReturn = root.join(attributeName);
        }
        return toReturn;
    }

    private List<Predicate>  handlePrimitiveEquality(CriteriaBuilder cb, CriteriaQuery query, Path path, Map<String, Object> filterMap) {
        List<Predicate> predicates = new ArrayList<>() ;
        Map<String, Object> primitiveMap = filterPrimitiveValues(filterMap);
        Root root = addRoot(path, query);
        Map<String, Attribute> attributeMap = convertStringMapToAttrMap(root, filterMap);
        Map<String, Attribute> singularAttrMap = filterSingularAttrs(attributeMap);
        Map<String, Attribute> collectionAttrMap = filterCollectionAttrs(attributeMap);
        Map<String, Object> attributesWithCollectionValuesMap = filterAttributesWithCollectionValues(filterMap);
        for (Map.Entry<String,Object> entry: primitiveMap.entrySet()) {
            String attributeName = entry.getKey();
            Attribute attribute = attributeMap.get(attributeName);
            Object value = entry.getValue();
            if (singularAttrMap.containsKey(attributeName)) {

                boolean isValTextSearch = (value instanceof String) && ((String) value).contains("%");
                if (isNullValue(root, query, attributeName, value)) {
                    Predicate predicate = cb.and(cb.isNull(path.get(attributeName)));
                    predicates.add(predicate);
                } else if (isValTextSearch) {
                    Predicate predicate = cb.and(cb.like(path.get(attributeName), (String) value));
                    predicates.add(predicate);
                } else if (isEnum(attribute) && value instanceof String) {
                    Predicate predicate = cb.equal(path.get(attributeName), Enum.valueOf(Class.class.cast(attribute.getJavaType()), (String) value));
                    predicates.add(predicate);
                } else {
                    Predicate predicate = cb.and(cb.equal(path.get(attributeName), value));
                    predicates.add(predicate);
                }
            } else if(collectionAttrMap.containsKey(attributeName)) {
                // primitive collectionTable only ??
                if (isNullValue(root, query, attributeName, value)) {
                    Predicate predicate = cb.and(cb.isNull(root.join(attributeName, JoinType.LEFT)));
                    predicates.add(predicate);
                }
                else {
                    Predicate predicate = cb.and(root.join(attributeName).in(Arrays.asList(value)));
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
                    Predicate predicate = cb.equal(path.get(entry.getKey()), val);
                    orPredicates.add(predicate);
                }
                predicates.add(cb.or(orPredicates.toArray(new Predicate[orPredicates.size()])));
            }
        }

        return predicates;
    }

    private List<Predicate> handlePrimitiveComparison(CriteriaBuilder cb, Path path, Map<String, Object> filterMap, String comparisonPostFix) {
        List<Predicate> predicates = new ArrayList<>() ;
        Map<String, Object> primitiveMap = filterPrimitiveValues(filterMap);
        for (Map.Entry<String,Object> entry: primitiveMap.entrySet()) {
            String attributeName = entry.getKey();
            if (attributeName.endsWith(comparisonPostFix)) {
                Object value = entry.getValue();
                if (value != null) {
                    switch (comparisonPostFix) {
                        case "Gte": { predicates.add(createGtePredicate(cb, path, attributeName, value)); break; }
                        case "Lte": { predicates.add(createLtePredicate(cb, path, attributeName, value)); break; }
                        case "Lt": { predicates.add(createLtPredicate(cb, path, attributeName, value)); break; }
                        case "Gt": { predicates.add(createGtPredicate(cb, path, attributeName, value)); break; }
                    }
                }

            }
        }
        return predicates;
    }

    private boolean isEnum(Attribute attribute) {
        String parentJavaClass = "";
        if (attribute.getJavaType().getSuperclass() != null) {
            parentJavaClass = attribute.getJavaType().getSuperclass().getSimpleName().toLowerCase();
        }
        return parentJavaClass.equals("enum");
    }


    private boolean isNullValue(Path path, CriteriaQuery query, String attributeName, Object val) {
        Root root = addRoot(path, query);
        Attribute attribute = convertStringToAttribute(root, attributeName);
        if (isPrimitiveAttribute(attribute)) {
            String attributeJavaClass = attribute.getJavaType().getSimpleName().toLowerCase();
            if (attributeJavaClass.equals("string")) {
                String valObj = (String) val;
                return StringUtils.isBlank(valObj) || valObj.equalsIgnoreCase("null");
            }
            else {
                return val == null;
            }
        }
        else {
            return val == null;
        }
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

    private Predicate createLtPredicate(CriteriaBuilder builder, Path path, String attributeName, Object val) {
        String cleanKey = cleanUpKey(attributeName);
        if (val instanceof String) {
            Timestamp timestamp = timeStamp((String)val);
            if (timestamp != null) {
                return builder.lessThan(builder.lower(path.get(cleanKey)), timestamp);
            }
            return builder.lessThan(builder.lower(path.get(cleanKey)), ((String) val).toLowerCase());
        } else if (val instanceof Integer) {
            return builder.lessThan(path.get(cleanKey), (Integer) val);
        }
        throw new IllegalArgumentException("val type not supported yet");
    }

    private Predicate createLtePredicate(CriteriaBuilder builder, Path path, String attributeName, Object val) {
        String cleanKey = cleanUpKey(attributeName);
        if (val instanceof String) {
            Timestamp timestamp = timeStamp((String)val);
            if (timestamp != null) {
                return builder.lessThanOrEqualTo(builder.lower(path.get(cleanKey)), timestamp);
            }
            return builder.lessThanOrEqualTo(builder.lower(path.get(cleanKey)), ((String) val).toLowerCase());
        } else if (val instanceof Integer) {
            return builder.lessThanOrEqualTo(path.get(cleanKey), (Integer) val);
        }
        throw new IllegalArgumentException("val type not supported yet");
    }


    private Predicate createGtPredicate(CriteriaBuilder builder, Path path, String attributeName, Object val) {
        String cleanKey = cleanUpKey(attributeName);
        if (val instanceof String) {
            Timestamp timestamp = timeStamp((String)val);
            if (timestamp != null) {
                return builder.greaterThan(builder.lower(path.get(cleanKey)), timestamp);
            }

            return builder.greaterThan(builder.lower(path.get(cleanKey)), ((String) val).toLowerCase());
        } else if (val instanceof Integer) {
            return builder.greaterThan(path.get(cleanKey), (Integer) val);
        }
        throw new IllegalArgumentException("val type not supported yet");
    }


    private Predicate createGtePredicate(CriteriaBuilder builder, Path path, String attributeName, Object val) {
        String cleanKey = cleanUpKey(attributeName);
        if (val instanceof String) {
            Timestamp timestamp = timeStamp((String)val);
            if (timestamp != null) {
                return builder.greaterThanOrEqualTo(builder.lower(path.get(cleanKey)), timestamp);
            }
            return builder.greaterThanOrEqualTo(builder.lower(path.get(cleanKey)), ((String) val).toLowerCase());
        } else if (val instanceof Integer) {
            return builder.greaterThanOrEqualTo(path.get(cleanKey), (Integer) val);
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



    private String getPrimaryKey(Join join) {
        String primaryKey =  getPrimaryKey(em, join.getJavaType()).getName();
        return primaryKey;
    }

    //https://stackoverflow.com/a/16911313/986160
    //https://stackoverflow.com/a/47793003/986160
    private Attribute getPrimaryKey(EntityManager em, Class<T> clazz) {
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

    private boolean isPrimitiveAttribute(Attribute attribute) {
        String attributeJavaClass = attribute.getJavaType().getSimpleName().toLowerCase();
        return attributeJavaClass.startsWith("int") ||
                attributeJavaClass.startsWith("long") ||
                attributeJavaClass.equals("boolean") ||
                attributeJavaClass.equals("string") ||
                attributeJavaClass.equals("float") ||
                attributeJavaClass.equals("double");
    }


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
