package com.mathbeta.models.types;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by xiuyou.xu on 2017/7/10.
 */
public class TypeMappingUtil {
    public static TypeMapping loadMappings() {
        try {
            JAXBContext context = JAXBContext.newInstance(TypeMapping.class);
            Unmarshaller unmarshaller = context.createUnmarshaller();

//            final FileReader reader = new FileReader(input);
//            final SAXParserFactory sax = SAXParserFactory.newInstance();
//            // 忽略namespace
//            sax.setNamespaceAware(false);
//            final XMLReader xmlReader = sax.newSAXParser().getXMLReader();
//            final SAXSource saxSource = new SAXSource(xmlReader, new InputSource(reader));

            return (TypeMapping) unmarshaller.unmarshal(TypeMappingUtil.class.getClassLoader().getResourceAsStream("jdbc-java-types.xml"));
        } catch (JAXBException e) {
            e.printStackTrace();
//        } catch (SAXException e) {
//            e.printStackTrace();
//        } catch (FileNotFoundException e) {
//            e.printStackTrace();
//        } catch (ParserConfigurationException e) {
//            e.printStackTrace();
        }
        return null;
    }

    public static Map<String, Map<String, String>> getMapping() {
        TypeMapping mapping = loadMappings();
        Map<String, Map<String, String>> ret = new HashMap<>();
        if (mapping != null && mapping.getTypes() != null && !mapping.getTypes().isEmpty()) {
            mapping.getTypes().stream().forEach(type -> {
                Map<String, String> map = new HashMap<>();
                List<Mapping> mappings = type.getMappings();
                if (mappings != null && !mappings.isEmpty()) {
                    mappings.stream().forEach(m -> {
                        map.put(m.getJdbc(), m.getJava());
                    });
                }

                ret.put(type.getName(), map);
            });
        }
        return ret;
    }

    public static void main(String[] args) {
//        TypeMapping mappings = loadMappings();
//        System.out.println(mappings);

        Map<String, Map<String, String>> mappings = getMapping();
        System.out.println(mappings);
    }
}
