package com.talentradar.assessment_service.config;

import com.talentradar.assessment_service.model.Comment;
import com.talentradar.assessment_service.model.DimensionDefinition;
import com.talentradar.assessment_service.model.GradingCriteria;
import com.talentradar.assessment_service.repository.CommentRepository;
import com.talentradar.assessment_service.repository.DimensionDefinitionRepository;
import com.talentradar.assessment_service.repository.GradingCriteriaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final DimensionDefinitionRepository dimensionDefinitionRepository;
    private final GradingCriteriaRepository gradingCriteriaRepository;
    private final CommentRepository commentRepository;

    @Override
    @Transactional
    public void run(String... args) {
        if (dimensionDefinitionRepository.count() == 0) {
            log.info("Initializing dimension definitions and grading criteria and comments...");
            initializeDimensionsAndCriteria();
            initializeCommentTemplates();
            log.info("Data initialization completed successfully!");
        } else {
            log.info("Data already exists. Skipping initialization.");
        }
    }

    private void initializeDimensionsAndCriteria() {
        // Technical Excellence
        Set<GradingCriteria> technicalCriteria = createGradingCriteria(List.of(
                "Code quality and maintainability",
                "Technology stack proficiency",
                "Innovation and technical leadership",
                "System design and architecture knowledge",
                "Problem-solving methodology"
        ));

        createDimensionDefinition(
                "Technical Excellence",
                "Mastery of programming languages, frameworks, architecture patterns, and technical problem-solving capabilities",
                new BigDecimal("25.0"),
                technicalCriteria
        );

        // Communication & Collaboration
        Set<GradingCriteria> communicationCriteria = createGradingCriteria(List.of(
                "Technical documentation quality",
                "Active listening and feedback incorporation",
                "Stakeholder management",
                "Presentation and explanation skills",
                "Cross-team collaboration effectiveness"
        ));

        createDimensionDefinition(
                "Communication & Collaboration",
                "Effectiveness in verbal, written, and cross-functional communication with technical and non-technical stakeholders",
                new BigDecimal("20.0"),
                communicationCriteria
        );

        // Team Dynamics & Leadership
        Set<GradingCriteria> teamCriteria = createGradingCriteria(List.of(
                "Mentorship and knowledge sharing",
                "Team culture contribution",
                "Leadership potential demonstration",
                "Conflict resolution and mediation",
                "Peer support and collaboration"
        ));

        createDimensionDefinition(
                "Team Dynamics & Leadership",
                "Contribution to team culture, mentoring capabilities, and collaborative problem-solving",
                new BigDecimal("20.0"),
                teamCriteria
        );

        // Execution & Results
        Set<GradingCriteria> executionCriteria = createGradingCriteria(List.of(
                "Project timeline adherence",
                "Business impact and value creation",
                "Continuous improvement mindset",
                "Quality standards compliance",
                "Risk management and mitigation"
        ));

        createDimensionDefinition(
                "Execution & Results",
                "Consistent delivery of high-quality work within timelines and business requirements",
                new BigDecimal("20.0"),
                executionCriteria
        );

        // Growth & Innovation
        Set<GradingCriteria> growthCriteria = createGradingCriteria(List.of(
                "Learning agility and curiosity",
                "Innovation and creative thinking",
                "Continuous skill development",
                "Change management and flexibility",
                "Technology trend awareness"
        ));

        createDimensionDefinition(
                "Growth & Innovation",
                "Ability to learn, adapt to change, and drive innovation in dynamic environments",
                new BigDecimal("15.0"),
                growthCriteria
        );
    }

    private Set<GradingCriteria> createGradingCriteria(List<String> criteriaNames) {
        Set<GradingCriteria> criteria = new HashSet<>();
        
        for (String criteriaName : criteriaNames) {
            // Check if criteria already exists to avoid duplicates
            GradingCriteria existingCriteria = gradingCriteriaRepository
                    .findByCriteriaName(criteriaName)
                    .orElse(null);
            
            if (existingCriteria != null) {
                criteria.add(existingCriteria);
            } else {
                GradingCriteria newCriteria = GradingCriteria.builder()
                        .criteriaName(criteriaName)
                        .dimensionDefinitions(new HashSet<>())
                        .build();
                
                GradingCriteria savedCriteria = gradingCriteriaRepository.save(newCriteria);
                criteria.add(savedCriteria);
                log.debug("Created grading criteria: {}", criteriaName);
            }
        }
        
        return criteria;
    }

    private void createDimensionDefinition(String name, String description, BigDecimal weight, Set<GradingCriteria> criteria) {
        DimensionDefinition dimension = DimensionDefinition.builder()
                .dimensionName(name)
                .description(description)
                .weight(weight)
                .gradingCriteriaSet(criteria)
                .build();

        DimensionDefinition savedDimension = dimensionDefinitionRepository.save(dimension);
        
        // Update the bidirectional relationship
        for (GradingCriteria criteria1 : criteria) {
            criteria1.getDimensionDefinitions().add(savedDimension);
            gradingCriteriaRepository.save(criteria1);
        }
        
        log.info("Created dimension definition: {} with {} criteria", name, criteria.size());
    }
    private void initializeCommentTemplates() {
        List<String> commentTitles = List.of(
                "Key Strengths & Achievements",
                "Development Opportunities",
                "Development Goals & Action Plan",
                "Overall Performance Summary"
        );

        for (String commentTitle : commentTitles) {
            // Check if comment template already exists to avoid duplicates
            if (commentRepository.findByCommentTitle(commentTitle).isEmpty()) {
                Comment comment = Comment.builder()
                        .commentTitle(commentTitle)
                        .build();

                Comment savedComment = commentRepository.save(comment);
                log.info("Created comment template: {}", commentTitle);
            } else {
                log.debug("Comment template already exists: {}", commentTitle);
            }
        }
    }
}