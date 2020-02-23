package springboot.rest.specifications;


import org.apache.commons.lang3.StringUtils;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.criteria.*;
import javax.persistence.metamodel.Attribute;
import javax.persistence.metamodel.IdentifiableType;
import javax.persistence.metamodel.Metamodel;
import java.util.*;

//from: https://github.com/zifnab87/spring-boot-rest-api-helpers/blob/master/src/main/java/springboot/rest/specifications/CustomSpecifications.java
@Service
public class CustomSpecifications<T> {

    @PersistenceContext
    private EntityManager em;

    public Specification<T> customSpecificationBuilder(Map<String, Object> map) {

        return (Specification<T>) (root, query, builder) -> {

            query.distinct(true);
            List<Predicate> predicates = handleMap(builder, root, null, query, map, new ArrayList<>());
            return builder.and(predicates.toArray(new Predicate[predicates.size()]));
        };
    }

    public Specification<T> customSpecificationBuilder(Map<String, Object> map, List<String> includeOnlyFields) {

        return (Specification<T>) (root, query, builder) -> {

            query.distinct(true);
            List<Predicate> predicates = handleMap(builder, root, null, query, map, includeOnlyFields);
            return builder.and(predicates.toArray(new Predicate[predicates.size()]));
        };
    }



    public Specification<T> customSpecificationBuilder(List<Map<String, Object>> list) {

        return (Specification<T>) (root, query, builder) -> {

            query.distinct(true);
            List<Predicate> orPredicates = new ArrayList<>();
            for (Map<String, Object> map: list) {
                List<Predicate> predicates = handleMap(builder, root, null, query, map, new ArrayList<>());
                Predicate orPred =  builder.and(predicates.toArray(new Predicate[predicates.size()]));
                orPredicates.add(orPred);
            }
            return builder.or(orPredicates.toArray(new Predicate[orPredicates.size()]));
        };
    }

    public List<Predicate> handleMap(CriteriaBuilder builder, Root root, Join join, CriteriaQuery query, Map<String, Object> map, List<String> includeOnlyFields) {
        if (join != null){
            root = query.from(getJavaTypeOfClassContainingAttribute(root, join.getAttribute().getName()));
        }

        List<Predicate> predicates = new ArrayList<>();
        Predicate pred;
        if (map.containsKey("q") && map.get("q") instanceof String) {


            predicates.add(searchInAllAttributesPredicate(builder, root, (String) map.get("q"), includeOnlyFields));
            map.remove("q");
        }
        Set<Attribute<? super T, ?>> attributes = root.getModel().getAttributes();
        for (Map.Entry e : map.entrySet()) {
            String key = (String) e.getKey();
            Object val = e.getValue();
            String cleanKey = cleanUpKey(key);

            Attribute a = root.getModel().getAttribute(cleanKey);
            if (attributes.contains(a)) {
                pred = handleAllCases(builder, root, join, query, a, key, val);
                predicates.add(pred);
            }
        }
        return predicates;
    }

