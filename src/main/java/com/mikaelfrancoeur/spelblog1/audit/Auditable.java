package com.mikaelfrancoeur.spelblog1.audit;

import java.util.List;

public class Auditable {

    @Audit(action = AuditAction.DISABLE_USER, expression = "#models.![userProfileId]")
    public void disableUsers(List<UserDisableModel> models) {
        // ...
    }

    @Audit(action = AuditAction.CREATE_USER, expression = "#userId")
    public void createUser(String userId) {
        // ...
    }
}
