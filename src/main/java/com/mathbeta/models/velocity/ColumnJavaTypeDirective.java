package com.mathbeta.models.velocity;

import com.mathbeta.models.types.TypeMappingUtil;
import org.apache.velocity.context.InternalContextAdapter;
import org.apache.velocity.exception.MethodInvocationException;
import org.apache.velocity.exception.ParseErrorException;
import org.apache.velocity.exception.ResourceNotFoundException;
import org.apache.velocity.runtime.directive.Directive;
import org.apache.velocity.runtime.directive.DirectiveConstants;
import org.apache.velocity.runtime.parser.node.Node;

import java.io.IOException;
import java.io.Writer;
import java.util.Map;

/**
 * Created by xiuyou.xu on 2017/8/11.
 */
public class ColumnJavaTypeDirective extends Directive {
    Map<String, String> mapping = TypeMappingUtil.getMapping().get("mysql");

    @Override
    public String getName() {
        return "getJavaType";
    }

    @Override
    public int getType() {
        return DirectiveConstants.LINE;
    }

    @Override
    public boolean render(InternalContextAdapter internalContextAdapter, Writer writer, Node node) throws IOException, ResourceNotFoundException, ParseErrorException, MethodInvocationException {
        Node dataTypeNode = node.jjtGetChild(0);
        String type = (String) dataTypeNode.value(internalContextAdapter);
        if (type != null) {
            if (type.contains("(")) {
                type = type.substring(0, type.indexOf("("));
            }
            writer.write(mapping.get(type));
        }
        return false;
    }
}
