package com.nooul.apihelpers.springbootrest.specifications;


import com.nooul.apihelpers.springbootrest.annotations.ValueObject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.criteria.*;
import jakarta.persistence.metamodel.Attribute;
import jakarta.persistence.metamodel.IdentifiableType;
import jakarta.persistence.metamodel.Metamodel;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

//from: https://github.com/Nooul/spring-boot-rest-api-helpers/blob/master/src/main/java/com/nooul/apihelpers/springbootrest/specifications/CustomSpecifications.java
@Service
public class CustomSpecifications<T> {

    @PersistenceContext
    private EntityManager em;

    public Specification<T> build(Map<String, Object> map) {
        return (root, query, builder) -> {
            query.distinct(true);
            List<Predicate> predicates = handleMap(builder, root, null, query, map, new ArrayList<>(), false);
            return builder.and(predicates.toArray(new Predicate[predicates.size()]));
        };
    }


    public Specification<T> build(Map<String, Object> map, List<String> includeOnlyFields) {
        return (root, query, builder) -> {
            query.distinct(true);
            if (map.containsKey("allowDuplicates") && map.get("allowDuplicates") instanceof Boolean && (boolean) map.get("allowDuplicates")) {
                query.distinct(false);
            }
            map.remove("allowDuplicates");
            List<Predicate> predicates = handleMap(builder, root, null, query, map, includeOnlyFields,false);
            return builder.and(predicates.toArray(new Predicate[predicates.size()]));
        };
    }


    public Specification<T> build(List<Map<String, Object>> list) {
        return (root, query, builder) -> {

            query.distinct(true);
            List<Predicate> orPredicates = new ArrayList<>();
            boolean needsLeftJoin = false;
            if (containsMapsWithNullValues(list)) {
                needsLeftJoin = true;
            }

            for (Map<String, Object> map : list) {
                List<Predicate> predicates = handleMap(builder, root, null, query, map, new ArrayList<>(), needsLeftJoin);
                Predicate orPred = builder.and(predicates.toArray(new Predicate[predicates.size()]));
                orPredicates.add(orPred);
            }
            return builder.or(orPredicates.toArray(new Predicate[orPredicates.size()]));

        };
    }


    private boolean containsMapsWithNullValues(Collection values) {
        for (Object obj : values) {
            if (obj instanceof Map) {
                Map map = (Map) obj;
                boolean containsNull = map.values().stream().anyMatch(v -> v == null);
                if (containsNull) {
                    return true;
                }
            }
        }
        return false;
    }




    public List<Predicate> handleMap(CriteriaBuilder builder, Root root, Join join, CriteriaQuery query, Map<String, Object> map, List<String> includeOnlyFields, boolean needsLeftJoin) {
        if (join != null){
            root = query.from(getJavaTypeOfClassContainingAttribute(root, join.getAttribute().getName(), needsLeftJoin));
        }

        List<Predicate> predicates = new ArrayList<>();
        Predicate pred;
        if (map.containsKey("q")) {
            if (map.get("q") instanceof String ||  map.get("q") instanceof Number) {
                predicates.add(searchInAllAttributesPredicate(builder, root, String.valueOf(map.get("q")), includeOnlyFields));
            }
            map.remove("q");
        }
        Set<Attribute<? super T, ?>> attributes = root.getModel().getAttributes();
        for (Map.Entry e : map.entrySet()) {
            String key = (String) e.getKey();
            Object val = e.getValue();
            String cleanKey = cleanUpKey(key);

            Attribute a = root.getModel().getAttribute(cleanKey);
            if (attributes.contains(a)) {
                pred = handleAllCases(builder, root, join, query, a, key, val, needsLeftJoin);
                predicates.add(pred);
            }
        }
        return predicates;
    }