    public Predicate handleAllCases(CriteriaBuilder builder, Root root, Join join, CriteriaQuery query, Attribute a, String key, Object val) {
        //boolean isPrimitive = isPrimitive(a);
        boolean isValueCollection = val instanceof Collection;
        boolean isValueMap = val instanceof Map;
        String cleanKey = cleanUpKey(key);
        boolean isKeyClean = cleanKey.equals(key);
        //boolean isValTextSearch = (val instanceof String) && ((String) val).contains("%");
        boolean isNegation = key.endsWith("Not");
        boolean isGt = key.endsWith("Gt");
        boolean isGte = key.endsWith("Gte");
        boolean isLt = key.endsWith("Lt");
        boolean isLte = key.endsWith("Lte");
        boolean isConjunction = key.endsWith("And");
        boolean isAssociation = a.isAssociation();

        if (isValueMap) {
            val = convertMapContainingPrimaryIdToValue(val, a, root);
        }
        if (val instanceof Map && isAssociation) {
            List<Predicate> predicates =  handleMap(builder, root, root.join(a.getName()), query, ((Map)val), Arrays.asList());
            Predicate[] predicatesArray = predicates.toArray(new Predicate[predicates.size()]);
            return  builder.and(predicatesArray);
        }



        if (isKeyClean) {
            return handleCleanKeyCase(builder, root, join, query, cleanKey, a,  val);
        } else if (isNegation) {
            return builder.not(handleCleanKeyCase(builder, root, join, query, cleanKey, a,  val));
        } else if (isConjunction) {
            if (isValueCollection) {
                return handleCollection(builder, root, join, query, a,  cleanKey, (Collection) val, true);
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

    public Predicate handleCollection(CriteriaBuilder builder, Root root, Join join, CriteriaQuery query, Attribute a, String key, Collection values, boolean conjunction) {
        List<Predicate> predicates = new ArrayList<>();
        for (Object val : values) {
            Predicate pred  = handleAllCases(builder, root, join, query, a, key, val);
            predicates.add(pred);
        }
        Predicate[] predicatesArray = predicates.toArray(new Predicate[predicates.size()]);
        return (conjunction) ? builder.and(predicatesArray): builder.or(predicatesArray);
    }

    public Predicate handleCleanKeyCase(CriteriaBuilder builder, Root root, Join join, CriteriaQuery query, String key, Attribute a, Object val) {
        boolean isValueCollection = val instanceof Collection;
        boolean isValTextSearch = (val instanceof String) && ((String) val).contains("%");
        if (isValueCollection) {
            return handleCollection(builder, root, join, query, a, key, (Collection) val, false);
        } else if (isValTextSearch) {
            return createLikePredicate(builder, root, join, a, (String) val);
        } else if(a.isCollection() && !a.isAssociation()) {
            return createEqualityPredicate(builder, root,  root.join(a.getName()), a, val);
        } else {
            return createEqualityPredicate(builder, root, join, a, val);
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
            if (javaTypeIsString && shouldSearch) {
                Predicate orPred = builder.like(root.get(a.getName()), finalText);
                orPredicates.add(orPred);
            }

        }

        return builder.or(orPredicates.toArray(new Predicate[orPredicates.size()]));

    }

    private Predicate createEqualityPredicate(CriteriaBuilder builder, Root root, Join join, Attribute a, Object val) {
        if (isNull(a, val)) {
            if (a.isAssociation() && a.isCollection()) {
                return builder.isEmpty(root.get(a.getName()));
            }
            else if(isPrimitive(a)) {
                return builder.isNull(root.get(a.getName()));
            }
            else {
                return root.get(a.getName()).isNull();
            }
        }
        else if (join == null) {
            if (isEnum(a)) {
                return builder.equal(root.get(a.getName()), Enum.valueOf(Class.class.cast(a.getJavaType()), (String) val));
            } else if (isPrimitive(a)) {
                return builder.equal(root.get(a.getName()), val);
            } else if(isUUID(a)) {
                return builder.equal(root.get(a.getName()), UUID.fromString(val.toString()));
            } else if(a.isAssociation()) {
                if (isPrimaryKeyUUID(a, root)) {
                    return prepareJoinAssociatedPredicate(root, a, UUID.fromString(val.toString()));
                }
                else {
                    return prepareJoinAssociatedPredicate(root, a, val);
                }
            }
        }
        else if (join != null) {
            if (isEnum(a)) {
                return builder.equal(join.get(a.getName()), Enum.valueOf(Class.class.cast(a.getJavaType()), (String) val));
            } else if (isPrimitive(a)) {
                return builder.equal(join.get(a.getName()), val);
            } else if (a.isAssociation()) {
                return builder.equal(join.get(a.getName()), val);
            }
            else if(a.isCollection()) {
                return builder.equal(join, val);
            }
        }
        throw new IllegalArgumentException("equality/inequality is currently supported on primitives and enums");
    }

    private Predicate createLikePredicate(CriteriaBuilder builder, Root<T> root, Join join, Attribute a, String val) {
        if (join == null) {
            return builder.like(root.get(a.getName()), val);
        }
        else {
            return builder.like(join.get(a.getName()), val);
        }
    }

    private Predicate createGtPredicate(CriteriaBuilder builder, Root root, Attribute a, Object val) {
        if (val instanceof String) {
            return builder.greaterThan(builder.lower(root.get(a.getName())), ((String) val).toLowerCase());
        } else if (val instanceof Integer) {
            return builder.greaterThan(root.get(a.getName()), (Integer) val);
        }
        throw new IllegalArgumentException("val type not supported yet");
    }

    private Predicate createGtePredicate(CriteriaBuilder builder, Root root, Attribute a, Object val) {
        if (val instanceof String) {
            return builder.greaterThanOrEqualTo(builder.lower(root.get(a.getName())), ((String) val).toLowerCase());
        } else if (val instanceof Integer) {
            return builder.greaterThanOrEqualTo(root.get(a.getName()), (Integer) val);
        }
        throw new IllegalArgumentException("val type not supported yet");
    }

    private Predicate createLtPredicate(CriteriaBuilder builder, Root root, Attribute a, Object val) {
        if (val instanceof String) {
            return builder.lessThan(builder.lower(root.get(a.getName())), ((String) val).toLowerCase());
        } else if (val instanceof Integer) {
            return builder.lessThan(root.get(a.getName()), (Integer) val);
        }
        throw new IllegalArgumentException("val type not supported yet");
    }

    private Predicate createLtePredicate(CriteriaBuilder builder, Root root, Attribute a, Object val) {
        if (val instanceof String) {
            return builder.lessThanOrEqualTo(builder.lower(root.get(a.getName())), ((String) val).toLowerCase());
        } else if (val instanceof Integer) {
            return builder.lessThanOrEqualTo(root.get(a.getName()), (Integer) val);
        }
        throw new IllegalArgumentException("val type not supported yet");
    }


    private Predicate prepareJoinAssociatedPredicate(Root root, Attribute a, Object val) {
        Path rootJoinGetName = root.join(a.getName());
        Class referencedClass = rootJoinGetName.getJavaType();
        String referencedPrimaryKey = getIdAttribute(em, referencedClass).getName();
        return rootJoinGetName.get(referencedPrimaryKey).in(val);
    }

    private Class getJavaTypeOfClassContainingAttribute(Root root, String attributeName) {
        Attribute a = root.getModel().getAttribute(attributeName);
        if (a.isAssociation()) {
            //root.getModel().getAttribute("actors").getName()
            return root.join(a.getName()).getJavaType();
        }
        return null;
    }

    private boolean isPrimaryKeyUUID(Attribute a, Root root) {
        Class javaTypeOfAttribute = getJavaTypeOfClassContainingAttribute(root, a.getName());
        String primaryKeyName = getIdAttribute(em, javaTypeOfAttribute).getJavaType().getSimpleName().toLowerCase();
        if (primaryKeyName.equalsIgnoreCase("uuid")) {
            return true;
        }
        else {
            return false;
        }
    }

    private Object convertMapContainingPrimaryIdToValue(Object val, Attribute a, Root root) {
        Class javaTypeOfAttribute = getJavaTypeOfClassContainingAttribute(root, a.getName());
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

    private boolean isUUID(Attribute attribute) {
        String attributeJavaClass = attribute.getJavaType().getSimpleName().toLowerCase();
        return attributeJavaClass.equalsIgnoreCase("uuid");
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

    private boolean isValuePrimitive(Object value) {
        String attributeJavaClass = value.getClass().getSimpleName().toLowerCase();
        return attributeJavaClass.startsWith("int") ||
                attributeJavaClass.startsWith("long") ||
                attributeJavaClass.equals("boolean") ||
                attributeJavaClass.equals("string") ||
                attributeJavaClass.equals("float") ||
                attributeJavaClass.equals("double");
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