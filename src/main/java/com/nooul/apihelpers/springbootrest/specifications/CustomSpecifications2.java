package com.nooul.apihelpers.springbootrest.specifications;

import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.util.List;
import java.util.Map;

@Service
public class CustomSpecifications2<T> {

    public Specification<T> customSpecificationBuilder(Map<String, Object> map) {

        return (Specification<T>) (root, query, builder) -> {

            query.distinct(true);
            return builder.and();
        };
    }

    public Predicate customSpecificationBuilder(CriteriaBuilder builder, CriteriaQuery query, Root root, Map<String, Object> map) {
        query.distinct(true);
        return builder.and();
    }

    public Predicate customSpecificationBuilder(CriteriaBuilder builder, CriteriaQuery query, Root root, Map<String, Object> map, List<String> includeOnlyFields) {
        query.distinct(true);
        return builder.and();
    }



    public Predicate customSpecificationBuilder(CriteriaBuilder builder, CriteriaQuery query, Root root, List<Map<String, Object>> list) {
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
