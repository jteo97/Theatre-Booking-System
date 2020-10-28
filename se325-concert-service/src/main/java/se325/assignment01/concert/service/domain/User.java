package se325.assignment01.concert.service.domain;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import javax.persistence.*;

/**
 * The User class models that of a User of the web service. It contains fields such as a generated ID, username and password
 * that the user creates, a version to see if updated, a cookie for authentication
 */
@Entity
@Table(name = "USERS")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    private Long id; //Database generated ID to uniquely identify concert

    @Column(name = "USERNAME")
    private String username; //Credentials of the user

    @Column(name = "PASSWORD")
    private String password; //Credentials of the user

    @Column(name = "VERSION")
    @Version
    private Long version; //A version to see if the user is updated, for concurrency control

    private String cookie; //Authentication cookie for verification

    public User() {

    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Long getVersion() {
        return version;
    }

    public void setVersion(Long version) {
        this.version = version;
    }

    public String getCookie() {
        return cookie;
    }

    public void setCookie(String cookie) {
        this.cookie = cookie;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof User))
            return false;

        if (obj == this)
            return true;

        User rhs = (User) obj;
        return new EqualsBuilder().
                append(username, rhs.username).
                isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 31).
                append(username).hashCode();
    }
}
