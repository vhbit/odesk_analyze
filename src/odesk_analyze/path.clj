(ns odesk_analyze.path
  (:require [clojure.string :as string])
  (:import (java.io File)))

(defn exists [fname]
  (.exists (File. fname)))

(defn expand [fname]
  (string/replace fname "~" (System/getProperty "user.home")))

(defn join [dir fname]
  (.toString (File. dir fname)))

(defn ensure-dir-exists [path]
  (let [fpath (expand path)]
    (if-not (exists path)
      (.mkdirs (File. fpath)))))
