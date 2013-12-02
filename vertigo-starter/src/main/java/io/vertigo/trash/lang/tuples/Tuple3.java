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
 * Type with three objects. Objetcs are immutable.
 * 
 * @param <T1> Type of the first value.
 * @param <T2> Type of the second value.
 * @param <T3> Type of the third value.
 */
public final class Tuple3<T1, T2, T3> extends Tuple {
	//
	//    /** Field �serialVersionUID�. */
	//    private static final long serialVersionUID = -239400793813527217L;
	//
	//    private final T1 val1;
	//    private final T2 val2;
	//    private final T3 val3;
	//
	//    /**
	//     * Create a new instance of Tuple2.
	//     * 
	//     * @param val1 Value 1.
	//     * @param val2 Value 2.
	//     * @param val3 Value 3.
	//     */
	//    public Tuple3(T1 val1, T2 val2, T3 val3) {
	//        super();
	//        this.val1 = val1;
	//        this.val2 = val2;
	//        this.val3 = val3;
	//    }
	//
	//    /** {@inheritDoc} */
	//    @Override
	//    public Object get(int i) throws IndexOutOfBoundsException {
	//        switch (i) {
	//            case 0:
	//                return val1;
	//            case 1:
	//                return val2;
	//            case 2:
	//                return val3;
	//            default:
	//                throw new IndexOutOfBoundsException();
	//        }
	//    }
	//
	//    /** {@inheritDoc} */
	//    @Override
	//    public int size() {
	//        return 3;
	//    }
	//
	//    /**
	//     * @return �val1� value.
	//     */
	//    public T1 getVal1() {
	//        return val1;
	//    }
	//
	//    /**
	//     * @return �val2� value.
	//     */
	//    public T2 getVal2() {
	//        return val2;
	//    }
	//
	//    /**
	//     * @return �val3� value.
	//     */
	//    public T3 getVal3() {
	//        return val3;
	//    }
	//
	//    @Override
	//    public int hashCode() {
	//        return Objects.hashCode(val1, val2, val3);
	//    }
	//
	//    /**
	//     * Compares the specified object with this tuple for equality. Tuple equality is defined as in <tt>java.util.List</tt> interface : two
	//     * tuples are defined to be equal if they contain the same elements in the same order.
	//     * 
	//     * @param obj the object to be compared for equality with this tuple
	//     * @return <tt>true</tt> if the specified object is equal to this tuple
	//     */
	//    @Override
	//    public boolean equals(Object obj) {
	//        if (this == obj) {
	//            return true;
	//        }
	//        if (!(obj instanceof Tuple3)) {
	//            return false;
	//        }
	//        Tuple3<?, ?, ?> that = (Tuple3<?, ?, ?>) obj;
	//        return Objects.equal(val1, that.val1) && Objects.equal(val2, that.val2) && Objects.equal(val3, that.val3);
	//    }
	//
	//    /** {@inheritDoc} */
	//    @Override
	//    public String toString() {
	//        return Objects.toStringHelper(this).add("val1", val1).add("val2", val2).add("val3", val3).toString();
	//    }

}
