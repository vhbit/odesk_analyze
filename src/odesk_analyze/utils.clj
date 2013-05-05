(ns odesk_analyze.utils
  (:require [clojure.edn :as edn])
  (:use [clojure.pprint :only [pprint]])
  (:use [clojure.java.io :only [file, reader, writer]]))

(defn read-edn [path]
  (if-not (.exists (file path))
    nil
    (-> path
        (slurp)
        (edn/read-string))))

(defn pr-edn [& xs]
  (binding [*print-length* nil
            *print-dup* nil
            *print-level* nil
            *print-readably* true]
    (apply pr xs)))


(defn write-edn [path data]
  (with-open [f-writer (writer path)]
    (binding [*out* f-writer]
      (pr-edn data))))