    public Predicate handleAllCases(CriteriaBuilder builder, Root root, Join join, CriteriaQuery query, Attribute a, String key, Object val, boolean needsLeftJoin) {
        boolean isValueCollection = val instanceof Collection;
        boolean isValueMap = val instanceof Map;
        String cleanKey = cleanUpKey(key);
        boolean isKeyClean = cleanKey.equals(key);
        boolean isNegation = key.endsWith("Not");
        boolean isGt = key.endsWith("Gt");
        boolean isGte = key.endsWith("Gte");
        boolean isLt = key.endsWith("Lt");
        boolean isLte = key.endsWith("Lte");
        boolean isConjunction = key.endsWith("And");
        boolean isAssociation = a.isAssociation();

        if (val instanceof Collection && containsNull((Collection) val)) {
            needsLeftJoin = true;
        }


        if (isValueMap) {
            val = convertMapContainingPrimaryIdToValue(val, a, root, needsLeftJoin);
        }
        if (val instanceof Map && isAssociation) {
            List<Predicate> predicates =  handleMap(builder, root, addJoinIfNotExists(root,a, isValueCollection, isConjunction, needsLeftJoin), query, ((Map)val), Arrays.asList(), needsLeftJoin);
            Predicate[] predicatesArray = predicates.toArray(new Predicate[predicates.size()]);
            return  builder.and(predicatesArray);
        }



        if (isKeyClean) {
            return handleCleanKeyCase(builder, root, join, query, cleanKey, a,  val, needsLeftJoin);
        } else if (isNegation) {
            return builder.not(handleCleanKeyCase(builder, root, join, query, cleanKey, a,  val, needsLeftJoin));
        } else if (isConjunction) {
            if (isValueCollection) {
                return handleCollection(builder, root, join, query, a,  cleanKey, (Collection) val, true, needsLeftJoin);
            }
        } else if (isLte) {
            return createLtePredicate(builder, root, a, val);
        } else if (isGte) {
            return createGtePredicate(builder, root, a, val);
        } else if (isLt) {
            return createLtPredicate(builder, root, a, val);
        } else if (isGt) {
            return createGtPredicate(builder, root, a, val);
        }
        return builder.conjunction();
    }

    private boolean containsNull(Collection val) {
        for (Object ent : val) {
            if (ent == null) {
                return true;
            }
        }
        return false;
    }

    public Predicate handleCollection(CriteriaBuilder builder, Root root, Join join, CriteriaQuery query, Attribute a, String key, Collection values, boolean conjunction, boolean needsLeftJoin) {

        List<Predicate> predicates = new ArrayList<>();



        for (Object val : values) {
            Predicate pred = handleAllCases(builder, root, join, query, a, key, val, needsLeftJoin);
            predicates.add(pred);
        }
        Predicate[] predicatesArray = predicates.toArray(new Predicate[predicates.size()]);
        return (conjunction) ? builder.and(predicatesArray): builder.or(predicatesArray);
    }


    public Predicate handleCleanKeyCase(CriteriaBuilder builder, Root root, Join join, CriteriaQuery query, String key, Attribute a, Object val, boolean needsLeftJoin) {
        boolean isValueCollection = val instanceof Collection;
        boolean isValTextSearch = (val instanceof String) && ((String) val).contains("%");
        if (isValueCollection && !a.isAssociation()) {
            return handleCollection(builder, root, join, query, a, key, (Collection) val, false, needsLeftJoin);
        } else if(isValueCollection && a.isAssociation()) {
            return handleCollection(builder, root, join, query, a, key, (Collection) val, false, needsLeftJoin);
        } else if (isValTextSearch) {
            return createLikePredicate(builder, root, join, a, (String) val);
        } else if(a.isCollection() && !a.isAssociation()) {
            return createEqualityPredicate(builder, root,  addJoinIfNotExists(root, a, false, isValueCollection, needsLeftJoin), a, val, needsLeftJoin);
        } else {
            return createEqualityPredicate(builder, root, join, a, val, needsLeftJoin);
        }
    }


