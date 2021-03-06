package com.mathbeta.models.velocity;

import com.mathbeta.models.utils.NameUtil;
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
 * 字段名到类属性名转换指令
 *
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
        writer.write(NameUtil.getFieldName(name, hasPrefix, tableNamePrefix));
        return false;
    }
}
