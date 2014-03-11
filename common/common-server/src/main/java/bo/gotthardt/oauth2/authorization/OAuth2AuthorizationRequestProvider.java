package bo.gotthardt.oauth2.authorization;

import bo.gotthardt.exception.JsonMessageException;
import bo.gotthardt.exception.WebAppPreconditions;
import bo.gotthardt.jersey.provider.AbstractInjectableProvider;
import com.google.common.base.Preconditions;
import com.sun.jersey.api.core.HttpContext;
import org.eclipse.jetty.http.HttpStatus;

import javax.ws.rs.core.Context;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.Provider;

/**
 * Jersey injectable provider that enables {@link OAuth2AccessTokenResource} methods to have {@link OAuth2AuthorizationRequest} parameters.
 *
 * @author Bo Gotthardt
 */
@Provider
public class OAuth2AuthorizationRequestProvider extends AbstractInjectableProvider<Context, OAuth2AuthorizationRequest> {

    public OAuth2AuthorizationRequestProvider() {
        super(OAuth2AuthorizationRequest.class);
    }

    @Override
    public OAuth2AuthorizationRequest getValue(HttpContext c) {
        MultivaluedMap<String, String> queryParameters = c.getRequest().getQueryParameters();

        String grantType = queryParameters.getFirst("grant_type");
        WebAppPreconditions.checkArgumentNotNull(grantType, "OAuth2 authentication request requires a 'grant_type' query parameter.");

        switch (grantType) {
            case "password":
                return passwordFromQueryParameters(queryParameters);
//            case "authorization_code":
//                throw new WebApplicationException();
//            case "client_credentials":
//                throw new WebApplicationException();
            default:
                throw new JsonMessageException(HttpStatus.NOT_IMPLEMENTED_501, "Grant type '%s' not implemented.", grantType);
        }
    }

    private static OAuth2AuthorizationPasswordRequest passwordFromQueryParameters(MultivaluedMap<String, String> queryParameters) {
        Preconditions.checkState("password".equals(queryParameters.getFirst("grant_type")));

        String username = queryParameters.getFirst("username");
        WebAppPreconditions.checkArgumentNotNull(username, "'Password' grant type requires a 'username' query parameter.");

        String password = queryParameters.getFirst("password");
        WebAppPreconditions.checkArgumentNotNull(password, "'Password' grant type requires a 'password' query parameter.");

        return new OAuth2AuthorizationPasswordRequest(username, password);
    }
}