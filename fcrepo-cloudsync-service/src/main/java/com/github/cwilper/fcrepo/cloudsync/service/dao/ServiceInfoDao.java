package com.github.cwilper.fcrepo.cloudsync.service.dao;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ResultSetExtractor;

import com.github.cwilper.fcrepo.cloudsync.api.ServiceInfo;
import com.github.cwilper.fcrepo.cloudsync.api.User;

public class ServiceInfoDao extends AbstractDao {
    
    private final UserDao userDao;

    public ServiceInfoDao(JdbcTemplate db, UserDao userDao) {
        super(db);
        this.userDao = userDao;
    }

    @Override
    public void initDb() {
        db.execute("CREATE TABLE ServiceInfo (initialized BOOLEAN NOT NULL)");
        db.execute("INSERT INTO ServiceInfo VALUES (false)");
    }

    public ServiceInfo getServiceInfo() {
        return db.query("SELECT * FROM ServiceInfo",
                new ResultSetExtractor<ServiceInfo>() {
                    @Override
                    public ServiceInfo extractData(ResultSet rs)
                            throws SQLException {
                        rs.next();
                        ServiceInfo c = new ServiceInfo();
                        c.setInitialized(rs.getBoolean("initialized"));
                        c.setVersion(System.getProperty("cloudsync.version"));
                        c.setBuildDate(System.getProperty("cloudsync.builddate"));
                        return c;
                    }
                });
    }

    public ServiceInfo updateServiceInfo(ServiceInfo newInfo) {
        ServiceInfo currentInfo = getServiceInfo();
        if (currentInfo.isInitialized()) {
            throw new RuntimeException("CloudSync is already initialized.");
        }
        String name = newInfo.getInitialAdminUsername();
        if (name == null || name.length() == 0) {
            throw new RuntimeException("initialAdminUsername must be non-empty.");
        }
        String pass = newInfo.getInitialAdminPassword();
        if (pass == null || pass.length() == 0) {
            throw new RuntimeException("initialAdminPassword must be non-empty.");
        }
        User admin = new User();
        admin.setName(name);
        admin.setPassword(pass);
        admin.setAdmin(true);
        admin.setEnabled(true);
        userDao.createUser(admin);
        db.update("UPDATE ServiceInfo SET initialized = true");
        newInfo.setBuildDate(currentInfo.getBuildDate());
        newInfo.setVersion(currentInfo.getVersion());
        newInfo.setInitialized(true);
        return newInfo;
    }

}
