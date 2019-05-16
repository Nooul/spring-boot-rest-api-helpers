package springboot.rest.specifications;


import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.persistence.metamodel.*;
import java.util.*;

//from: https://github.com/zifnab87/spring-boot-rest-api-helpers/blob/master/src/main/java/springboot/rest/specifications/CustomSpecifications.java
@Service
public class CustomSpecifications<T> {

    @PersistenceContext
    private EntityManager em;

    public Specification<T> customSpecificationBuilder(Map<String, Object> map, List<String> includeOnlyFields) {

        return (Specification<T>) (root, query, builder) -> {

            final List<Predicate> predicates = new ArrayList<>();
            Predicate pred;
            if (map.containsKey("q") && map.get("q") instanceof String) {
                predicates.add(searchInAllAttributesPredicate(builder, root, (String) map.get("q"), includeOnlyFields));
                map.remove("q");
            }

            Set<Attribute<? super T, ?>> attributes = root.getModel().getAttributes();

            Class<T> rootFullClass = root.getModel().getJavaType();
            String primaryKeyName = getIdAttribute(em, rootFullClass);
            for (Map.Entry e : map.entrySet()) {
                String key = (String) e.getKey();
                Object val = extractId(e.getValue(), primaryKeyName);
                String cleanKey = cleanUpKey(key);
                Attribute a = root.getModel().getAttribute(cleanKey);

                if (attributes.contains(a)) {
                    pred = handleAllCases(builder, root, a, key, val);
                    predicates.add(pred);
                }
            }
            return builder.and(predicates.toArray(new Predicate[predicates.size()]));
        };
    }

    public Predicate handleAllCases(CriteriaBuilder builder, Root root, Attribute a, String key, Object val) {
        //boolean isPrimitive = isPrimitive(a);
        boolean isValueCollection = val instanceof Collection;
        String cleanKey = cleanUpKey(key);
        boolean isKeyClean = cleanKey.equals(key);
        //boolean isValTextSearch = (val instanceof String) && ((String) val).contains("%");
        boolean isNegation = key.endsWith("Not");
        boolean isGt = key.endsWith("Gt");
        boolean isGte = key.endsWith("Gte");
        boolean isLt = key.endsWith("Lt");
        boolean isLte = key.endsWith("Lte");
        boolean isConjunction = key.endsWith("And");
        //boolean isAssociation = a.isAssociation();

        if (isKeyClean) {
            return hanldleCleanKeyCase(builder, root, a, val);
        } else if (isNegation) {
            return builder.not(hanldleCleanKeyCase(builder, root, a, val));
        } else if (isConjunction) {
            if (isValueCollection) {
                return createMultiValueEqualityPredicate(builder, root, a, (Collection) val, true);
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

    public Predicate hanldleCleanKeyCase(CriteriaBuilder builder, Root root, Attribute a, Object val) {
        boolean isValueCollection = val instanceof Collection;
        boolean isValTextSearch = (val instanceof String) && ((String) val).contains("%");
        if (isValueCollection) {
            return createMultiValueEqualityPredicate(builder, root, a, (Collection) val, false);
        } else if (isValTextSearch) {
            return createLikePredicate(builder, root, a, (String) val);
        } else {
            return createEqualityPredicate(builder, root, a, val);
        }
    }


    //https://stackoverflow.com/a/16911313/986160
    //https://stackoverflow.com/a/47793003/986160
    public String getIdAttribute(EntityManager em, Class<T> clazz) {
        Metamodel m = em.getMetamodel();
        IdentifiableType<T> of = (IdentifiableType<T>) m.managedType(clazz);
        return of.getId(of.getIdType().getJavaType()).getName();
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

    private Predicate createEqualityPredicate(CriteriaBuilder builder, Root root, Attribute a, Object val) {
        if (val == null) {
            if (a.isAssociation() && a.isCollection()) {
                return builder.isEmpty(root.get(a.getName()));
            } else {
                return root.get(a.getName()).isNull();
            }
        } else if (isEnum(a)) {
            return builder.equal(root.get(a.getName()), Enum.valueOf(Class.class.cast(a.getJavaType()), (String) val));
        } else if (isPrimitive(a)) {
            return builder.equal(root.get(a.getName()), val);
        } else if (a.isAssociation()) {
            return prepareJoinAssociatedPredicate(root, a, val);
        }
        throw new IllegalArgumentException("equality/inequality is currently supported on primitives and enums");
    }

    private Predicate createLikePredicate(CriteriaBuilder builder, Root<T> root, Attribute a, String val) {
        return builder.like(root.get(a.getName()), val);
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

    private Predicate createMultiValueEqualityPredicate(CriteriaBuilder builder, Root root, Attribute a, Collection values, boolean conjunction) {
        List<Predicate> predicates = new ArrayList<>();
        for (Object val : values) {
            Predicate pred;
            if (a.isAssociation()) {
                pred = prepareJoinAssociatedPredicate(root, a, val);
            } else {
                pred = builder.equal(root.get(a.getName()), val);
            }
            predicates.add(pred);
        }
        Predicate[] predicatesArray = predicates.toArray(new Predicate[predicates.size()]);
        return (conjunction) ? builder.and(predicatesArray): builder.or(predicatesArray);
    }


    private Predicate prepareJoinAssociatedPredicate(Root root, Attribute a, Object val) {
        Path rootJoinGetName = root.join(a.getName());
        Class referencedClass = rootJoinGetName.getJavaType();
        String referencedPrimaryKey = getIdAttribute(em, referencedClass);
        return rootJoinGetName.get(referencedPrimaryKey).in(val);
    }


    private Object extractId(Object val, String primaryKeyName) {
        if (val instanceof Map) {
            val = ((Map) val).get(primaryKeyName);
        } else if (val instanceof ArrayList && !((ArrayList) val).isEmpty() && ((ArrayList) val).get(0) instanceof Map) {
            val = ((Map) ((ArrayList) val).get(0)).get(primaryKeyName);
        }

        return val;
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

    private boolean isEnum(Attribute attribute) {
        String parentJavaClass = "";
        if (attribute.getJavaType().getSuperclass() != null) {
            parentJavaClass = attribute.getJavaType().getSuperclass().getSimpleName().toLowerCase();
        }
        return parentJavaClass.equals("enum");
    }

}