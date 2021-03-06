;;; distributions_tests.clj -- Tests of incanter.distributions namespace

;; by Mark Fredrickson http://www.markmfredrickson.com
;; May 10, 2010

;; Copyright (c) Mark M. Fredrickson, 2010. All rights reserved.  The use
;; and distribution terms for this software are covered by the Eclipse
;; Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
;; which can be found in the file epl-v10.htincanter.at the root of this
;; distribution.  By using this software in any fashion, you are
;; agreeing to be bound by the terms of this license.  You must not
;; remove this notice, or any other, from this software.

(ns incanter.distributions-tests
  (:use clojure.test
        clojure.set
        (incanter distributions stats)))

;; testing helpers
(defn- all? [coll] (every? true? coll))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; UNIT TESTS FOR incanter.distributions.clj
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(deftest extending-basic-types 
	(is (= (pdf [1 2 2] 1) 1/3))
  (is (= (pdf '(1 2 1 2 2 1) 2) 1/2))
	(is (= (support [1 2 3 2 :foo :bar]) #{1 2 3 :foo :bar}))
  (is (= (cdf [1 2 3] 2) 2/3))
  (is (= (pdf #{:foo :bar :baz} :baz) 1/3))
  (is (= (pdf #{:foo :bar} :baz) 0)))

(deftest basic-integer-distribution-tests
  (is (thrown? AssertionError (integer-distribution 5 0))) ; wrong argment order
  (let [u (integer-distribution 1 5)]
    (is (< 0 (draw u)))
    (is (= (pdf u 5) 1/4))
    (is (= (cdf u 2) 1/2))
  	(is (all? (repeatedly 100 #(> (:end u) (draw u)))))))

(deftest large-integer-tests
  (let [u (integer-distribution (reduce * (repeat 100 2)) (reduce * (repeat 100 3)))]
    (is (all? (repeatedly 100 #(<= (:start u) (draw u)))))
		(is (all? (repeatedly 100 #(> (:end u) (draw u)))))))

(deftest combination-distribution-tests
  (let [cd (combination-distribution 5 3)]
    (is (= 3 (count (draw cd))))
		(is (= 3 (count (set (draw cd)))))))

(deftest normal-tests
  ;; generate a sample of standard normal data
  (let [N (normal-distribution)
        std-normal-data (repeatedly 1000 #(draw N))]
  	(is (= (count std-normal-data) 1000))
  	(is (= (Math/round (mean std-normal-data)) 0))
  	(is (= (Math/round (sd std-normal-data)) 1)))
  
  ;; generate a sample of normal data with mean = 10 and sd = 5
  (let [N (normal-distribution 10 5)
        nonstd-normal-data (repeatedly 1000 #(draw N))]
 		(is (= (count nonstd-normal-data) 1000))
  	(is (= (Math/round (mean nonstd-normal-data)) 10))
  	(is (= (Math/round (sd nonstd-normal-data)) 5))))

(deftest lady-tasting-tea
  ; http://en.wikipedia.org/wiki/Lady_tasting_tea
  ; under fisher's lady tasting tea experiment, the test statistic is
  ; the number of milk first cups correctly classified. Therefore, we only care
  ; that the treatment units correspond to the "true" treatments,
  ; which I arbitrarily decide are cups 0 -3
  (let [lady-fn (fn [x] (count (intersection (set x) #{0 1 2 3})))
        lady-tasting-tea (test-statistic-distribution lady-fn 8 4)]
    (= (support lady-tasting-tea) #{0 1 2 3}) ; possible cups correctly classified
    (= (map #(pdf lady-tasting-tea %) [0 1 2 3 4]) [1/70 16/70 36/70 16/70 1/70])))

    
