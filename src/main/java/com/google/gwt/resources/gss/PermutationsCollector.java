/*
 * Copyright 2013 Julien Dramaix.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package com.google.gwt.resources.gss;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;
import com.google.common.collect.Queues;
import com.google.common.css.compiler.ast.CssBooleanExpressionNode;
import com.google.common.css.compiler.ast.CssBooleanExpressionNode.Type;
import com.google.common.css.compiler.ast.CssCompilerPass;
import com.google.common.css.compiler.ast.CssConditionalRuleNode;
import com.google.common.css.compiler.ast.CssValueNode;
import com.google.common.css.compiler.ast.DefaultTreeVisitor;
import com.google.common.css.compiler.ast.ErrorManager;
import com.google.common.css.compiler.ast.GssError;
import com.google.common.css.compiler.ast.MutatingVisitController;

import java.util.Deque;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PermutationsCollector extends DefaultTreeVisitor implements CssCompilerPass {
  private static final Pattern IS_FUNCTION = Pattern.compile("^is\\([\"']([^\"']*)[\"'](?:,[\"']([^\"']*)[\"'])?\\)$");
  private static final String USER_AGENT_PERMUTATION = "user.agent";

  private final MutatingVisitController delegate;
  private final ErrorManager errorManager;
  private Deque<CssBooleanExpressionNode> childrenStack;
  private ImmutableList.Builder<String> permutationAxesListBuilder;

  public PermutationsCollector(MutatingVisitController delegate, ErrorManager errorManager) {
    this.delegate = delegate;
    this.errorManager = errorManager;
  }

  @Override
  public boolean enterConditionalRule(CssConditionalRuleNode node) {
    // unfortunately the VisitController doesn't visit the children of a CssConditionalRuleNode
    for (CssValueNode children : node.getChildren()) {
      if (children instanceof CssBooleanExpressionNode) {
        childrenStack.addFirst((CssBooleanExpressionNode) children);
      }
    }

    while (!childrenStack.isEmpty()) {
      CssBooleanExpressionNode visitingNode = childrenStack.pop();
      visitBooleanExpression(visitingNode);

      if (visitingNode.getLeft() != null) {
        childrenStack.addFirst(visitingNode.getLeft());
      }
      if (visitingNode.getRight() != null) {
        childrenStack.addFirst(visitingNode.getRight());
      }
    }

    return true;
  }

  public List<String> getPermutationAxes() {
    return permutationAxesListBuilder.build();
  }

  private void visitBooleanExpression(CssBooleanExpressionNode booleanExpressionNode) {
    if (booleanExpressionNode.getType() == Type.CONSTANT && booleanExpressionNode.getValue() != null) {
      Matcher m = IS_FUNCTION.matcher(booleanExpressionNode.getValue());

      if (m.matches()) {
        String permutationName = m.group(1);
        String permutationValue = m.group(2);

        if (permutationValue == null) {
          permutationValue = permutationName;
          permutationName = USER_AGENT_PERMUTATION;
        }

        booleanExpressionNode.setValue(permutationName + ":" + permutationValue);

        permutationAxesListBuilder.add(permutationName);

      } else {
        GssError warning = new GssError("The expression [" + booleanExpressionNode.getValue() +
            "] cannot be evaluated. It will be considered as false.",
            booleanExpressionNode.getSourceCodeLocation());
        errorManager.reportWarning(warning);
      }
    }
  }

  @Override
  public void runPass() {
    childrenStack  = Queues.newArrayDeque();
    permutationAxesListBuilder = new Builder<String>();
    delegate.startVisit(this);
  }
}
