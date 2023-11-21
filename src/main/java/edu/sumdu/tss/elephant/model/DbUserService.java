package edu.sumdu.tss.elephant.model;

import edu.sumdu.tss.elephant.helper.DBPool;
import edu.sumdu.tss.elephant.helper.utils.CmdUtil;
import edu.sumdu.tss.elephant.helper.utils.ParameterizedStringFactory;
import io.javalin.core.util.JavalinLogger;

import java.io.File;

public class DbUserService {

    private static final ParameterizedStringFactory CREATE_USER_SQL = new ParameterizedStringFactory("CREATE USER :name WITH PASSWORD ':password' CONNECTION LIMIT 5 IN ROLE customer;");
    private static final ParameterizedStringFactory DELETE_USER_SQL = new ParameterizedStringFactory("DROP USER :name");

    public static void initUser(String username, String password) {
        //Create user
        System.out.println("Username: " + username);
        String createUserString = CREATE_USER_SQL.addParameter("name", username).addParameter("password", password).toString();
        System.out.println(createUserString);
        DBPool.getConnection().open().createQuery(createUserString, false).executeUpdate();
        //Create tablespace
        String path = UserService.userStoragePath(username);
        System.out.println("Tablespace path:" + path);
        UserService.createTablespace(username, path + File.separator + "tablespace");
    }

    //TODO: SQL injection here!
    private static final ParameterizedStringFactory RESET_USER_SQL = new ParameterizedStringFactory("ALTER USER :name WITH PASSWORD ':password'");

    public static void dbUserPasswordReset(String name, String password) {
        String query = RESET_USER_SQL.addParameter("name", name).addParameter("password", password).toString();
        JavalinLogger.info(query);
        DBPool.getConnection().open().createQuery(query, false).executeUpdate();
    }

    public static void dropUser(String name) {
        //Drop user
        var connection = DBPool.getConnection();
        try (var context = connection.beginTransaction()) {
            context.createQuery(DELETE_USER_SQL.addParameter("name", name).toString(), false).executeUpdate();
        }
        //TODO: drop all database
        //Drop tablespace
        String path = UserService.userStoragePath(name);
        //TODO: create script for removing user profile
        CmdUtil.exec(String.format("sudo remove-user %s", path));
    }
}
