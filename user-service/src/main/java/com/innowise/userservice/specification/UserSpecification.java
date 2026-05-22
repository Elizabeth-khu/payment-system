package com.innowise.userservice.specification;

import com.innowise.userservice.entity.User;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;

public class UserSpecification {

    public static Specification<User> getFilterSpecification(String name, String surname) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (StringUtils.hasText(name)) {
                predicates.add(criteriaBuilder.equal(root.get("name"), name));
            }

            if (StringUtils.hasText(surname)) {
                predicates.add(criteriaBuilder.equal(root.get("surname"), surname));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}