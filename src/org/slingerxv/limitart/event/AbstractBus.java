/*
 * Copyright (c) 2016-present The Limitart Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.slingerxv.limitart.event;

import org.slingerxv.limitart.base.Proc1;

import java.util.*;

/**
 * 抽象总线
 *
 * @author hank
 * @version 2018/4/12 0012 21:13
 */
public abstract class AbstractBus implements Bus {
    protected final Map<Class<? extends Event>, Set<Proc1>> callbacks = new HashMap<>();

    @Override
    public <E extends Event> void addListener(final Class<E> eventType, final Proc1<E> callback) {
        Set<Proc1> set = callbacks.computeIfAbsent(eventType, k -> new LinkedHashSet<>());
        set.add(callback);
    }
}