    //https://stackoverflow.com/a/16911313/986160
    //https://stackoverflow.com/a/47793003/986160
    public Attribute getIdAttribute(EntityManager em, Class<T> clazz) {
        Metamodel m = em.getMetamodel();
        IdentifiableType<T> of = (IdentifiableType<T>) m.managedType(clazz);
        return of.getId(of.getIdType().getJavaType());
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

    public Predicate searchInAllAttributesPredicate(CriteriaBuilder builder, Root root, String text, List<String> includeOnlyFields) {

        if (!text.contains("%")) {
            text = "%" + text + "%";
        }
        final String finalText = text;

        Set<Attribute> attributes = root.getModel().getAttributes();
        List<Predicate> orPredicates = new ArrayList<>();
        for (Attribute a : attributes) {
            boolean javaTypeIsString = a.getJavaType().getSimpleName().equalsIgnoreCase("string");
            boolean shouldSearch = includeOnlyFields.isEmpty() || includeOnlyFields.contains(a.getName());
            if ((javaTypeIsString || isUUID(a) || isValueObject(a)) && shouldSearch) {
                Predicate orPred = builder.like(root.get(a.getName()).as(String.class), finalText);
                orPredicates.add(orPred);
            }

        }

        return builder.or(orPredicates.toArray(new Predicate[orPredicates.size()]));

    }


    private Predicate createEqualityPredicate(CriteriaBuilder builder, Root root, Join join, Attribute a, Object val, boolean needsLeftJoin) {
        if (isNull(a, val)) {
            if (a.isAssociation() && a.isCollection()) {
                return builder.isEmpty(root.get(a.getName()));
            } else if(isPrimitive(a)) {
                return builder.isNull(root.get(a.getName()));
            } else if (isValueObject(a)) {
                return builder.equal(root.get(a.getName()).as(String.class), "null");
            }else {
                return root.get(a.getName()).isNull();
            }
        }
        else if (join == null) {
            if (isEnum(a)) {
                return builder.equal(root.get(a.getName()), Enum.valueOf(Class.class.cast(a.getJavaType()), (String) val));
            } else if (isPrimitive(a)) {
                return builder.equal(root.get(a.getName()), val);
            } else if (isValueObject(a)) {
                return builder.equal(root.get(a.getName()).as(String.class), val);
            } else if(isUUID(a)) {
                return builder.equal(root.get(a.getName()), UUID.fromString(val.toString()));
            } else if(a.isAssociation()) {
                if (isPrimaryKeyOfAttributeUUID(a, root, needsLeftJoin)) {
                    return prepareJoinAssociatedPredicate(builder, root, a, UUID.fromString(val.toString()), needsLeftJoin);
                }
                else {
                    return prepareJoinAssociatedPredicate(builder, root, a, val, needsLeftJoin);
                }
            }
        }
        else if (join != null) {
            if (isEnum(a)) {
                return builder.equal(join.get(a.getName()), Enum.valueOf(Class.class.cast(a.getJavaType()), (String) val));
            } else if (isPrimitive(a)) {
                return builder.equal(join.get(a.getName()), val);
            } else if (isValueObject(a)) {
                return builder.equal(join.get(a.getName()).as(String.class), val);
            } else if (a.isAssociation()) {
                Path associationPath = join.get(a.getName());
                String associationPathIdKeyName = getIdAttribute(em, associationPath.getJavaType()).getName();
                return builder.equal(associationPath.get(associationPathIdKeyName), val);
            } else if(a.isCollection()) {
                return builder.equal(join, val);
            }
        }
        throw new IllegalArgumentException("equality/inequality is currently supported on primitives, enums and value objects");
    }

    private Predicate createLikePredicate(CriteriaBuilder builder, Root<T> root, Join join, Attribute a, String val) {
        if (join == null) {
            return builder.like(root.get(a.getName()).as(String.class), val);
        }
        else {
            return builder.like(join.get(a.getName()).as(String.class), val);
        }
    }

    private Predicate createGtPredicate(CriteriaBuilder builder, Root root, Attribute a, Object val) {
        if (val instanceof String) {
            Timestamp timestamp = timeStamp((String)val);
            if (timestamp != null && !isInstant(a)) {
                return builder.greaterThan(root.get(a.getName()),  timestamp);
            } else if (timestamp != null && isInstant(a)) {
                return builder.greaterThan(root.get(a.getName()), timestamp.toInstant());
            }

            return builder.greaterThan(builder.lower(root.get(a.getName())), ((String) val).toLowerCase());
        } else if (val instanceof Integer) {
            return builder.greaterThan(root.get(a.getName()), (Integer) val);
        }
        throw new IllegalArgumentException("val type not supported yet");
    }


    private Predicate createGtePredicate(CriteriaBuilder builder, Root root, Attribute a, Object val) {
        if (val instanceof String) {
            Timestamp timestamp = timeStamp((String)val);
            if (timestamp != null && !isInstant(a)) {
                return builder.greaterThanOrEqualTo(root.get(a.getName()), timestamp);
            } else if(timestamp != null && isInstant(a)) {
                return builder.greaterThanOrEqualTo(root.get(a.getName()), timestamp.toInstant());
            }
            return builder.greaterThanOrEqualTo(builder.lower(root.get(a.getName())), ((String) val).toLowerCase());
        } else if (val instanceof Integer) {
            return builder.greaterThanOrEqualTo(root.get(a.getName()), (Integer) val);
        }
        throw new IllegalArgumentException("val type not supported yet");
    }

    private Predicate createLtPredicate(CriteriaBuilder builder, Root root, Attribute a, Object val) {
        if (val instanceof String) {
            Timestamp timestamp = timeStamp((String)val);
            if (timestamp != null && !isInstant(a)) {
                return builder.lessThan(root.get(a.getName()), timestamp);
            } else if(timestamp != null && isInstant(a)) {
                return builder.lessThan(root.get(a.getName()), timestamp.toInstant());
            }
            return builder.lessThan(builder.lower(root.get(a.getName())), ((String) val).toLowerCase());
        } else if (val instanceof Integer) {
            return builder.lessThan(root.get(a.getName()), (Integer) val);
        }
        throw new IllegalArgumentException("val type not supported yet");
    }

    private Predicate createLtePredicate(CriteriaBuilder builder, Root root, Attribute a, Object val) {
        if (val instanceof String) {
            Timestamp timestamp = timeStamp((String)val);
            if (timestamp != null && !isInstant(a)) {
                return builder.lessThanOrEqualTo(root.get(a.getName()), timestamp);
            } else if (timestamp != null && isInstant(a)) {
                return builder.lessThanOrEqualTo(root.get(a.getName()), timestamp.toInstant());
            }
            return builder.lessThanOrEqualTo(builder.lower(root.get(a.getName())), ((String) val).toLowerCase());
        } else if (val instanceof Integer) {
            return builder.lessThanOrEqualTo(root.get(a.getName()), (Integer) val);
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


    private Predicate prepareJoinAssociatedPredicate(CriteriaBuilder builder, Root root, Attribute a, Object val, boolean needsLeftJoin) {


        Path rootJoinGetName = addJoinIfNotExists(root, a, false, false, needsLeftJoin);
        Class referencedClass = rootJoinGetName.getJavaType();
        String referencedPrimaryKey = getIdAttribute(em, referencedClass).getName();
        return builder.equal(rootJoinGetName.get(referencedPrimaryKey), val);
    }


    private Join addJoinIfNotExists(Root root, Attribute a, boolean isConjunction, boolean isValueCollection, boolean needsLeftJoin) {
        if(isConjunction && isValueCollection) {
            return root.join(a.getName(), needsLeftJoin ? JoinType.LEFT : JoinType.INNER);
        }

        Set<Join> joins = root.getJoins();
        Join toReturn = null;
        for (Join join: joins) {
            if (a.getName().equals(join.getAttribute().getName())){
                toReturn =  join;
                break;
            }
        }
        if (toReturn == null) {
            toReturn = root.join(a.getName(), needsLeftJoin ? JoinType.LEFT : JoinType.INNER);
        }
        return toReturn;
    }


    private Class getJavaTypeOfClassContainingAttribute(Root root, String attributeName, boolean needsLeftJoin) {
        Attribute a = root.getModel().getAttribute(attributeName);
        if (a.isAssociation()) {
            return addJoinIfNotExists(root, a, false, false, needsLeftJoin).getJavaType();
        }
        return null;
    }

    private boolean isPrimaryKeyOfAttributeUUID(Attribute a, Root root, boolean needsLeftJoin) {
        Class javaTypeOfAttribute = getJavaTypeOfClassContainingAttribute(root, a.getName(), needsLeftJoin);
        String primaryKeyName = getIdAttribute(em, javaTypeOfAttribute).getJavaType().getSimpleName().toLowerCase();
        return primaryKeyName.equalsIgnoreCase("uuid");
    }

    private Object convertMapContainingPrimaryIdToValue(Object val, Attribute a, Root root, boolean needsLeftJoin) {
        Class javaTypeOfAttribute = getJavaTypeOfClassContainingAttribute(root, a.getName(), needsLeftJoin);
        String primaryKeyName = getIdAttribute(em, javaTypeOfAttribute).getName();
        if (val instanceof Map && ((Map) val).keySet().size() == 1) {
            Map map = ((Map) val);
            for (Object key: map.keySet()) {
                if (key.equals(primaryKeyName)) {
                    return map.get(primaryKeyName);
                }
            }
        }
        return val;
    }

    private boolean isValueObject(Attribute attribute) {
        boolean test = attribute.getJavaType().isAnnotationPresent(ValueObject.class);
        return test;
    }

    private boolean isUUID(Attribute attribute) {
        String attributeJavaClass = attribute.getJavaType().getSimpleName().toLowerCase();
        return attributeJavaClass.equalsIgnoreCase("uuid");
    }

    private boolean isInstant(Attribute attribute) {
        String attributeJavaClass = attribute.getJavaType().getSimpleName().toLowerCase();
        return attributeJavaClass.equals("instant");
    }

    private boolean isPrimitive(Attribute attribute) {
        String attributeJavaClass = attribute.getJavaType().getSimpleName().toLowerCase();
        return attributeJavaClass.startsWith("int") ||
                attributeJavaClass.startsWith("long") ||
                attributeJavaClass.equals("boolean") ||
                attributeJavaClass.equals("string") ||
                attributeJavaClass.equals("float") ||
                attributeJavaClass.equals("double");
    }

    private boolean isPrimitiveValue(Object obj) {
        String javaClass = obj.getClass().getSimpleName().toLowerCase();
        return javaClass.startsWith("int") ||
                javaClass.startsWith("long") ||
                javaClass.equals("boolean") ||
                javaClass.equals("string") ||
                javaClass.equals("float") ||
                javaClass.equals("double");
    }

    private boolean isEnum(Attribute attribute) {
        String parentJavaClass = "";
        if (attribute.getJavaType().getSuperclass() != null) {
            parentJavaClass = attribute.getJavaType().getSuperclass().getSimpleName().toLowerCase();
        }
        return parentJavaClass.equals("enum");
    }

    private boolean isNull(Attribute attribute, Object val) {
        if (isPrimitive(attribute)) {
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
}