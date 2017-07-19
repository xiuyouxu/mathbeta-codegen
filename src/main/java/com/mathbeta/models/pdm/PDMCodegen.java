package com.mathbeta.models.pdm;

import com.mathbeta.models.ICodeGenerator;
import com.mathbeta.models.IModel;
import com.mathbeta.models.types.TypeMappingUtil;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.sax.SAXSource;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Created by xiuyou.xu on 2017/7/5.
 */
public class PDMCodegen implements ICodeGenerator {
    public static void main(String[] args) {
        PDMCodegen codegen = new PDMCodegen(new File("D:\\mes-db.pdm"), "mes_", "pdm-code-gen");
//        PDMCodegen codegen = new PDMCodegen(new File("d:/tables.xml"), "mes_");
        PDMModel model = codegen.read();
        codegen.generateCode(model);
    }

    @Override
    public void generateCode(IModel model) {
        PDMModel pdmModel = (PDMModel) model;
        genEntity(pdmModel);
        genRestful(pdmModel);
        genDubbo(pdmModel);
        genMapper(pdmModel);

        genTableDescriptions(pdmModel);
        genTableCreateSql(pdmModel);
    }

    private File input;
    private String tableNamePrefix;
    private String subDir;

    public PDMCodegen(File input, String tableNamePrefix, String subDir) {
        this.input = input;
        this.tableNamePrefix = tableNamePrefix;
        this.subDir = subDir;

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

    private void check(PDMModel model) {
        if (model == null) {
            throw new RuntimeException("model is null");
        }
    }

    /**
     * 创建表名和描述之间的对应关系
     *
     * @param pdmModel
     */
    private void genTableDescriptions(PDMModel pdmModel) {
        check(pdmModel);

        try {
            File file = new File(input.getParent() + "/" + subDir, "tables.xls");
            BufferedWriter bw = new BufferedWriter(new FileWriter(file));
//            bw.append("Table Name").append("\t").append("Table Description").append("\r\n");

            List<PDMTable> tables = pdmModel.getTables();
            if (tables != null && !tables.isEmpty()) {
                tables.stream().forEach(table -> {
                    try {
                        bw.append(table.getName()).append("\t").append(table.getDescription()).append("\r\n");
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });
            }
            bw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 生成创建表sql语句
     *
     * @param pdmModel
     */
    private void genTableCreateSql(PDMModel pdmModel) {
        check(pdmModel);

        try {
            File file = new File(input.getParent() + "/" + subDir, "mes.sql");
            BufferedWriter bw = new BufferedWriter(new FileWriter(file));

            List<PDMTable> tables = pdmModel.getTables();
            if (tables != null && !tables.isEmpty()) {
                tables.stream().forEach(table -> {
                    try {
                        bw.append("DROP TABLE IF EXISTS `").append(table.getName()).append("`;\r\n");
                        bw.append("CREATE TABLE `").append(table.getName()).append("` (\r\n");

                        List<PDMKey> keys = table.getKeys();
                        List<PDMColumn> columns = table.getColumns();
                        if (columns != null && !columns.isEmpty()) {
                            List<String> list = columns.stream().map(column -> {
                                StringBuilder sb = new StringBuilder();
                                sb.append("  `").append(column.getName()).append("`\t").append(column.getDataType());
                                boolean isKey = false;
                                if (keys != null && !keys.isEmpty()) {
                                    isKey = keys.stream().anyMatch(key -> {
                                        if (key.getColumns() != null) {
                                            return key.getColumns().stream().anyMatch(c -> {
                                                return c.getRef().equals(column.getId());
                                            });
                                        }
                                        return false;
                                    });
                                }
                                if (isKey) {
                                    sb.append(" NOT NULL");
                                }
                                sb.append(" comment '").append(column.getDescription()).append("'");
                                return sb.toString();
                            }).collect(Collectors.toList());
                            bw.append(String.join(",\r\n", list));

                            if (keys != null && !keys.isEmpty()) {
                                String keyNames = String.join(", ", keys.stream().map(key -> {
                                    if (key.getColumns() != null) {
                                        return String.join(", ", key.getColumns().stream().map(column -> {
                                            return columns.stream().filter(c -> {
                                                return c.getId().equals(column.getRef());
                                            }).map(c -> {
                                                return "`" + c.getName() + "`";
                                            }).findFirst().get();
                                        }).collect(Collectors.toList()));
                                    }
                                    return "";
                                }).collect(Collectors.toList()));
                                if (keyNames != null && !keyNames.isEmpty()) {
                                    bw.append(",\r\n  PRIMARY KEY (").append(keyNames).append(")");
                                }
                            }
                        }

                        bw.append("\r\n) comment '").append(table.getDescription()).append("';\r\n\r\n");
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });
            }
            bw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void genEntity(PDMModel pdmModel) {
        check(pdmModel);
        new File(input.getParent(), subDir + "/entity").mkdirs();

        // load type mappings
        Map<String, Map<String, String>> mappings = TypeMappingUtil.getMapping();
        Map<String, String> mapping = mappings.get("mysql");

        List<PDMTable> tables = pdmModel.getTables();
        if (tables != null && !tables.isEmpty()) {
            tables.stream().forEach(table -> {
                String entityName = getEntityName(table.getName(), true);
                File file = new File(input.getParent() + "/" + subDir + "/entity", entityName + ".java");
                try (BufferedWriter bw = new BufferedWriter(new FileWriter(file))) {
                    bw.append("import com.mes.common.framework.domain.TrackableEntity;\r\n");
                    bw.append("import io.swagger.annotations.ApiModel;\r\n");
                    bw.append("import io.swagger.annotations.ApiModelProperty;\r\n\r\n");
                    bw.append("/**\r\n");
                    bw.append(" * ").append(table.getDescription()).append("\r\n");
                    bw.append("*/\r\n");
                    bw.append("@ApiModel(value = \"").append(entityName).append("\", description = \"").append(table.getDescription()).append("\")\r\n");
                    bw.append("public class ").append(entityName).append(" extends TrackableEntity {\r\n");
                    List<PDMColumn> columns = table.getColumns();
                    if (columns != null && !columns.isEmpty()) {
                        columns.stream().filter(column -> {
                            String code = column.getName();
                            if ("id".equalsIgnoreCase(code) || "create_date".equalsIgnoreCase(code) || "update_date".equalsIgnoreCase(code)) {
                                return false;
                            }
                            return true;
                        }).forEach(column -> {
                            try {
                                String type = column.getDataType();
                                if (type == null) {
                                    return;
                                }
                                if (type.contains("(")) {
                                    type = type.substring(0, type.indexOf("("));
                                }
                                String javaType = mapping.get(type);
                                String field = getFieldName(column.getName(), false);
                                bw.append("\t@ApiModelProperty(value = \"").append(column.getDescription()).append("\")\r\n");
                                bw.append("\tprivate ").append(javaType).append(" ").append(field).append(";\r\n");
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        });
                        bw.append("\r\n\r\n");
                        columns.stream().filter(column -> {
                            String code = column.getName();
                            if ("id".equalsIgnoreCase(code) || "create_date".equalsIgnoreCase(code) || "update_date".equalsIgnoreCase(code)) {
                                return false;
                            }
                            return true;
                        }).forEach(column -> {
                            try {
                                String type = column.getDataType();
                                if (type == null) {
                                    return;
                                }
                                if (type.contains("(")) {
                                    type = type.substring(0, type.indexOf("("));
                                }
                                String javaType = mapping.get(type);
                                String field = getFieldName(column.getName(), false);
                                String getterSetterName = getEntityName(column.getName(), false);
                                // generate getter, setter
                                bw.append("\tpublic ").append(javaType).append(" get").append(getterSetterName).append("() {\r\n").append("\t\treturn ").append(field).append(";\r\n").append("\t}\r\n");
                                bw.append("\tpublic void set").append(getterSetterName).append("(").append(javaType).append(" ").append(field).append(") {\r\n").append("\t\tthis.").append(field).append(" = ").append(field).append(";\r\n").append("\t}\r\n");
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        });
                    }
                    bw.append("}\r\n");
                    bw.flush();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
        }
    }

    private String getEntityName(String name, boolean hasPrefix) {
        if (hasPrefix && name.startsWith(tableNamePrefix)) {
            name = name.substring(tableNamePrefix.length());
        }
        String[] names = name.split("_");
        StringBuilder sb = new StringBuilder();
        if (names != null && names.length > 0) {
            for (int i = 0; i < names.length; i++) {
                sb.append(camel(names[i]));
            }
        }
        return sb.toString();
    }

    private String getFieldName(String name, boolean hasPrefix) {
        if (hasPrefix && name.startsWith(tableNamePrefix)) {
            name = name.substring(tableNamePrefix.length());
        }
        String[] names = name.split("_");
        StringBuilder sb = new StringBuilder();
        if (names != null && names.length > 0) {
            sb.append(names[0]);
            for (int i = 1; i < names.length; i++) {
                sb.append(camel(names[i]));
            }
        }
        return sb.toString();
    }

    private String camel(String name) {
        return String.valueOf(name.charAt(0)).toUpperCase() + name.substring(1);
    }

    public void genRestful(PDMModel pdmModel) {
        check(pdmModel);
        new File(input.getParent(), subDir + "/restful").mkdirs();

        List<PDMTable> tables = pdmModel.getTables();
        if (tables != null && !tables.isEmpty()) {
            File constantFile = new File(input.getParent() + "/" + subDir + "/restful/RestConstants.java");
            BufferedWriter bufferedWriter = null;
            try {
                bufferedWriter = new BufferedWriter(new FileWriter(constantFile));
            } catch (IOException e) {
                e.printStackTrace();
            }
            final BufferedWriter constantBw = bufferedWriter;
            tables.stream().forEach(table -> {
                String entityName = getEntityName(table.getName(), true);
                if (constantBw != null) {
                    try {
                        constantBw.append("public static final String ").append(entityName.toUpperCase()).append(" = \"").append(entityName.toLowerCase()).append("\";\r\n");
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

                File file = new File(input.getParent() + "/" + subDir + "/restful", entityName + "RestServer.java");
                try (BufferedWriter bw = new BufferedWriter(new FileWriter(file))) {
                    bw.append("import com.mes.common.framework.rest.impl.BaseRestServerInterfaceImpl;\r\n");
                    bw.append("import com.mes.dubbo.consumer.ControlConsumer;\r\n");
                    bw.append("import com.mes.dubbo.interprovider.control.I").append(entityName).append("Provider;\r\n");
                    bw.append("import com.mes.entity.control.").append(entityName).append(";\r\n");
                    bw.append("import com.mes.utils.RestConstants;\r\n");
                    bw.append("import io.swagger.annotations.Api;\r\n\r\n");
                    bw.append("import javax.ws.rs.Path;\r\n\r\n");
                    bw.append("/**\r\n");
                    bw.append(" * ").append(table.getDescription()).append("\r\n");
                    bw.append("*/\r\n");
                    bw.append("@Api(value = \"").append(table.getDescription()).append("\", description = \"").append(table.getDescription()).append("\")\r\n");
                    bw.append("@Path(RestConstants.RestPathPrefix.").append(entityName.toUpperCase()).append(")\r\n");
                    bw.append("public class ").append(entityName).append("RestServer extends BaseRestServerInterfaceImpl<").append(entityName).append("> {\r\n");
                    bw.append("\t@Override\r\n");
                    bw.append("\tpublic I").append(entityName).append("Provider getDubboBaseInterface() {\r\n");
                    bw.append("\t\treturn ControlConsumer.get").append(entityName).append("Provider();\r\n");
                    bw.append("\t}\r\n");
                    bw.append("}\r\n");
                    bw.flush();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
            if (constantBw != null) {
                try {
                    constantBw.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void genDubbo(PDMModel pdmModel) {
        check(pdmModel);
        new File(input.getParent(), subDir + "/dubbo/impl").mkdirs();

        List<PDMTable> tables = pdmModel.getTables();
        if (tables != null && !tables.isEmpty()) {
            tables.stream().forEach(table -> {
                String entityName = getEntityName(table.getName(), true);
                String field = getFieldName(table.getName(), true);
                // dubbo interface
                File file = new File(input.getParent() + "/" + subDir + "/dubbo", "I" + entityName + "Provider.java");
                try (BufferedWriter bw = new BufferedWriter(new FileWriter(file))) {
                    bw.append("import com.mes.common.framework.dubbo.DubboBaseInterface;\r\n");
                    bw.append("import com.mes.entity.control.").append(entityName).append(";\r\n\r\n");
                    bw.append("/**\r\n");
                    bw.append(" * ").append(table.getDescription()).append("\r\n");
                    bw.append("*/\r\n");
                    bw.append("public interface I").append(entityName).append("Provider extends DubboBaseInterface<").append(entityName).append("> {\r\n");
                    bw.append("}\r\n");
                    bw.flush();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                // dubbo impl
                File impl = new File(input.getParent() + "/" + subDir + "/dubbo/impl", entityName + "ProviderImpl.java");
                try (BufferedWriter bw = new BufferedWriter(new FileWriter(impl))) {
                    bw.append("import com.mes.control.mapper.").append(entityName).append("Mapper;\r\n");
                    bw.append("import com.mes.dubbo.interprovider.control.I").append(entityName).append("Provider;\r\n");
                    bw.append("import com.mes.entity.control.").append(entityName).append(";\r\n");
                    bw.append("import org.springframework.beans.factory.annotation.Autowired;\r\n");
                    bw.append("import org.springframework.beans.factory.annotation.Qualifier;\r\n\r\n");
                    bw.append("/**\r\n");
                    bw.append(" * ").append(table.getDescription()).append("\r\n");
                    bw.append("*/\r\n");
                    bw.append("public class ").append(entityName).append("ProviderImpl extends BaseProviderImpl<").append(entityName).append("> implements I").append(entityName).append("Provider {\r\n");
                    bw.append("\t@Autowired\r\n");
                    bw.append("\t@Qualifier(\"").append(field).append("Mapper\")\r\n");
                    bw.append("\tprivate ").append(entityName).append("Mapper ").append(field).append("Mapper;\r\n\r\n");
                    bw.append("\t@Override\r\n");
                    bw.append("\tpublic ").append(entityName).append("Mapper getBaseInterfaceMapper() {\r\n");
                    bw.append("\t\treturn ").append(field).append("Mapper;\r\n");
                    bw.append("\t}\r\n");
                    bw.append("}\r\n");
                    bw.flush();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });

            // consumer getter method
            File consumer1 = new File(input.getParent() + "/" + subDir + "/dubbo", "Consumer.java");
            try (BufferedWriter bw = new BufferedWriter(new FileWriter(consumer1))) {
                tables.stream().forEach(table -> {
                    String entityName = getEntityName(table.getName(), true);
                    String field = getFieldName(table.getName(), true);

                    try {
                        bw.append("\t/**\r\n");
                        bw.append("\t * ").append(table.getDescription()).append("\r\n");
                        bw.append("\t*/\r\n");
                        bw.append("\tpublic static I").append(entityName).append("Provider get").append(entityName).append("Provider() {\r\n");
                        bw.append("\t\tI").append(entityName).append("Provider ").append(field).append("Provider = null;\r\n");
                        bw.append("\ttry {\r\n");
                        bw.append("\t\t\t").append(field).append("Provider = (I").append(entityName).append("Provider) ServiceBeanContext.getInstance().getBean(\"").append(field).append("Provider\");\r\n");
                        bw.append("\t\t} catch (Exception e) {\r\n");
                        bw.append("\t\t\tlog.error(\"获取 get").append(entityName).append("Provider ServiceBean 失败！  \", e);\r\n");
                        bw.append("\t\t}\r\n");
                        bw.append("\t\treturn ").append(field).append("Provider;\r\n");
                        bw.append("\t}\r\n\r\n");
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });

                bw.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }

            // consumer xml
            File consumer2 = new File(input.getParent() + "/" + subDir + "/dubbo", "consumer.xml");
            try (BufferedWriter bw = new BufferedWriter(new FileWriter(consumer2))) {
                tables.stream().forEach(table -> {
                    String entityName = getEntityName(table.getName(), true);
                    String field = getFieldName(table.getName(), true);

                    try {
                        bw.append("\t<!-- ").append(table.getDescription()).append(" -->\r\n");
                        bw.append("\t<dubbo:reference id=\"").append(field).append("Provider\" interface=\"com.mes.dubbo.interprovider.control.I").append(entityName).append("Provider\" timeout=\"20000\"/>\r\n\r\n");
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });

                bw.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }

            // provider xml
            File provider = new File(input.getParent() + "/" + subDir + "/dubbo", "provider.xml");
            try (BufferedWriter bw = new BufferedWriter(new FileWriter(provider))) {
                tables.stream().forEach(table -> {
                    String entityName = getEntityName(table.getName(), true);
                    String field = getFieldName(table.getName(), true);

                    try {
                        bw.append("\t<!-- ").append(table.getDescription()).append(" -->\r\n");
                        bw.append("\t<bean id=\"").append(field).append("Provider\" class=\"com.mes.control.provider.").append(entityName).append("ProviderImpl\"/>\r\n");
                        bw.append("\t<dubbo:service interface=\"com.mes.dubbo.interprovider.control.I").append(entityName).append("Provider\" ref=\"").append(field).append("Provider\" retries=\"0\" protocol=\"dubbo\"/>\r\n\r\n");
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });

                bw.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void genMapper(PDMModel pdmModel) {
        check(pdmModel);
        new File(input.getParent(), subDir + "/mapper/xml").mkdirs();

        List<PDMTable> tables = pdmModel.getTables();
        if (tables != null && !tables.isEmpty()) {
            tables.stream().forEach(table -> {
                String entityName = getEntityName(table.getName(), true);
                String field = getFieldName(table.getName(), true);
                // mapper interface
                File file = new File(input.getParent() + "/" + subDir + "/mapper", entityName + "Mapper.java");
                try (BufferedWriter bw = new BufferedWriter(new FileWriter(file))) {
                    bw.append("import com.mes.common.framework.mapper.BaseInterfaceMapper;\r\n");
                    bw.append("import com.mes.entity.control.Dept;\r\n\r\n");
                    bw.append("/**\r\n");
                    bw.append(" * ").append(table.getDescription()).append("\r\n");
                    bw.append("*/\r\n");
                    bw.append("public interface ").append(entityName).append("Mapper extends BaseInterfaceMapper<").append(entityName).append("> {\r\n");
                    bw.append("}\r\n");
                    bw.flush();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                // mapper.xml
                File xml = new File(input.getParent() + "/" + subDir + "/mapper/xml", entityName + "Mapper.xml");
                try (BufferedWriter bw = new BufferedWriter(new FileWriter(xml))) {
                    bw.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                            "<!DOCTYPE mapper PUBLIC \"-//mybatis.org//DTD Mapper 3.0//EN\"\n" +
                            "        \"http://mybatis.org/dtd/mybatis-3-mapper.dtd\">\r\n\r\n");
                    bw.append("<!-- ").append(table.getDescription()).append(" -->\r\n");
                    bw.append("<mapper namespace=\"com.mes.control.mapper.").append(entityName).append("Mapper\">\r\n");
                    // result map
                    bw.append("\t<resultMap type=\"com.mes.entity.control.").append(entityName).append("\" id=\"").append(field).append("ResultMap\">\r\n");
                    List<PDMColumn> columns = table.getColumns();
                    if (columns != null && !columns.isEmpty()) {
                        columns.stream().forEach(column -> {
                            try {
                                String type = column.getDataType();
                                if (type == null) {
                                    return;
                                }
                                if (type != null && type.contains("(")) {
                                    type = type.substring(0, type.indexOf("("));
                                }
                                if ("datetime".equalsIgnoreCase(type)) {
                                    type = "date";
                                }
                                if ("int".equalsIgnoreCase(type)) {
                                    type = "INTEGER";
                                }
                                if ("varchar".equalsIgnoreCase(type) || type.toLowerCase().contains("text")) {
                                    type = "VARCHAR";
                                }
                                bw.append("\t\t<result column=\"").append(column.getName()).append("\" property=\"").append(getFieldName(column.getName(), false)).append("\" jdbcType=\"").append(type.toUpperCase()).append("\"/>\r\n");
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        });
                    }
                    bw.append("\t</resultMap>\r\n\r\n");

                    // find by id
                    bw.append("\t<select id=\"findById\" resultMap=\"").append(field).append("ResultMap\">\r\n");
                    bw.append("\t\t<include refid=\"sql_select\"/>\r\n");
                    bw.append("\t\twhere s.id = #{id}\r\n");
                    bw.append("\t</select>\r\n\r\n");

                    // save
                    bw.append("\t<!--新增操作 -->\r\n");
                    bw.append("\t<insert id=\"save\" parameterType=\"com.mes.entity.control.").append(entityName).append("\">\r\n");
                    bw.append("\t\tinsert into ").append(table.getName()).append(" (");
                    if (columns != null && !columns.isEmpty()) {
                        bw.append(String.join(",", columns.stream().map(column -> {
                            return column.getName();
                        }).collect(Collectors.toList())));
                    }
                    bw.append(")\r\n\t\tvalues (");
                    if (columns != null && !columns.isEmpty()) {
                        List<String> names = new ArrayList<>();
                        bw.append(String.join(",", columns.stream().map(column -> {
                            return "#{" + getFieldName(column.getName(), false) + "}";
                        }).collect(Collectors.toList())));
                    }
                    bw.append(")\r\n");
                    bw.append("\t</insert>\r\n\r\n");
                    bw.append("\t<!--更新操作-->\r\n");
                    bw.append("\t<update id=\"update\" parameterType=\"com.mes.entity.control.").append(entityName).append("\">\r\n");
                    bw.append("\t\tupdate ").append(table.getName()).append(" s <include refid=\"sql_update\"/> where s.id = #{id}\r\n");
                    bw.append("\t</update>\r\n\r\n");
                    bw.append("\t<!--根据id删除-->\r\n");
                    bw.append("\t<delete id=\"deleteById\" parameterType=\"java.lang.String\">\r\n");
                    bw.append("\t\tdelete from ").append(table.getName()).append(" where id =#{id}\r\n");
                    bw.append("\t</delete>\r\n\r\n");
                    bw.append("\t<!--获取数据条数-->\r\n");
                    bw.append("\t<select id=\"getCount\" parameterType=\"java.util.Map\" resultType=\"int\">\r\n");
                    bw.append("\t\tselect count(1) from ").append(table.getName()).append(" t <include refid=\"sql_where_and_equal\"/>\r\n");
                    bw.append("\t</select>\r\n\r\n");
                    bw.append("\t<!--分页查询-->\r\n");
                    bw.append("\t<select id=\"findByPage\" parameterType=\"java.util.Map\" resultMap=\"").append(field).append("ResultMap\">\r\n");
                    bw.append("\t\t<include refid=\"sql_select\"/> <include refid=\"sql_where_and_equal\"/> order by s.create_date desc limit #{startRowNum},#{pageSize}\r\n");
                    bw.append("\t</select>\r\n\r\n");
                    bw.append("\t<!--根据条件查询-->\r\n");
                    bw.append("\t<select id=\"findByMap\" parameterType=\"java.util.Map\" resultMap=\"").append(field).append("ResultMap\">\r\n");
                    bw.append("\t\t<include refid=\"sql_select\"/> <include refid=\"sql_where_and_equal\"/> order by s.create_date desc\r\n");
                    bw.append("\t</select>\r\n\r\n");
                    bw.append("\t<!--查询所有-->\r\n");
                    bw.append("\t<select id=\"findAll\" resultMap=\"").append(field).append("ResultMap\">\r\n");
                    bw.append("\t\t<include refid=\"sql_select\"/> <include refid=\"sql_where_and_equal\"/> order by s.create_date desc\r\n");
                    bw.append("\t</select>\r\n\r\n");
                    bw.append("\t<!--查询字段-->\r\n");
                    bw.append("\t<sql id=\"sql_select\">\r\n");
                    bw.append("\t\tSELECT ");
                    if (columns != null && !columns.isEmpty()) {
                        bw.append(String.join(",", columns.stream().map(column -> {
                            return column.getName();
                        }).collect(Collectors.toList())));
                    }
                    bw.append(" from ").append(table.getName()).append(" s\r\n");
                    bw.append("\t</sql>\r\n\r\n");
                    bw.append("\t<!--查询条件-->\r\n");
                    bw.append("\t<sql id=\"sql_where_and_equal\">\r\n");
                    bw.append("\t\t<where>\r\n");
                    if (columns != null && !columns.isEmpty()) {
                        // 添加模糊查询
                        bw.append("\t\t\t<if test=\"search != null and search != '' \">\r\n");
                        bw.append("\t\t\t\tand (\r\n");
                        List<String> likes = columns.stream().filter(column -> {
                            // 过滤掉主键
                            if (table.getKeys() != null) {
                                return table.getKeys().stream().noneMatch(key -> {
                                    return key.getColumns().stream().anyMatch(k -> {
                                        return column.getId().equals(k.getRef());
                                    });
                                });
                            }
                            return true;
                        }).map(column -> {
                            return column.getName() + " like CONCAT('%', #{search}, '%')";
                        }).collect(Collectors.toList());
                        bw.append("\t\t\t\t").append(String.join("\r\n\t\t\t\tor ", likes));
                        bw.append("\r\n\t\t\t\t)\r\n\t\t\t</if>\r\n");

                        columns.stream().filter(column -> {
                            // 过滤掉主键
                            if (table.getKeys() != null) {
                                return table.getKeys().stream().noneMatch(key -> {
                                    return key.getColumns().stream().anyMatch(k -> {
                                        return column.getId().equals(k.getRef());
                                    });
                                });
                            }
                            return true;
                        }).forEach(column -> {
                            try {
                                String fieldName = getFieldName(column.getName(), false);
                                bw.append("\t\t\t<if test=\"").append(fieldName).append(" != null  and ").append(fieldName).append(" != '' \">\r\n");
                                bw.append("\t\t\t\t<![CDATA[\r\n");
//                                bw.append("\t\t\t\t\tand ").append(column.getDescription()).append(" LIKE CONCAT('%', #{").append(fieldName).append("}, '%')\r\n");
                                bw.append("\t\t\t\t\tand ").append(column.getName()).append(" = #{").append(fieldName).append("}\r\n");
                                bw.append("\t\t\t\t]]>\r\n");
                                bw.append("\t\t\t</if>\r\n");
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        });
                    }
                    bw.append("\t\t</where>\r\n");
                    bw.append("\t</sql>\r\n\r\n");
                    bw.append("\t<!--查询条件-->\r\n");
                    bw.append("\t<sql id=\"sql_where_or_equal\">\r\n");
                    bw.append("\t\t<where>\r\n");
                    if (columns != null && !columns.isEmpty()) {
                        columns.stream().filter(column -> {
                            // 过滤掉主键
                            if (table.getKeys() != null) {
                                return table.getKeys().stream().noneMatch(key -> {
                                    return key.getColumns().stream().anyMatch(k -> {
                                        return column.getId().equals(k.getRef());
                                    });
                                });
                            }
                            return true;
                        }).forEach(column -> {
                            try {
                                String fieldName = getFieldName(column.getName(), false);
                                bw.append("\t\t\t<if test=\"").append(fieldName).append(" != null  and ").append(fieldName).append(" != '' \">\r\n");
                                bw.append("\t\t\t\t<![CDATA[\r\n");
//                                bw.append("\t\t\t\t\tand ").append(column.getDescription()).append(" LIKE CONCAT('%', #{").append(fieldName).append("}, '%')\r\n");
                                bw.append("\t\t\t\t\tor ").append(column.getName()).append(" = #{").append(fieldName).append("}\r\n");
                                bw.append("\t\t\t\t]]>\r\n");
                                bw.append("\t\t\t</if>\r\n");
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        });
                    }
                    bw.append("\t\t</where>\r\n");
                    bw.append("\t</sql>\r\n\r\n");
                    bw.append("\t<!--查询条件-->\r\n");
                    bw.append("\t<sql id=\"sql_where_and_like\">\r\n");
                    bw.append("\t\t<where>\r\n");
                    if (columns != null && !columns.isEmpty()) {
                        columns.stream().filter(column -> {
                            // 过滤掉主键
                            if (table.getKeys() != null) {
                                return table.getKeys().stream().noneMatch(key -> {
                                    return key.getColumns().stream().anyMatch(k -> {
                                        return column.getId().equals(k.getRef());
                                    });
                                });
                            }
                            return true;
                        }).forEach(column -> {
                            try {
                                String fieldName = getFieldName(column.getName(), false);
                                bw.append("\t\t\t<if test=\"").append(fieldName).append(" != null  and ").append(fieldName).append(" != '' \">\r\n");
                                bw.append("\t\t\t\t<![CDATA[\r\n");
//                                bw.append("\t\t\t\t\tand ").append(column.getDescription()).append(" LIKE CONCAT('%', #{").append(fieldName).append("}, '%')\r\n");
                                bw.append("\t\t\t\t\tand ").append(column.getName()).append(" like CONCAT('%', #{").append(fieldName).append("}, '%')\r\n");
                                bw.append("\t\t\t\t]]>\r\n");
                                bw.append("\t\t\t</if>\r\n");
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        });
                    }
                    bw.append("\t\t</where>\r\n");
                    bw.append("\t</sql>\r\n\r\n");
                    bw.append("\t<!--查询条件-->\r\n");
                    bw.append("\t<sql id=\"sql_where_or_like\">\r\n");
                    bw.append("\t\t<where>\r\n");
                    if (columns != null && !columns.isEmpty()) {
                        columns.stream().filter(column -> {
                            // 过滤掉主键
                            if (table.getKeys() != null) {
                                return table.getKeys().stream().noneMatch(key -> {
                                    return key.getColumns().stream().anyMatch(k -> {
                                        return column.getId().equals(k.getRef());
                                    });
                                });
                            }
                            return true;
                        }).forEach(column -> {
                            try {
                                String fieldName = getFieldName(column.getName(), false);
                                bw.append("\t\t\t<if test=\"").append(fieldName).append(" != null  and ").append(fieldName).append(" != '' \">\r\n");
                                bw.append("\t\t\t\t<![CDATA[\r\n");
//                                bw.append("\t\t\t\t\tand ").append(column.getDescription()).append(" LIKE CONCAT('%', #{").append(fieldName).append("}, '%')\r\n");
                                bw.append("\t\t\t\t\tor ").append(column.getName()).append(" like CONCAT('%', #{").append(fieldName).append("}, '%')\r\n");
                                bw.append("\t\t\t\t]]>\r\n");
                                bw.append("\t\t\t</if>\r\n");
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        });
                    }
                    bw.append("\t\t</where>\r\n");
                    bw.append("\t</sql>\r\n\r\n");
                    bw.append("\t<!--更新操作-->\r\n");
                    bw.append("\t<sql id=\"sql_update\">\r\n");
                    bw.append("\t\t<set>\r\n");
                    if (columns != null && !columns.isEmpty()) {
                        columns.stream().filter(column -> {
                            if (table.getKeys() != null) {
                                return table.getKeys().stream().noneMatch(key -> {
                                    return key.getColumns().stream().anyMatch(k -> {
                                        return column.getId().equals(k.getRef());
                                    });
                                });
                            }
                            return true;
                        }).forEach(column -> {
                            try {
                                String fieldName = getFieldName(column.getName(), false);
                                bw.append("\t\t\t<if test=\"").append(fieldName).append(" != null  and ").append(fieldName).append(" != '' \">\r\n");
                                bw.append("\t\t\t\t<![CDATA[\r\n");
                                bw.append("\t\t\t\t\t").append(column.getName()).append(" = #{").append(fieldName).append("},\r\n");
                                bw.append("\t\t\t\t]]>\r\n");
                                bw.append("\t\t\t</if>\r\n");
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        });
                    }
                    bw.append("\t\t</set>\r\n");
                    bw.append("\t</sql>\r\n");

                    bw.append("</mapper>\r\n");
                    bw.flush();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
        }
    }
}
