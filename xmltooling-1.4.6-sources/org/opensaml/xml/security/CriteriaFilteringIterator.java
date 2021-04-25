/*
 * Licensed to the University Corporation for Advanced Internet Development,
 * Inc. (UCAID) under one or more contributor license agreements.  See the
 * NOTICE file distributed with this work for additional information regarding
 * copyright ownership. The UCAID licenses this file to You under the Apache
 * License, Version 2.0 (the "License"); you may not use this file except in
 * compliance with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.opensaml.xml.security;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Set;

/**Iterator的此实现包装另一个特定类型的Iterator，其中包含要针对给定的EvaluableCriteria集进行评估的候选对象。遍历迭代器时，将通过EvaluableCriteria.evaluate（Object）对基础包装迭代器的每个候选元素执行标准评估。迭代器仅返回满足条件集指示的条件的那些元素，如下所示。如果参数metAllCriteria为true，则必须满足条件集中的所有条件，才能返回该元素。从本质上讲，这将条件集的条件与逻辑“与”联系起来。如果为false，则如果元素满足条件集的任何条件，则将其返回。从本质上讲，这将条件集的成员与逻辑OR相连。如果参数unevaluableSatisfies为true，则如果条件通过EvaluableCriteria.evaluate（Object）对候选者的评估表明它无法评估候选者，则准则将在确定是否返回候选人方面被认为是满意的。如果为假，则出于确定的目的，将认为标准不满足。应谨慎地将这两个参数值组合在一起以达到期望的结果。
 * <p>This implementation of {@link Iterator} wraps another Iterator of a particular type, containing candidates
 * which are to be evaluated against a given set of {@link EvaluableCriteria}.  When the iterator is traversed,
 * criteria evaluation is performed on each candidate element of the underlying wrapped iterator
 * via {@link EvaluableCriteria#evaluate(Object)}. Only those elements which satisfy the criteria indicated by
 * the criteria set are returned by the Iterator, as follows.</p>
 *
 * <p>If the parameter <code>meetAllCriteria</code> is <code>true</code>, then all criteria in the criteria
 * set must be satisfied in order for the element to be returned.  This in essence connects the criteria of the criteria
 * set with a logical <code>AND</code>.  If <code>false</code>, then if an element satisfies any of the criteria of the
 * criteria set, it will be returned.  This in essence connects the members of the criteria set with a logical
 * <code>OR</code>.</p>
 *
 * <p>If the parameter <code>unevaluableSatisfies</code> is <code>true</code>, then if a criteria's evaluation
 * of the candidate via {@link EvaluableCriteria#evaluate(Object)} indicates that it is unable to evaluate
 * the candidate, the criteria will be considered satisfied as far as the determination of whether to return
 * the candidate. If <code>false</code>, then the criteria will be considered unsatisfied for purposes
 * of this determination.</p>
 *
 * <p>Care should be exercised in combining these two parameter values to achieve the desired result.</p>
 *
 * @param <T> the type of candidate elements being evaluated
 */
public class CriteriaFilteringIterator<T> implements Iterator<T> {//看过了，标准迭代器操作。只是依赖内部被包装的迭代器进行操作

    /** The candidates to evaluate. */
    private Iterator<? extends T> candidateIter;

    /** The set of criteria against which to evaluate the candidates. */
    private Set<EvaluableCriteria<T>> criteriaSet;

    /** Flag indicating whether the candidate must satisfy all the criteria in the set, or just one. */
    private boolean meetAll;//看过了 true 即and,false 即or效果

    /** Flag indicating how candidates which can not be evaluated by a criteria are to be handled. */
    private boolean unevaledSatisfies;//看过了，不能评估的是否认为通过了

    /** The current candidate which will be returned by the next call to next(). */
    private T current;

    /**
     * Constructor.
     *
     * @param candidatesIterator the candidates to evaluate
     * @param criteria the set of criteria against which to evaluate the candidates
     * @param meetAllCriteria whether a candidate must meet all criteria, or just one
     * @param unevaluableSatisfies whether a can-not-evaluate result of a particular criteria's evaluation
     *          is treated as the candidate having satisfied or not satisfied the criteria, for purposes
     *          of determinig whether to return the element
     */
    public CriteriaFilteringIterator(Iterator<? extends T> candidatesIterator,
            Set<EvaluableCriteria<T>> criteria, boolean meetAllCriteria, boolean unevaluableSatisfies) {

        candidateIter = candidatesIterator;
        criteriaSet = criteria;
        meetAll = meetAllCriteria;
        unevaledSatisfies = unevaluableSatisfies;

        current = null;
    }

    /** {@inheritDoc} */
    public boolean hasNext() {//看过了
        if (current != null) {
            return true;
        }
        current = getNextMatch();
        if (current != null) {
            return true;
        }
        return false;
    }

    /** {@inheritDoc} */
    public T next() {
        T temp;
        if (current != null) {
            temp = current;
            current = null;
            return temp;
        }
        temp = getNextMatch();
        if (temp != null) {
            return temp;
        } else {
            throw new NoSuchElementException("No more elements are available");
        }
    }

    /** {@inheritDoc} */
    public void remove() {
        throw new UnsupportedOperationException("Remove operation is not supported by this iterator");
    }

    /**
     * Get the next matching candidate.
     *
     * @return the next matching candidate
     */
    private T getNextMatch() {
        while (candidateIter.hasNext()) {//看过了，依靠被包装的迭代器进行各种操作
            T candidate = candidateIter.next();
            if (match(candidate)) {
                return candidate;
            }
        }
        return null;
    }

    /**
     * Evaluate the candidate against all the criteria.
     *
     * @param candidate the candidate to evaluate
     * @return true if the candidate satisfies the set of criteria, false otherwise
     */
    private boolean match(T candidate) {
        boolean sawOneSatisfied = false;

        // Edge case of empty criteria, should match everything
        if (criteriaSet.isEmpty()) {
            return true;
        }

        for (EvaluableCriteria<T> criteria : criteriaSet) {
            Boolean result = criteria.evaluate(candidate);
            if (result == Boolean.FALSE) {
                if (meetAll) {
                    return false;
                }
            } else if (result == Boolean.TRUE) {
                if (!meetAll) {
                    return true;
                }
                sawOneSatisfied = true;
            } else {
                // Was null, criteria said could not evaluate
                if (meetAll && !unevaledSatisfies) {
                    return false;
                } else if (!meetAll && unevaledSatisfies) {
                    return true;
                }
                if (unevaledSatisfies) {
                    sawOneSatisfied = true;
                }
            }
        }
        return sawOneSatisfied;
    }

}
