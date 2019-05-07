package reactAdmin.rest.specifications;


import org.apache.commons.lang3.StringUtils;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import javax.persistence.criteria.Predicate;
import javax.persistence.metamodel.Attribute;
import javax.persistence.metamodel.PluralAttribute;
import javax.persistence.metamodel.SingularAttribute;
import java.util.*;

//from: https://github.com/zifnab87/react-admin-java-rest/blob/master/src/main/java/reactAdmin/rest/specifications/ReactAdminSpecifications.java
@Service
public class CustomSpecifications<T> {

    public Specification<T> seachInAllAttributes(String text, List<String> includeOnlyFields) {

        if (!text.contains("%")) {
            text = "%" + text + "%";
        }
        final String finalText = text;

        return (Specification<T>) (root, cq, builder) -> builder.or(root.getModel().getAttributes().stream().filter(a ->
                        (a.getJavaType().getSimpleName().equalsIgnoreCase("string")
                                && (includeOnlyFields.isEmpty() || includeOnlyFields.contains(a.getName())))
                ).map(a -> builder.like(root.get(a.getName()), finalText)
                ).toArray(Predicate[]::new)
        );
    }


    public Specification<T> equalToEachColumn(Map<String, Object> map) {

        return (Specification<T>) (root, query, builder) -> {

            final List<Predicate> predicates = new ArrayList<>();

            Set<SingularAttribute<? super T, ?>> singularAttributes = root.getModel().getSingularAttributes();
            Set<PluralAttribute<? super T, ?, ?>> pluralAttributes = root.getModel().getPluralAttributes();


            root.getModel().getAttributes().stream().forEach(a ->
            {


                Predicate pred = builder.conjunction();
                if (map.containsKey(a.getName())) {


                    Object val = map.get(a.getName());

                    val = extractId(val);
//                    boolean isAttributeSingular = singularAttributes.contains(a);
//                    boolean isAttributePlural = pluralAttributes.contains(a);
                    boolean isAttributePrimitive = isPrimitive(a);
                    boolean isAttributeReferenced = a.isAssociation();//isCollection(a) || (!isPrimitive(a) && !isEnum(a));
                    boolean isAttributeEnum = isEnum(a);
                    boolean isValueNull = val == null;
                    boolean isValueCollection = val instanceof Collection;

                    if (isValueNull && !isAttributeReferenced) {
                        pred = builder.isNull(root.get(a.getName()));
                    }
                    else if (isAttributePrimitive) {

                        if (!isValueCollection) {
                            pred = builder.equal(root.get(a.getName()), val);
                        }
                        else {
                            Collection colVal = (Collection) val;
                            List<Predicate> orPredicates = new ArrayList<>();
                            for (Object el : colVal) {
                                Predicate orPred = builder.equal(root.get(a.getName()), el);
                                orPredicates.add(orPred);
                            }
                            pred = builder.or(orPredicates.toArray(new Predicate[0]));
                        }
                    }
                    else if (isAttributeEnum) {
                        pred = builder.equal(root.get(a.getName()), Enum.valueOf(Class.class.cast(a.getJavaType()), (String) val));

                    }
                    else if (isAttributeReferenced) {
                        if (isValueNull){
                            pred = builder.isNull(root.get(a.getName()));
                        }
                        else if (!isValueCollection) {
                            pred = root.join(a.getName()).get("id").in(val);
                        }
                        else {
                            Collection colVal = (Collection) val;
                            List<Predicate> orPredicates = new ArrayList<>();
                            for (Object el : colVal) {
                                Predicate orPred = root.join(a.getName()).get("id").in(el);
                                orPredicates.add(orPred);
                            }
                            pred = builder.or(orPredicates.toArray(new Predicate[0]));
                        }
                    }
                    else {
                        pred = builder.equal(root.join(a.getName()).get("id"), val);
                    }
                }

                if (pred == null) {
                    pred = builder.conjunction();
                }
                predicates.add(pred);

                if (map.containsKey(a.getName() + "Lte")) {
                    Object val = map.get(a.getName() + "Lte");
                    if (val instanceof String) {
                        pred = builder.lessThanOrEqualTo(root.get(a.getName()), ((String) val).toLowerCase());
                        predicates.add(pred);
                    } else if (val instanceof Integer) {
                        pred = builder.lessThanOrEqualTo(root.get(a.getName()), (Integer) val);
                        predicates.add(pred);
                    }
                }
                if (map.containsKey(a.getName() + "Gte")) {
                    Object val = map.get(a.getName() + "Gte");
                    if (val instanceof String) {
                        pred = builder.greaterThanOrEqualTo(root.get(a.getName()), ((String) val).toLowerCase());
                        predicates.add(pred);
                    } else if (val instanceof Integer) {
                        pred = builder.greaterThanOrEqualTo(root.get(a.getName()), (Integer) val);
                        predicates.add(pred);
                    }
                }
                if (map.containsKey(a.getName() + "Lt")) {
                    Object val = map.get(a.getName() + "Lt");
                    if (val instanceof String) {
                        pred = builder.lessThan(root.get(a.getName()), ((String) val).toLowerCase());
                        predicates.add(pred);
                    } else if (val instanceof Integer) {
                        pred = builder.lessThan(root.get(a.getName()), (Integer) val);
                        predicates.add(pred);
                    }
                }
                if (map.containsKey(a.getName() + "Gt")) {
                    Object val = map.get(a.getName() + "Gt");
                    if (val instanceof String) {
                        pred = builder.greaterThan(root.get(a.getName()), ((String) val).toLowerCase());
                        predicates.add(pred);
                    } else if (val instanceof Integer) {
                        pred = builder.greaterThan(root.get(a.getName()), (Integer) val);
                        predicates.add(pred);
                    }
                }


            });
            return builder.and(predicates.toArray(new Predicate[0]));
        };

    }

    private Object extractId(Object val) {
        if (val instanceof Map) {
            val = ((Map) val).get("id");
        } else if (val instanceof ArrayList && !((ArrayList) val).isEmpty() && ((ArrayList) val).get(0) instanceof Map) {
            val = ((Map) ((ArrayList) val).get(0)).get("id");
        }

        return val;
    }

    private boolean isPrimitive(Attribute attribute) {
        String attributeJavaClass = attribute.getJavaType().getSimpleName().toLowerCase();
        return attributeJavaClass.startsWith("int") ||
                attributeJavaClass.equals("boolean") ||
                attributeJavaClass.equals("string") ||
                attributeJavaClass.equals("float") ||
                attributeJavaClass.equals("double");
    }

    private boolean isCollection(Attribute attribute) {
        String attributeJavaClass = attribute.getJavaType().getSimpleName().toLowerCase();
        List<String> allowdRefTypes = new ArrayList<>();
        allowdRefTypes.add("set");
        allowdRefTypes.add("list");
        return allowdRefTypes.contains(attributeJavaClass.toLowerCase());
    }

    private boolean isEnum(Attribute attribute) {
        String parentJavaClass = "";
        if (attribute.getJavaType().getSuperclass() != null) {
            parentJavaClass = attribute.getJavaType().getSuperclass().getSimpleName().toLowerCase();
        }
        return parentJavaClass.equals("enum");
    }

}