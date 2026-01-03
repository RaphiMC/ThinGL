/*
 * This file is part of ThinGL - https://github.com/RaphiMC/ThinGL
 * Copyright (C) 2024-2026 RK_01/RaphiMC and contributors
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package net.raphimc.thingl.util.glsl;

import io.github.ocelot.glslprocessor.api.node.GlslCompoundNode;
import io.github.ocelot.glslprocessor.api.node.GlslNode;
import io.github.ocelot.glslprocessor.api.node.branch.*;
import io.github.ocelot.glslprocessor.api.node.expression.*;
import io.github.ocelot.glslprocessor.api.node.function.GlslFunctionNode;
import io.github.ocelot.glslprocessor.api.node.function.GlslInvokeFunctionNode;
import io.github.ocelot.glslprocessor.api.node.variable.GlslGetArrayNode;
import io.github.ocelot.glslprocessor.api.node.variable.GlslGetFieldNode;
import io.github.ocelot.glslprocessor.api.node.variable.GlslNewFieldNode;

import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Function;

public class GlslNodeMutator {

    public static void mutate(final List<GlslNode> nodes, final Function<GlslNode, GlslNode> mutator) {
        if (nodes != null) {
            for (int i = 0; i < nodes.size(); i++) {
                final GlslNode oldNode = nodes.get(i);
                final GlslNode newNode = mutate(oldNode, mutator);
                if (oldNode != newNode) {
                    nodes.set(i, newNode);
                }
            }
        }
    }

    public static GlslNode mutate(final GlslNode node, final Function<GlslNode, GlslNode> mutator) {
        if (node instanceof GlslAssignmentNode assignmentNode) {
            mutate(assignmentNode, mutator, GlslAssignmentNode::getFirst, GlslAssignmentNode::setFirst);
            mutate(assignmentNode, mutator, GlslAssignmentNode::getSecond, GlslAssignmentNode::setSecond);
        } else if (node instanceof GlslBitwiseNode bitwiseNode) {
            mutate(bitwiseNode.getExpressions(), mutator);
        } else if (node instanceof GlslCaseLabelNode caseLabelNode) {
            mutate(caseLabelNode, mutator, GlslCaseLabelNode::getCondition, GlslCaseLabelNode::setCondition);
        } else if (node instanceof GlslCompareNode compareNode) {
            mutate(compareNode, mutator, GlslCompareNode::getFirst, GlslCompareNode::setFirst);
            mutate(compareNode, mutator, GlslCompareNode::getSecond, GlslCompareNode::setSecond);
        } else if (node instanceof GlslCompoundNode compoundNode) {
            mutate(compoundNode.getChildren(), mutator);
        } else if (node instanceof GlslConditionalNode conditionalNode) {
            mutate(conditionalNode, mutator, GlslConditionalNode::getCondition, GlslConditionalNode::setCondition);
            mutate(conditionalNode, mutator, GlslConditionalNode::getFirst, GlslConditionalNode::setFirst);
            mutate(conditionalNode, mutator, GlslConditionalNode::getSecond, GlslConditionalNode::setSecond);
        } else if (node instanceof GlslForLoopNode forLoopNode) {
            mutate(forLoopNode, mutator, GlslForLoopNode::getInit, GlslForLoopNode::setInit);
            mutate(forLoopNode, mutator, GlslForLoopNode::getCondition, GlslForLoopNode::setCondition);
            mutate(forLoopNode, mutator, GlslForLoopNode::getIncrement, GlslForLoopNode::setIncrement);
            mutate(forLoopNode.getBody(), mutator);
        } else if (node instanceof GlslFunctionNode functionNode) {
            mutate(functionNode.getBody(), mutator);
        } else if (node instanceof GlslGetArrayNode getArrayNode) {
            mutate(getArrayNode, mutator, GlslGetArrayNode::getExpression, GlslGetArrayNode::setExpression);
            mutate(getArrayNode, mutator, GlslGetArrayNode::getIndex, GlslGetArrayNode::setIndex);
        } else if (node instanceof GlslGetFieldNode getFieldNode) {
            mutate(getFieldNode, mutator, GlslGetFieldNode::getExpression, GlslGetFieldNode::setExpression);
        } else if (node instanceof GlslIfNode ifNode) {
            mutate(ifNode, mutator, GlslIfNode::getCondition, GlslIfNode::setCondition);
            mutate(ifNode.getFirst(), mutator);
            mutate(ifNode.getSecond(), mutator);
        } else if (node instanceof GlslInvokeFunctionNode invokeFunctionNode) {
            mutate(invokeFunctionNode, mutator, GlslInvokeFunctionNode::getHeader, GlslInvokeFunctionNode::setHeader);
            mutate(invokeFunctionNode.getParameters(), mutator);
        } else if (node instanceof GlslNewFieldNode newFieldNode) {
            mutate(newFieldNode, mutator, GlslNewFieldNode::getInitializer, GlslNewFieldNode::setInitializer);
        } else if (node instanceof GlslOperationNode operationNode) {
            mutate(operationNode, mutator, GlslOperationNode::getFirst, GlslOperationNode::setFirst);
            mutate(operationNode, mutator, GlslOperationNode::getSecond, GlslOperationNode::setSecond);
        } else if (node instanceof GlslReturnNode returnNode) {
            mutate(returnNode, mutator, GlslReturnNode::getValue, GlslReturnNode::setValue);
        } else if (node instanceof GlslSwitchNode switchNode) {
            mutate(switchNode, mutator, GlslSwitchNode::getCondition, GlslSwitchNode::setCondition);
            mutate(switchNode.getBranches(), mutator);
        } else if (node instanceof GlslUnaryNode unaryNode) {
            mutate(unaryNode, mutator, GlslUnaryNode::getExpression, GlslUnaryNode::setExpression);
        } else if (node instanceof GlslWhileLoopNode whileLoopNode) {
            mutate(whileLoopNode, mutator, GlslWhileLoopNode::getCondition, GlslWhileLoopNode::setCondition);
            mutate(whileLoopNode.getBody(), mutator);
        }

        if (node != null) {
            return mutator.apply(node);
        } else {
            return null;
        }
    }

    private static <T extends GlslNode> void mutate(final T parentNode, final Function<GlslNode, GlslNode> mutator, final Function<T, GlslNode> getter, final BiConsumer<T, GlslNode> setter) {
        final GlslNode oldNode = getter.apply(parentNode);
        final GlslNode newNode = mutate(oldNode, mutator);
        if (oldNode != newNode) {
            setter.accept(parentNode, newNode);
        }
    }

}
