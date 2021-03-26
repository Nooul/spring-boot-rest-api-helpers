/*
 * https://github.com/Nooul/spring-boot-rest-api-helpers
 * 
 * Released under the MIT License.
 * Please refer to LICENSE file for licensing information.
 */

package com.nooul.apihelpers.springbootrest.specifications;

import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.criteria.*;
import javax.persistence.metamodel.Attribute;
import javax.persistence.metamodel.IdentifiableType;
import javax.persistence.metamodel.Metamodel;

import com.nooul.apihelpers.springbootrest.utils.MapUtils;
import com.nooul.apihelpers.springbootrest.utils.StringUtils;

import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

@Service
@SuppressWarnings({ "rawtypes", "unchecked" })
public class CustomSpecifications<T> {

    @PersistenceContext
    private EntityManager entityManager;

    /**
     * Build predicates for filter
     * 
     * @param builder
     * @param query
     * @param root
     * @param filterMap
     * @param searchOnlyInFields
     * @param useSnakeCase
     * @return
     */
    public Predicate customSpecificationBuilder(CriteriaBuilder builder, CriteriaQuery query, Root root,
            Map<String, Object> filterMap, List<String> searchOnlyInFields, boolean useSnakeCase) {
        // set distinct
        query.distinct(true);

        // check snake case
        if (useSnakeCase) {
            filterMap = MapUtils.toCamelCase((HashMap<String, Object>) filterMap);
        }

        // build predicate
        List<Predicate> andPredicates = handleMap(builder, root, null, query, filterMap, searchOnlyInFields);

        // chain and predicate
        return builder.and(andPredicates.toArray(new Predicate[andPredicates.size()]));
    }

    /**
     * Build predicates for or filter
     * 
     * @param cb
     * @param query
     * @param root
     * @param filterMap
     * @param searchOnlyInFields
     * @param useSnakeCase
     * @return
     */
    public Predicate customSpecificationBuilder(CriteriaBuilder cb, CriteriaQuery query, Root root,
            List<Map<String, Object>> filterMap, List<String> searchOnlyInFields, boolean useSnakeCase) {
        // set distinct
        query.distinct(true);

        // iterate all elements
        List<Predicate> restrictions = new ArrayList<Predicate>();
        for (Map<String, Object> element : filterMap) {
            Map<String, Object> elementsel = element;
            // check snake case
            if (useSnakeCase) {
                elementsel = MapUtils.toCamelCase((HashMap<String, Object>) elementsel);
            }
            // build predicate
            List<Predicate> andPredicates = handleMap(cb, root, null, query, elementsel, searchOnlyInFields);
            // chain and predicate
            restrictions.add(cb.and(andPredicates.toArray(new Predicate[andPredicates.size()])));
        }

        // chain or predicate
        return cb.or(restrictions.toArray(new Predicate[restrictions.size()]));
    }

    /**
     * Handle a map
     * 
     * @param builder
     * @param root
     * @param join
     * @param query
     * @param map
     * @param includeOnlyFields
     * @return
     */
    private List<Predicate> handleMap(CriteriaBuilder builder, Root root, Join join, CriteriaQuery query,
            Map<String, Object> map, List<String> includeOnlyFields) {

        // check root by join
        if (join != null) {
            root = query.from(getJavaTypeOfClassContainingAttribute(root, join.getAttribute().getName()));
        }

        List<Predicate> predicates = new ArrayList<>();
        Predicate predicate;

        // check "q"
        if (map.containsKey("q") && map.get("q") instanceof String) {
            predicates
                    .add(createSearchInAllAttributesPredicate(builder, root, (String) map.get("q"), includeOnlyFields));
            map.remove("q");
        }

        // perform predicate on all keys
        Set<Attribute<? super T, ?>> attributes = root.getModel().getAttributes();
        for (Map.Entry entry : map.entrySet()) {
            String key = (String) entry.getKey();
            Object val = entry.getValue();
            String keyClean = cleanUpKey(key);

            Attribute attribute = root.getModel().getAttribute(keyClean);
            if (attributes.contains(attribute)) {
                predicate = handleAllCases(builder, root, join, query, attribute, key, val);
                predicates.add(predicate);
            }
        }

        return predicates;
    }

