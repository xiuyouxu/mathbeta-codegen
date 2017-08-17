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
 * 字段数据类型到mybatis jdbc type转换指令
 *
 * Created by xiuyou.xu on 2017/8/16.
 */
public class ColumnJdbcTypeDirective extends Directive {
    @Override
    public String getName() {
        return "getJdbcType";
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
            if (type != null) {
                if (type != null && type.contains("(")) {
                    type = type.substring(0, type.indexOf("("));
                }
                if ("date".equalsIgnoreCase(type) || "datetime".equalsIgnoreCase(type)) {
                    type = "TIMESTAMP";
                }
                if ("int".equalsIgnoreCase(type)) {
                    type = "INTEGER";
                }
                if ("varchar".equalsIgnoreCase(type) || type.toLowerCase().contains("text")) {
                    type = "VARCHAR";
                }
                writer.write(type);
            }
        }
        return true;
    }
}
