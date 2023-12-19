package edu.sumdu.tss.elephant.middleware;

import edu.sumdu.tss.elephant.helper.UserRole;
import io.javalin.http.Context;
import io.javalin.http.Handler;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.mockito.Mockito.*;

class CustomAccessManagerTest {

    @Test
    void handleWithNoRoles() throws Exception {
        Handler handler = mock(Handler.class);
        Context ctx = mock(Context.class);

        CustomAccessManager.accessManager.manage(handler, ctx, Set.of());

        verify(handler, times(1)).handle(ctx);
    }

    @Test
    void handleWithUnauthorizedUser() throws Exception {
        Handler handler = mock(Handler.class);
        Context ctx = mock(Context.class);

        CustomAccessManager.accessManager.manage(handler, ctx, Set.of(UserRole.ADMIN));

        verify(handler, never()).handle(ctx);
        verify(ctx).redirect(anyString(), eq(302));
    }

}