    /**
     * Handle all the cases
     * 
     * @param builder
     * @param root
     * @param join
     * @param query
     * @param attribute
     * @param key
     * @param val
     * @return
     */
    private Predicate handleAllCases(CriteriaBuilder builder, Root root, Join join, CriteriaQuery query,
            Attribute attribute, String key, Object val) {
        // check value types
        boolean valIsCollection = val instanceof Collection;
        String cleanKey = cleanUpKey(key);
        boolean isKeyClean = cleanKey.equals(key);
        boolean isNegation = key.endsWith("Not");
        boolean isGt = key.endsWith("Gt");
        boolean isGte = key.endsWith("Gte");
        boolean isLt = key.endsWith("Lt");
        boolean isLte = key.endsWith("Lte");
        boolean isConjunction = key.endsWith("And");
        boolean isAssociation = attribute.isAssociation();

        // rebuild value if is a map
        if (val instanceof Map) {
            val = convertMapContainingPrimaryIdToValue(val, attribute, root);
        }

        // check by type of key
        if (isAssociation && val instanceof Map) {
            List<Predicate> predicates = handleMap(builder, root,
                    addJoinIfNotExists(root, attribute, valIsCollection, isConjunction), query, ((Map) val),
                    Arrays.asList());
            return builder.and(predicates.toArray(new Predicate[predicates.size()]));
        } else if (isKeyClean) {
            return handleKeyClean(builder, root, join, query, cleanKey, attribute, val);
        } else if (isNegation) {
            return builder.not(handleKeyClean(builder, root, join, query, cleanKey, attribute, val));
        } else if (isConjunction && valIsCollection) {
            return handleCollection(builder, root, join, query, attribute, cleanKey, (Collection) val, true);
        } else if (isLte) {
            return createLtePredicate(builder, root, attribute, val);
        } else if (isGte) {
            return createGtePredicate(builder, root, attribute, val);
        } else if (isLt) {
            return createLtPredicate(builder, root, attribute, val);
        } else if (isGt) {
            return createGtPredicate(builder, root, attribute, val);
        }

        return builder.conjunction();
    }

    /**
     * Handle Collection
     * 
     * @param builder
     * @param root
     * @param join
     * @param query
     * @param attribute
     * @param key
     * @param values
     * @param conjunction
     * @return
     */
    private Predicate handleCollection(CriteriaBuilder builder, Root root, Join join, CriteriaQuery query,
            Attribute attribute, String key, Collection values, boolean conjunction) {
        List<Predicate> predicates = new ArrayList<>();

        // iterate all values
        for (Object val : values) {
            Predicate predicate = handleAllCases(builder, root, join, query, attribute, key, val);
            predicates.add(predicate);
        }

        if (conjunction)
            return builder.and(predicates.toArray(new Predicate[predicates.size()]));
        else
            return builder.or(predicates.toArray(new Predicate[predicates.size()]));
    }

    /**
     * Handle key clean
     * 
     * @param builder
     * @param root
     * @param join
     * @param query
     * @param key
     * @param attribute
     * @param val
     * @return
     */
    private Predicate handleKeyClean(CriteriaBuilder builder, Root root, Join join, CriteriaQuery query, String key,
            Attribute attribute, Object val) {

        // check value type
        boolean isValueCollection = val instanceof Collection;
        boolean isValTextSearch = (val instanceof String) && ((String) val).contains("%");

        // check by type of key
        if (isValueCollection) {
            return handleCollection(builder, root, join, query, attribute, key, (Collection) val, false);
        } else if (isValTextSearch) {
            return createLikePredicate(builder, root, join, attribute, (String) val);
        } else if (attribute.isCollection() && !attribute.isAssociation()) {
            return createEqualityPredicate(builder, root, addJoinIfNotExists(root, attribute, false, isValueCollection),
                    attribute, val);
        } else {
            return createEqualityPredicate(builder, root, join, attribute, val);
        }
    }

