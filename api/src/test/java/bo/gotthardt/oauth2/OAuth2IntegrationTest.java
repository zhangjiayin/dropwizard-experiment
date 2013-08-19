package bo.gotthardt.oauth2;

import bo.gotthardt.api.UserResource;
import bo.gotthardt.model.OAuth2AccessToken;
import bo.gotthardt.model.User;
import bo.gotthardt.oauth2.authentication.OAuth2Authenticator;
import bo.gotthardt.oauth2.authorization.OAuth2AccessTokenResource;
import bo.gotthardt.oauth2.authorization.OAuth2AuthorizationRequestProvider;
import bo.gotthardt.util.ImprovedResourceTest;
import bo.gotthardt.util.InMemoryDatastore;
import com.google.code.morphia.Datastore;
import com.google.common.net.HttpHeaders;
import com.sun.jersey.api.client.ClientResponse;
import com.yammer.dropwizard.auth.oauth.OAuthProvider;
import org.joda.time.Duration;
import org.junit.Test;

import javax.ws.rs.core.Response;

import static bo.gotthardt.util.fest.DropwizardAssertions.assertThat;

/**
 * Tests for the OAuth2 functionality working end-to-end.
 *
 * @author Bo Gotthardt
 */
public class OAuth2IntegrationTest extends ImprovedResourceTest {
    private final Datastore ds = new InMemoryDatastore();
    private final User user = createUser();

    @Override
    protected void setUpResources() throws Exception {
        addResource(new OAuth2AccessTokenResource(ds));
        addResource(new UserResource(ds));

        addProvider(OAuth2AuthorizationRequestProvider.class);
        addProvider(new OAuthProvider<User>(new OAuth2Authenticator(ds), "realm"));
    }

    @Test
    public void shouldCreateAndSendTokenThatIdentifiesUser() {
        ClientResponse response = POST("/token/?grant_type=password&username=testuser&password=testpass", null);
        assertThat(response).hasStatus(Response.Status.OK);

        OAuth2AccessToken token = response.getEntity(OAuth2AccessToken.class);
        // The token sent in the response won't have any user information, but if we get it from the database it will have.
        assertThat(ds.get(OAuth2AccessToken.class, token.getAccessToken()).getUser().getId())
                .isEqualTo(user.getId());
    }

    @Test
    public void shouldNotCreateTokenForInvalidCredentials() {
        assertThat(POST("/token/?grant_type=password&username=testuser&password=WRONGPASSWORD", null))
                .hasStatus(Response.Status.UNAUTHORIZED);

        assertThat(ds.getCount(OAuth2AccessToken.class))
                .isEqualTo(0);
    }

    @Test
    public void shouldNotCreateTokenForNonexistentCredentials() {
        assertThat(POST("/token/?grant_type=password&username=DOESNOTEXIST&password=testpass", null))
                .hasStatus(Response.Status.UNAUTHORIZED);

        assertThat(ds.getCount(OAuth2AccessToken.class))
                .isEqualTo(0);
    }

    @Test
    public void should400WhenMissingGrantType() {
        assertThat(POST("/token", null))
                .hasStatus(Response.Status.BAD_REQUEST);
    }

    @Test
    public void should400WhenGrantTypePasswordIsMissingUsername() {
        assertThat(POST("/token/?grant_type=password&password=testpass", null))
                .hasStatus(Response.Status.BAD_REQUEST);

    }

    @Test
    public void should400WhenGrantTypePasswordIsMissingPassword() {
        assertThat(POST("/token/?grant_type=password&username=testuser", null))
                .hasStatus(Response.Status.BAD_REQUEST);

    }

    @Test
    public void shouldRefuseNonAuthorizedAccessToAuthProtectedResource() {
        assertThat(GET("/users/" + user.getId()))
                .hasStatus(Response.Status.UNAUTHORIZED);
    }

    @Test
    public void shouldRefuseUnauthorizedAccessToAuthProtectedResource() {
        ClientResponse response = client().resource("/users/" + user.getId())
                .header(HttpHeaders.AUTHORIZATION, "Bearer WRONGTOKEN")
                .get(ClientResponse.class);

        assertThat(response)
                .hasStatus(Response.Status.UNAUTHORIZED);
    }

    @Test
    public void shouldAllowAuthorizedAccessToProtectedResource() {
        OAuth2AccessToken token = POST("/token/?grant_type=password&username=testuser&password=testpass", null).getEntity(OAuth2AccessToken.class);

        ClientResponse response = client().resource("/users/" + user.getId())
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token.getAccessToken())
                .get(ClientResponse.class);

        assertThat(response)
                .hasStatus(Response.Status.OK)
                .hasJsonContent(user);
    }

    @Test
    public void shouldInvalidateTokens() {
        OAuth2AccessToken token = new OAuth2AccessToken(user, Duration.standardHours(1));
        ds.save(token);

        ClientResponse response = client().resource("/token")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token.getAccessToken())
                .delete(ClientResponse.class);
        ds.get(token);

        assertThat(token.isValid()).isFalse();
    }

    private User createUser() {
        User user = new User("testuser", "testpass");
        ds.save(user);
        return user;
    }
}
