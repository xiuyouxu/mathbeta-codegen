package com.mathbeta.models.sql;

import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.Statements;
import net.sf.jsqlparser.statement.create.table.ColumnDefinition;
import net.sf.jsqlparser.statement.create.table.CreateTable;

import java.io.*;
import java.util.List;

/**
 * Created by xiuyou.xu on 2017/7/18.
 */
public class ParserTest {
    public static void main(String[] args) {
        try {
            String sqls = read("d:/mes-db.sql");
            Statements statements = CCJSqlParserUtil.parseStatements(sqls);
            List<Statement> list = statements.getStatements();
            if (list != null && !list.isEmpty()) {
                list.stream().forEach(stmt -> {
                    if (stmt instanceof CreateTable) {
                        CreateTable createTable = (CreateTable) stmt;
                        List<ColumnDefinition> definitions = createTable.getColumnDefinitions();
                        if (definitions != null && !definitions.isEmpty()) {
                            System.out.println(createTable.getTable().getName());
                            System.out.println(createTable.getTableOptionsStrings());
                            definitions.stream().forEach(def -> {
                                System.out.println(def.getColumnName() + "->" + def.getColDataType().getArgumentsStringList() + ", " + def.getColumnSpecStrings());
                            });
                            if (createTable.getIndexes() != null && !createTable.getIndexes().isEmpty()) {
                                System.out.println(createTable.getIndexes().get(0).getColumnsNames());
                            }
                            System.out.println("===========================================================");
                        }
                    }
                });
            }
        } catch (JSQLParserException e) {
            e.printStackTrace();
        }
    }

    private static String read(String path) {
        StringBuilder sb = new StringBuilder();
        try (BufferedReader br = new BufferedReader(new FileReader(new File(path)))) {
            String line = null;
            while ((line = br.readLine()) != null) {
                sb.append(line).append("\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return sb.toString();
    }
}
