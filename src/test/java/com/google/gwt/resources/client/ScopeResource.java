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

package com.google.gwt.resources.client;

import com.google.gwt.resources.client.CssResource.Shared;

public interface ScopeResource extends ClientBundle {
  interface ScopeA extends GssResource {
    String foo();
  }

  interface ScopeB extends ScopeA {
    String foo();
  }

  interface ScopeC extends ScopeA {
    // Intentionally not defining foo()
  }

  @Shared
  interface SharedParent extends GssResource {
    String sharedClassName1();
    String sharedClassName2();
  }

  interface SharedChild1 extends SharedParent {
    String nonSharedClassName();
  }

  interface SharedChild2 extends SharedParent {
    String nonSharedClassName();
  }

  interface SharedGreatChild extends SharedChild2 {
    // Intentionally empty
  }

  SharedChild1 sharedChild1();

  SharedChild2 sharedChild2();

  SharedGreatChild sharedGreatChild();

  SharedParent sharedParent();

  ScopeA scopeA();

  ScopeA scopeA2();

  ScopeB scopeB();

  ScopeC scopeC();
}
