package com.mathbeta.models.velocity;

import com.mathbeta.models.Column;
import org.apache.velocity.context.InternalContextAdapter;
import org.apache.velocity.exception.MethodInvocationException;
import org.apache.velocity.exception.ParseErrorException;
import org.apache.velocity.exception.ResourceNotFoundException;
import org.apache.velocity.runtime.directive.Directive;
import org.apache.velocity.runtime.directive.DirectiveConstants;
import org.apache.velocity.runtime.parser.node.Node;

import java.io.IOException;
import java.io.Writer;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by xiuyou.xu on 2017/8/16.
 */
public class ColumnNamesJoinDirective extends Directive {
    @Override
    public String getName() {
        return "joinColumns";
    }

    @Override
    public int getType() {
        return DirectiveConstants.LINE;
    }

    @Override
    public boolean render(InternalContextAdapter internalContextAdapter, Writer writer, Node node) throws IOException, ResourceNotFoundException, ParseErrorException, MethodInvocationException {
        Node columnsNode = node.jjtGetChild(0);
        List<Column> columns = (List<Column>) columnsNode.value(internalContextAdapter);
        if (columns == null) {
            return false;
        }
        Node prefixNode = node.jjtGetChild(1);
        String prefix = (String) prefixNode.value(internalContextAdapter);
        Node suffixNode = node.jjtGetChild(1);
        String suffix = (String) suffixNode.value(internalContextAdapter);
        writer.write(String.join(",", columns.stream().map(column -> {
            return prefix + column.getName() + suffix;
        }).collect(Collectors.toList())));
        return true;
    }
}
