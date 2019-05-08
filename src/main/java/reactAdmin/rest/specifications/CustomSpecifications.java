package reactAdmin.rest.specifications;


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

//from: https://github.com/zifnab87/react-admin-java-rest/blob/master/src/main/java/reactAdmin/rest/specifications/ReactAdminSpecifications.java
@Service
public class CustomSpecifications<T> {

    @PersistenceContext
    EntityManager em;

    public Specification<T> customSpecificationBuilder(Map<String, Object> map, List<String> includeOnlyFields, String primaryKeyName) {

        return (Specification<T>) (root, query, builder) -> {

            final List<Predicate> predicates = new ArrayList<>();
            Predicate pred = builder.conjunction();
            if (map.containsKey("q") && map.get("q") instanceof String) {
                predicates.add(searchInAllAttributesPredicate(builder, root, (String) map.get("q"), includeOnlyFields));
                map.remove("q");
            }

            Set<Attribute<? super T, ?>> attributes = root.getModel().getAttributes();

            System.out.println(primaryKeyName);
            for (Map.Entry e : map.entrySet()) {
                String key = (String) e.getKey();
                Object val = extractId(e.getValue(), primaryKeyName);
                String cleanKey = cleanUpKey(key);
                Attribute a = root.getModel().getAttribute(cleanKey);

                if (attributes.contains(a)) {

                    boolean isValueCollection = val instanceof Collection;
                    boolean isKeyClean = cleanKey.equals(key);
                    boolean isValTextSearch = (val instanceof String) && ((String)val).contains("%");
                    boolean isNegation = key.endsWith("Not");
                    boolean isGt = key.endsWith("Gt");
                    boolean isGte = key.endsWith("Gte");
                    boolean isLt = key.endsWith("Lt");
                    boolean isLte = key.endsWith("Lte");
                    boolean isConjunction = key.endsWith("And");


                    if (isKeyClean) {
                        if(isValueCollection) {
                            pred = createDisjunctiveEqualityPredicate(builder, root, a, (Collection) val);
                        }
                        else if (isValTextSearch) {
                            pred = createLikePredicate(builder, root, a, (String) val);
                        }
                        else {
                            pred = createEqualityPredicate(builder, root, a, val);
                        }
                    }
                    else if (isNegation) {
                        if(isValueCollection) {
                            pred = createConjunctiveInequalityPredicate(builder, root, a, (Collection)val);
                        }
                        else if(isValTextSearch) {
                            pred = createNotLikePredicate(builder, root, a, (String) val);
                        }
                        else {
                            pred = createNegationPredicate(builder, root, a, val);
                        }
                    }
                    else if (isConjunction) {
                        if(isValueCollection) {
                            pred = createConjunctiveEqualityPredicate(builder, root, a, (Collection)val);
                        }
                    }
                    else if (isLte) {
                        pred = createLtePredicate(builder, root, a, val);
                    } else if (isGte) {
                        pred = createGtePredicate(builder, root, a, val);
                    } else if (isLt) {
                        pred = createLtPredicate(builder, root, a, val);
                    } else if (isGt) {
                        pred = createGtPredicate(builder, root, a, val);
                    }
                    if (pred == null) {
                        pred = builder.conjunction();
                    }
                    predicates.add(pred);
                }
            }
            return builder.and(predicates.toArray(new Predicate[predicates.size()]));
        };
    }



    // https://stackoverflow.com/a/16911313/986160
    //https://stackoverflow.com/a/47793003/986160
    public String getIdAttribute(EntityManager em, String fullClassName) {
        Class<? extends Object> clazz = null;
        try {
            clazz = Class.forName(fullClassName);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        Metamodel m = em.getMetamodel();
        IdentifiableType<T> of = (IdentifiableType<T>) m.managedType(clazz);
        return of.getId(of.getIdType().getJavaType()).getName();
    }

    private String cleanUpKey(String key) {

        List<String> postfixes = Arrays.asList("Gte", "Gt", "Lte", "Lt", "Not", "And");
        for (String postfix: postfixes) {
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
        for (Attribute a: attributes) {
            boolean javaTypeIsString = a.getJavaType().getSimpleName().equalsIgnoreCase("string");
            boolean shouldSearch = includeOnlyFields.isEmpty() || includeOnlyFields.contains(a.getName());
            if (javaTypeIsString && shouldSearch) {
                Predicate orPred = builder.like(root.get(a.getName()), finalText);
                orPredicates.add(orPred);
            }

        }

        return builder.or(orPredicates.toArray(new Predicate[orPredicates.size()]));

    }

    private Predicate createNegationPredicate(CriteriaBuilder builder, Root root, Attribute a, Object val) {
        return builder.not(createEqualityPredicate(builder, root, a, val));
    }


    private Predicate createEqualityPredicate(CriteriaBuilder builder, Root root, Attribute a, Object val) {
        if (val == null) {
            if (a.isAssociation() && a.isCollection()) {
                return builder.isEmpty(root.get(a.getName()));
            }
            else {
                return root.get(a.getName()).isNull();
            }
        }
        else if (isEnum(a)) {
            return builder.equal(root.get(a.getName()), Enum.valueOf(Class.class.cast(a.getJavaType()), (String) val));
        }
        else if (isPrimitive(a)) {
            return builder.equal(root.get(a.getName()), val);
        }
        else if(a.isAssociation()) {
            return prepareJoinAssociatedPredicate(root, a, val);
        }
        throw new IllegalArgumentException("equality/inequality is currently supported on primitives and enums");
    }


    private Predicate createLikePredicate(CriteriaBuilder builder, Root<T> root, Attribute a, String val) {
        return builder.like(root.get(a.getName()), val);
    }

    private Predicate createNotLikePredicate(CriteriaBuilder builder, Root<T> root, Attribute a, String val) {
        return builder.notLike(root.get(a.getName()), val);
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

    private Predicate createConjunctiveInequalityPredicate(CriteriaBuilder builder, Root root, Attribute a, Collection values) {
        return builder.not(createDisjunctiveEqualityPredicate(builder, root, a, values));
    }

    private Predicate createDisjunctiveEqualityPredicate(CriteriaBuilder builder, Root root, Attribute a, Collection values) {
        List<Predicate> orPredicates = new ArrayList<>();
        for (Object val : values) {
            Predicate orPred;
            if (a.isAssociation()) {
                orPred = prepareJoinAssociatedPredicate(root, a, val);
            }
            else {
                orPred = builder.equal(root.get(a.getName()), val);
            }
            orPredicates.add(orPred);
        }
        return builder.or(orPredicates.toArray(new Predicate[orPredicates.size()]));

    }

    private Predicate createConjunctiveEqualityPredicate(CriteriaBuilder builder, Root root, Attribute a, Collection values) {
        List<Predicate> andPredicates = new ArrayList<>();
        for (Object val : values) {
            Predicate andPred;
            if (a.isAssociation()) {
                andPred = prepareJoinAssociatedPredicate(root, a, val);
            }
            else {
                andPred = builder.equal(root.get(a.getName()), val);
            }
            andPredicates.add(andPred);
        }
        return builder.and(andPredicates.toArray(new Predicate[andPredicates.size()]));

    }


    private Predicate prepareJoinAssociatedPredicate(Root root, Attribute a, Object val) {
        Path rootJoinGetName = root.join(a.getName());
        String referencedClassName = rootJoinGetName.getJavaType().getName();
        String referencedPrimaryKey = getIdAttribute(em, referencedClassName);
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