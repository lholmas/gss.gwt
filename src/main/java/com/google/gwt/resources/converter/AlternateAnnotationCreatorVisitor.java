/*
 * Copyright 2014 Daniel Kurka.
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
package com.google.gwt.resources.converter;

import com.google.gwt.resources.css.ast.Context;
import com.google.gwt.resources.css.ast.CssProperty;
import com.google.gwt.resources.css.ast.CssRule;
import com.google.gwt.resources.css.ast.CssVisitor;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Visitor that adds @alternate annotations where required in CSSRules.
 */
public class AlternateAnnotationCreatorVisitor extends CssVisitor {

  @Override
  public boolean visit(CssRule x, Context ctx) {
    List<CssProperty> properties = x.getProperties();
    Set<String> duplicatePropertyNames = new HashSet<String>();
    Set<String> seenPropertyNames = new HashSet<String>();

    for (CssProperty cssProperty : properties) {
      String name = cssProperty.getName();
      if(!seenPropertyNames.contains(name)) {
        seenPropertyNames.add(name);
      } else {
        duplicatePropertyNames.add(name);
      }
    }

    for (String dupName : duplicatePropertyNames) {
      boolean firstProperty = true;
      for (CssProperty cssProperty : properties) {
        String name = cssProperty.getName();
        if(name.equals(dupName)) {
          if(firstProperty) {
            firstProperty = false;
          } else {
            cssProperty.setName("/* @alternate */ " + name);
          }
        }
      }
    }
    return false;
  }
}
