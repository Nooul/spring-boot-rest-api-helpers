package reactAdmin.rest.specifications;


import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import javax.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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

    public static boolean isManyToMany(String className) {
        List<String> allowdRefTypes = new ArrayList<>();
        allowdRefTypes.add("set");
        return allowdRefTypes.contains(className.toLowerCase());
    }

    //https://stackoverflow.com/questions/604424/how-to-get-an-enum-value-from-a-string-value-in-java
    //https://stackoverflow.com/a/1723239
    public static <E extends Enum<E>> E getEnumFromString(Class<?> c, String string) {
        if (c != null && string != null) {
            try {
                return Enum.valueOf((Class<E>) c, string.trim().toUpperCase());
            } catch (IllegalArgumentException ex) {
            }
        }
        return null;
    }

    public Specification<T> equalToEachColumn(Map<String, Object> map) {

        return (Specification<T>) (root, query, builder) -> {

            final List<Predicate> predicates = new ArrayList<>();
            root.getModel().getAttributes().stream().forEach(a ->
            {
                Predicate pred = null;
                if (!map.containsKey(a.getName())) {
                    pred = builder.conjunction();
                } else if (map.containsKey(a.getName())) {
                    Object val = map.get(a.getName());
                    String attributeJavaClass = a.getJavaType().getSimpleName().toLowerCase();
                    String parentJavaClass = a.getJavaType().getSuperclass().getSimpleName().toLowerCase();

                    if (val instanceof Map) {
                        val = ((Map) val).get("id");
                    }

                    if (val instanceof ArrayList && !((ArrayList) val).isEmpty() && ((ArrayList) val).get(0) instanceof Map) {
                        val = ((Map) ((ArrayList) val).get(0)).get("id");
                    }
                    if (attributeJavaClass.startsWith("int") ||
                            attributeJavaClass.equals("boolean") ||
                            attributeJavaClass.equals("string") ||
                            attributeJavaClass.equals("float") ||
                            attributeJavaClass.equals("double")) {

                    } else if (parentJavaClass.equals("enum")) {

                        pred = builder.equal(root.get(a.getName()), Enum.<Enum>valueOf(Class.class.cast(a.getJavaType()), (String) val));

                    } else if (CustomSpecifications.isManyToMany(attributeJavaClass)) {
                        pred = builder.isTrue(root.join(a.getName()).get("id").in(val));
                    } else {
                        if (val == null) {
                            pred = builder.isNull(root.get(a.getName()));
                        } else {
                            pred = builder.equal(root.get(a.getName()).get("id"), val);
                        }
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
}