    /**
     * Create equality predicate
     * 
     * @param builder
     * @param root
     * @param join
     * @param attribute
     * @param val
     * @return
     */
    private Predicate createEqualityPredicate(CriteriaBuilder builder, Root root, Join join, Attribute attribute,
            Object val) {
        // check by type of key
        if (isAttributeNull(attribute, val)) {
            if (attribute.isAssociation() && attribute.isCollection()) {
                return builder.isEmpty(root.get(attribute.getName()));
            } else if (isPrimitive(attribute)) {
                return builder.isNull(root.get(attribute.getName()));
            } else {
                return root.get(attribute.getName()).isNull();
            }
        } else if (join == null) {
            if (isAttributeEnum(attribute)) {
                return builder.equal(root.get(attribute.getName()),
                        Enum.valueOf(Class.class.cast(attribute.getJavaType()), (String) val));
            } else if (isPrimitive(attribute)) {
                return builder.equal(root.get(attribute.getName()), val);
            } else if (isAttributeUUID(attribute)) {
                return builder.equal(root.get(attribute.getName()), UUID.fromString(val.toString()));
            } else if (attribute.isAssociation()) {
                if (isPrimaryKeyUUID(attribute, root)) {
                    return prepareJoinAssociatedPredicate(builder, root, attribute, UUID.fromString(val.toString()));
                } else {
                    return prepareJoinAssociatedPredicate(builder, root, attribute, val);
                }
            }
        } else if (join != null) {
            if (isAttributeEnum(attribute)) {
                return builder.equal(join.get(attribute.getName()),
                        Enum.valueOf(Class.class.cast(attribute.getJavaType()), (String) val));
            } else if (isPrimitive(attribute)) {
                return builder.equal(join.get(attribute.getName()), val);
            } else if (attribute.isAssociation()) {
                return builder.equal(join.get(attribute.getName()), val);
            } else if (attribute.isCollection()) {
                return builder.equal(join, val);
            }
        }
        throw new IllegalArgumentException("equality/inequality is currently supported on primitives and enums");
    }

    /**
     * Create Like predicate
     * 
     * @param builder
     * @param root
     * @param join
     * @param attribute
     * @param val
     * @return
     */
    private Predicate createLikePredicate(CriteriaBuilder builder, Root root, Join join, Attribute attribute,
            String val) {
        // like over join or root
        if (join == null) {
            return builder.like(root.get(attribute.getName()), val);
        } else {
            return builder.like(join.get(attribute.getName()), val);
        }
    }

    /**
     * Create Gte predicate
     * 
     * @param builder
     * @param root
     * @param attribute
     * @param val
     * @return
     */
    private Predicate createGtPredicate(CriteriaBuilder builder, Root root, Attribute attribute, Object val) {
        if (val instanceof String) {
            // get timestamp
            Timestamp timestamp = convertTimestamp((String) val);
            if (timestamp != null) {
                return builder.greaterThan(builder.lower(root.get(attribute.getName())), timestamp);
            }
            return builder.greaterThan(builder.lower(root.get(attribute.getName())), ((String) val).toLowerCase());
        } else if (val instanceof Integer) {
            return builder.greaterThan(root.get(attribute.getName()), (Integer) val);
        }
        throw new IllegalArgumentException("val type not supported yet");
    }

    /**
     * Create Gt predicate
     * 
     * @param builder
     * @param root
     * @param a
     * @param val
     * @return
     */
    private Predicate createGtePredicate(CriteriaBuilder builder, Root root, Attribute a, Object val) {
        if (val instanceof String) {
            // get timestamp
            Timestamp timestamp = convertTimestamp((String) val);
            if (timestamp != null) {
                return builder.greaterThanOrEqualTo(builder.lower(root.get(a.getName())), timestamp);
            }
            return builder.greaterThanOrEqualTo(builder.lower(root.get(a.getName())), ((String) val).toLowerCase());
        } else if (val instanceof Integer) {
            return builder.greaterThanOrEqualTo(root.get(a.getName()), (Integer) val);
        }
        throw new IllegalArgumentException("val type not supported yet");
    }

