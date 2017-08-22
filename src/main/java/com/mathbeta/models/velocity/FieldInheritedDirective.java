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
 * Created by xiuyou.xu on 2017/8/22.
 */
public class FieldInheritedDirective extends Directive {
    @Override
    public String getName() {
        return "fieldInherited";
    }

    @Override
    public int getType() {
        return DirectiveConstants.LINE;
    }

    @Override
    public boolean render(InternalContextAdapter internalContextAdapter, Writer writer, Node node) throws IOException, ResourceNotFoundException, ParseErrorException, MethodInvocationException {
        Node nameNode = node.jjtGetChild(0);
        String name = (String) nameNode.value(internalContextAdapter);
        if ("id".equalsIgnoreCase(name) || "create_date".equalsIgnoreCase(name) || "update_date".equalsIgnoreCase(name)) {
            writer.write("y");
        } else {
            writer.write("n");
        }
        return true;
    }
}
