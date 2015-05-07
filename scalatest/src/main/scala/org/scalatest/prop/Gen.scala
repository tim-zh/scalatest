/*
 * Copyright 2001-2015 Artima, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.scalatest.prop

/*
I need to know how many or at least what percentage of edges I should
produce. When I map and flatmap I want it to combine edges and regulars
both. What if instead of T, we filled two buckets, and instead of just
a size, we also pass in how many of each we want?
Or, how about an Rng for edge conditions, and I just hmm. Yes that might
work. If I have 7 edges, then I want a random number from 0 to 6 and
I pick from the edges. Yes, so my edges could be a thing on Gen, which
also gets mapped and flatMapped? Do I have a number of them? Or do I not
worry about that. Probably just don't worry about it.
Edges[T] {
  can have map and flatmap on it
}
*/
// (size: Int, randomNumGen: Rnd) => (value, new randomNumGen)
// def yeOldeNext(size: Int = 10, rnd: Rnd = Rnd.default()): (T, Rnd, Gen[T]) = next(size, rnd)
trait Gen[T] { thisGenOfT =>
  def next(size: Int, rnd: Rnd): (T, Rnd, Gen[T])
  def map[U](f: T => U): Gen[U] =
    new Gen[U] {
      def next(size: Int, rnd: Rnd): (U, Rnd, Gen[U]) = {
        val (nextValue, nextRnd, nextGenOfT) = thisGenOfT.next(size, rnd)
        (f(nextValue), nextRnd, nextGenOfT.map(f))
      }
    }
  def flatMap[U](f: T => Gen[U]): Gen[U] = 
    new Gen[U] {
      def next(size: Int, rnd: Rnd): (U, Rnd, Gen[U]) = {
        val (nextValue, nextRnd, nextGenOfT) = thisGenOfT.next(size, rnd)
        f(nextValue).next(size, nextRnd)
      }
    }
}

object Gen {
  implicit val intGen: Gen[Int] =
    new Gen[Int] { thisGen =>
      def next(size: Int, rnd: Rnd): (Int, Rnd, Gen[Int]) = {
        val (nextValue, nextRnd) = rnd.nextInt
        (nextValue, nextRnd, thisGen)
      }
    }
  implicit val doubleGen: Gen[Double] =
    new Gen[Double] { thisGen =>
      def next(size: Int, rnd: Rnd): (Double, Rnd, Gen[Double]) = {
        val (nextValue, nextRnd) = rnd.nextDouble
        (nextValue, nextRnd, thisGen)
      }
    }
}