    /**
     * Create Lt predicate
     * 
     * @param builder
     * @param root
     * @param attribute
     * @param val
     * @return
     */
    private Predicate createLtPredicate(CriteriaBuilder builder, Root root, Attribute attribute, Object val) {
        if (val instanceof String) {
            // get timestamp
            Timestamp timestamp = convertTimestamp((String) val);
            if (timestamp != null) {
                return builder.lessThan(builder.lower(root.get(attribute.getName())), timestamp);
            }
            return builder.lessThan(builder.lower(root.get(attribute.getName())), ((String) val).toLowerCase());
        } else if (val instanceof Integer) {
            return builder.lessThan(root.get(attribute.getName()), (Integer) val);
        }
        throw new IllegalArgumentException("val type not supported yet");
    }

    /**
     * Create Lte predicate
     * 
     * @param builder
     * @param root
     * @param attribute
     * @param val
     * @return
     */
    private Predicate createLtePredicate(CriteriaBuilder builder, Root root, Attribute attribute, Object val) {
        if (val instanceof String) {
            // get timestamp
            Timestamp timestamp = convertTimestamp((String) val);
            if (timestamp != null) {
                return builder.lessThanOrEqualTo(builder.lower(root.get(attribute.getName())), timestamp);
            }
            return builder.lessThanOrEqualTo(builder.lower(root.get(attribute.getName())),
                    ((String) val).toLowerCase());
        } else if (val instanceof Integer) {
            return builder.lessThanOrEqualTo(root.get(attribute.getName()), (Integer) val);
        }
        throw new IllegalArgumentException("val type not supported yet");
    }

    /**
     * Search in all attributes
     * 
     * @param builder
     * @param root
     * @param text
     * @param includeOnlyFields
     * @return
     */
    private Predicate createSearchInAllAttributesPredicate(CriteriaBuilder builder, Root root, String text,
            List<String> includeOnlyFields) {

        // add like search
        if (!text.contains("%")) {
            text = "%" + text + "%";
        }

        // build the text search
        final String finalText = text;
        Set<Attribute> attributes = root.getModel().getAttributes();
        List<Predicate> predicates = new ArrayList<>();
        for (Attribute attribute : attributes) {
            boolean javaTypeIsString = attribute.getJavaType().getSimpleName().equalsIgnoreCase("string");
            boolean shouldSearch = includeOnlyFields.isEmpty() || includeOnlyFields.contains(attribute.getName());
            if (javaTypeIsString && shouldSearch) {
                Predicate predicate = builder.like(root.get(attribute.getName()), finalText);
                predicates.add(predicate);
            }
        }

        // return or on all predicates
        return builder.or(predicates.toArray(new Predicate[predicates.size()]));

    }

    /**
     * Get timestamp from string
     * 
     * @param s
     * @return
     */
    private static Timestamp convertTimestamp(String s) {
        // get the format
        DateFormat dateFormat;
        if (s.contains("T")) {
            dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
        } else {
            dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        }

        // part the date
        Date date;
        try {
            date = dateFormat.parse(s);
        } catch (ParseException e) {
            return null;
        }

        // return timestamp
        return new Timestamp(date.getTime());
    }

    /**
     * Retrieve primary key column definition of a generic entity in JPA
     * 
     * @param e
     * @param c
     * @return
     */
    private Attribute getIdAttribute(EntityManager e, Class<T> c) {
        Metamodel m = e.getMetamodel();
        IdentifiableType<T> of = (IdentifiableType<T>) m.managedType(c);
        return of.getId(of.getIdType().getJavaType());
    }

    /**
     * Clean up a key
     * 
     * @param key
     * @return
     */
    private String cleanUpKey(String key) {
        List<String> postfixes = Arrays.asList("Gte", "Gt", "Lte", "Lt", "Not", "And");
        for (String postfix : postfixes) {
            if (key.endsWith(postfix)) {
                return key.substring(0, key.length() - postfix.length());
            }
        }
        return key;
    }

    /**
     * Prepare join if attribut is Association
     * 
     * @param builder
     * @param root
     * @param attribute
     * @param val
     * @return
     */
    private Predicate prepareJoinAssociatedPredicate(CriteriaBuilder builder, Root root, Attribute attribute,
            Object val) {
        // add a join to path
        Path rootJoinGetName = addJoinIfNotExists(root, attribute, false, false);
        // get referenced calls
        Class referencedClass = rootJoinGetName.getJavaType();
        // get primary key
        String referencedPrimaryKey = getIdAttribute(entityManager, referencedClass).getName();
        // return join equal
        return builder.equal(rootJoinGetName.get(referencedPrimaryKey), val);
    }

