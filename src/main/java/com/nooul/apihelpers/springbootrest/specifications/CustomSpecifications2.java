//package com.nooul.apihelpers.springbootrest.specifications;
//
//import jakarta.persistence.criteria.*;
//import org.springframework.data.jpa.domain.Specification;
//import org.springframework.stereotype.Service;
//import org.springframework.util.StringUtils;
//
//import java.lang.reflect.Field;
//import java.time.*;
//import java.util.*;
//import java.util.function.BiFunction;
//import java.util.stream.Collectors;
//@Service
//public class CustomSpecifications2<T> {
//
//    public Specification<T> build(Object filter) {
//        if (filter instanceof Map) {
//            return buildFromMap((Map<String, Object>) filter, new HashSet<>());
//        } else if (filter instanceof List) {
//            List<Map<String, Object>> filters = (List<Map<String, Object>>) filter;
//            return filters.stream()
//                    .map(f -> buildFromMap(f, new HashSet<>()))
//                    .reduce(Specification::or)
//                    .orElse(null);
//        }
//        return null;
//    }
//
//    private Specification<T> buildFromMap(Map<String, Object> filter, Set<String> joinPaths) {
//        return (root, query, cb) -> buildPredicateTree(filter, root, query, cb, joinPaths);
//    }
//
//    private Predicate buildPredicateTree(Map<String, Object> filter, From<?, ?> root, CriteriaQuery<?> query, CriteriaBuilder cb, Set<String> joinPaths) {
//        List<Predicate> predicates = new ArrayList<>();
//
//        for (Map.Entry<String, Object> entry : filter.entrySet()) {
//            String rawKey = entry.getKey();
//            Object value = entry.getValue();
//
//            Operator op = Operator.EQ;
//            String field = rawKey;
//
//            for (Operator o : Operator.values()) {
//                if (rawKey.endsWith(o.suffix)) {
//                    op = o;
//                    field = rawKey.substring(0, rawKey.length() - o.suffix.length());
//                    break;
//                }
//            }
//
//            Path<?> path;
//            if (value instanceof Map || value instanceof List) {
//                Join<Object, Object> join = getOrCreateJoin(root, field, JoinType.LEFT);
//                if (value instanceof Map mapValue) {
//                    predicates.add(buildPredicateTree(mapValue, join, query, cb, joinPaths));
//                } else if (value instanceof List listValue) {
//                    List<Predicate> orPredicates = listValue.stream()
//                            .filter(v -> v instanceof Map)
//                            .map(v -> buildPredicateTree((Map<String, Object>) v, join, query, cb, joinPaths))
//                            .toList();
//                    predicates.add(cb.or(orPredicates.toArray(new Predicate[0])));
//                }
//                continue;
//            }
//
//            try {
//                path = root.get(field);
//            } catch (IllegalArgumentException ex) {
//                Join<Object, Object> join = getOrCreateJoin(root, field, value == null ? JoinType.LEFT : JoinType.INNER);
//                path = join.get("id");
//            }
//
//            Class<?> type = path.getJavaType();
//            Object typedValue = convertValue(value, type);
//            predicates.add(op.predicate(cb, path, typedValue));
//        }
//
//        return cb.and(predicates.toArray(new Predicate[0]));
//    }
//
//    private Join<Object, Object> getOrCreateJoin(From<?, ?> root, String field, JoinType joinType) {
//        return root.getJoins().stream()
//                .filter(j -> j.getAttribute().getName().equals(field))
//                .findFirst()
//                .map(j -> (Join<Object, Object>) j)
//                .orElse(root.join(field, joinType));
//    }
//
//    private Object convertValue(Object value, Class<?> type) {
//        if (value == null) return null;
//        if (type.isInstance(value)) return value;
//
//        String str = value.toString();
//
//        try {
//            if (type == UUID.class) return UUID.fromString(str);
//            if (type == Long.class || type == long.class) return Long.parseLong(str);
//            if (type == Integer.class || type == int.class) return Integer.parseInt(str);
//            if (type == Instant.class) return Instant.parse(str);
//            if (type == LocalDate.class) return LocalDate.parse(str);
//            if (type == LocalDateTime.class) return LocalDateTime.parse(str);
//            if (type == Boolean.class || type == boolean.class) return Boolean.parseBoolean(str);
//        } catch (Exception ignored) {
//        }
//
//        return str;
//    }
//
//    enum Operator {
//        NOT("Not", (cb, path, val) -> val == null ? cb.isNotNull(path) : cb.notEqual(path, val)),
//        GT("Gt", (cb, path, val) -> cb.greaterThan(path.as(Comparable.class), (Comparable) val)),
//        LT("Lt", (cb, path, val) -> cb.lessThan(path.as(Comparable.class), (Comparable) val)),
//        EQ("", (cb, path, val) -> {
//            if (val == null) return cb.isNull(path);
//            if (val instanceof String str && str.contains("%")) return cb.like(path.as(String.class), str);
//            return cb.equal(path, val);
//        });
//
//        final String suffix;
//        final TriFunction<CriteriaBuilder, Path<?>, Object, Predicate> predicate;
//
//        Operator(String suffix, TriFunction<CriteriaBuilder, Path<?>, Object, Predicate> predicate) {
//            this.suffix = suffix;
//            this.predicate = predicate;
//        }
//
//        Predicate predicate(CriteriaBuilder cb, Path<?> path, Object val) {
//            return predicate.apply(cb, path, val);
//        }
//    }
//
//    @FunctionalInterface
//    interface TriFunction<A, B, C, R> {
//        R apply(A a, B b, C c);
//    }
//}
