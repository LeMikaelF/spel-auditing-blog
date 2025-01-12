package com.mikaelfrancoeur.spelblog1.audit;

import static org.mockito.Mockito.verify;

import java.util.List;

import org.assertj.core.api.WithAssertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@SpringBootTest(classes = { AuditAspect.class, AuditConfig.class, AuditTest.TestAuditable.class })
class AuditTest implements WithAssertions {

    private static final String USER_ID = "userId";

    @Autowired
    private TestAuditable auditable;

    @MockitoBean
    private AuditService auditService;

    @Test
    void itAuditsAllIdsWhenTheExpressionEvaluatesToACollection() {
        List<UserDisableModel> models = List.of(
                new UserDisableModel("id1"),
                new UserDisableModel("id2"),
                new UserDisableModel("id3")
        );

        auditable.disableUsers(models);

        verify(auditService).audit(AuditAction.DISABLE_USER, "id1");
        verify(auditService).audit(AuditAction.DISABLE_USER, "id2");
        verify(auditService).audit(AuditAction.DISABLE_USER, "id3");
    }

    @Test
    void itAuditsTheIdWhenTheExpressionEvaluatesToAString() {
        auditable.createUser(USER_ID);

        verify(auditService).audit(AuditAction.CREATE_USER, USER_ID);
    }

    @Test
    void itThrowsAnExceptionWhenTheExpressionDoesNotEvaluateToAString() {
        assertThatThrownBy(() -> auditable.withExceptionEvaluatingToNumber())
                .hasMessage("@Audit expression evaluated to non-string type java.lang.Integer");
    }

    @Test
    void itThrowsAnExceptionWhenTheExpressionEvaluatesToACollectionContainingNonStringElements() {
        assertThatThrownBy(() -> auditable.withExceptionEvaluatingToCollectionWithNonStringElements())
                .hasMessage("@Audit expression evaluated to collection with non-string element: java.lang.Integer");
    }

    static class TestAuditable {
        @Audit(action = AuditAction.DISABLE_USER, expression = "123")
        void withExceptionEvaluatingToNumber() {
        }

        @Audit(action = AuditAction.DISABLE_USER, expression = "{ 'abc', 123 }")
        void withExceptionEvaluatingToCollectionWithNonStringElements() {
        }

        @Audit(action = AuditAction.DISABLE_USER, expression = "#models.![userProfileId]")
        public void disableUsers(List<UserDisableModel> models) {
        }

        @SuppressWarnings("unused")
        @Audit(action = AuditAction.CREATE_USER, expression = "#userId")
        public void createUser(String userId) {
        }
    }
}
