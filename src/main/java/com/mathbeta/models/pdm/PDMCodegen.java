package com.mathbeta.models.pdm;

import com.mathbeta.models.ICodeGen;
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
public class PDMCodegen implements ICodeGen {
    public static void main(String[] args) {
        PDMCodegen codegen = new PDMCodegen(new File("D:\\documents\\工作文档\\项目文档\\MES\\mes-db.pdm"), "mes_");
//        PDMCodegen codegen = new PDMCodegen(new File("d:/tables.xml"), "mes_");
        codegen.read();
        codegen.genEntity();
        codegen.genRestful();
        codegen.genDubbo();
        codegen.genMapper();
    }

    private File input;
    private String tableNamePrefix;
    private PDMModel model;

    public PDMCodegen(File input, String tableNamePrefix) {
        this.input = input;
        this.tableNamePrefix = tableNamePrefix;

        new File(input.getParent(), "pdm-code-gen").mkdirs();
    }

    public void read() {
        if (model == null) {
            try {
                JAXBContext context = JAXBContext.newInstance(PDMModel.class);
                Unmarshaller unmarshaller = context.createUnmarshaller();

                final FileReader reader = new FileReader(input);
                final SAXParserFactory sax = SAXParserFactory.newInstance();
                // 忽略namespace
                sax.setNamespaceAware(false);
                final XMLReader xmlReader = sax.newSAXParser().getXMLReader();
                final SAXSource saxSource = new SAXSource(xmlReader, new InputSource(reader));

                model = (PDMModel) unmarshaller.unmarshal(saxSource);
            } catch (JAXBException e) {
                e.printStackTrace();
            } catch (SAXException e) {
                e.printStackTrace();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (ParserConfigurationException e) {
                e.printStackTrace();
            }
        }
    }

    private void check() {
        if (model == null) {
            throw new RuntimeException("model is null");
        }
    }

    @Override
    public void genEntity() {
        check();
        new File(input.getParent(), "pdm-code-gen/entity").mkdirs();

        // load type mappings
        Map<String, Map<String, String>> mappings = TypeMappingUtil.getMapping();
        Map<String, String> mapping = mappings.get("mysql");

        List<PDMTable> tables = model.getRootObject().getChildren().get(0).getTables();
        if (tables != null && !tables.isEmpty()) {
            tables.stream().forEach(table -> {
                String entityName = getEntityName(table.getCode(), true);
                File file = new File(input.getParent() + "/pdm-code-gen/entity", entityName + ".java");
                try (BufferedWriter bw = new BufferedWriter(new FileWriter(file))) {
                    bw.append("@ApiModel(value = \"").append(entityName).append("\")\r\n");
                    bw.append("public class ").append(entityName).append(" extends TrackableEntity {\r\n");
                    List<PDMColumn> columns = table.getColumns();
                    if (columns != null && !columns.isEmpty()) {
                        columns.stream().filter(column -> {
                            String code = column.getCode();
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
                                String field = getFieldName(column.getCode(), false);
                                bw.append("\t@ApiModelProperty(value = \"").append(column.getName()).append("\")\r\n");
                                bw.append("\tprivate ").append(javaType).append(" ").append(field).append(";\r\n");
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        });
                        bw.append("\r\n\r\n");
                        columns.stream().filter(column -> {
                            String code = column.getCode();
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
                                String field = getFieldName(column.getCode(), false);
                                String getterSetterName = getEntityName(column.getCode(), false);
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

    @Override
    public void genRestful() {
        check();
        new File(input.getParent(), "pdm-code-gen/restful").mkdirs();

        List<PDMTable> tables = model.getRootObject().getChildren().get(0).getTables();
        if (tables != null && !tables.isEmpty()) {
            tables.stream().forEach(table -> {
                String entityName = getEntityName(table.getCode(), true);
                File file = new File(input.getParent() + "/pdm-code-gen/restful", entityName + "RestServer.java");
                try (BufferedWriter bw = new BufferedWriter(new FileWriter(file))) {
                    bw.append("@Api(value = \"").append(table.getName()).append("\", description = \"").append(table.getName()).append("\")\r\n");
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
        }
    }

    @Override
    public void genDubbo() {
        check();
        new File(input.getParent(), "pdm-code-gen/dubbo/impl").mkdirs();

        List<PDMTable> tables = model.getRootObject().getChildren().get(0).getTables();
        if (tables != null && !tables.isEmpty()) {
            tables.stream().forEach(table -> {
                String entityName = getEntityName(table.getCode(), true);
                String field = getFieldName(table.getCode(), true);
                // dubbo interface
                File file = new File(input.getParent() + "/pdm-code-gen/dubbo", "I" + entityName + "Provider.java");
                try (BufferedWriter bw = new BufferedWriter(new FileWriter(file))) {
                    bw.append("public interface I").append(entityName).append("Provider extends DubboBaseInterface<").append(entityName).append("> {\r\n");
                    bw.append("}\r\n");
                    bw.flush();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                // dubbo impl
                File impl = new File(input.getParent() + "/pdm-code-gen/dubbo/impl", entityName + "ProviderImpl.java");
                try (BufferedWriter bw = new BufferedWriter(new FileWriter(impl))) {
                    bw.append("public class ").append(entityName).append("ProviderImpl extends BaseProviderImpl<").append(entityName).append("> implements I").append(entityName).append("Provider {\r\n");
                    bw.append("\t@Autowired\r\n");
                    bw.append("\t@Qualifier(\"").append(field).append("Mapper\")\r\n");
                    bw.append("\tprivate ").append(entityName).append("Mapper ").append(field).append("Mapper;\r\n\r\n");
                    bw.append("\t@Override\r\n");
                    bw.append("\tpublic BaseInterfaceMapper<").append(entityName).append("> getBaseInterfaceMapper() {\r\n");
                    bw.append("\t\treturn ").append(field).append("Mapper;\r\n");
                    bw.append("\t}\r\n");
                    bw.append("}\r\n");
                    bw.flush();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });

            // consumer getter method
            File consumer1 = new File(input.getParent() + "/pdm-code-gen/dubbo", "Consumer.java");
            try (BufferedWriter bw = new BufferedWriter(new FileWriter(consumer1))) {
                tables.stream().forEach(table -> {
                    String entityName = getEntityName(table.getCode(), true);
                    String field = getFieldName(table.getCode(), true);

                    try {
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
            File consumer2 = new File(input.getParent() + "/pdm-code-gen/dubbo", "consumer.xml");
            try (BufferedWriter bw = new BufferedWriter(new FileWriter(consumer2))) {
                tables.stream().forEach(table -> {
                    String entityName = getEntityName(table.getCode(), true);
                    String field = getFieldName(table.getCode(), true);

                    try {
                        bw.append("\t<!-- ").append(table.getName()).append(" -->\r\n");
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
            File provider = new File(input.getParent() + "/pdm-code-gen/dubbo", "provider.xml");
            try (BufferedWriter bw = new BufferedWriter(new FileWriter(provider))) {
                tables.stream().forEach(table -> {
                    String entityName = getEntityName(table.getCode(), true);
                    String field = getFieldName(table.getCode(), true);

                    try {
                        bw.append("\t<!-- ").append(table.getName()).append(" -->\r\n");
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

    @Override
    public void genMapper() {
        check();
        new File(input.getParent(), "pdm-code-gen/mapper/xml").mkdirs();

        List<PDMTable> tables = model.getRootObject().getChildren().get(0).getTables();
        if (tables != null && !tables.isEmpty()) {
            tables.stream().forEach(table -> {
                String entityName = getEntityName(table.getCode(), true);
                String field = getFieldName(table.getCode(), true);
                // mapper interface
                File file = new File(input.getParent() + "/pdm-code-gen/mapper", entityName + "Mapper.java");
                try (BufferedWriter bw = new BufferedWriter(new FileWriter(file))) {
                    bw.append("public interface ").append(entityName).append("Mapper extends BaseInterfaceMapper<").append(entityName).append("> {\r\n");
                    bw.append("}\r\n");
                    bw.flush();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                // mapper.xml
                File xml = new File(input.getParent() + "/pdm-code-gen/mapper/xml", entityName + "Mapper.xml");
                try (BufferedWriter bw = new BufferedWriter(new FileWriter(xml))) {
                    bw.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                            "<!DOCTYPE mapper PUBLIC \"-//mybatis.org//DTD Mapper 3.0//EN\"\n" +
                            "        \"http://mybatis.org/dtd/mybatis-3-mapper.dtd\">\r\n");
                    bw.append("<mapper namespace=\"com.mes.control.mapper.").append(entityName).append("Mapper\">\r\n");
                    // result map
                    bw.append("\t<resultMap type=\"com.mes.entity.control.").append(entityName).append("\" id=\"").append(field).append("ResultMap\">\r\n");
                    List<PDMColumn> columns = table.getColumns();
                    if (columns != null && !columns.isEmpty()) {
                        columns.stream().forEach(column -> {
                            try {
                                String type = column.getDataType();
                                if (type != null && type.contains("(")) {
                                    type = type.substring(0, type.indexOf("("));
                                }
                                bw.append("\t\t<result column=\"").append(column.getCode()).append("\" property=\"").append(getFieldName(column.getCode(), false)).append("\" jdbcType=\"").append(type).append("\"/>\r\n");
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
                    bw.append("\t\tinsert into ").append(table.getCode()).append(" (");
                    if (columns != null && !columns.isEmpty()) {
                        bw.append(String.join(",", columns.stream().map(column -> {
                            return column.getCode();
                        }).collect(Collectors.toList())));
                    }
                    bw.append(")\r\n\t\tvalues (");
                    if (columns != null && !columns.isEmpty()) {
                        List<String> names = new ArrayList<>();
                        bw.append(String.join(",", columns.stream().map(column -> {
                            return "#{" + getFieldName(column.getCode(), false) + "}";
                        }).collect(Collectors.toList())));
                    }
                    bw.append(")\r\n");
                    bw.append("\t</insert>\r\n\r\n");
                    bw.append("\t<!--更新操作-->\r\n");
                    bw.append("\t<update id=\"update\" parameterType=\"com.mes.entity.control.").append(entityName).append("\">\r\n");
                    bw.append("\t\tupdate ").append(table.getCode()).append(" s <include refid=\"sql_update\"/> where s.id = #{id}\r\n");
                    bw.append("\t</update>\r\n\r\n");
                    bw.append("\t<!--根据id删除-->\r\n");
                    bw.append("\t<delete id=\"deleteById\" parameterType=\"java.lang.String\">\r\n");
                    bw.append("\t\tdelete from ").append(table.getCode()).append(" where id =#{id}\r\n");
                    bw.append("\t</delete>\r\n\r\n");
                    bw.append("\t<!--获取数据条数-->\r\n");
                    bw.append("\t<select id=\"getCount\" parameterType=\"java.util.Map\" resultType=\"int\">\r\n");
                    bw.append("\t\tselect count(1) from ").append(table.getCode()).append(" t <include refid=\"sql_where\"/>\r\n");
                    bw.append("\t</select>\r\n\r\n");
                    bw.append("\t<!--分页查询-->\r\n");
                    bw.append("\t<select id=\"findByPage\" parameterType=\"java.util.Map\" resultMap=\"").append(field).append("ResultMap\">\r\n");
                    bw.append("\t\t<include refid=\"sql_select\"/> <include refid=\"sql_where\"/> order by s.create_date desc limit #{startRowNum},#{pageSize}\r\n");
                    bw.append("\t</select>\r\n\r\n");
                    bw.append("\t<!--根据条件查询-->\r\n");
                    bw.append("\t<select id=\"findByMap\" parameterType=\"java.util.Map\" resultMap=\"").append(field).append("ResultMap\">\r\n");
                    bw.append("\t\t<include refid=\"sql_select\"/> <include refid=\"sql_where\"/> order by s.create_date desc\r\n");
                    bw.append("\t</select>\r\n\r\n");
                    bw.append("\t<!--查询所有-->\r\n");
                    bw.append("\t<select id=\"findAll\" resultMap=\"").append(field).append("ResultMap\">\r\n");
                    bw.append("\t\t<include refid=\"sql_select\"/> <include refid=\"sql_where\"/> order by s.create_date desc\r\n");
                    bw.append("\t</select>\r\n\r\n");
                    bw.append("\t<!--查询字段-->\r\n");
                    bw.append("\t<sql id=\"sql_select\">\r\n");
                    bw.append("\t\tSELECT id,parent_id,name,code,description,create_date,update_date from ").append(table.getCode()).append(" s\r\n");
                    bw.append("\t</sql>\r\n\r\n");
                    bw.append("\t<!--查询条件-->\r\n");
                    bw.append("\t<sql id=\"sql_where\">\r\n");
                    bw.append("\t\t<where>\r\n");
                    if (columns != null && !columns.isEmpty()) {
                        columns.stream().filter(column -> {
                            // 过滤掉主键
                            return table.getKeys().stream().noneMatch(key -> {
                                return key.getKeyColumns().stream().anyMatch(k -> {
                                    return column.getId().equals(k.getRef());
                                });
                            });
                        }).forEach(column -> {
                            try {
                                String fieldName = getFieldName(column.getCode(), false);
                                bw.append("\t\t\t<if test=\"").append(fieldName).append(" != null  and ").append(fieldName).append(" != '' \">\r\n");
                                bw.append("\t\t\t\t<![CDATA[\r\n");
                                bw.append("\t\t\t\t\tand ").append(column.getCode()).append(" LIKE CONCAT('%', #{").append(fieldName).append("}, '%')\r\n");
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
                            return table.getKeys().stream().noneMatch(key -> {
                                return key.getKeyColumns().stream().anyMatch(k -> {
                                    return column.getId().equals(k.getRef());
                                });
                            });
                        }).forEach(column -> {
                            try {
                                String fieldName = getFieldName(column.getCode(), false);
                                bw.append("\t\t\t<if test=\"").append(fieldName).append(" != null  and ").append(fieldName).append(" != '' \">\r\n");
                                bw.append("\t\t\t\t<![CDATA[\r\n");
                                bw.append("\t\t\t\t\t").append(column.getCode()).append(" = #{").append(fieldName).append("},\r\n");
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

    public PDMModel getModel() {
        return model;
    }

    public void setModel(PDMModel model) {
        this.model = model;
    }
}
