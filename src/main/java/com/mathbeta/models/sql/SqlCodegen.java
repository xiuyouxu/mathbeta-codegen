package com.mathbeta.models.sql;

import com.google.common.collect.Lists;
import com.mathbeta.models.KeyColumnFilterGenerator;
import com.mathbeta.models.common.CodeGeneratorAdapter;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.Statements;
import net.sf.jsqlparser.statement.create.table.ColDataType;
import net.sf.jsqlparser.statement.create.table.ColumnDefinition;
import net.sf.jsqlparser.statement.create.table.CreateTable;
import net.sf.jsqlparser.statement.create.table.Index;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by xiuyou.xu on 2017/7/18.
 */
public class SqlCodegen extends CodeGeneratorAdapter {
    public static void main(String[] args) {
        SqlCodegen codegen = new SqlCodegen(new File("D:\\mes-db.sql"), "mes_", "sql-code-gen");
        SqlModel model = codegen.read();
        codegen.generateCode(model, codegen.getInput().getParent() + "/" + codegen.getSubDir(), codegen.getTableNamePrefix());
    }

    @Override
    public KeyColumnFilterGenerator getKeyColumnFilterGenerator() {
        return null;
    }

    public File getInput() {
        return input;
    }

    public void setInput(File input) {
        this.input = input;
    }

    public String getSubDir() {
        return subDir;
    }

    public void setSubDir(String subDir) {
        this.subDir = subDir;
    }

    public String getTableNamePrefix() {
        return tableNamePrefix;
    }

    public void setTableNamePrefix(String tableNamePrefix) {
        this.tableNamePrefix = tableNamePrefix;
    }

    private File input;
    private String subDir;
    private String tableNamePrefix;

    public SqlCodegen(File input, String tableNamePrefix, String subDir) {
        this.input = input;
        this.tableNamePrefix = tableNamePrefix;
        this.subDir = subDir;

        new File(input.getParent(), subDir).mkdirs();
    }

    private String file2String(File input) {
        StringBuilder sb = new StringBuilder();
        try (BufferedReader br = new BufferedReader(new FileReader(input))) {
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
                        table.setName(removeQuotes(createTable.getTable().getName()));
                        List<?> options = createTable.getTableOptionsStrings();
                        if (options != null && !options.isEmpty()) {
                            for (int i = 0; i < options.size(); i++) {
                                if (options.get(i) instanceof String) {
                                    String option = (String) options.get(i);
                                    if ("comment".equalsIgnoreCase(option) && i < options.size() - 1) {
                                        table.setDescription(removeQuotes((String) options.get(i + 1)));
                                        break;
                                    }
                                }
                            }
                        }
                        List<SqlColumn> columns = Lists.newArrayList();
                        definitions.stream().forEach(def -> {
                            SqlColumn column = new SqlColumn();
                            column.setName(removeQuotes(def.getColumnName()));
                            ColDataType dataType = def.getColDataType();
                            column.setDataType(dataType.getDataType());
                            column.setNullable(true);
                            List<String> args = dataType.getArgumentsStringList();
                            if (args != null && !args.isEmpty()) {
                                column.setLength(Integer.parseInt(args.get(0)));
                            }
                            List<?> specs = def.getColumnSpecStrings();
                            if (specs != null && !specs.isEmpty()) {
                                for (int i = 0; i < specs.size(); i++) {
                                    String spec = (String) specs.get(i);
                                    if ("default".equalsIgnoreCase(spec)) {
                                        column.setDefaultValue(removeQuotes((String) specs.get(i + 1)));
                                    }
                                    if ("comment".equalsIgnoreCase(spec)) {
                                        column.setDescription(removeQuotes((String) specs.get(i + 1)));
                                    }
                                    if ("not".equalsIgnoreCase(spec) && "null".equalsIgnoreCase((String) specs.get(i + 1))) {
                                        column.setNullable(false);
                                    }
                                }
                            }

                            columns.add(column);
                        });
                        table.setColumns(columns);

                        List<SqlKey> keys = Lists.newArrayList();
                        List<Index> indexes = createTable.getIndexes();
                        if (indexes != null && !indexes.isEmpty()) {
                            indexes.stream().forEach(index -> {
                                SqlKey key = new SqlKey();
                                key.setName(index.getName());
//                                key.setDescription(index.getIndexSpec());
                                List<SqlColumn> keyColumns = Lists.newArrayList();
                                List<String> names = index.getColumnsNames();
                                if (names != null && !names.isEmpty()) {
                                    names.stream().forEach(name -> {
                                        SqlColumn column = new SqlColumn();
                                        column.setName(removeQuotes(name));

                                        keyColumns.add(column);
                                    });
                                }
                                key.setColumns(keyColumns);

                                keys.add(key);
                            });
                        }
                        table.setKeys(keys);

                        tables.add(table);
                    }
                }
            });
        }
        model.setTables(tables);
        return model;
    }

    private String removeQuotes(String name) {
        if (name != null && !name.trim().isEmpty()) {
            String n = name.trim();
            if (n.startsWith("`") && n.endsWith("`")) {
                return n.substring(1, n.length() - 1);
            }
            if (n.startsWith("'") && n.endsWith("'")) {
                return n.substring(1, n.length() - 1);
            }
        }
        return name;
    }
}
