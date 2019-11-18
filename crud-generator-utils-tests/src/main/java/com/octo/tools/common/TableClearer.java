package com.octo.tools.common;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TableClearer {
	
	private static final Logger logger = LoggerFactory.getLogger(TableClearer.class);

    private DataSource dataSource;

    private Connection connection;

    public TableClearer(DataSource dataSource) {
		super();
		this.dataSource = dataSource;
	}

	public void clearTables() {
        try {
            connection = dataSource.getConnection();
            tryToClearTables();
            connection.commit();
            logger.info("DB clean OK");
        } catch (SQLException e) {
        	logger.error("Exception in TableClean", e);
            throw new RuntimeException(e);
        }
    }

    private void tryToClearTables() throws SQLException {
        List<String> tableNames = getTableNames();
        clear(tableNames);
        
    }

    private List<String> getTableNames() throws SQLException {
        List<String> tableNames = new ArrayList<>();

        DatabaseMetaData metaData = connection.getMetaData();
        ResultSet rs = metaData.getTables(
                connection.getCatalog(), null, null, new String[]{"TABLE"});

        while (rs.next()) {
            tableNames.add(rs.getString("TABLE_NAME"));
        }

        return tableNames;
    }

    private void clear(List<String> tableNames) throws SQLException {
        Statement statement = buildSqlStatement(tableNames);

        logger.debug("Executing DELETE SQL");
        statement.executeBatch();
        
        logger.debug("Executing DROP SQL");
        
        tableNames.forEach(tableName -> {
            try {
                statement.execute("DROP TABLE " + tableName + " CASCADE");
            } catch (SQLException e) {
                logger.warn("Exception while dropping table "+tableName, e);
            }
        });
    }

    private Statement buildSqlStatement(List<String> tableNames) throws SQLException {
        Statement statement = connection.createStatement();

        statement.addBatch(sql("SET FOREIGN_KEY_CHECKS = 0"));
        addDeleteSatements(tableNames, statement);        

        return statement;
    }

    private void addDeleteSatements(List<String> tableNames, Statement statement) {
        tableNames.forEach(tableName -> {
            try {
                statement.addBatch(sql("DELETE FROM " + tableName));
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        });
    }

    private String sql(String sql) {
        logger.debug("Adding SQL: {}", sql);
        return sql;
    }
}
