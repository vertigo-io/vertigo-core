/**
 * vertigo - simple java starter
 *
 * Copyright (C) 2013, KleeGroup, direction.technique@kleegroup.com (http://www.kleegroup.com)
 * KleeGroup, Centre d'affaire la Boursidiere - BP 159 - 92357 Le Plessis Robinson Cedex - France
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package vertigo.kernel.lang.tuples;


/**
 * Base class of any tuple. Tuple are immutable objects.
 */
public abstract class Tuple /*implements Iterable<Object>, Serializable*/{
	//
	//    /** Field �serialVersionUID�. */
	//    private static final long serialVersionUID = -1923665016688618997L;
	//
	//    /**
	//     * @param i Item.
	//     * @return the i-th item.
	//     * @throws IndexOutOfBoundsException if there is no i value.
	//     */
	//    public abstract Object get(int i) throws IndexOutOfBoundsException;
	//
	//    /**
	//     * @return The size of the tuple.
	//     */
	//    public abstract int size();
	//
	//    @Override
	//    public Iterator<Object> iterator() {
	//        return new Iter();
	//    }
	//
	//    private class Iter implements Iterator<Object> {
	//        private int idx;
	//
	//        @Override
	//        public boolean hasNext() {
	//            return idx < size();
	//        }
	//
	//        @Override
	//        public Object next() {
	//            if (!hasNext()) {
	//                throw new NoSuchElementException();
	//            }
	//            return get(idx++);
	//        }
	//
	//        @Override
	//        public void remove() {
	//            throw new UnsupportedOperationException();
	//        }
	//    }
	//
	//    @Override
	//    public abstract int hashCode();
	//
	//    /**
	//     * Compares the specified object with this tuple for equality. Tuple equality is defined as in <tt>java.util.List</tt> interface : two
	//     * tuples are defined to be equal if they contain the same elements in the same order.
	//     * 
	//     * @param obj the object to be compared for equality with this tuple
	//     * @return <tt>true</tt> if the specified object is equal to this tuple
	//     */
	//    @Override
	//    public abstract boolean equals(Object obj);

}
