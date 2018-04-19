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
package org.slingerxv.limitart.base;


import java.util.function.Predicate;

/**
 * 判断函数帮助类
 *
 * @author hank
 */
public class Tests {
    public static <T> boolean invoke(@Nullable Predicate<T> predicate, T t) {
        return predicate != null && predicate.test(t);
    }

    public static boolean invoke(@Nullable Test test) {
        return test != null && test.test();
    }

    public static <T> boolean invoke(@Nullable Test1<T> test, T t) {
        return test != null && test.test(t);
    }

    public static <T1, T2> boolean invoke(@Nullable Test2<T1, T2> test, T1 t1, T2 t2) {
        return test != null && test.test(t1, t2);
    }
}