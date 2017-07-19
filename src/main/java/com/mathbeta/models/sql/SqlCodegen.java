package com.mathbeta.models.sql;

import com.mathbeta.models.ICodeGenerator;
import com.mathbeta.models.IModel;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.Statements;
import net.sf.jsqlparser.statement.create.table.ColumnDefinition;
import net.sf.jsqlparser.statement.create.table.CreateTable;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by xiuyou.xu on 2017/7/18.
 */
public class SqlCodegen implements ICodeGenerator {
    private String input;
    private String tableNamePrefix;

    public SqlCodegen(String input, String tableNamePrefix) {
        this.input = input;
        this.tableNamePrefix = tableNamePrefix;

        new File(new File(input).getParent(), "sql-code-gen").mkdirs();
    }

    public static void main(String[] args) {
        SqlCodegen codegen = new SqlCodegen("D:\\mes-db.sql", "mes_");
        SqlModel model = codegen.read();
        codegen.generateCode(model);
    }

    private String file2String(String path) {
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

    private SqlModel read() {
        SqlModel model = new SqlModel();
        List<SqlTable> tables = new ArrayList<>();

        String sqls = file2String(input);
        Statements statements = null;
        try {
            statements = CCJSqlParserUtil.parseStatements(sqls);
        } catch (JSQLParserException e) {
            e.printStackTrace();
        }
        List<Statement> list = statements.getStatements();
        if (list != null && !list.isEmpty()) {
            list.stream().forEach(stmt -> {
                if (stmt instanceof CreateTable) {
                    CreateTable createTable = (CreateTable) stmt;
                    List<ColumnDefinition> definitions = createTable.getColumnDefinitions();
                    if (definitions != null && !definitions.isEmpty()) {
                        SqlTable table = new SqlTable();
                        table.setName(createTable.getTable().getName());
                        List<?> options = createTable.getTableOptionsStrings();
                        if(options!=null&&!options.isEmpty()) {
                            for(int i=0;i<options.size();i++) {
                                if(options.get(i) instanceof String) {
                                    String option = (String) options.get(i);
                                    if("comment".equalsIgnoreCase(option) && i<options.size()-1) {

                                    }
                                }
                            }
                        }
//                        table.setDescription(createTable.getTableOptionsStrings());
                        System.out.println(createTable.getTable().getName());
                        definitions.stream().forEach(def -> {
                            System.out.println(def.getColumnName() + "->" + def.getColDataType() + ", " + def.getColumnSpecStrings());
                        });
                        System.out.println("===========================================================");
                    }
                }
            });
        }
        model.setTables(tables);
        return model;
    }

    @Override
    public void generateCode(IModel model) {

    }
}
