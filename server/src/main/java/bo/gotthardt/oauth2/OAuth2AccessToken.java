package bo.gotthardt.oauth2;

import bo.gotthardt.model.User;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.joda.time.DateTime;
import org.joda.time.Duration;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import java.util.UUID;

/**
 * OAuth2 access token.
 * <br/><br/>
 * This is the value added in a header on all requests to prove that they have been sent by a particular user.
 *
 * @author Bo Gotthardt
 */
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class OAuth2AccessToken {
    /** The access token itself. */
    @Id
    private String accessToken; // TODO hash this

    /** The token's expiration date, after which it is no longer valid. */
    @Setter
    private DateTime expirationDate;

    /** The user that this token authenticates. */
    @ManyToOne
    @JsonIgnore
    private User user;

    /**
     * Constructor.
     * @param user The user that the token should authenticate.
     * @param duration How long the token should be valid for, starting from now.
     */
    public OAuth2AccessToken(User user, Duration duration) {
        // UUID is a convenient way to generate a random string.
        this.accessToken = UUID.randomUUID().toString();
        this.expirationDate = DateTime.now().plus(duration);
        this.user = user;
    }

    /**
     * Returns whether this token is currently valid.
     */
    public boolean isValid() {
        return expirationDate.isAfterNow();
    }
}
