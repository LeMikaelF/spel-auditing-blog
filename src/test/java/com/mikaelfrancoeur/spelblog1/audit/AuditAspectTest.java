package com.mikaelfrancoeur.spelblog1.audit;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import java.util.List;

import org.assertj.core.api.WithAssertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.Mockito;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.util.AopTestUtils;

@ExtendWith(SpringExtension.class)
@Import({ AuditAspect.class, AuditConfig.class, AuditAspectTest.TestAuditable.class })
class AuditAspectTest implements WithAssertions {

    private static final String USER_ID = "userId";

    @MockitoSpyBean
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

    @Test
    void itAuditsAfterTheAdvisedMethod() {
        auditable.createUser(USER_ID);

        TestAuditable targetObject = AopTestUtils.getUltimateTargetObject(auditable);
        InOrder inOrder = Mockito.inOrder(targetObject, auditService);

        inOrder.verify(targetObject).createUser(USER_ID);
        inOrder.verify(auditService).audit(AuditAction.CREATE_USER, USER_ID);
    }

    @Test
    void itDoesNotAuditWhenTheAdvisedMethodsThrowsAnException() {
        assertThatThrownBy(auditable::throwException)
                .hasMessage("test exception");

        verifyNoInteractions(auditService);
    }

    static class TestAuditable {
        @Audit(action = AuditAction.DISABLE_USER, expression = "123")
        void withExceptionEvaluatingToNumber() {
        }

        @Audit(action = AuditAction.DISABLE_USER, expression = "{ 'abc', 123 }")
        void withExceptionEvaluatingToCollectionWithNonStringElements() {
        }

        @Audit(action = AuditAction.DISABLE_USER, expression = "#models.![userProfileId]")
        void disableUsers(List<UserDisableModel> models) {
        }

        @Audit(action = AuditAction.CREATE_USER, expression = "#userId")
        void createUser(@SuppressWarnings({ "unused", "SameParameterValue" }) String userId) {
        }

        @Audit(action = AuditAction.CREATE_USER, expression = "'abc'")
        void throwException() {
            throw new RuntimeException("test exception");
        }
    }
}
