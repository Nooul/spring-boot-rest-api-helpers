package reactAdmin.rest.specifications;

import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@Service
public class ReactAdminSpecifications<T> {

    public Specification<T> seachInAllAttributes(String text, List<String> includeOnlyFields) {

        if (!text.contains("%")) {
            text = "%"+text+"%";
        }
        final String finalText = text;

        return new Specification<T>() {
            @Override
            public Predicate toPredicate(Root<T> root, CriteriaQuery<?> cq, CriteriaBuilder builder) {
                return builder.or(root.getModel().getAttributes().stream().filter(a-> {

                    if (a.getJavaType().getSimpleName().equalsIgnoreCase("string") && (includeOnlyFields.isEmpty() || includeOnlyFields.contains(a.getName()))) {
                        return true;
                    }
                    else {
                        return false;
                }}).map(a -> builder.like(root.get(a.getName()), finalText)
                    ).toArray(Predicate[]::new)
                );
            }
        };
    }

    public static boolean isManyToMany(String className) {
        List<String> allowdRefTypes = new ArrayList<>();
        allowdRefTypes.add("set");
        return allowdRefTypes.contains(className.toLowerCase());
    }

    public Specification<T> equalToEachColumn(HashMap<String,Object> map) {

        return new Specification<T>() {
            @Override
            public Predicate toPredicate(Root<T> root, CriteriaQuery<?> query, CriteriaBuilder builder) {

                final List<Predicate> predicates = new ArrayList<>();

                root.getModel().getAttributes().stream().forEach(a ->
                    {
                        Predicate pred = null;
                        if (!map.containsKey(a.getName())) {
                            pred = builder.conjunction();
                        }
                        else if (map.containsKey(a.getName())){
                            Object val = map.get(a.getName());
                            String attributeJavaClass = a.getJavaType().getSimpleName().toLowerCase();
                            if (val instanceof ArrayList && ((ArrayList)val).size() > 0) {
                                val = ((ArrayList) val).get(0);
                            }
                            if (attributeJavaClass.startsWith("int") ||
                                attributeJavaClass.equals("boolean") ||
                                attributeJavaClass.equals("string")  ||
                                attributeJavaClass.equals("float")   ||
                                attributeJavaClass.equals("double")) {
                                pred = builder.equal(root.get(a.getName()), val);
                            }
                            else if (ReactAdminSpecifications.isManyToMany(attributeJavaClass)) {
                                pred = builder.isTrue(root.join(a.getName()).get("id").in(val));
                            }
                            else { /*&& ReactAdminSpecifications.isReferenced(a.getJavaType().getSimpleName())) {*/
                                pred = builder.equal(root.get(a.getName()).get("id"), val);
                            }
                        }
                        if (pred == null ) {
                            pred = builder.conjunction();
                        }
                        predicates.add(pred);

                        if (map.containsKey(a.getName()+"Lte")) {
                            Object val = map.get(a.getName()+"Lte");
                            if (val instanceof String) {
                                pred = builder.lessThanOrEqualTo(root.get(a.getName()), ((String) val).toLowerCase());
                                predicates.add(pred);
                            }
                            else if(val instanceof Integer) {
                                pred = builder.lessThanOrEqualTo(root.get(a.getName()), (Integer)val);
                                predicates.add(pred);
                            }
                        }
                        else if (map.containsKey(a.getName()+"Gte")) {
                            Object val = map.get(a.getName()+"Gte");
                            if (val instanceof String) {
                                pred = builder.greaterThanOrEqualTo(root.get(a.getName()), ((String) val).toLowerCase());
                                predicates.add(pred);
                            }
                            else if(val instanceof Integer) {
                                pred = builder.greaterThanOrEqualTo(root.get(a.getName()), (Integer)val);
                                predicates.add(pred);
                            }
                        }
                        if (map.containsKey(a.getName()+"Lt")) {
                            Object val = map.get(a.getName()+"Lt");
                            if (val instanceof String) {
                                pred = builder.lessThan(root.get(a.getName()), ((String) val).toLowerCase());
                                predicates.add(pred);
                            }
                            else if(val instanceof Integer) {
                                pred = builder.lessThan(root.get(a.getName()), (Integer)val);
                                predicates.add(pred);
                            }
                        }
                        else if (map.containsKey(a.getName()+"Gt")) {
                            Object val = map.get(a.getName()+"Gt");
                            if (val instanceof String) {
                                pred = builder.greaterThan(root.get(a.getName()), ((String) val).toLowerCase());
                                predicates.add(pred);
                            }
                            else if(val instanceof Integer) {
                                pred = builder.greaterThan(root.get(a.getName()), (Integer)val);
                                predicates.add(pred);
                            }
                        }


                    });
                return builder.and(predicates.toArray(new Predicate[0]));
            }
        };
    }

 }
