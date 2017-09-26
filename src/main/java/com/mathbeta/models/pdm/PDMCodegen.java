package com.mathbeta.models.pdm;

import com.mathbeta.models.IModel;
import com.mathbeta.models.ColumnFilterGenerator;
import com.mathbeta.models.Table;
import com.mathbeta.models.common.CodeGeneratorAdapter;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.sax.SAXSource;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.List;
import java.util.function.Predicate;

/**
 * Created by xiuyou.xu on 2017/7/5.
 */
public class PDMCodegen extends CodeGeneratorAdapter {
    public static void main(String[] args) {
        PDMCodegen codegen = new PDMCodegen(new File("D:\\workspace\\MES5.0实施项目\\05 蓝图设计\\MES 5.0设计\\数据库设计\\mes-db.pdm"), "mes_", "pdm-code-gen", "com.mes");
        PDMModel model = codegen.read();
        codegen.generateCode(model, codegen.getInput().getParent() + "/" + codegen.getSubDir(), codegen.getTableNamePrefix(), codegen.getBasePackageName());
    }

    private File input;
    private String subDir;
    private String tableNamePrefix;
    private String basePackageName;

    public File getInput() {
        return input;
    }

    public void setInput(File input) {
        this.input = input;
    }

    public String getTableNamePrefix() {
        return tableNamePrefix;
    }

    public void setTableNamePrefix(String tableNamePrefix) {
        this.tableNamePrefix = tableNamePrefix;
    }

    public String getSubDir() {
        return subDir;
    }

    public void setSubDir(String subDir) {
        this.subDir = subDir;
    }

    public String getBasePackageName() {
        return basePackageName;
    }

    public void setBasePackageName(String basePackageName) {
        this.basePackageName = basePackageName;
    }

    public PDMCodegen(File input, String tableNamePrefix, String subDir, String basePackageName) {
        this.input = input;
        this.tableNamePrefix = tableNamePrefix;
        this.subDir = subDir;
        this.basePackageName = basePackageName;

        new File(input.getParent(), subDir).mkdirs();
    }

    public PDMModel read() {
        try {
            JAXBContext context = JAXBContext.newInstance(PDMModel.class);
            Unmarshaller unmarshaller = context.createUnmarshaller();

            final FileReader reader = new FileReader(input);
            final SAXParserFactory sax = SAXParserFactory.newInstance();
            // 忽略namespace
            sax.setNamespaceAware(false);
            final XMLReader xmlReader = sax.newSAXParser().getXMLReader();
            final SAXSource saxSource = new SAXSource(xmlReader, new InputSource(reader));

            PDMModel model = (PDMModel) unmarshaller.unmarshal(saxSource);
            return model;
        } catch (JAXBException e) {
            e.printStackTrace();
        } catch (SAXException e) {
            e.printStackTrace();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void addCheckName(IModel model) {
        List<Table> tables = model.getTables();
        if(tables!=null&&!tables.isEmpty()) {
            tables.stream().forEach(table -> {

            });
        }
    }

    @Override
    public ColumnFilterGenerator getColumnFilterGenerator() {
        return new ColumnFilterGenerator<PDMColumn, PDMKey>() {
            @Override
            public Predicate<PDMColumn> generateNonKeyColumnFilter(Table table) {
                return column -> {
                    // 过滤掉主键
                    if (table.getKeys() != null) {
                        return ((List<PDMKey>) table.getKeys()).stream().noneMatch(key -> {
                            return key.getColumns().stream().anyMatch(k -> {
                                return column.getId().equals(k.getRef());
                            });
                        });
                    }
                    return true;
                };
            }

            @Override
            public Predicate<PDMKey> determineColumnIsKeyFilter(PDMColumn column) {
                return key -> {
                    if (key.getColumns() != null) {
                        return key.getColumns().stream().anyMatch(c -> {
                            return c.getRef().equals(column.getId());
                        });
                    }
                    return false;
                };
            }

            @Override
            public Predicate<PDMColumn> generateEntityFieldsFilter() {
                return column -> {
                    String name = column.getName();
                    if ("id".equalsIgnoreCase(name) || "create_date".equalsIgnoreCase(name) || "update_date".equalsIgnoreCase(name)) {
                        return false;
                    }
                    return true;
                };
            }
        };
    }
}
