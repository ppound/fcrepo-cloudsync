package com.github.cwilper.fcrepo.cloudsync.service.dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.security.authentication.encoding.PasswordEncoder;
import org.springframework.security.authentication.encoding.ShaPasswordEncoder;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;

import com.github.cwilper.fcrepo.cloudsync.api.UnauthorizedException;
import com.github.cwilper.fcrepo.cloudsync.api.User;

public class UserDao extends AbstractDao {
    
    private static final String ROLE_USER = "ROLE_USER";
    private static final String ROLE_ADMIN = "ROLE_ADMIN";

    private final TransactionTemplate tt;

    public UserDao(JdbcTemplate db, TransactionTemplate tt) {
        super(db);
        this.tt = tt;
    }

    @Override
    public void initDb() {
        db.execute("CREATE TABLE Users ("
                + "id INTEGER PRIMARY KEY NOT NULL GENERATED BY DEFAULT AS IDENTITY, "
                + "username VARCHAR(64) NOT NULL, "
                + "password VARCHAR(64) NOT NULL, "
                + "enabled BOOLEAN NOT NULL, "
                + "CONSTRAINT UsersUnique UNIQUE (username)"
                + ")");
        db.execute("CREATE TABLE Authorities ("
                + "username VARCHAR(64) NOT NULL, "
                + "authority VARCHAR(64) NOT NULL, "
                + "CONSTRAINT AuthoritiesUnique UNIQUE (username, authority)"
                + ")");
    }

    // NOTE: Password is hashed before being stored
    public User createUser(final User user) throws UnauthorizedException {
        User requestingUser = getCurrentUser();
        if (requestingUser != null && !requestingUser.isAdmin()) {
            throw new UnauthorizedException("Only admins can create new accounts!");
        }
        tt.execute(new TransactionCallbackWithoutResult() {
            public void doInTransactionWithoutResult(TransactionStatus status) {
                boolean success = false;
                try {
                    PasswordEncoder encoder = new ShaPasswordEncoder();
                    String hashedPass = encoder.encodePassword(user.getPassword(), null);
                    user.setId(insert(
                            "INSERT INTO Users (username, password, enabled) "
                            + "VALUES (?, ?, ?)", user.getName(),
                            hashedPass, user.isEnabled()));
                    addAuthority(user.getName(), ROLE_USER);
                    if (user.isAdmin()) {
                        addAuthority(user.getName(), ROLE_ADMIN);
                    }
                    success = true;
                } finally {
                    if (!success) {
                        status.setRollbackOnly();
                    }
                }
            }
        });
        return user;
    }
        
    private void addAuthority(String username, String authority) {
        db.update("INSERT INTO Authorities (username, authority) VALUES (?, ?)",
                username, authority);
    }

    public List<User> listUsers() {
        return db.query("SELECT * FROM Users",
                new RowMapper<User>() {
                    public User mapRow(ResultSet rs, int i) throws SQLException {
                        return getUser(rs);
                    }
                });
    }

    // NOTE: Password is never exposed in get requests.
    public User getUser(String id) {
        return db.query("SELECT * FROM Users WHERE id = ?",
                new ResultSetExtractor<User>() {
                    public User extractData(ResultSet rs)
                            throws SQLException {
                        if (rs.next()) {
                            return getUser(rs);
                        } else {
                            return null;
                        }
                    }
                },
                Integer.parseInt(id));
    }

    private User getUser(ResultSet rs) throws SQLException {
        User u = new User();
        u.setId("" + rs.getInt("id"));
        u.setName(rs.getString("username"));
        u.setEnabled(rs.getBoolean("enabled"));
        u.setAdmin(checkAdmin(u.getName()));
        return u;
    }
    
    private boolean checkAdmin(String username) {
        return db.queryForInt("SELECT COUNT(*) FROM Authorities "
                + "WHERE username = ? AND authority = ?",
                username, ROLE_ADMIN) > 0;
    }

    public User getCurrentUser() {
        String userName = getCurrentUserName();
        if (userName == null) return null;
        return getUser("" + getUserId(userName));
    }

