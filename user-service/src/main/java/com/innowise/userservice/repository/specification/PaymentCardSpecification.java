package com.innowise.userservice.repository.specification;

import com.innowise.userservice.entity.PaymentCard;
import org.springframework.data.jpa.domain.Specification;

public class PaymentCardSpecification {

    public static Specification<PaymentCard> hasUserId(String userId) {
        return (root, query, criteriaBuilder) -> {
            if (userId == null || userId.isBlank()) {
                return null;
            }
            return criteriaBuilder.equal(root.get("user").get("id"), userId);
        };
    }

    public static Specification<PaymentCard> isActive(Boolean active) {
        return (root, query, criteriaBuilder) -> {
            if (active == null) {
                return null;
            }
            return criteriaBuilder.equal(root.get("active"), active);
        };
    }
}