    /**
     * Add a join
     * 
     * @param root
     * @param attribute
     * @param isConjunction
     * @param isValueCollection
     * @return
     */
    private Join addJoinIfNotExists(Root root, Attribute attribute, boolean isConjunction, boolean isValueCollection) {
        // simple join
        if (isConjunction && isValueCollection) {
            return root.join(attribute.getName());
        }
        // get all joins
        Join ret = null;
        Set<Join> joins = root.getJoins();
        for (Join join : joins) {
            // set a join
            if (attribute.getName().equals(join.getAttribute().getName())) {
                ret = join;
                break;
            }
        }
        if (ret == null) {
            ret = root.join(attribute.getName());
        }
        return ret;
    }

    /**
     * Get the JavaType of an attribute
     * 
     * @param root
     * @param attributeName
     * @return
     */
    private Class getJavaTypeOfClassContainingAttribute(Root root, String attributeName) {
        Attribute attribute = root.getModel().getAttribute(attributeName);
        if (attribute.isAssociation()) {
            return addJoinIfNotExists(root, attribute, false, false).getJavaType();
        }
        return null;
    }

    /**
     * Convert a map that contains primary id
     * 
     * @param val
     * @param a
     * @param root
     * @return
     */
    private Object convertMapContainingPrimaryIdToValue(Object val, Attribute a, Root root) {
        Class javaTypeOfAttribute = getJavaTypeOfClassContainingAttribute(root, a.getName());
        String primaryKeyName = getIdAttribute(entityManager, javaTypeOfAttribute).getName();
        if (val instanceof Map && ((Map) val).keySet().size() == 1) {
            Map map = ((Map) val);
            for (Object key : map.keySet()) {
                if (key.equals(primaryKeyName)) {
                    return map.get(primaryKeyName);
                }
            }
        }
        return val;
    }

    /**
     * Check if the primary key is of type UUID
     * 
     * @param a
     * @param root
     * @return
     */
    private boolean isPrimaryKeyUUID(Attribute a, Root root) {
        Class javaTypeOfAttribute = getJavaTypeOfClassContainingAttribute(root, a.getName());
        String primaryKeyName = getIdAttribute(entityManager, javaTypeOfAttribute).getJavaType().getSimpleName()
                .toLowerCase();
        return primaryKeyName.equalsIgnoreCase("uuid");
    }

    /**
     * Check if an attribute is a primitive variable
     * 
     * @param attribute
     * @return
     */
    private boolean isPrimitive(Attribute attribute) {
        String attributeClassName = attribute.getJavaType().getSimpleName().toLowerCase();
        return attributeClassName.startsWith("int") || attributeClassName.startsWith("long")
                || attributeClassName.equals("boolean") || attributeClassName.equals("string")
                || attributeClassName.equals("float") || attributeClassName.equals("double");
    }

    /**
     * Check if an attribute is a UUID
     * 
     * @param attribute
     * @return
     */
    private boolean isAttributeUUID(Attribute attribute) {
        String attributeClassName = attribute.getJavaType().getSimpleName().toLowerCase();
        return attributeClassName.equalsIgnoreCase("uuid");
    }

    /**
     * Check if an attribute is an enum
     * 
     * @param attribute
     * @return
     */
    private boolean isAttributeEnum(Attribute attribute) {
        boolean ret = false;
        if (attribute.getJavaType().getSuperclass() != null) {
            String attributeClassName = attribute.getJavaType().getSimpleName().toLowerCase();
            ret = attributeClassName.equals("enum");
        }
        return ret;
    }

    /**
     * Check if an attribute is null
     * 
     * @param attribute
     * @param val
     * @return
     */
    private boolean isAttributeNull(Attribute attribute, Object val) {
        if (isPrimitive(attribute)) {
            String attributeClassName = attribute.getJavaType().getSimpleName().toLowerCase();
            if (attributeClassName.equals("string")) {
                if (val == null)
                    return true;
                else {
                    return StringUtils.isBlank(val.toString()) || val.toString().equalsIgnoreCase("null");
                }
            } else {
                return val == null;
            }
        } else {
            return val == null;
        }

    }
}