    public User updateUser(String id, User user) throws UnauthorizedException {
        User orig = getUser(id);
        String hashedPass = null;
        if (user.getId() == null) {
            user.setId(orig.getId());
        }
        if (user.getName() == null) {
            user.setName(orig.getName());
        }
        if (user.isAdmin() == null) {
            user.setAdmin(orig.isAdmin());
        }
        if (user.isEnabled() == null) {
            user.setEnabled(orig.isEnabled());
        }
        if (user.getPassword() != null) {
            PasswordEncoder encoder = new ShaPasswordEncoder();
            hashedPass = encoder.encodePassword(user.getPassword(), null);
        }
        checkUpdateUserPermission(user);
        replaceUser(user, orig.isAdmin(), hashedPass);
        user.setPassword(null); // never return the password
        return user;
    }
  
    private void checkUpdateUserPermission(User user) throws UnauthorizedException {
        User requestingUser = getCurrentUser();
        if (requestingUser.isAdmin()) {
            if (requestingUser.getId().equals(user.getId())) {
                if (!user.isEnabled()) {
                    throw new UnauthorizedException("You can't disable your own account!");
                } else if (!user.isAdmin()) {
                    throw new UnauthorizedException("You can't remove admin privileges from own account!");
                }
            }
        } else if (requestingUser.getId().equals(user.getId())) {
            if (!user.isEnabled()) {
                throw new UnauthorizedException("You can't disable your own account!");
            } else if (user.isAdmin()) {
                throw new UnauthorizedException("You can't add admin privileges to own account!");
            }
        } else {
            throw new UnauthorizedException("Only admins can modify other accounts!");
        }
    }
    
    private void replaceUser(final User user, final boolean wasAdmin, final String hashedPass) {
        tt.execute(new TransactionCallbackWithoutResult() {
            public void doInTransactionWithoutResult(TransactionStatus status) {
                boolean success = false;
                try {
                    StringBuilder sql = new StringBuilder();
                    sql.append("UPDATE Users SET username = ?");
                    if (hashedPass != null) {
                        sql.append(", password = ?");
                    }
                    sql.append(", enabled = ? WHERE id = ?");
                    if (hashedPass != null) {
                        db.update(sql.toString(), user.getName(), hashedPass,
                                user.isEnabled(), user.getId());
                    } else {
                        db.update(sql.toString(), user.getName(),
                                user.isEnabled(), user.getId());
                    }
                    if (wasAdmin && !user.isAdmin()) {
                        db.update("DELETE FROM Authorities WHERE username = ? AND authority = ?",
                                user.getName(), ROLE_ADMIN);
                    } else if (!wasAdmin && user.isAdmin()) {
                        addAuthority(user.getName(), ROLE_ADMIN);
                    }
                    success = true;
                } finally {
                    if (!success) {
                        status.setRollbackOnly();
                    }
                }
            }
        });        
    }

    public void deleteUser(final String id) throws UnauthorizedException {
        User requestingUser = getCurrentUser();
        if (!requestingUser.isAdmin()) {
            throw new UnauthorizedException("Only admins can delete accounts!");
        }
        if (requestingUser.getId().equals(id)) {
            throw new UnauthorizedException("You can't delete your own account!");
        }
        final String name = getUser(id).getName();
        tt.execute(new TransactionCallbackWithoutResult() {
            public void doInTransactionWithoutResult(TransactionStatus status) {
                boolean success = false;
                try {
                    db.update("DELETE FROM Users WHERE id = ?", Integer.parseInt(id));
                    db.update("DELETE FROM Authorities WHERE username = ?", name);
                    success = true;
                } finally {
                    if (!success) {
                        status.setRollbackOnly();
                    }
                }
            }
        });        
    }

    // or null if nobody's logged in
    private static String getCurrentUserName() {
        Authentication a = SecurityContextHolder.getContext().getAuthentication();
        if (a == null) return null;
        org.springframework.security.core.userdetails.User u =
                (org.springframework.security.core.userdetails.User)
                        a.getPrincipal();
        if (u == null) return null;
        return u.getUsername();
    }

    private Integer getUserId(String username) {
        if (username == null) return null;
        return db.query("SELECT id FROM Users WHERE username = ?",
                new ResultSetExtractor<Integer>() {
                    public Integer extractData(ResultSet rs) throws SQLException {
                        if (rs.next()) {
                            return rs.getInt("id");
                        } else {
                            return null;
                        }
                    }
                },
                username);
    }

}