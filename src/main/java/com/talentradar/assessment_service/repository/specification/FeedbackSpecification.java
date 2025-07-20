package com.talentradar.assessment_service.repository.specification;

import com.talentradar.assessment_service.dto.feedback.request.FeedbackSearchCriteria;
import com.talentradar.assessment_service.model.Feedback;
import org.springframework.data.jpa.domain.Specification;

import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;

public class FeedbackSpecification {

    public static Specification<Feedback> createSpecification(FeedbackSearchCriteria criteria) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (criteria.getManagerId() != null) {
                predicates.add(criteriaBuilder.equal(root.get("managerId"), criteria.getManagerId()));
            }

            if (criteria.getDeveloperId() != null) {
                predicates.add(criteriaBuilder.equal(root.get("developerId"), criteria.getDeveloperId()));
            }

            if (criteria.getFeedbackVersion() != null) {
                predicates.add(criteriaBuilder.equal(root.get("feedbackVersion"), criteria.getFeedbackVersion()));
            }

            if (criteria.getCreatedAfter() != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("createdAt"), criteria.getCreatedAfter()));
            }

            if (criteria.getCreatedBefore() != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("createdAt"), criteria.getCreatedBefore()));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}