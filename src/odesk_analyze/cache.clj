(ns odesk_analyze.cache
  (:use [odesk_analyze.utils :only [read-edn write-edn]])
  (:require [odesk_analyze.path :as path])
  (:require odesk_analyze.api))

(defn +file-cache [path]
  (let [fpath (path/expand path)
        path-for-key (fn [key]
                       (path/join fpath key))]
    (reify odesk_analyze.api.KeyValueStorage
      (get-value [this key] (read-edn (path-for-key key)))
      (set-value [this key value] (write-edn (path-for-key key) value)))))