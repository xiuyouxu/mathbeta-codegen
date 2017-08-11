package com.mathbeta.models.velocity;

import org.apache.velocity.context.InternalContextAdapter;
import org.apache.velocity.exception.MethodInvocationException;
import org.apache.velocity.exception.ParseErrorException;
import org.apache.velocity.exception.ResourceNotFoundException;
import org.apache.velocity.runtime.directive.Directive;
import org.apache.velocity.runtime.directive.DirectiveConstants;
import org.apache.velocity.runtime.parser.node.Node;

import java.io.IOException;
import java.io.Writer;

/**
 * Created by xiuyou.xu on 2017/8/11.
 */
public class FieldNameDirective extends Directive {
    @Override
    public String getName() {
        return "getFieldName";
    }

    @Override
    public int getType() {
        return DirectiveConstants.LINE;
    }

    @Override
    public boolean render(InternalContextAdapter internalContextAdapter, Writer writer, Node node) throws IOException, ResourceNotFoundException, ParseErrorException, MethodInvocationException {
        Node nameNode = node.jjtGetChild(0);
        String name = (String) nameNode.value(internalContextAdapter);
        Node hasPrefixNode = node.jjtGetChild(1);
        boolean hasPrefix = (boolean) hasPrefixNode.value(internalContextAdapter);
        Node tableNamePrefixNode = node.jjtGetChild(2);
        String tableNamePrefix = (String) tableNamePrefixNode.value(internalContextAdapter);
        writer.write(getFieldName(name, hasPrefix, tableNamePrefix));
        return false;
    }

    private String getFieldName(String name, boolean hasPrefix, String tableNamePrefix) {
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
}
